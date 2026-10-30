package com.artmms.starfall.world.structure;

import com.artmms.starfall.block.ModBlocks;
import com.artmms.starfall.fluid.ModFluids;
import com.artmms.starfall.world.ModStructures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * Places the oil of one pocket. Big pockets span several chunks, so this runs
 * once per chunk and only writes inside that chunk. The shape uses a fixed seed,
 * so every chunk agrees on where the oil is.
 */
public class OilPocketPiece extends StructurePiece {
    private final BlockPos center;
    private final int rx;
    private final int ry;
    private final int rz;
    private final boolean tar;
    private final long seed;

    public OilPocketPiece(BlockPos center, int rx, int ry, int rz, boolean tar, long seed, int topY) {
        super(ModStructures.OIL_POCKET_PIECE.get(), 0, new BoundingBox(
                center.getX() - rx, center.getY() - ry, center.getZ() - rz,
                center.getX() + rx, Math.max(center.getY() + ry, topY), center.getZ() + rz));
        this.center = center;
        this.rx = rx;
        this.ry = ry;
        this.rz = rz;
        this.tar = tar;
        this.seed = seed;
    }

    /** Loads a piece saved in the world. */
    public OilPocketPiece(CompoundTag tag) {
        super(ModStructures.OIL_POCKET_PIECE.get(), tag);
        this.center = new BlockPos(tag.getInt("CX"), tag.getInt("CY"), tag.getInt("CZ"));
        this.rx = tag.getInt("RX");
        this.ry = tag.getInt("RY");
        this.rz = tag.getInt("RZ");
        this.tar = tag.getBoolean("Tar");
        this.seed = tag.getLong("Seed");
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("CX", center.getX());
        tag.putInt("CY", center.getY());
        tag.putInt("CZ", center.getZ());
        tag.putInt("RX", rx);
        tag.putInt("RY", ry);
        tag.putInt("RZ", rz);
        tag.putBoolean("Tar", tar);
        tag.putLong("Seed", seed);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
                            RandomSource random, BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        placeOil(level, chunkBox);
        if (tar) {
            placeTar(level, chunkBox);
        }
    }

    private void placeOil(WorldGenLevel level, BoundingBox chunkBox) {
        BlockState oil = ModFluids.CRUDE_OIL.get().defaultFluidState().createLegacyBlock();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        int minX = Math.max(center.getX() - rx, chunkBox.minX());
        int maxX = Math.min(center.getX() + rx, chunkBox.maxX());
        int minZ = Math.max(center.getZ() - rz, chunkBox.minZ());
        int maxZ = Math.min(center.getZ() + rz, chunkBox.maxZ());
        int minY = Math.max(center.getY() - ry, chunkBox.minY());
        int maxY = Math.min(center.getY() + ry, chunkBox.maxY());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    int dx = x - center.getX();
                    int dy = y - center.getY();
                    int dz = z - center.getZ();
                    double distance = (dx * dx) / (double) (rx * rx)
                            + (dy * dy) / (double) (ry * ry)
                            + (dz * dz) / (double) (rz * rz);

                    // Small fixed noise so pockets are not perfect eggs
                    if (distance > 1.0D - noise(x, y, z) * 0.15D) {
                        continue;
                    }

                    pos.set(x, y, z);
                    if (level.getBlockState(pos).is(BlockTags.BASE_STONE_OVERWORLD)) {
                        level.setBlock(pos, oil, 2);
                        // Oil touching a cave must flow (like lava), not hang in the air:
                        // world generation doesn't update fluids, so we ask for an update
                        if (touchesOpenSpace(level, pos)) {
                            level.scheduleTick(pos.immutable(), ModFluids.CRUDE_OIL.get(), 1);
                        }
                    }
                }
            }
        }
    }

    /** True if any neighbor is air (or something the oil can flow into). */
    private static boolean touchesOpenSpace(WorldGenLevel level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos neighbor = pos.relative(direction);
            BlockState state = level.getBlockState(neighbor);
            if (state.isAir() || (state.canBeReplaced() && level.getFluidState(neighbor).isEmpty())) {
                return true;
            }
        }
        return false;
    }

    /** 2 to 5 tar patches on the ground right above the pocket. */
    private void placeTar(WorldGenLevel level, BoundingBox chunkBox) {
        BlockState tarPatch = ModBlocks.TAR_PATCH.get().defaultBlockState();
        RandomSource tarRandom = RandomSource.create(seed);
        int patches = 2 + tarRandom.nextInt(4);

        for (int i = 0; i < patches; i++) {
            int x = center.getX() + tarRandom.nextInt(7) - 3;
            int z = center.getZ() + tarRandom.nextInt(7) - 3;

            // Only write inside the chunk being generated
            if (x < chunkBox.minX() || x > chunkBox.maxX() || z < chunkBox.minZ() || z > chunkBox.maxZ()) {
                continue;
            }

            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
            BlockPos pos = new BlockPos(x, y, z);
            BlockPos below = pos.below();

            boolean freeSpace = level.getBlockState(pos).isAir();
            boolean solidGround = level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)
                    && level.getFluidState(below).isEmpty();

            if (freeSpace && solidGround) {
                level.setBlock(pos, tarPatch, 2);
            }
        }
    }

    /** Deterministic value between 0 and 1 for a position. */
    private double noise(int x, int y, int z) {
        long hash = Mth.getSeed(x, y, z) ^ seed;
        return ((hash >>> 16) & 0xFFFF) / 65536.0D;
    }
}
