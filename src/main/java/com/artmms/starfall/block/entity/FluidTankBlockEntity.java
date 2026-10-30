package com.artmms.starfall.block.entity;

import com.artmms.starfall.config.StarfallConfig;
import com.artmms.starfall.fuel.FuelValues;
import com.artmms.starfall.tag.ModTags;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

/**
 * Fluid Tank: stores one fluid at a time (water, lava or crude oil).
 * Exposed to other mods and pipes through the NeoForge fluid capability.
 */
public class FluidTankBlockEntity extends BlockEntity {
    private static final String TANK_TAG = "Tank";

    private final FluidTank tank = new FluidTank(StarfallConfig.BASIC_TANK_CAPACITY.get(),
            stack -> stack.is(ModTags.Fluids.BASIC_TANK_FLUIDS)) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    public FluidTankBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUID_TANK.get(), pos, state);
    }

    public FluidTank getTank() {
        return tank;
    }

    /** Text shown when the player right-clicks the tank with an empty hand. */
    public Component describe() {
        FluidStack fluid = tank.getFluid();
        if (fluid.isEmpty()) {
            return Component.translatable("message.starfall.tank.empty", tank.getCapacity());
        }

        Component base = Component.translatable("message.starfall.tank.contents",
                fluid.getHoverName(), tank.getFluidAmount(), tank.getCapacity());

        if (FuelValues.isFuel(fluid)) {
            return base.copy().append(Component.translatable("message.starfall.fuel_points",
                    FuelValues.fuelPoints(fluid)));
        }
        return base;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put(TANK_TAG, tank.writeToNBT(registries, new CompoundTag()));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains(TANK_TAG)) {
            tank.readFromNBT(registries, tag.getCompound(TANK_TAG));
        }
    }
}
