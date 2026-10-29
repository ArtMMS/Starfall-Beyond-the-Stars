package com.artmms.starfall.datagen;

import java.util.concurrent.CompletableFuture;

import com.artmms.starfall.Starfall;
import com.artmms.starfall.block.ModBlocks;
import com.artmms.starfall.item.ModItems;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;

/** Generates crafting recipes. */
public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        // 9 star fragments -> 1 block
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.STAR_FRAGMENT_BLOCK.get())
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', ModItems.STAR_FRAGMENT.get())
                .unlockedBy("has_star_fragment", has(ModItems.STAR_FRAGMENT.get()))
                .save(output);

        // 1 block -> 9 star fragments
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.STAR_FRAGMENT.get(), 9)
                .requires(ModBlocks.STAR_FRAGMENT_BLOCK.get())
                .unlockedBy("has_star_fragment_block", has(ModBlocks.STAR_FRAGMENT_BLOCK.get()))
                .save(output, Starfall.id("star_fragment_from_block"));
    }
}
