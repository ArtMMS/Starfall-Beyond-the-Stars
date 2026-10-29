package com.artmms.starfall.item;

import com.artmms.starfall.Starfall;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** All Starfall items. Block items are registered from ModBlocks. */
public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Starfall.MODID);

    public static final DeferredItem<StarFragmentItem> STAR_FRAGMENT =
            ITEMS.register("star_fragment", () -> new StarFragmentItem(new Item.Properties()));

    private ModItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
