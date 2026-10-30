package com.artmms.starfall.datagen;

import com.artmms.starfall.Starfall;
import com.artmms.starfall.item.ModItems;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * Generates item models. Flat items use textures/item/NAME.png.
 * The crude oil bucket model is hand-written (models/item/crude_oil_bucket.json):
 * it reuses the vanilla bucket and paints the liquid with the fluid color.
 */
public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Starfall.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.STAR_FRAGMENT.get());
    }
}
