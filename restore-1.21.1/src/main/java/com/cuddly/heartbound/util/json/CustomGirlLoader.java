package com.cuddly.heartbound.util.json;

import com.cuddly.heartbound.Heartbound;
import com.cuddly.heartbound.component.HeartboundDataComponentTypes;
import com.cuddly.heartbound.item.HeartboundItems;
import com.cuddly.heartbound.util.variables.CustomGirlProfile;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class CustomGirlLoader {
   public static Map<String, CustomGirlProfile> LOADED_PROFILES = new HashMap<>();
   public static Map<Item, CustomGirlProfile> REGISTERED_PROFILES = new HashMap<>();
   public static Map<String, ItemStack> PROFILE_SPAWN_EGGS = new HashMap<>();

   public static void register() {
      Heartbound.LOGGER.info("CustomGirlLoader.register() called");
      Path dir = FabricLoader.getInstance().getConfigDir().resolve("heartbound/girls");

      try {
         Files.createDirectories(dir);
      } catch (Exception var5) {
      }

      try (Stream<Path> files = Files.list(dir)) {
         files.filter(f -> f.toString().endsWith(".json")).forEach(CustomGirlLoader::loadFile);
      } catch (Exception var7) {
         Heartbound.LOGGER.error("[CustomGirlLoader] Failed loading girl profiles", var7);
      }

      validateAndRegisterProfiles();
   }

   private static void loadFile(Path file) {
      try {
         JsonObject json = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
         CustomGirlProfile profile = CustomGirlParser.parse(json);
         LOADED_PROFILES.put(profile.id(), profile);
         Heartbound.LOGGER.info("[CustomGirlLoader] Loaded custom girl: {}", profile.id());
      } catch (Exception var3) {
         Heartbound.LOGGER.error("[CustomGirlLoader] Error parsing girl JSON: " + file, var3);
      }
   }

   public static CustomGirlProfile checkItem(Item item) {
      if (REGISTERED_PROFILES.isEmpty()) {
         return null;
      } else {
         return REGISTERED_PROFILES.containsKey(item) ? REGISTERED_PROFILES.get(item) : null;
      }
   }

   private static void validateAndRegisterProfiles() {
      Heartbound.LOGGER.info("validateAndRegisterProfiles called. LOADED_PROFILES size: " + LOADED_PROFILES.size());

      for (CustomGirlProfile profile : LOADED_PROFILES.values()) {
         Item tameItem = profile.tameItem();
         if (!REGISTERED_PROFILES.containsKey(tameItem)) {
            REGISTERED_PROFILES.put(tameItem, profile);
            createSpawnEggForProfile(profile);
         } else {
            CustomGirlProfile first = REGISTERED_PROFILES.get(tameItem);
            Heartbound.LOGGER
               .error(
                  "[CustomGirlLoader] Duplicate tame item detected!\nItem: {}\nFirst girl: {}\nConflicting girl: {}\nSkipping registration of {}.\n",
                  new Object[]{tameItem, first.id(), profile.id(), profile.id()}
               );
         }
      }

      Heartbound.LOGGER.info("validateAndRegisterProfiles finished. REGISTERED_PROFILES size: " + REGISTERED_PROFILES.size());
   }

   private static void createSpawnEggForProfile(CustomGirlProfile profile) {
      ItemStack spawnEggStack = new ItemStack(HeartboundItems.CUSTOM_GIRL_SPAWN_EGG);
      spawnEggStack.set(HeartboundDataComponentTypes.GIRL_PROFILE_ID, profile.id());
      PROFILE_SPAWN_EGGS.put(profile.id(), spawnEggStack);
      Heartbound.LOGGER.info("[CustomGirlLoader] Created spawn egg stack for profile: {}", profile.id());
   }

   public static CustomGirlProfile getGirlOrDefault(String id) {
      for (CustomGirlProfile profile : REGISTERED_PROFILES.values()) {
         if (profile.id().equals(id)) {
            return profile;
         }
      }

      return CustomGirlProfile.DEFAULT;
   }

   public static ItemStack getSpawnEggForProfile(String profileId) {
      return PROFILE_SPAWN_EGGS.get(profileId);
   }
}
