package com.auroali.sanguinisluxuria.common.registry;

import com.auroali.sanguinisluxuria.BLResources;
import com.auroali.sanguinisluxuria.common.blocks.*;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.minecraft.block.*;
import net.minecraft.block.enums.Instrument;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.DyeColor;

public class BLBlocks {
    public static final Block BLOOD_SPLATTER = new BloodSplatterBlock(
      AbstractBlock.Settings.create(/*Materials.DECORATION*/)
        .mapColor(DyeColor.RED)
        .breakInstantly()
        .noCollision()
        .ticksRandomly()
        .sounds(BlockSoundGroup.HONEY)
    );

    //public static final FluidBlock BLOOD = new FluidBlock(BLFluids.BLOOD_STILL, FabricBlockSettings.of(Material.LAVA));
    public static final Block BLOOD_CAULDRON = new BloodCauldronBlock(AbstractBlock.Settings.copy(Blocks.CAULDRON));
    public static final Block ALTAR = new AltarBlock(AbstractBlock.Settings.create().mapColor(DyeColor.BLACK).requiresTool().strength(1.5F, 6.0F));
    public static final Block PEDESTAL = new PedestalBlock(AbstractBlock.Settings.create().mapColor(DyeColor.BLACK).requiresTool().strength(1.5F, 6.0F));
    public static final Block SILVER_BLOCK = new Block(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK).sounds(BlockSoundGroup.NETHERITE));
    public static final Block SILVER_ORE = new Block(AbstractBlock.Settings.copy(Blocks.IRON_ORE));
    public static final Block DEEPSLATE_SILVER_ORE = new Block(AbstractBlock.Settings.copy(Blocks.DEEPSLATE_IRON_ORE));
    public static final Block RAW_SILVER_BLOCK = new Block(AbstractBlock.Settings.copy(Blocks.RAW_COPPER_BLOCK));
    public static final Block HUNGRY_DECAYED_LOG = new HungryDecayedLogBlock(AbstractBlock.Settings.create().burnable().ticksRandomly().mapColor(MapColor.OFF_WHITE).instrument(Instrument.BASS).strength(2.f).sounds(BlockSoundGroup.WOOD));
    public static final Block STRIPPED_HUNGRY_DECAYED_LOG = new HungryDecayedLogBlock(AbstractBlock.Settings.create().burnable().ticksRandomly().mapColor(MapColor.OFF_WHITE).instrument(Instrument.BASS).strength(2.f).sounds(BlockSoundGroup.WOOD));
    public static final Block DECAYED_LOG = new PillarBlock(AbstractBlock.Settings.create().mapColor(MapColor.OFF_WHITE).burnable().instrument(Instrument.BASS).strength(2.f).sounds(BlockSoundGroup.WOOD));
    public static final Block DECAYED_WOOD = new PillarBlock(AbstractBlock.Settings.create().mapColor(MapColor.OFF_WHITE).burnable().instrument(Instrument.BASS).strength(2.f).sounds(BlockSoundGroup.WOOD));
    public static final Block STRIPPED_DECAYED_LOG = new PillarBlock(AbstractBlock.Settings.create().mapColor(MapColor.OFF_WHITE).burnable().instrument(Instrument.BASS).strength(2.f).sounds(BlockSoundGroup.WOOD));
    public static final Block STRIPPED_DECAYED_WOOD = new PillarBlock(AbstractBlock.Settings.create().mapColor(MapColor.OFF_WHITE).burnable().instrument(Instrument.BASS).strength(2.f).sounds(BlockSoundGroup.WOOD));
    public static final Block DECAYED_TWIGS = new DecayedTwigsBlock(AbstractBlock.Settings.create().mapColor(MapColor.OFF_WHITE).noCollision().burnable().instrument(Instrument.BASS).sounds(BlockSoundGroup.WOOD).breakInstantly());
    public static final Block HUNGRY_SAPLING = new HungrySaplingBlock(AbstractBlock.Settings.copy(Blocks.OAK_SAPLING).ticksRandomly());

    public static void register() {
        Registry.register(Registries.BLOCK, BLResources.BLOOD_SPLATTER_ID, BLOOD_SPLATTER);
        Registry.register(Registries.BLOCK, BLResources.ALTAR_ID, ALTAR);
        Registry.register(Registries.BLOCK, BLResources.PEDESTAL_ID, PEDESTAL);
        Registry.register(Registries.BLOCK, BLResources.BLOOD_CAULDRON_ID, BLOOD_CAULDRON);
        Registry.register(Registries.BLOCK, BLResources.SILVER_BLOCK_ID, SILVER_BLOCK);
        Registry.register(Registries.BLOCK, BLResources.SILVER_ORE_ID, SILVER_ORE);
        Registry.register(Registries.BLOCK, BLResources.DEEPSLATE_SILVER_ORE_ID, DEEPSLATE_SILVER_ORE);
        Registry.register(Registries.BLOCK, BLResources.RAW_SILVER_BLOCK_ID, RAW_SILVER_BLOCK);
        Registry.register(Registries.BLOCK, BLResources.HUNGRY_DECAYED_LOG, HUNGRY_DECAYED_LOG);
        Registry.register(Registries.BLOCK, BLResources.STRIPPED_HUNGRY_DECAYED_LOG, STRIPPED_HUNGRY_DECAYED_LOG);
        Registry.register(Registries.BLOCK, BLResources.STRIPPED_DECAYED_LOG, STRIPPED_DECAYED_LOG);
        Registry.register(Registries.BLOCK, BLResources.STRIPPED_DECAYED_WOOD, STRIPPED_DECAYED_WOOD);
        Registry.register(Registries.BLOCK, BLResources.DECAYED_LOG, DECAYED_LOG);
        Registry.register(Registries.BLOCK, BLResources.DECAYED_WOOD, DECAYED_WOOD);
        Registry.register(Registries.BLOCK, BLResources.DECAYED_TWIGS, DECAYED_TWIGS);
        Registry.register(Registries.BLOCK, BLResources.HUNGRY_SAPLING, HUNGRY_SAPLING);

        StrippableBlockRegistry.register(HUNGRY_DECAYED_LOG, STRIPPED_HUNGRY_DECAYED_LOG);
        StrippableBlockRegistry.register(DECAYED_LOG, STRIPPED_DECAYED_LOG);
        StrippableBlockRegistry.register(DECAYED_WOOD, STRIPPED_DECAYED_WOOD);

        FlammableBlockRegistry fireRegistry = FlammableBlockRegistry.getDefaultInstance();
        fireRegistry.add(DECAYED_WOOD, 5, 5);
        fireRegistry.add(STRIPPED_DECAYED_WOOD, 5, 5);
        fireRegistry.add(DECAYED_LOG, 5, 5);
        fireRegistry.add(STRIPPED_DECAYED_LOG, 5, 5);
        fireRegistry.add(HUNGRY_DECAYED_LOG, 5, 5);
        fireRegistry.add(STRIPPED_HUNGRY_DECAYED_LOG, 5, 5);
        fireRegistry.add(DECAYED_TWIGS, 5, 5);
    }
}
