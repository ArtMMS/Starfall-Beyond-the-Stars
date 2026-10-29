package com.artmms.starfall.item;

import com.artmms.starfall.Starfall;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The Starfall creative tab. Every registered Starfall item shows up here automatically. */
public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Starfall.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> STARFALL_TAB =
            CREATIVE_TABS.register("starfall", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.starfall"))
                    .icon(() -> new ItemStack(ModItems.STAR_FRAGMENT.get()))
                    .displayItems((parameters, output) ->
                            ModItems.ITEMS.getEntries().forEach(item -> output.accept(item.get())))
                    .build());

    private ModCreativeTabs() {
    }

    public static void register(IEventBus modEventBus) {
        CREATIVE_TABS.register(modEventBus);
    }
}
