package com.artmms.starfall.block;

import javax.annotation.Nullable;

import com.artmms.starfall.block.entity.ModBlockEntities;
import com.artmms.starfall.block.entity.OilExtractorBlockEntity;

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
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidUtil;

/**
 * Oil Extractor block.
 * - Right-click with an empty hand: shows the status
 * - Right-click with an empty bucket: takes 1 bucket
 * - Connect an Iron Pipe and a Steel Crank to pump into tanks
 */
public class OilExtractorBlock extends Block implements EntityBlock {
    public OilExtractorBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new OilExtractorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (level.isClientSide() || type != ModBlockEntities.OIL_EXTRACTOR.get()) {
            return null;
        }
        return (tickLevel, pos, tickState, blockEntity) -> ((OilExtractorBlockEntity) blockEntity).serverTick();
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.getCapability(Capabilities.FluidHandler.ITEM) == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof OilExtractorBlockEntity extractor) {
            FluidUtil.interactWithFluidHandler(player, hand, extractor.getTank());
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hitResult) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof OilExtractorBlockEntity extractor) {
            player.displayClientMessage(extractor.describe(), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
