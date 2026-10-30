package com.artmms.starfall.fluid;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import com.artmms.starfall.config.StarfallConfig;
import com.artmms.starfall.tag.ModTags;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/**
 * What the Oil Extractor does: it stores nothing, it only lets the pipes suck
 * fluid (crude oil, water or lava) out of the pool or pocket it touches.
 * Each pumped bucket removes one source block, so pockets run out.
 */
public final class FluidExtraction {
    /** Safety limit for the pocket scan. */
    private static final int MAX_SCANNED_BLOCKS = 4096;

    private FluidExtraction() {
    }

    /** Summary of what an extractor can reach. */
    public record PocketInfo(@Nullable Fluid fluid, int sourceBlocks) {
    }

    /**
     * Pumps exactly 1 bucket from the pocket touching the extractor into the target.
     * Only works if the target accepts the whole bucket (so nothing is ever lost).
     *
     * @return true if a bucket was moved
     */
    public static boolean pumpOneBucket(Level level, BlockPos extractorPos, IFluidHandler target) {
        // If the tank already holds something, only that fluid can be pumped into it
        FluidStack inTarget = target.getTanks() > 0 ? target.getFluidInTank(0) : FluidStack.EMPTY;
        Fluid wanted = inTarget.isEmpty() ? null : inTarget.getFluid();

        BlockPos source = findSource(level, extractorPos, wanted);
        if (source == null) {
            return false;
        }

        Fluid fluid = level.getFluidState(source).getType();
        FluidStack bucket = new FluidStack(fluid, FluidType.BUCKET_VOLUME);
        if (target.fill(bucket, IFluidHandler.FluidAction.SIMULATE) < FluidType.BUCKET_VOLUME) {
            return false; // target full, or it doesn't accept this fluid
        }

        level.setBlock(source, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        target.fill(bucket, IFluidHandler.FluidAction.EXECUTE);
        return true;
    }

    /** What the extractor is touching, for the status message. */
    public static PocketInfo inspect(Level level, BlockPos extractorPos) {
        Fluid[] found = new Fluid[1];
        int[] sources = new int[1];
        walk(level, extractorPos, null, (pos, fluidState) -> {
            if (found[0] == null) {
                found[0] = fluidState.getType();
            }
            if (fluidState.isSource()) {
                sources[0]++;
            }
        });
        return new PocketInfo(found[0], sources[0]);
    }

    /**
     * Walks through the fluid connected to the extractor and returns the highest
     * source block, so the pocket drains from the top down.
     *
     * @param wanted only this fluid is taken; null = whatever fluid is touching the extractor
     */
    @Nullable
    private static BlockPos findSource(Level level, BlockPos extractorPos, @Nullable Fluid wanted) {
        BlockPos[] best = new BlockPos[1];
        walk(level, extractorPos, wanted, (pos, fluidState) -> {
            if (fluidState.isSource() && (best[0] == null || pos.getY() > best[0].getY())) {
                best[0] = pos.immutable();
            }
        });
        return best[0];
    }

    private interface Visitor {
        void visit(BlockPos pos, FluidState fluidState);
    }

    /** Flood-fills the connected fluid (one fluid type only) within range. */
    private static void walk(Level level, BlockPos start, @Nullable Fluid wanted, Visitor visitor) {
        int range = StarfallConfig.EXTRACTOR_RANGE.get();
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Fluid target = wanted;

        for (Direction direction : Direction.values()) {
            BlockPos next = start.relative(direction);
            visited.add(next);
            queue.add(next);
        }

        while (!queue.isEmpty() && visited.size() < MAX_SCANNED_BLOCKS) {
            BlockPos pos = queue.poll();

            if (!isInRange(start, pos, range) || !level.isLoaded(pos)) {
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

            visitor.visit(pos, fluid);

            for (Direction direction : Direction.values()) {
                BlockPos next = pos.relative(direction);
                if (visited.add(next)) {
                    queue.add(next);
                }
            }
        }
    }

    private static boolean isInRange(BlockPos center, BlockPos pos, int range) {
        return Math.abs(pos.getX() - center.getX()) <= range
                && Math.abs(pos.getY() - center.getY()) <= range
                && Math.abs(pos.getZ() - center.getZ()) <= range;
    }
}
