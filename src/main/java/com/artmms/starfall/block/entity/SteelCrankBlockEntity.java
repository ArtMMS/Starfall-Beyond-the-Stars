package com.artmms.starfall.block.entity;

import com.artmms.starfall.block.SteelCrankBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Remembers when the crank started spinning, so:
 * - the server ignores clicks until the current turn ends
 * - the client can draw the spin smoothly
 */
public class SteelCrankBlockEntity extends BlockEntity {
    private static final String SPIN_TAG = "SpinStart";
    /** "Long ago", so the crank starts ready to turn. */
    private static final long NEVER = -1_000_000L;

    private long spinStart = NEVER;

    public SteelCrankBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STEEL_CRANK.get(), pos, state);
    }

    /** Starts a turn if the previous one is over. Server side only. */
    public boolean tryStartSpin() {
        if (level == null || isSpinning(0.0F)) {
            return false;
        }
        spinStart = level.getGameTime();
        setChanged();
        BlockState state = getBlockState();
        level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        return true;
    }

    public boolean isSpinning(float partialTick) {
        float progress = getSpinProgress(partialTick);
        return progress > 0.0F && progress < 1.0F;
    }

    /** 0..1 while spinning, 0 when idle. */
    public float getSpinProgress(float partialTick) {
        if (level == null) {
            return 0.0F;
        }
        float elapsed = (level.getGameTime() - spinStart) + partialTick;
        if (elapsed < 0.0F || elapsed >= SteelCrankBlock.SPIN_TICKS) {
            return 0.0F;
        }
        // Tiny offset so the very first tick already counts as spinning
        return Math.max(0.0001F, elapsed / SteelCrankBlock.SPIN_TICKS);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong(SPIN_TAG, spinStart);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        spinStart = tag.contains(SPIN_TAG) ? tag.getLong(SPIN_TAG) : NEVER;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putLong(SPIN_TAG, spinStart);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
