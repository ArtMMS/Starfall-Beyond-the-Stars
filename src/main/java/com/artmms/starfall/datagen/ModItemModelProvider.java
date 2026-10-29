package com.artmms.starfall.datagen;

import com.artmms.starfall.Starfall;
import com.artmms.starfall.item.ModItems;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/** Generates item models. Flat items use textures/item/NAME.png. */
public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Starfall.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.STAR_FRAGMENT.get());
    }
}
