package com.cuddly.heartbound.item;

import com.cuddly.heartbound.block.HeartboundBlocks;
import com.cuddly.heartbound.entity.HeartboundEntities;
import com.cuddly.heartbound.util.json.CustomGirlLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;

public class HeartboundItemGroups {
   public static final CreativeModeTab PLEASURE_CRAFT_ITEM_GROUP = Registry.register(
      BuiltInRegistries.CREATIVE_MODE_TAB,
      Identifier.fromNamespaceAndPath("heartbound", "heartbound_items"),
      CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
         .icon(() -> new ItemStack(HeartboundEntities.getFirstSpawnEgg()))
         .title(Component.translatable("itemgroup.heartbound.pleasure_craft_items"))
         .displayItems((displayContext, entries) -> {
            HeartboundEntities.getAllSpawnEggs().forEach(entries::accept);
            CustomGirlLoader.PROFILE_SPAWN_EGGS.values().forEach(entries::accept);
            entries.accept(HeartboundItems.LOVE_BALL);
            entries.accept(HeartboundItems.MASTER_RING);
            entries.accept(HeartboundItems.HEART_GUIDE);
            entries.accept(HeartboundItems.X_PILL);
         })
         .build()
   );
   public static final CreativeModeTab PLEASURE_CRAFT_BLOCK_GROUP = Registry.register(
      BuiltInRegistries.CREATIVE_MODE_TAB,
      Identifier.fromNamespaceAndPath("heartbound", "heartbound_blocks"),
      CreativeModeTab.builder(CreativeModeTab.Row.TOP, 1)
         .icon(() -> new ItemStack(HeartboundBlocks.MAGICAL_PUMPKIN))
         .title(Component.translatable("itemgroup.heartbound.pleasure_craft_blocks"))
         .displayItems((displayContext, entries) -> {
            entries.accept(HeartboundBlocks.MAGICAL_PUMPKIN);
            entries.accept(HeartboundBlocks.LITTED_MAGICAL_PUMPKIN);
            entries.accept(HeartboundBlocks.FUSION_TABLE);
         })
         .build()
   );

   public static void registerItemGroups() {
   }
}
