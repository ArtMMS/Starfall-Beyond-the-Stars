package com.artmms.starfall.fluid;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * Spills a fluid into the world around a position, one source block per bucket.
 * Used when a Fluid Tank is broken.
 */
public final class FluidSpill {
    /** How far the spill can spread from the broken tank. */
    private static final int MAX_RADIUS = 5;
    /** Order matters: down first, then sideways, then up (so it pools on the floor). */
    private static final Direction[] SPREAD_ORDER = {
            Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP
    };

    private FluidSpill() {
    }

    public static void spill(Level level, BlockPos origin, FluidStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        FluidType type = stack.getFluid().getFluidType();
        if (type.isVaporizedOnPlacement(level, origin, stack)) {
            // e.g. water in the Nether
            type.onVaporize(null, level, origin, stack);
            return;
        }

        // Round to the nearest bucket; anything from half a bucket still spills one block
        int blocks = (stack.getAmount() + FluidType.BUCKET_VOLUME / 2) / FluidType.BUCKET_VOLUME;
        if (blocks <= 0) {
            return;
        }

        FluidState fluidState = stack.getFluid().defaultFluidState();
        if (fluidState.isEmpty()) {
            return;
        }
        BlockState fluidBlock = fluidState.createLegacyBlock();

        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        visited.add(origin);
        for (Direction direction : SPREAD_ORDER) {
            BlockPos next = origin.relative(direction);
            visited.add(next);
            queue.add(next);
        }

        while (!queue.isEmpty() && blocks > 0) {
            BlockPos pos = queue.poll();
            if (origin.distManhattan(pos) > MAX_RADIUS || !level.isLoaded(pos)) {
                continue;
            }

            BlockState current = level.getBlockState(pos);
            if (!current.canBeReplaced() || !level.getFluidState(pos).isEmpty()) {
                continue;
            }

            level.setBlock(pos, fluidBlock, Block.UPDATE_ALL);
            blocks--;

            for (Direction direction : SPREAD_ORDER) {
                BlockPos next = pos.relative(direction);
                if (visited.add(next)) {
                    queue.add(next);
                }
            }
        }
    }
}
