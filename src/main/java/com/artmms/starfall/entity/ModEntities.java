package com.artmms.starfall.entity;

import com.artmms.starfall.Starfall;
import com.artmms.starfall.entity.rocket.RocketEntity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Entities (rocket, aliens, meteors...). */
public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, Starfall.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<RocketEntity>> ROCKET =
            ENTITIES.register("rocket", () -> EntityType.Builder.<RocketEntity>of(RocketEntity::new, MobCategory.MISC)
                    .sized(RocketEntity.WIDTH, RocketEntity.HEIGHT)
                    .clientTrackingRange(10)
                    // Send the position every tick, so the flight looks smooth
                    .updateInterval(1)
                    .fireImmune()
                    .build("starfall:rocket"));

    private ModEntities() {
    }

    public static void register(IEventBus modEventBus) {
        ENTITIES.register(modEventBus);
    }
}
