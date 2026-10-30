package com.artmms.starfall.block.entity;

import com.artmms.starfall.config.StarfallConfig;
import com.artmms.starfall.fluid.FluidUnits;
import com.artmms.starfall.fuel.FuelValues;
import com.artmms.starfall.tag.ModTags;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

/**
 * Fluid Tank: stores one fluid at a time (water, lava or crude oil).
 * Exposed to other mods through the NeoForge fluid capability.
 * The contents are synced to the client so the liquid can be drawn inside the glass.
 */
public class FluidTankBlockEntity extends BlockEntity {
    /** How many visual fill levels the glass shows. */
    public static final int VISUAL_LEVELS = 16;
    private static final String TANK_TAG = "Tank";

    private final FluidTank tank = new FluidTank(StarfallConfig.BASIC_TANK_CAPACITY.get(),
            stack -> stack.is(ModTags.Fluids.BASIC_TANK_FLUIDS)) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            syncToClient();
        }
    };

    public FluidTankBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUID_TANK.get(), pos, state);
    }

    public FluidTank getTank() {
        return tank;
    }

    /** 0 = empty, 1..16 = how high the liquid is drawn (any amount shows at least level 1). */
    public int getVisualLevel() {
        int amount = tank.getFluidAmount();
        if (amount <= 0) {
            return 0;
        }
        int level = (int) Math.ceil(amount * (double) VISUAL_LEVELS / tank.getCapacity());
        return Math.max(1, Math.min(VISUAL_LEVELS, level));
    }

    /** Text shown when the player right-clicks the tank with an empty hand. */
    public Component describe() {
        FluidStack fluid = tank.getFluid();
        String capacity = FluidUnits.liters(tank.getCapacity());
        if (fluid.isEmpty()) {
            return Component.translatable("message.starfall.tank.empty", capacity);
        }

        Component base = Component.translatable("message.starfall.tank.contents",
                fluid.getHoverName(), FluidUnits.liters(tank.getFluidAmount()), capacity);

        if (FuelValues.isFuel(fluid)) {
            return base.copy().append(Component.translatable("message.starfall.fuel_points",
                    FuelValues.fuelPoints(fluid)));
        }
        return base;
    }

    private void syncToClient() {
        if (level != null && !level.isClientSide()) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    // ===== Save / load =====

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put(TANK_TAG, tank.writeToNBT(registries, new CompoundTag()));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains(TANK_TAG)) {
            tank.readFromNBT(registries, tag.getCompound(TANK_TAG));
        }
    }

    // ===== Client sync =====

    /** Sent when the chunk loads on the client. */
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.put(TANK_TAG, tank.writeToNBT(registries, new CompoundTag()));
        return tag;
    }

    /** Sent every time the contents change. */
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
