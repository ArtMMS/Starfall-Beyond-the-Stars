package com.artmms.starfall.item;

import com.artmms.starfall.Starfall;
import com.artmms.starfall.fluid.ModFluids;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** All Starfall items. Block items are registered from ModBlocks. */
public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Starfall.MODID);

    public static final DeferredItem<StarFragmentItem> STAR_FRAGMENT =
            ITEMS.register("star_fragment", () -> new StarFragmentItem(new Item.Properties()));

    public static final DeferredItem<BucketItem> CRUDE_OIL_BUCKET =
            ITEMS.register("crude_oil_bucket", () -> new BucketItem(ModFluids.CRUDE_OIL.get(),
                    new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    private ModItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
