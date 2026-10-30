package com.artmms.starfall.entity.rocket;

import java.util.EnumMap;
import java.util.Map;

import com.artmms.starfall.client.ClientHooks;
import com.artmms.starfall.config.StarfallConfig;
import com.artmms.starfall.fuel.FuelValues;
import com.artmms.starfall.item.ModItems;
import com.artmms.starfall.network.RocketActionPayload;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * The rocket. It's an entity (not blocks), so it can really take off with the crew.
 *
 * - Fuel tank in Fuel Points (FP), with 3 tank levels
 * - 1 or 3 engines (3 = faster, same fuel cost)
 * - 2 seats: the first player to board is the pilot, the second the co-pilot
 * - Each part has its own HP (saved now, damage comes later)
 * - Launching does a test flight for now: it climbs and comes back to the pad
 *
 * Controls:
 * - Right-click with fuel (crude oil bucket, lava bucket, coal): refuel
 * - Right-click with an empty hand: board
 * - Sneak + right-click, or R while seated: open the control panel
 * - Sneak + attack: pick the rocket back up (fuel is lost)
 */
public class RocketEntity extends Entity {
    public static final float WIDTH = 3.0F;
    public static final float HEIGHT = 18.5F;
    /** Seat heights (player's feet), lined up with the two windows. */
    public static final double PILOT_SEAT_Y = 13.0D;
    public static final double COPILOT_SEAT_Y = 10.2D;
    public static final int MAX_SEATS = 2;

