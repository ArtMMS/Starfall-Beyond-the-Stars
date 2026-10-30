package com.artmms.starfall.pipe;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.artmms.starfall.block.IronPipeBlock;
import com.artmms.starfall.block.OilExtractorBlock;
import com.artmms.starfall.block.SteelCrankBlock;
import com.artmms.starfall.block.entity.FluidTankBlockEntity;
import com.artmms.starfall.fluid.FluidExtraction;
import com.artmms.starfall.fluid.FluidUnits;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * Finds everything connected to a Steel Crank through Iron Pipes,
 * and pumps fluid from the extractors' pockets into the tanks.
 */
public final class PipeNetwork {
    /** Safety limit: biggest network a crank can scan. */
    private static final int MAX_BLOCKS = 512;

    private PipeNetwork() {
    }

    public record PumpResult(Component message) {
    }

    /**
     * Pumps whole buckets into each connected tank ({@code amountPerTank} mB, at least 1 bucket).
     * Fluid comes straight from the pockets the connected extractors touch.
     */
    public static PumpResult pump(Level level, BlockPos crankPos, int amountPerTank) {
        List<FluidTankBlockEntity> tanks = new ArrayList<>();
        List<BlockPos> extractors = new ArrayList<>();
        scan(level, crankPos, tanks, extractors);

        if (extractors.isEmpty()) {
            return new PumpResult(Component.translatable("message.starfall.crank.no_extractor"));
        }
        if (tanks.isEmpty()) {
            return new PumpResult(Component.translatable("message.starfall.crank.no_tank"));
        }

        int bucketsPerTank = Math.max(1, amountPerTank / FluidType.BUCKET_VOLUME);
        int totalBuckets = 0;
        int filledTanks = 0;

        for (FluidTankBlockEntity tank : tanks) {
            int movedIntoThis = 0;
            for (int i = 0; i < bucketsPerTank; i++) {
                boolean moved = false;
                for (BlockPos extractor : extractors) {
                    if (FluidExtraction.pumpOneBucket(level, extractor, tank.getTank())) {
                        moved = true;
                        break;
                    }
                }
                if (!moved) {
                    break; // this tank is full or every pocket is empty
                }
                movedIntoThis++;
            }
            if (movedIntoThis > 0) {
                totalBuckets += movedIntoThis;
                filledTanks++;
            }
        }

        if (totalBuckets <= 0) {
            return new PumpResult(Component.translatable("message.starfall.crank.nothing"));
        }
        return new PumpResult(Component.translatable("message.starfall.crank.pumped",
                FluidUnits.liters(totalBuckets * FluidType.BUCKET_VOLUME), filledTanks));
    }

    /** Walks the pipes starting at the one the crank is attached to. */
    private static void scan(Level level, BlockPos crankPos, List<FluidTankBlockEntity> tanks,
                             List<BlockPos> extractors) {
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> pipes = new ArrayDeque<>();
        visited.add(crankPos);

        BlockState crank = level.getBlockState(crankPos);
        if (!(crank.getBlock() instanceof SteelCrankBlock)) {
            return;
        }
        Direction attached = crank.getValue(SteelCrankBlock.FACING);
        check(level, crankPos.relative(attached), visited, pipes, tanks, extractors);

        while (!pipes.isEmpty() && visited.size() < MAX_BLOCKS) {
            BlockPos pipe = pipes.poll();
            for (Direction direction : Direction.values()) {
                check(level, pipe.relative(direction), visited, pipes, tanks, extractors);
            }
        }
    }

    private static void check(Level level, BlockPos pos, Set<BlockPos> visited, ArrayDeque<BlockPos> pipes,
                              List<FluidTankBlockEntity> tanks, List<BlockPos> extractors) {
        if (!visited.add(pos) || !level.isLoaded(pos)) {
            return;
        }

        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof IronPipeBlock) {
            pipes.add(pos);
        } else if (state.getBlock() instanceof OilExtractorBlock) {
            extractors.add(pos);
        } else if (level.getBlockEntity(pos) instanceof FluidTankBlockEntity tank) {
            tanks.add(tank);
        }
    }
}
