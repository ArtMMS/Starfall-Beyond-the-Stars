package com.artmms.starfall.block;

import com.artmms.starfall.fluid.FluidExtraction;
import com.artmms.starfall.fluid.FluidUnits;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * Oil Extractor: the suction head of the pipe network. It stores nothing.
 * Place it in a pocket or pool (crude oil, water or lava), connect Iron Pipes
 * and turn a Steel Crank to pump the fluid into tanks.
 * - Right-click with an empty hand: shows what it can reach
 */
public class OilExtractorBlock extends Block {
    public OilExtractorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            FluidExtraction.PocketInfo info = FluidExtraction.inspect(level, pos);
            Component message;
            if (info.fluid() == null || info.sourceBlocks() <= 0) {
                message = Component.translatable("message.starfall.extractor.no_fluid");
            } else {
                Component fluidName = new FluidStack(info.fluid(), FluidType.BUCKET_VOLUME).getHoverName();
                message = Component.translatable("message.starfall.extractor.source", fluidName,
                        FluidUnits.liters(info.sourceBlocks() * FluidType.BUCKET_VOLUME));
            }
            player.displayClientMessage(message, true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
