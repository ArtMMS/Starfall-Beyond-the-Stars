package com.artmms.starfall.block.entity;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import com.artmms.starfall.config.StarfallConfig;
import com.artmms.starfall.fluid.ModFluids;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/**
 * Oil Extractor: pulls crude oil from the pocket it touches, 1 bucket at a time.
 * - Works for free, but slowly
 * - Turning the crank (right-click) speeds it up
 * - Removes the source blocks, so pockets run out
 * - Pushes oil into neighboring tanks automatically
 */
public class OilExtractorBlockEntity extends BlockEntity {
    private static final String TANK_TAG = "Tank";
    private static final String PROGRESS_TAG = "Progress";
    /** Safety limit for the pocket scan. */
    private static final int MAX_SCANNED_BLOCKS = 4096;
    /** How often it tries to push oil into neighbors (ticks). */
    private static final int PUSH_INTERVAL = 20;

    private final OutputOnlyTank tank = new OutputOnlyTank(StarfallConfig.EXTRACTOR_CAPACITY.get(),
            stack -> stack.getFluid().isSame(ModFluids.CRUDE_OIL.get()), this::setChanged);

    private int progress;
    private boolean noOil;

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

    /** Called when a player turns the crank. */
    public void crank() {
        if (tank.getSpace() >= FluidType.BUCKET_VOLUME) {
            progress = Math.min(progress + StarfallConfig.EXTRACTOR_CRANK_BOOST.get(), ticksPerBucket());
            setChanged();
        }
    }

    public Component describe() {
        if (tank.getSpace() < FluidType.BUCKET_VOLUME) {
            return Component.translatable("message.starfall.extractor.full",
                    tank.getFluidAmount(), tank.getCapacity());
        }
        if (noOil) {
            return Component.translatable("message.starfall.extractor.no_oil",
                    tank.getFluidAmount(), tank.getCapacity());
        }
        int percent = progress * 100 / ticksPerBucket();
        return Component.translatable("message.starfall.extractor.status",
                percent, tank.getFluidAmount(), tank.getCapacity());
    }

    private int ticksPerBucket() {
        return Math.max(1, StarfallConfig.EXTRACTOR_TICKS_PER_BUCKET.get());
    }

    private void extractOneBucket() {
        BlockPos source = findOilSource();
        if (source == null) {
            noOil = true;
            return;
        }

        noOil = false;
        level.setBlock(source, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        tank.fillInternal(new FluidStack(ModFluids.CRUDE_OIL.get(), FluidType.BUCKET_VOLUME));
    }

    /**
     * Walks through the oil connected to the extractor and returns the highest
     * source block, so the pocket drains from the top down.
     */
    @Nullable
    private BlockPos findOilSource() {
        int range = StarfallConfig.EXTRACTOR_RANGE.get();
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        BlockPos best = null;

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

            FluidState fluid = level.getFluidState(pos);
            if (!fluid.getType().isSame(ModFluids.CRUDE_OIL.get())) {
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

    /** Sends oil to any neighbor that accepts fluids (Starfall tanks, other mods' tanks, pipes). */
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
