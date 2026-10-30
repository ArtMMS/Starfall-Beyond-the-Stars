package com.artmms.starfall.fluid;

import java.util.Locale;

/**
 * Converts the internal unit (mB, used by NeoForge and every other mod)
 * into what players see in Starfall: liters, where 1 bucket = 1 liter.
 * Example: 6500 mB is shown as "6.5".
 */
public final class FluidUnits {
    public static final int MB_PER_LITER = 1000;

    private FluidUnits() {
    }

    /** Liters for an amount in mB, with at most one decimal ("6", "6.5"). */
    public static String liters(int millibuckets) {
        if (millibuckets % MB_PER_LITER == 0) {
            return Integer.toString(millibuckets / MB_PER_LITER);
        }
        return String.format(Locale.ROOT, "%.1f", millibuckets / (double) MB_PER_LITER);
    }
}
