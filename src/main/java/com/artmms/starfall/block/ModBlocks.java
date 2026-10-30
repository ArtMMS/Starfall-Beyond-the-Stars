package com.artmms.starfall.block;

import java.util.function.Supplier;

import com.artmms.starfall.Starfall;
import com.artmms.starfall.fluid.ModFluids;
import com.artmms.starfall.item.ModItems;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/** All Starfall blocks. Each block registered with registerBlock also gets a BlockItem. */
public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Starfall.MODID);

    // ===== Test content (Phase 1) =====
    public static final DeferredBlock<Block> STAR_FRAGMENT_BLOCK = registerBlock("star_fragment_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .strength(5.0F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.AMETHYST)
                    .lightLevel(state -> 7)));

    // ===== Crude oil =====
    /** The liquid block itself. No BlockItem: players use the bucket. */
    public static final DeferredBlock<CrudeOilBlock> CRUDE_OIL = BLOCKS.register("crude_oil",
            () -> new CrudeOilBlock(ModFluids.CRUDE_OIL.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER)
                    .mapColor(MapColor.COLOR_BLACK)
                    .ignitedByLava()
                    .noLootTable()));

    /** Surface hint: a sticky patch that appears above big oil pockets. Drops nothing. */
    public static final DeferredBlock<CarpetBlock> TAR_PATCH = registerBlock("tar_patch",
            () -> new CarpetBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(0.3F)
                    .sound(SoundType.HONEY_BLOCK)
                    .speedFactor(0.5F)
                    .jumpFactor(0.6F)
                    .noLootTable()));

    // ===== Fuel machines =====
    /** noOcclusion: the glass is see-through, so the liquid inside can be drawn. */
    public static final DeferredBlock<FluidTankBlock> FLUID_TANK = registerBlock("fluid_tank",
            () -> new FluidTankBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.0F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)
                    .noOcclusion()));

    public static final DeferredBlock<OilExtractorBlock> OIL_EXTRACTOR = registerBlock("oil_extractor",
            () -> new OilExtractorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.5F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)));

    // ===== Pipes =====
    public static final DeferredBlock<IronPipeBlock> IRON_PIPE = registerBlock("iron_pipe",
            () -> new IronPipeBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.0F, 6.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion()));

    /** The pump of the pipe network. */
    public static final DeferredBlock<SteelCrankBlock> STEEL_CRANK = registerBlock("steel_crank",
            () -> new SteelCrankBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.5F, 6.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion()));

    private ModBlocks() {
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> registered = BLOCKS.register(name, block);
        ModItems.ITEMS.registerSimpleBlockItem(name, registered);
        return registered;
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
