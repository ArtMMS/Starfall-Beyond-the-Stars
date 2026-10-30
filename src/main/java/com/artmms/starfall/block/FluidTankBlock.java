package com.artmms.starfall.block;

import javax.annotation.Nullable;

import com.artmms.starfall.block.entity.FluidTankBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidUtil;

/**
 * Fluid Tank block.
 * - Right-click with a bucket: fills or empties the bucket
 * - Right-click with an empty hand: shows the contents
 */
public class FluidTankBlock extends Block implements EntityBlock {
    public FluidTankBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FluidTankBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        // Only fluid containers (buckets etc.) interact; other items behave normally
        if (stack.getCapability(Capabilities.FluidHandler.ITEM) == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof FluidTankBlockEntity tank) {
            FluidUtil.interactWithFluidHandler(player, hand, tank.getTank());
        }
        // Success on both sides, so the bucket never spills the fluid in front of the tank
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hitResult) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof FluidTankBlockEntity tank) {
            player.displayClientMessage(tank.describe(), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
