package com.artmms.starfall.datagen;

import java.util.concurrent.CompletableFuture;

import com.artmms.starfall.Starfall;
import com.artmms.starfall.block.ModBlocks;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/** Generates block tags (which tool mines each block, tool tier, etc.). */
public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                                ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Starfall.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.STAR_FRAGMENT_BLOCK.get())
                .add(ModBlocks.FLUID_TANK.get())
                .add(ModBlocks.OIL_EXTRACTOR.get())
                .add(ModBlocks.IRON_PIPE.get())
                .add(ModBlocks.STEEL_CRANK.get());

        tag(BlockTags.NEEDS_IRON_TOOL)
                .add(ModBlocks.STAR_FRAGMENT_BLOCK.get());

        tag(BlockTags.MINEABLE_WITH_SHOVEL)
                .add(ModBlocks.TAR_PATCH.get());
    }
}
