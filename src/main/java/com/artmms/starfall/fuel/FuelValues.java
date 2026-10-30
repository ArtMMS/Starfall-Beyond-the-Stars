package com.artmms.starfall.fuel;

import com.artmms.starfall.config.StarfallConfig;
import com.artmms.starfall.item.ModItems;
import com.artmms.starfall.tag.ModTags;

import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * Starfall fuel system (no FE). Every fuel is converted to Fuel Points (FP),
 * which is what the rocket consumes. Values come from the config.
 * Best to worst: crude oil > lava > coal.
 */
public final class FuelValues {
    /** A block of coal is worth this many pieces of coal. */
    private static final int COAL_BLOCK_MULTIPLIER = 9;

    private FuelValues() {
    }

    // ===== Fluids =====

    /** FP given by 1 bucket (1000 mB) of this fluid. 0 = not a fuel. */
    public static int fuelPointsPerBucket(Fluid fluid) {
        if (fluid.is(ModTags.Fluids.CRUDE_OIL)) {
            return StarfallConfig.CRUDE_OIL_FUEL_POINTS.get();
        }
        if (fluid.is(FluidTags.LAVA)) {
            return StarfallConfig.LAVA_FUEL_POINTS.get();
        }
        return 0;
    }

    public static boolean isFuel(FluidStack stack) {
        return !stack.isEmpty() && fuelPointsPerBucket(stack.getFluid()) > 0;
    }

    /** Total FP of a fluid stack, proportional to the amount. */
    public static long fuelPoints(FluidStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        return (long) fuelPointsPerBucket(stack.getFluid()) * stack.getAmount() / FluidType.BUCKET_VOLUME;
    }

    // ===== Items (used to refuel the rocket by hand) =====

    /** FP given by one of this item. 0 = not a fuel. */
    public static int fuelPointsForItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        if (stack.is(ModItems.CRUDE_OIL_BUCKET.get())) {
            return StarfallConfig.CRUDE_OIL_FUEL_POINTS.get();
        }
        if (stack.is(Items.LAVA_BUCKET)) {
            return StarfallConfig.LAVA_FUEL_POINTS.get();
        }
        if (stack.is(Items.COAL_BLOCK)) {
            return StarfallConfig.COAL_FUEL_POINTS.get() * COAL_BLOCK_MULTIPLIER;
        }
        if (stack.is(ItemTags.COALS)) {
            return StarfallConfig.COAL_FUEL_POINTS.get();
        }
        return 0;
    }
}
