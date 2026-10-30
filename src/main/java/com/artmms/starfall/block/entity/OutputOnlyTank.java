package com.artmms.starfall.block.entity;

import java.util.function.Predicate;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

/**
 * A tank that others can only drain from (pipes, buckets, neighbor tanks).
 * The owning machine fills it with {@link #fillInternal(FluidStack)}.
 */
public class OutputOnlyTank extends FluidTank {
    private final Runnable onChange;

    public OutputOnlyTank(int capacity, Predicate<FluidStack> validator, Runnable onChange) {
        super(capacity, validator);
        this.onChange = onChange;
    }

    /** External fills are refused. */
    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return 0;
    }

    /** Used by the machine itself. */
    public int fillInternal(FluidStack resource) {
        return super.fill(resource, FluidAction.EXECUTE);
    }

    @Override
    protected void onContentsChanged() {
        onChange.run();
    }
}
