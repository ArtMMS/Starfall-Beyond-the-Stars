package com.artmms.starfall.world.structure;

import java.util.Optional;

import com.artmms.starfall.world.ModStructures;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/**
 * An underground pocket of crude oil. The deeper it is, the bigger it gets:
 * - Shallow (Y 35 and up): small, no surface hint
 * - Medium (Y 15 to 34): medium, sometimes leaves tar patches on the surface
 * - Deep (below Y 15): large, always leaves tar patches on the surface
 */
public class OilPocketStructure extends Structure {
    public static final MapCodec<OilPocketStructure> CODEC = simpleCodec(OilPocketStructure::new);

    private static final int MIN_Y = -16;
    private static final int MAX_Y = 56;
    /** Minimum rock between the top of the pocket and the ground. */
    private static final int MIN_COVER = 6;
    private static final int MEDIUM_BELOW = 35;
    private static final int LARGE_BELOW = 15;
    private static final float MEDIUM_TAR_CHANCE = 0.3F;

    public OilPocketStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        WorldgenRandom random = context.random();

        int x = chunkPos.getBlockX(random.nextInt(16));
        int z = chunkPos.getBlockZ(random.nextInt(16));

        int floorY = context.chunkGenerator().getFirstOccupiedHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG,
                context.heightAccessor(), context.randomState());
        int surfaceY = context.chunkGenerator().getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG,
                context.heightAccessor(), context.randomState());

        // Pick a height, but always keep some rock above the pocket
        int y = MIN_Y + random.nextInt(MAX_Y - MIN_Y + 1);
        y = Math.min(y, floorY - 4 - MIN_COVER);
        if (y - 4 < context.heightAccessor().getMinBuildHeight() + 5) {
            return Optional.empty();
        }

        // Size by depth
        int rx;
        int ry;
        int rz;
        boolean tar;
        if (y < LARGE_BELOW) {
            rx = 5 + random.nextInt(3); // 5..7
            rz = 5 + random.nextInt(3);
            ry = 3 + random.nextInt(2); // 3..4
            tar = true;
        } else if (y < MEDIUM_BELOW) {
            rx = 3 + random.nextInt(3); // 3..5
            rz = 3 + random.nextInt(3);
            ry = 2 + random.nextInt(2); // 2..3
            tar = random.nextFloat() < MEDIUM_TAR_CHANCE;
        } else {
            rx = 2 + random.nextInt(2); // 2..3
            rz = 2 + random.nextInt(2);
            ry = 2;
            tar = false;
        }

        // Tar only on dry land (not under oceans or rivers)
        boolean placeTar = tar && surfaceY == floorY;
        long seed = random.nextLong();
        int topY = placeTar ? surfaceY + 1 : y + ry;

        BlockPos center = new BlockPos(x, y, z);
        int finalRx = rx;
        int finalRy = ry;
        int finalRz = rz;

        return Optional.of(new GenerationStub(center, builder -> builder.addPiece(
                new OilPocketPiece(center, finalRx, finalRy, finalRz, placeTar, seed, topY))));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.OIL_POCKET.get();
    }
}
