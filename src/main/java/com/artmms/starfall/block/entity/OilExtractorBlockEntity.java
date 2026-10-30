package com.artmms.starfall.block.entity;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import com.artmms.starfall.config.StarfallConfig;
import com.artmms.starfall.fluid.FluidUnits;
import com.artmms.starfall.tag.ModTags;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/**
 * Oil Extractor: pulls fluid from the pool or pocket it touches, 1 bucket at a time.
 * Works with crude oil, water and lava (whatever it is placed in).
 * - Fills its internal tank by itself, slowly
 * - The Steel Crank pumps from it through Iron Pipes (and straight from the pocket when the internal tank is empty)
 * - Removes the source blocks, so pockets run out
 * - Also pushes fluid into tanks touching it
 */
public class OilExtractorBlockEntity extends BlockEntity {
    private static final String TANK_TAG = "Tank";
    private static final String PROGRESS_TAG = "Progress";
    /** Safety limit for the pocket scan. */
    private static final int MAX_SCANNED_BLOCKS = 4096;
    /** How often it tries to push fluid into neighbors (ticks). */
    private static final int PUSH_INTERVAL = 20;
    /** Safety limit for one pump call. */
    private static final int MAX_PUMP_STEPS = 64;

    private final OutputOnlyTank tank = new OutputOnlyTank(StarfallConfig.EXTRACTOR_CAPACITY.get(),
            stack -> stack.is(ModTags.Fluids.BASIC_TANK_FLUIDS), this::setChanged);

    private int progress;
    private boolean noFluid;

    public OilExtractorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.OIL_EXTRACTOR.get(), pos, state);
    }

    public OutputOnlyTank getTank() {
        return tank;
    }

    public void serverTick() {
        if (level == null) {
            return;
        }

        if (level.getGameTime() % PUSH_INTERVAL == 0) {
            pushToNeighbors();
        }

        // Internal tank full: wait
        if (tank.getSpace() < FluidType.BUCKET_VOLUME) {
            return;
        }

        progress++;
        if (progress >= ticksPerBucket()) {
            progress = 0;
            extractOneBucket();
        }
        setChanged();
    }

    /**
     * Moves up to {@code max} mB into the target (used by the Steel Crank).
     * Takes from the internal tank first, then straight from the pocket.
     *
     * @return how much was moved, in mB
     */
    public int pumpInto(IFluidHandler target, int max) {
        int moved = 0;
        for (int step = 0; step < MAX_PUMP_STEPS && moved < max; step++) {
            if (tank.isEmpty() && !extractOneBucket()) {
                break; // pocket is empty
            }

            FluidStack available = tank.getFluid();
            int wanted = Math.min(max - moved, available.getAmount());
            int accepted = target.fill(available.copyWithAmount(wanted), IFluidHandler.FluidAction.SIMULATE);
            if (accepted <= 0) {
                break; // target full or holds another fluid
            }

            FluidStack drained = tank.drain(accepted, IFluidHandler.FluidAction.EXECUTE);
            target.fill(drained, IFluidHandler.FluidAction.EXECUTE);
            moved += drained.getAmount();
        }
        return moved;
    }

    public Component describe() {
        String stored = FluidUnits.liters(tank.getFluidAmount());
        String capacity = FluidUnits.liters(tank.getCapacity());

        if (tank.getSpace() < FluidType.BUCKET_VOLUME) {
            return Component.translatable("message.starfall.extractor.full", stored, capacity);
        }
        if (noFluid) {
            return Component.translatable("message.starfall.extractor.no_fluid", stored, capacity);
        }
        int percent = progress * 100 / ticksPerBucket();
        return Component.translatable("message.starfall.extractor.status", percent, stored, capacity);
    }

    private int ticksPerBucket() {
        return Math.max(1, StarfallConfig.EXTRACTOR_TICKS_PER_BUCKET.get());
    }

    /** Removes one source block from the pocket and stores 1 bucket. */
    private boolean extractOneBucket() {
        if (level == null || tank.getSpace() < FluidType.BUCKET_VOLUME) {
            return false;
        }

        Fluid wanted = tank.isEmpty() ? null : tank.getFluid().getFluid();
        BlockPos source = findSource(wanted);
        if (source == null) {
            noFluid = true;
            return false;
        }

        Fluid fluid = level.getFluidState(source).getType();
        noFluid = false;
        level.setBlock(source, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        tank.fillInternal(new FluidStack(fluid, FluidType.BUCKET_VOLUME));
        return true;
    }

    /**
     * Walks through the fluid connected to the extractor and returns the highest
     * source block, so the pocket drains from the top down.
     *
     * @param wanted the fluid already stored (only that one is taken), or null for any
     */
    @Nullable
    private BlockPos findSource(@Nullable Fluid wanted) {
        int range = StarfallConfig.EXTRACTOR_RANGE.get();
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        BlockPos best = null;
        Fluid target = wanted;

        for (Direction direction : Direction.values()) {
            BlockPos next = worldPosition.relative(direction);
            visited.add(next);
            queue.add(next);
        }

        while (!queue.isEmpty() && visited.size() < MAX_SCANNED_BLOCKS) {
            BlockPos pos = queue.poll();

            if (!isInRange(pos, range) || !level.isLoaded(pos)) {
                continue;
            }

            // Only real liquid blocks (never waterlogged blocks)
            if (!(level.getBlockState(pos).getBlock() instanceof LiquidBlock)) {
                continue;
            }

            FluidState fluid = level.getFluidState(pos);
            if (fluid.isEmpty() || !fluid.getType().is(ModTags.Fluids.BASIC_TANK_FLUIDS)) {
                continue;
            }
            if (target == null) {
                target = fluid.getType();
            } else if (!fluid.getType().isSame(target)) {
                continue;
            }

            if (fluid.isSource() && (best == null || pos.getY() > best.getY())) {
                best = pos;
            }

            for (Direction direction : Direction.values()) {
                BlockPos next = pos.relative(direction);
                if (visited.add(next)) {
                    queue.add(next);
                }
            }
        }

        return best;
    }

    private boolean isInRange(BlockPos pos, int range) {
        return Math.abs(pos.getX() - worldPosition.getX()) <= range
                && Math.abs(pos.getY() - worldPosition.getY()) <= range
                && Math.abs(pos.getZ() - worldPosition.getZ()) <= range;
    }

    /** Sends fluid to any neighbor that accepts it (Starfall tanks, other mods' tanks). */
    private void pushToNeighbors() {
        for (Direction direction : Direction.values()) {
            if (tank.isEmpty()) {
                return;
            }
            IFluidHandler target = level.getCapability(Capabilities.FluidHandler.BLOCK,
                    worldPosition.relative(direction), direction.getOpposite());
            if (target != null) {
                FluidUtil.tryFluidTransfer(target, tank, FluidType.BUCKET_VOLUME, true);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put(TANK_TAG, tank.writeToNBT(registries, new CompoundTag()));
        tag.putInt(PROGRESS_TAG, progress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains(TANK_TAG)) {
            tank.readFromNBT(registries, tag.getCompound(TANK_TAG));
        }
        progress = tag.getInt(PROGRESS_TAG);
    }
}
