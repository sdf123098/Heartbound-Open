package com.cuddly.heartbound.item;

import com.cuddly.heartbound.Heartbound;
import com.cuddly.heartbound.component.HeartboundDataComponentTypes;
import com.cuddly.heartbound.item.items.HeartGuide;
import com.cuddly.heartbound.item.items.LoveBall;
import com.cuddly.heartbound.item.items.MasterRing;
import com.cuddly.heartbound.item.items.XPill;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;

public class HeartboundItems {
   public static Item LOVE_BALL = registerItem("love_ball", settings -> new LoveBall(settings.stacksTo(1)));
   public static Item MASTER_RING = registerItem("master_ring", settings -> new MasterRing(settings.stacksTo(1)));
   public static Item HEART_GUIDE = registerItem("heart_guide", settings -> new HeartGuide(settings.stacksTo(1)));
   public static Item X_PILL = registerItem("x_pill", settings -> new XPill(settings.stacksTo(16)));
   public static Item CUSTOM_GIRL_SPAWN_EGG = registerItem("custom_girl_spawn_egg", settings -> new CustomGirlSpawnEggItem(settings.stacksTo(64)));

   private static Item registerItem(String name, Function<Properties, Item> function) {
      Identifier id = Identifier.fromNamespaceAndPath("heartbound", name);
      ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
      return Registry.register(BuiltInRegistries.ITEM, key, function.apply(new Properties().setId(key)));
   }

   public static void registerItems() {
      Heartbound.LOGGER.info("Registering Items for Heartbound");
   }

   public static ItemStack createCustomGirlSpawnEgg(String profileId) {
      ItemStack stack = new ItemStack(CUSTOM_GIRL_SPAWN_EGG);
      stack.set(HeartboundDataComponentTypes.GIRL_PROFILE_ID, profileId);
      return stack;
   }
}
