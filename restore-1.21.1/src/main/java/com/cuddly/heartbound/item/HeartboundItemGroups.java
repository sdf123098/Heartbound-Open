package com.cuddly.heartbound.item;

import com.cuddly.heartbound.Heartbound;
import com.cuddly.heartbound.block.HeartboundBlocks;
import com.cuddly.heartbound.entity.HeartboundEntities;
import com.cuddly.heartbound.util.json.CustomGirlLoader;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class HeartboundItemGroups {
   public static final ItemGroup PLEASURE_CRAFT_ITEM_GROUP = Registry.register(
      Registries.ITEM_GROUP,
      Identifier.of("heartbound", "heartbound_items"),
      FabricItemGroup.builder()
         .icon(() -> new ItemStack(HeartboundEntities.getFirstSpawnEgg()))
         .displayName(Text.translatable("itemgroup.heartbound.pleasure_craft_items"))
         .entries((displayContext, entries) -> {
            HeartboundEntities.getAllSpawnEggs().forEach(entries::add);
            CustomGirlLoader.PROFILE_SPAWN_EGGS.values().forEach(entries::add);
            entries.add(HeartboundItems.LOVE_BALL);
            entries.add(HeartboundItems.MASTER_RING);
            entries.add(HeartboundItems.HEART_GUIDE);
            entries.add(HeartboundItems.X_PILL);
         })
         .build()
   );
   public static final ItemGroup PLEASURE_CRAFT_BLOCK_GROUP = Registry.register(
      Registries.ITEM_GROUP,
      Identifier.of("heartbound", "heartbound_blocks"),
      FabricItemGroup.builder()
         .icon(() -> new ItemStack(HeartboundBlocks.MAGICAL_PUMPKIN))
         .displayName(Text.translatable("itemgroup.heartbound.pleasure_craft_blocks"))
         .entries((displayContext, entries) -> {
            entries.add(HeartboundBlocks.MAGICAL_PUMPKIN);
            entries.add(HeartboundBlocks.LITTED_MAGICAL_PUMPKIN);
            entries.add(HeartboundBlocks.FUSION_TABLE);
         })
         .build()
   );

   public static void registerItemGroups() {
      Heartbound.LOGGER.info("Registering Item Groups for Heartbound");
      ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(entries -> {
         HeartboundEntities.getAllSpawnEggs().forEach(entries::add);
         CustomGirlLoader.PROFILE_SPAWN_EGGS.values().forEach(entries::add);
      });
   }
}
