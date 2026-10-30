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
import net.minecraft.world.item.Items;

/** Generates crafting recipes. */
public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        // ===== Star fragments =====
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.STAR_FRAGMENT_BLOCK.get())
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', ModItems.STAR_FRAGMENT.get())
                .unlockedBy("has_star_fragment", has(ModItems.STAR_FRAGMENT.get()))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.STAR_FRAGMENT.get(), 9)
                .requires(ModBlocks.STAR_FRAGMENT_BLOCK.get())
                .unlockedBy("has_star_fragment_block", has(ModBlocks.STAR_FRAGMENT_BLOCK.get()))
                .save(output, Starfall.id("star_fragment_from_block"));

        // ===== Fuel machines =====
        // Fluid Tank: iron frame with glass
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.FLUID_TANK.get())
                .pattern("IGI")
                .pattern("G G")
                .pattern("IGI")
                .define('I', Items.IRON_INGOT)
                .define('G', Items.GLASS)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(output);

        // Oil Extractor: piston + bucket in an iron casing
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.OIL_EXTRACTOR.get())
                .pattern("IPI")
                .pattern("IBI")
                .pattern("III")
                .define('I', Items.IRON_INGOT)
                .define('P', Items.PISTON)
                .define('B', Items.BUCKET)
                .unlockedBy("has_bucket", has(Items.BUCKET))
                .save(output);
    }
}
