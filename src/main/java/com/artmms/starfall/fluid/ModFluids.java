package com.artmms.starfall.fluid;

import com.artmms.starfall.Starfall;
import com.artmms.starfall.block.ModBlocks;
import com.artmms.starfall.item.ModItems;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** Fluids and fluid types. */
public final class ModFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, Starfall.MODID);

    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, Starfall.MODID);

    // ===== Crude oil =====
    // Heavy and thick: swimming is very slow (see CrudeOilBlock), and you can drown.
    public static final DeferredHolder<FluidType, FluidType> CRUDE_OIL_TYPE = FLUID_TYPES.register("crude_oil",
            () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("block.starfall.crude_oil")
                    .canSwim(true)
                    .canDrown(true)
                    .supportsBoating(false)
                    .density(1500)
                    .viscosity(6000)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> CRUDE_OIL =
            FLUIDS.register("crude_oil", () -> new BaseFlowingFluid.Source(crudeOilProperties()));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> FLOWING_CRUDE_OIL =
            FLUIDS.register("flowing_crude_oil", () -> new BaseFlowingFluid.Flowing(crudeOilProperties()));

    private ModFluids() {
    }

    /** Flows slowly and not very far, like lava. */
    private static BaseFlowingFluid.Properties crudeOilProperties() {
        return new BaseFlowingFluid.Properties(CRUDE_OIL_TYPE, CRUDE_OIL, FLOWING_CRUDE_OIL)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(2)
                .tickRate(30)
                .explosionResistance(100.0F)
                .block(ModBlocks.CRUDE_OIL)
                .bucket(ModItems.CRUDE_OIL_BUCKET);
    }

    public static void register(IEventBus modEventBus) {
        FLUID_TYPES.register(modEventBus);
        FLUIDS.register(modEventBus);
    }
}
