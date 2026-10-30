package com.artmms.starfall.fuel;

import com.artmms.starfall.config.StarfallConfig;
import com.artmms.starfall.tag.ModTags;

import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * Starfall fuel system (no FE). Every fuel is converted to Fuel Points (FP),
 * which is what the rocket consumes. Values come from the config.
 * New fuels (coal, lava, natural gas...) are added here.
 */
public final class FuelValues {
    private FuelValues() {
    }

    /** FP given by 1 bucket (1000 mB) of this fluid. 0 = not a fuel. */
    public static int fuelPointsPerBucket(Fluid fluid) {
        if (fluid.is(ModTags.Fluids.CRUDE_OIL)) {
            return StarfallConfig.CRUDE_OIL_FUEL_POINTS.get();
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
}