    private static final EntityDataAccessor<Integer> FUEL =
            SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TANK_TIER =
            SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ENGINES =
            SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FLIGHT =
            SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.INT);

    public enum FlightState {
        IDLE, ASCENDING, DESCENDING;

        static FlightState byId(int id) {
            FlightState[] values = values();
            return id >= 0 && id < values.length ? values[id] : IDLE;
        }
    }

    private final Map<RocketPart, Integer> partHealth = new EnumMap<>(RocketPart.class);
    private double padY;
    private int flightTicks;

    public RocketEntity(EntityType<? extends RocketEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
        for (RocketPart part : RocketPart.values()) {
            partHealth.put(part, part.maxHealth());
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(FUEL, 0);
        builder.define(TANK_TIER, RocketTankTier.BASIC.ordinal());
        builder.define(ENGINES, 1);
        builder.define(FLIGHT, FlightState.IDLE.ordinal());
    }

    // ===== Getters (synced to clients, used by the panel and the renderer) =====

    public int getFuel() {
        return entityData.get(FUEL);
    }

    private void setFuel(int fuel) {
        entityData.set(FUEL, Mth.clamp(fuel, 0, getCapacity()));
    }

    public RocketTankTier getTankTier() {
        return RocketTankTier.byId(entityData.get(TANK_TIER));
    }

    public int getCapacity() {
        return getTankTier().capacity();
    }

    public int getEngines() {
        return entityData.get(ENGINES);
    }

    public FlightState getFlightState() {
        return FlightState.byId(entityData.get(FLIGHT));
    }

    private void setFlightState(FlightState state) {
        entityData.set(FLIGHT, state.ordinal());
    }

    public boolean isFlying() {
        return getFlightState() != FlightState.IDLE;
    }

    /** 3 engines = faster, for the same amount of fuel. */
    public double getSpeedMultiplier() {
        return getEngines() >= 3 ? StarfallConfig.ROCKET_TRIPLE_ENGINE_SPEED.get() : 1.0D;
    }

    /** FP spent on each launch. */
    public int getLaunchCost() {
        return (int) Math.round(StarfallConfig.ROCKET_LAUNCH_COST.get() * StarfallConfig.FUEL_CONSUMPTION.get());
    }

    public int getPartHealth(RocketPart part) {
        return partHealth.getOrDefault(part, part.maxHealth());
    }

    /** Ready for later: meteors, aliens... will call this. */
    public void damagePart(RocketPart part, int amount) {
        partHealth.put(part, Math.max(0, getPartHealth(part) - amount));
    }

    // ===== Interaction =====

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        boolean client = level().isClientSide();
        ItemStack stack = player.getItemInHand(hand);

        // Refuel
        int fuelPoints = FuelValues.fuelPointsForItem(stack);
        if (fuelPoints > 0) {
            if (!client) {
                refuel(player, hand, stack, fuelPoints);
            }
            return InteractionResult.sidedSuccess(client);
        }

        // Control panel
        if (player.isSecondaryUseActive()) {
            if (client) {
                ClientHooks.openRocketScreen(this);
            }
            return InteractionResult.sidedSuccess(client);
        }

        // Board
        if (!stack.isEmpty() || isFlying()) {
            return InteractionResult.PASS;
        }
        if (!client) {
            if (getPassengers().size() >= MAX_SEATS) {
                player.displayClientMessage(Component.translatable("message.starfall.rocket.seats_full"), true);
            } else if (player.startRiding(this)) {
                player.displayClientMessage(Component.translatable("message.starfall.rocket.boarded"), true);
            }
        }
        return InteractionResult.sidedSuccess(client);
    }

    private void refuel(Player player, InteractionHand hand, ItemStack stack, int fuelPoints) {
        if (getFuel() + fuelPoints > getCapacity()) {
            player.displayClientMessage(Component.translatable("message.starfall.rocket.tank_full"), true);
            return;
        }

        setFuel(getFuel() + fuelPoints);
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.BUCKET_EMPTY, SoundSource.NEUTRAL, 1.0F, 1.0F);

        if (!player.getAbilities().instabuild) {
            // Buckets give the empty bucket back
            ItemStack remainder = stack.getCraftingRemainingItem();
            stack.shrink(1);
            if (!remainder.isEmpty()) {
                if (stack.isEmpty()) {
                    player.setItemInHand(hand, remainder);
                } else if (!player.getInventory().add(remainder)) {
                    player.drop(remainder, false);
                }
            }
        }

        player.displayClientMessage(Component.translatable("message.starfall.rocket.refueled",
                fuelPoints, getFuel(), getCapacity()), true);
    }

    /** Sneak + attack picks the rocket up. Other damage will hit the parts later. */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isInvulnerableTo(source) || level().isClientSide()) {
            return false;
        }
        if (source.getEntity() instanceof Player player && player.isSecondaryUseActive() && !isFlying()) {
            ejectPassengers();
            if (!player.getAbilities().instabuild) {
                spawnAtLocation(new ItemStack(ModItems.ROCKET.get()));
            }
            discard();
            return true;
        }
        return false;
    }

    @Override
    public boolean isPickable() {
        return !isRemoved();
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    // ===== Seats =====

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return getPassengers().size() < MAX_SEATS;
    }

    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction callback) {
        int seat = getPassengers().indexOf(passenger);
        double y = seat == 0 ? PILOT_SEAT_Y : COPILOT_SEAT_Y;
        callback.accept(passenger, getX(), getY() + y, getZ());
    }

    /** Getting out puts the player on the ground next to the rocket. */
    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        Vec3 front = Vec3.directionFromRotation(0.0F, getYRot()).scale(2.5D);
        return new Vec3(getX() + front.x, getY(), getZ() + front.z);
    }

    // ===== Panel actions (sent by the client) =====

    public static void handleAction(Player player, RocketActionPayload payload) {
        if (!(player.level().getEntity(payload.entityId()) instanceof RocketEntity rocket)) {
            return;
        }
        // Must be on board or close by
        if (!rocket.hasPassenger(player) && player.distanceToSqr(rocket) > 16 * 16) {
            return;
        }

        switch (payload.action()) {
            case RocketActionPayload.LAUNCH -> rocket.tryLaunch(player);
            case RocketActionPayload.CYCLE_TANK -> rocket.cycleTankTier();
            case RocketActionPayload.TOGGLE_ENGINES -> rocket.toggleEngines();
            default -> {
            }
        }
    }

    private void tryLaunch(Player requester) {
        if (isFlying()) {
            return;
        }
        if (!(getFirstPassenger() instanceof Player)) {
            requester.displayClientMessage(Component.translatable("gui.starfall.rocket.status.no_pilot"), true);
            return;
        }
        int cost = getLaunchCost();
        if (getFuel() < cost) {
            requester.displayClientMessage(Component.translatable("gui.starfall.rocket.status.no_fuel"), true);
            return;
        }

        setFuel(getFuel() - cost);
        padY = getY();
        flightTicks = 0;
        setFlightState(FlightState.ASCENDING);
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.FIREWORK_ROCKET_LAUNCH,
                SoundSource.NEUTRAL, 4.0F, 0.5F);
        requester.displayClientMessage(Component.translatable("message.starfall.rocket.launching", cost), true);
    }

    /** Temporary: switches the tank level. Fuel above the new capacity is lost. */
    private void cycleTankTier() {
        if (isFlying()) {
            return;
        }
        entityData.set(TANK_TIER, getTankTier().next().ordinal());
        setFuel(getFuel());
    }

    /** Temporary: switches between 1 and 3 engines. */
    private void toggleEngines() {
        if (isFlying()) {
            return;
        }
        entityData.set(ENGINES, getEngines() >= 3 ? 1 : 3);
    }

    // ===== Flight =====

    @Override
    public void tick() {
        super.tick();

        FlightState state = getFlightState();
        if (state == FlightState.IDLE) {
            return;
        }

        if (level().isClientSide()) {
            spawnEngineParticles();
            return;
        }

        double speed = StarfallConfig.ROCKET_SPEED.get() * getSpeedMultiplier();
        if (state == FlightState.ASCENDING) {
            double ceiling = level().getMaxBuildHeight() - HEIGHT - 2.0D;
            double newY = Math.min(getY() + speed, ceiling);
            setPos(getX(), newY, getZ());
            flightTicks++;
            if (flightTicks >= StarfallConfig.ROCKET_TEST_FLIGHT_SECONDS.get() * 20 || newY >= ceiling) {
                setFlightState(FlightState.DESCENDING);
            }
        } else {
            double newY = getY() - speed;
            if (newY <= padY) {
                newY = padY;
                setFlightState(FlightState.IDLE);
            }
            setPos(getX(), newY, getZ());
        }
    }

    private void spawnEngineParticles() {
        float yawRad = -getYRot() * Mth.DEG_TO_RAD;
        float cos = Mth.cos(yawRad);
        float sin = Mth.sin(yawRad);

        for (int engine = 0; engine < getEngines(); engine++) {
            // Center engine at 0, side engines 1 block left and right
            double side = engine == 0 ? 0.0D : (engine == 1 ? -1.0D : 1.0D);
            double x = getX() + side * cos;
            double z = getZ() - side * sin;
            for (int i = 0; i < 3; i++) {
                double spreadX = (random.nextDouble() - 0.5D) * 0.4D;
                double spreadZ = (random.nextDouble() - 0.5D) * 0.4D;
                level().addParticle(ParticleTypes.FLAME, x + spreadX, getY() - 0.2D, z + spreadZ,
                        spreadX * 0.2D, -0.4D, spreadZ * 0.2D);
                level().addParticle(ParticleTypes.LARGE_SMOKE, x + spreadX, getY() - 0.8D, z + spreadZ,
                        spreadX, -0.1D, spreadZ);
            }
        }
    }

    // ===== Save / load =====

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Fuel", getFuel());
        tag.putInt("TankTier", getTankTier().ordinal());
        tag.putInt("Engines", getEngines());
        tag.putBoolean("Flying", isFlying());
        tag.putDouble("PadY", padY);

        CompoundTag parts = new CompoundTag();
        for (RocketPart part : RocketPart.values()) {
            parts.putInt(part.id(), getPartHealth(part));
        }
        tag.put("Parts", parts);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(TANK_TIER, tag.getInt("TankTier"));
        entityData.set(ENGINES, tag.contains("Engines") ? tag.getInt("Engines") : 1);
        setFuel(tag.getInt("Fuel"));
        padY = tag.getDouble("PadY");

        // Saved mid-flight: put it back on the pad
        if (tag.getBoolean("Flying")) {
            setPos(getX(), padY, getZ());
        }
        setFlightState(FlightState.IDLE);

        CompoundTag parts = tag.getCompound("Parts");
        for (RocketPart part : RocketPart.values()) {
            partHealth.put(part, parts.contains(part.id()) ? parts.getInt(part.id()) : part.maxHealth());
        }
    }
}
