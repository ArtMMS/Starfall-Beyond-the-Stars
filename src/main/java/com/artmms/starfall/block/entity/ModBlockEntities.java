package com.artmms.starfall.block.entity;

import com.artmms.starfall.Starfall;
import com.artmms.starfall.block.ModBlocks;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Block entities (machines and storage). */
public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Starfall.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidTankBlockEntity>> FLUID_TANK =
            BLOCK_ENTITIES.register("fluid_tank", () -> BlockEntityType.Builder
                    .of(FluidTankBlockEntity::new, ModBlocks.FLUID_TANK.get())
                    .build(null));

    /** Only keeps the spin animation timing (synced to clients). */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SteelCrankBlockEntity>> STEEL_CRANK =
            BLOCK_ENTITIES.register("steel_crank", () -> BlockEntityType.Builder
                    .of(SteelCrankBlockEntity::new, ModBlocks.STEEL_CRANK.get())
                    .build(null));

    private ModBlockEntities() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
    }
}
