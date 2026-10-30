package com.artmms.starfall.block;

import com.artmms.starfall.config.StarfallConfig;
import com.artmms.starfall.pipe.PipeNetwork;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Steel Crank: the pump of the pipe network.
 * Each right-click spins the crank and pumps fluid from the connected
 * extractors into every connected Fluid Tank.
 *
 * Animation: ROTATION has 8 steps of 45 degrees. A click spins one full turn,
 * advancing one step every 2 ticks (SPIN = steps left).
 */
public class SteelCrankBlock extends Block {
    public static final IntegerProperty ROTATION = IntegerProperty.create("rotation", 0, 7);
    public static final IntegerProperty SPIN = IntegerProperty.create("spin", 0, 7);

    private static final int TICKS_PER_STEP = 2;
    private static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 15.0D, 12.0D);

    public SteelCrankBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(ROTATION, 0).setValue(SPIN, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ROTATION, SPIN);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            PipeNetwork.PumpResult result = PipeNetwork.pump(level, pos, StarfallConfig.CRANK_PUMP_AMOUNT.get());
            player.displayClientMessage(result.message(), true);

            level.playSound(null, pos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 0.5F, 1.2F);
            startSpinning(level, pos, state);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private void startSpinning(Level level, BlockPos pos, BlockState state) {
        boolean alreadySpinning = state.getValue(SPIN) > 0;
        level.setBlock(pos, nextStep(state).setValue(SPIN, 7), Block.UPDATE_CLIENTS);
        if (!alreadySpinning) {
            level.scheduleTick(pos, this, TICKS_PER_STEP);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int spin = state.getValue(SPIN);
        if (spin <= 0) {
            return;
        }
        level.setBlock(pos, nextStep(state).setValue(SPIN, spin - 1), Block.UPDATE_CLIENTS);
        if (spin - 1 > 0) {
            level.scheduleTick(pos, this, TICKS_PER_STEP);
        }
    }

    private static BlockState nextStep(BlockState state) {
        return state.setValue(ROTATION, (state.getValue(ROTATION) + 1) % 8);
    }
}
