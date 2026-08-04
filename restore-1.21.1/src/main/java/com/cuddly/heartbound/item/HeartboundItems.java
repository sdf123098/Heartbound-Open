package com.cuddly.heartbound.item;

import com.cuddly.heartbound.Heartbound;
import com.cuddly.heartbound.component.HeartboundDataComponentTypes;
import com.cuddly.heartbound.item.items.HeartGuide;
import com.cuddly.heartbound.item.items.LoveBall;
import com.cuddly.heartbound.item.items.MasterRing;
import com.cuddly.heartbound.item.items.XPill;
import java.util.function.Function;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.Settings;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class HeartboundItems {
   public static Item LOVE_BALL = registerItem("love_ball", settings -> new LoveBall(settings.maxCount(1)));
   public static Item MASTER_RING = registerItem("master_ring", settings -> new MasterRing(settings.maxCount(1)));
   public static Item HEART_GUIDE = registerItem("heart_guide", settings -> new HeartGuide(settings.maxCount(1)));
   public static Item X_PILL = registerItem("x_pill", settings -> new XPill(settings.maxCount(16)));
   public static Item CUSTOM_GIRL_SPAWN_EGG = registerItem("custom_girl_spawn_egg", settings -> new CustomGirlSpawnEggItem(settings.maxCount(64)));

   private static Item registerItem(String name, Function<Settings, Item> function) {
      return Registry.register(Registries.ITEM, Identifier.of("heartbound", name), function.apply(new Settings()));
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
