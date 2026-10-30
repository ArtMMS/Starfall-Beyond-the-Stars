package com.artmms.starfall.block;

import java.util.Map;

import javax.annotation.Nullable;

import com.artmms.starfall.block.entity.SteelCrankBlockEntity;
import com.artmms.starfall.config.StarfallConfig;
import com.artmms.starfall.pipe.PipeNetwork;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Steel Crank: the pump of the pipe network.
 * - Attaches to an Iron Pipe on any side (FACING = the side the pipe is on)
 *   and spins around the pipe's axis
 * - Each right-click spins it once and pumps fluid from the connected
 *   extractors into every connected Fluid Tank
 * - A new turn only starts after the previous animation ends
 * The crank is drawn by SteelCrankRenderer (that's how it spins smoothly).
 */
public class SteelCrankBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    /** Length of one turn, in ticks (20 ticks = 1 second). */
    public static final int SPIN_TICKS = 16;

    /** Hitbox for each side the crank can be attached to. */
    private static final Map<Direction, VoxelShape> SHAPES = Map.of(
            Direction.DOWN, Block.box(2, 0, 2, 14, 15, 14),
            Direction.UP, Block.box(2, 1, 2, 14, 16, 14),
            Direction.NORTH, Block.box(2, 2, 0, 14, 14, 15),
            Direction.SOUTH, Block.box(2, 2, 1, 14, 14, 16),
            Direction.WEST, Block.box(0, 2, 2, 15, 14, 14),
            Direction.EAST, Block.box(1, 2, 2, 16, 14, 14));

    /** Preferred order when looking for a pipe to attach to. */
    private static final Direction[] SEARCH_ORDER = {
            Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST
    };

    public SteelCrankBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.DOWN));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SteelCrankBlockEntity(pos, state);
    }

    /** The world model is drawn by the renderer, not by the block model. */
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    // ===== Attaching to pipes =====

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        // Clicked directly on a pipe: attach to it
        Direction clicked = context.getClickedFace().getOpposite();
        if (isPipe(level, pos, clicked)) {
            return defaultBlockState().setValue(FACING, clicked);
        }

        Direction found = findPipe(level, pos);
        return defaultBlockState().setValue(FACING, found != null ? found : clicked);
    }

    /** If the pipe it was attached to is gone, it re-attaches to another pipe nearby. */
    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (isPipe(level, pos, state.getValue(FACING))) {
            return state;
        }
        Direction found = findPipe(level, pos);
        return found != null ? state.setValue(FACING, found) : state;
    }

    @Nullable
    private static Direction findPipe(BlockGetter level, BlockPos pos) {
        for (Direction direction : SEARCH_ORDER) {
            if (isPipe(level, pos, direction)) {
                return direction;
            }
        }
        return null;
    }

    private static boolean isPipe(BlockGetter level, BlockPos pos, Direction direction) {
        return level.getBlockState(pos.relative(direction)).getBlock() instanceof IronPipeBlock;
    }

    // ===== Turning =====

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hitResult) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof SteelCrankBlockEntity crank) {
            // Still spinning: ignore the click until the turn ends
            if (crank.tryStartSpin()) {
                PipeNetwork.PumpResult result = PipeNetwork.pump(level, pos, StarfallConfig.CRANK_PUMP_AMOUNT.get());
                player.displayClientMessage(result.message(), true);
                level.playSound(null, pos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 0.5F, 1.2F);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
