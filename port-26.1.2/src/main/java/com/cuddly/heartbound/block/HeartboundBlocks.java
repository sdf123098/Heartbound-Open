package com.cuddly.heartbound.block;

import com.cuddly.heartbound.Heartbound;
import com.cuddly.heartbound.block.blocks.FusionTableBlock;
import com.cuddly.heartbound.block.blocks.LittedMagicalPumpkinBlock;
import com.cuddly.heartbound.block.blocks.MagicalPumpkinBlock;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public class HeartboundBlocks {
   public static Block MAGICAL_PUMPKIN = registerBlock(
      "magical_pumpkin",
      properties -> new MagicalPumpkinBlock(
            properties.mapColor(MapColor.COLOR_ORANGE)
               .strength(1.0F)
               .sound(SoundType.WOOD)
               .isValidSpawn((state, world, pos, type) -> true)
               .pushReaction(PushReaction.DESTROY)
         )
   );
   public static Block LITTED_MAGICAL_PUMPKIN = registerBlock(
      "litted_magical_pumpkin",
      properties -> new LittedMagicalPumpkinBlock(
            properties.mapColor(MapColor.COLOR_ORANGE).strength(1.0F).sound(SoundType.WOOD).lightLevel(state -> 15).pushReaction(PushReaction.DESTROY)
         )
   );
   public static Block FUSION_TABLE = registerBlock("fusion_table", properties -> new FusionTableBlock(properties.strength(2.5F).sound(SoundType.WOOD)));

   private static Block registerBlock(String name, Function<Properties, Block> function) {
      Identifier id = Identifier.fromNamespaceAndPath("heartbound", name);
      ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, id);
      Block toRegister = function.apply(Properties.of().setId(key));
      registerBlockItem(name, toRegister, key);
      return Registry.register(BuiltInRegistries.BLOCK, key, toRegister);
   }

   private static Block registerBlockWithoutBlockItem(String name, Function<Properties, Block> function) {
      Identifier id = Identifier.fromNamespaceAndPath("heartbound", name);
      ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, id);
      return Registry.register(BuiltInRegistries.BLOCK, key, function.apply(Properties.of().setId(key)));
   }

   private static void registerBlockItem(String name, Block block, ResourceKey<Block> blockKey) {
      ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, blockKey.identifier());
      Registry.register(BuiltInRegistries.ITEM, itemKey, new BlockItem(block, new Item.Properties().setId(itemKey)));
   }

   public static void registerBlocks() {
      Heartbound.LOGGER.info("Registering Block for Heartbound");
   }
}
