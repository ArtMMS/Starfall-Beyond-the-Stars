package com.artmms.starfall.world;

import com.artmms.starfall.Starfall;
import com.artmms.starfall.world.structure.OilPocketPiece;
import com.artmms.starfall.world.structure.OilPocketStructure;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * World structures. Oil pockets are structures (not features) so that
 * "/locate structure starfall:oil_pocket" can find them.
 * Where and how often they spawn: data/starfall/worldgen/structure_set.
 */
public final class ModStructures {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, Starfall.MODID);

    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, Starfall.MODID);

    public static final DeferredHolder<StructureType<?>, StructureType<OilPocketStructure>> OIL_POCKET =
            STRUCTURE_TYPES.register("oil_pocket",
                    () -> (StructureType<OilPocketStructure>) () -> OilPocketStructure.CODEC);

    public static final DeferredHolder<StructurePieceType, StructurePieceType> OIL_POCKET_PIECE =
            STRUCTURE_PIECES.register("oil_pocket",
                    () -> (StructurePieceType.ContextlessType) OilPocketPiece::new);

    private ModStructures() {
    }

    public static void register(IEventBus modEventBus) {
        STRUCTURE_TYPES.register(modEventBus);
        STRUCTURE_PIECES.register(modEventBus);
    }
}
