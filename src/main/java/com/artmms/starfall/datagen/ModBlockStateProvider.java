package com.artmms.starfall.datagen;

import com.artmms.starfall.Starfall;
import com.artmms.starfall.block.ModBlocks;

import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

/** Generates blockstates, block models and block item models. */
public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Starfall.MODID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        cubeAllWithItem(ModBlocks.STAR_FRAGMENT_BLOCK);

        // Liquid block: only needs a particle texture (the fluid renderer draws the rest)
        simpleBlock(ModBlocks.CRUDE_OIL.get(), models().getBuilder("crude_oil")
                .texture("particle", mcLoc("block/water_still")));

        simpleBlockWithItem(ModBlocks.TAR_PATCH.get(),
                models().carpet("tar_patch", modLoc("block/tar_patch")));

        simpleBlockWithItem(ModBlocks.FLUID_TANK.get(),
                models().cubeColumn("fluid_tank",
                        modLoc("block/fluid_tank_side"),
                        modLoc("block/fluid_tank_top")));

        simpleBlockWithItem(ModBlocks.OIL_EXTRACTOR.get(),
                models().cubeBottomTop("oil_extractor",
                        modLoc("block/oil_extractor_side"),
                        modLoc("block/oil_extractor_bottom"),
                        modLoc("block/oil_extractor_top")));
    }

    /** Simple full block with the same texture on every face (textures/block/NAME.png). */
    private void cubeAllWithItem(DeferredBlock<? extends Block> block) {
        simpleBlockWithItem(block.get(), cubeAll(block.get()));
    }
}
