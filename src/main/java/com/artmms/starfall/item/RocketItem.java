package com.artmms.starfall.item;

import javax.annotation.Nullable;

import com.artmms.starfall.entity.ModEntities;
import com.artmms.starfall.entity.rocket.RocketEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Places a rocket on the ground (no recipe yet).
 * The launch pad must be a flat, solid 5x5 area, with free space above:
 * 5x5 for the fins (3 blocks tall) and 3x3 for the body (the whole rocket height).
 */
public class RocketItem extends Item {
    /** 2 = a 5x5 pad. */
    private static final int PAD_RADIUS = 2;
    /** 1 = the 3x3 body. */
    private static final int BODY_RADIUS = 1;
    private static final int FIN_HEIGHT = 3;
    private static final int ROCKET_HEIGHT = (int) Math.ceil(RocketEntity.HEIGHT);

    public RocketItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (context.getClickedFace() != Direction.UP) {
            message(player, "message.starfall.rocket.place_on_top");
            return InteractionResult.FAIL;
        }

        BlockPos ground = context.getClickedPos();
        String problem = checkPad(level, ground);
        if (problem != null) {
            message(player, problem);
            return InteractionResult.FAIL;
        }

        RocketEntity rocket = ModEntities.ROCKET.get().create(level);
        if (rocket == null) {
            return InteractionResult.FAIL;
        }

        // Windows face the player who placed it
        float yaw = player != null ? Math.round(player.getYRot() / 90.0F) * 90.0F + 180.0F : 0.0F;
        rocket.moveTo(ground.getX() + 0.5D, ground.getY() + 1.0D, ground.getZ() + 0.5D, yaw, 0.0F);
        level.addFreshEntity(rocket);

        if (player == null || !player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        message(player, "message.starfall.rocket.placed");
        return InteractionResult.CONSUME;
    }

    /** @return the translation key of the problem, or null if the pad is fine */
    @Nullable
    private static String checkPad(Level level, BlockPos ground) {
        for (int dx = -PAD_RADIUS; dx <= PAD_RADIUS; dx++) {
            for (int dz = -PAD_RADIUS; dz <= PAD_RADIUS; dz++) {
                BlockPos floor = ground.offset(dx, 0, dz);
                if (!level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)) {
                    return "message.starfall.rocket.needs_flat";
                }
                for (int dy = 1; dy <= FIN_HEIGHT; dy++) {
                    if (!isFree(level, floor.above(dy))) {
                        return "message.starfall.rocket.needs_space";
                    }
                }
            }
        }

        for (int dx = -BODY_RADIUS; dx <= BODY_RADIUS; dx++) {
            for (int dz = -BODY_RADIUS; dz <= BODY_RADIUS; dz++) {
                for (int dy = 1; dy <= ROCKET_HEIGHT; dy++) {
                    if (!isFree(level, ground.offset(dx, dy, dz))) {
                        return "message.starfall.rocket.needs_space";
                    }
                }
            }
        }

        AABB area = new AABB(ground.above()).inflate(PAD_RADIUS, 0, PAD_RADIUS).expandTowards(0, ROCKET_HEIGHT, 0);
        if (!level.getEntitiesOfClass(RocketEntity.class, area).isEmpty()) {
            return "message.starfall.rocket.needs_space";
        }
        return null;
    }

    private static boolean isFree(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.canBeReplaced();
    }

    private static void message(@Nullable Player player, String key) {
        if (player != null) {
            player.displayClientMessage(Component.translatable(key), true);
        }
    }
}
