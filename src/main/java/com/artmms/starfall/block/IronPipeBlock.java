package com.artmms.starfall.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/**
 * Iron Pipe: connects automatically like a fence, but in all 6 directions
 * (so it can go straight up from an extractor). It only connects to other pipes,
 * Fluid Tanks, extractors and the Steel Crank. Pipes don't move fluid by
 * themselves: the Steel Crank pumps through them.
 */
public class IronPipeBlock extends PipeBlock {
    public static final MapCodec<IronPipeBlock> CODEC = simpleCodec(IronPipeBlock::new);

    public IronPipeBlock(Properties properties) {
        // 0.125 = the pipe is 4 pixels thick (from 6 to 10)
        super(0.125F, properties);
        registerDefaultState(stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(UP, false)
                .setValue(DOWN, false));
    }

    @Override
    protected MapCodec<? extends PipeBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return withConnections(context.getLevel(), context.getClickedPos(), defaultBlockState());
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state.setValue(PROPERTY_BY_DIRECTION.get(direction), connectsTo(neighborState));
    }

    private BlockState withConnections(BlockGetter level, BlockPos pos, BlockState state) {
        for (Direction direction : Direction.values()) {
            BlockState neighbor = level.getBlockState(pos.relative(direction));
            state = state.setValue(PROPERTY_BY_DIRECTION.get(direction), connectsTo(neighbor));
        }
        return state;
    }

    /** What a pipe can connect to. */
    public static boolean connectsTo(BlockState state) {
        Block block = state.getBlock();
        return block instanceof IronPipeBlock
                || block instanceof FluidTankBlock
                || block instanceof OilExtractorBlock
                || block instanceof SteelCrankBlock;
    }
}
