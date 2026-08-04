package com.cuddly.heartbound.block;

import com.cuddly.heartbound.Heartbound;
import com.cuddly.heartbound.block.blocks.FusionTableBlock;
import com.cuddly.heartbound.block.blocks.LittedMagicalPumpkinBlock;
import com.cuddly.heartbound.block.blocks.MagicalPumpkinBlock;
import java.util.function.Function;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class HeartboundBlocks {
   public static Block MAGICAL_PUMPKIN = registerBlock(
      "magical_pumpkin",
      properties -> new MagicalPumpkinBlock(
            properties.mapColor(MapColor.ORANGE)
               .strength(1.0F)
               .sounds(BlockSoundGroup.WOOD)
               .allowsSpawning((state, world, pos, type) -> true)
               .pistonBehavior(PistonBehavior.DESTROY)
         )
   );
   public static Block LITTED_MAGICAL_PUMPKIN = registerBlock(
      "litted_magical_pumpkin",
      properties -> new LittedMagicalPumpkinBlock(
            properties.mapColor(MapColor.ORANGE).strength(1.0F).sounds(BlockSoundGroup.WOOD).luminance(state -> 15).pistonBehavior(PistonBehavior.DESTROY)
         )
   );
   public static Block FUSION_TABLE = registerBlock("fusion_table", properties -> new FusionTableBlock(properties.strength(2.5F).sounds(BlockSoundGroup.WOOD)));

   private static Block registerBlock(String name, Function<Settings, Block> function) {
      Block toRegister = function.apply(Settings.create());
      registerBlockItem(name, toRegister);
      return Registry.register(Registries.BLOCK, Identifier.of("heartbound", name), toRegister);
   }

   private static Block registerBlockWithoutBlockItem(String name, Function<Settings, Block> function) {
      return Registry.register(Registries.BLOCK, Identifier.of("heartbound", name), function.apply(Settings.create()));
   }

   private static void registerBlockItem(String name, Block block) {
      Registry.register(Registries.ITEM, Identifier.of("heartbound", name), new BlockItem(block, new net.minecraft.item.Item.Settings()));
   }

   public static void registerBlocks() {
      Heartbound.LOGGER.info("Registering Block for Heartbound");
   }
}
