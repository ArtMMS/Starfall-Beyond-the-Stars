package com.artmms.starfall.item;

import com.artmms.starfall.network.AlertLevel;
import com.artmms.starfall.network.AlertPayload;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * A fragment of a fallen star. For now, right-clicking it makes the server
 * send an alert packet to the player (used to test the network system).
 */
public class StarFragmentItem extends Item {
    public StarFragmentItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer,
                    new AlertPayload("message.starfall.star_fragment.hum", AlertLevel.INFO));
            player.getCooldowns().addCooldown(this, 20);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
