package com.artmms.starfall.pipe;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.artmms.starfall.block.IronPipeBlock;
import com.artmms.starfall.block.entity.FluidTankBlockEntity;
import com.artmms.starfall.block.entity.OilExtractorBlockEntity;
import com.artmms.starfall.fluid.FluidUnits;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Finds everything connected to a Steel Crank through Iron Pipes,
 * and pumps fluid from the extractors into the tanks.
 */
public final class PipeNetwork {
    /** Safety limit: biggest network a crank can scan. */
    private static final int MAX_BLOCKS = 512;

    private PipeNetwork() {
    }

    public record PumpResult(Component message) {
    }

    /**
     * Pumps up to {@code amountPerTank} mB into each connected tank.
     * Fluid comes from the connected extractors (their internal tank first,
     * then straight from the pocket).
     */
    public static PumpResult pump(Level level, BlockPos crankPos, int amountPerTank) {
        List<FluidTankBlockEntity> tanks = new ArrayList<>();
        List<OilExtractorBlockEntity> extractors = new ArrayList<>();
        scan(level, crankPos, tanks, extractors);

        if (extractors.isEmpty()) {
            return new PumpResult(Component.translatable("message.starfall.crank.no_extractor"));
        }
        if (tanks.isEmpty()) {
            return new PumpResult(Component.translatable("message.starfall.crank.no_tank"));
        }

        int total = 0;
        int filledTanks = 0;
        for (FluidTankBlockEntity tank : tanks) {
            int needed = amountPerTank;
            int movedIntoThis = 0;
            for (OilExtractorBlockEntity extractor : extractors) {
                if (needed <= 0) {
                    break;
                }
                int moved = extractor.pumpInto(tank.getTank(), needed);
                needed -= moved;
                movedIntoThis += moved;
            }
            if (movedIntoThis > 0) {
                total += movedIntoThis;
                filledTanks++;
            }
        }

        if (total <= 0) {
            return new PumpResult(Component.translatable("message.starfall.crank.nothing"));
        }
        return new PumpResult(Component.translatable("message.starfall.crank.pumped",
                FluidUnits.liters(total), filledTanks));
    }

    /** Walks the pipes starting next to the crank, collecting tanks and extractors. */
    private static void scan(Level level, BlockPos start, List<FluidTankBlockEntity> tanks,
                             List<OilExtractorBlockEntity> extractors) {
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> pipes = new ArrayDeque<>();
        visited.add(start);

        // Neighbors of the crank itself
        for (Direction direction : Direction.values()) {
            check(level, start.relative(direction), visited, pipes, tanks, extractors);
        }

        while (!pipes.isEmpty() && visited.size() < MAX_BLOCKS) {
            BlockPos pipe = pipes.poll();
            for (Direction direction : Direction.values()) {
                check(level, pipe.relative(direction), visited, pipes, tanks, extractors);
            }
        }
    }

    private static void check(Level level, BlockPos pos, Set<BlockPos> visited, ArrayDeque<BlockPos> pipes,
                              List<FluidTankBlockEntity> tanks, List<OilExtractorBlockEntity> extractors) {
        if (!visited.add(pos) || !level.isLoaded(pos)) {
            return;
        }

        if (level.getBlockState(pos).getBlock() instanceof IronPipeBlock) {
            pipes.add(pos);
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof FluidTankBlockEntity tank) {
            tanks.add(tank);
        } else if (blockEntity instanceof OilExtractorBlockEntity extractor) {
            extractors.add(extractor);
        }
    }
}
