package com.artmms.starfall.world.feature;

import com.artmms.starfall.block.ModBlocks;
import com.artmms.starfall.fluid.ModFluids;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * An underground pocket of crude oil (roughly 50 to 300 source blocks).
 * It only replaces natural stone, so it never floods caves on purpose.
 * Sometimes it leaves tar patches on the surface above as a hint.
 */
public class OilPocketFeature extends Feature<NoneFeatureConfiguration> {
    /** Chance of leaving tar patches on the surface above the pocket. */
    private static final float SURFACE_HINT_CHANCE = 0.6F;
    /** Pockets smaller than this (e.g. mostly inside a cave) leave no hint. */
    private static final int MIN_BLOCKS_FOR_HINT = 40;

    public OilPocketFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        // Ellipsoid radii
        int rx = 3 + random.nextInt(3); // 3..5
        int rz = 3 + random.nextInt(3); // 3..5
        int ry = 2 + random.nextInt(2); // 2..3

        BlockState oil = ModFluids.CRUDE_OIL.get().defaultFluidState().createLegacyBlock();
        int placed = 0;

        for (int dx = -rx; dx <= rx; dx++) {
            for (int dy = -ry; dy <= ry; dy++) {
                for (int dz = -rz; dz <= rz; dz++) {
                    double distance = (dx * dx) / (double) (rx * rx)
                            + (dy * dy) / (double) (ry * ry)
                            + (dz * dz) / (double) (rz * rz);

                    // A little noise so pockets are not perfect eggs
                    if (distance > 1.0D - random.nextDouble() * 0.15D) {
                        continue;
                    }

                    BlockPos pos = origin.offset(dx, dy, dz);
                    if (level.getBlockState(pos).is(BlockTags.BASE_STONE_OVERWORLD)) {
                        level.setBlock(pos, oil, 2);
                        placed++;
                    }
                }
            }
        }

        if (placed >= MIN_BLOCKS_FOR_HINT && random.nextFloat() < SURFACE_HINT_CHANCE) {
            placeSurfaceHint(level, origin, random);
        }

        return placed > 0;
    }

    /** Places 1 to 3 tar patches on the surface, right above the pocket. */
    private void placeSurfaceHint(WorldGenLevel level, BlockPos origin, RandomSource random) {
        BlockState tar = ModBlocks.TAR_PATCH.get().defaultBlockState();
        int patches = 1 + random.nextInt(3);

        for (int i = 0; i < patches; i++) {
            int x = origin.getX() + random.nextInt(5) - 2;
            int z = origin.getZ() + random.nextInt(5) - 2;
            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);

            BlockPos pos = new BlockPos(x, y, z);
            BlockPos below = pos.below();

            boolean freeSpace = level.getBlockState(pos).isAir();
            boolean solidGround = level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)
                    && level.getFluidState(below).isEmpty();

            if (freeSpace && solidGround) {
                level.setBlock(pos, tar, 2);
            }
        }
    }
}
