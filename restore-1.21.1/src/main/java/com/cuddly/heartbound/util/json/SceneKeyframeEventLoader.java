package com.cuddly.heartbound.util.json;

import com.cuddly.heartbound.Heartbound;
import com.cuddly.heartbound.registries.SceneKeyframeEventRegistry;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceManager;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class SceneKeyframeEventLoader {
   public static void loadFromAssets(ResourceManager resourceManager) {
      resourceManager.findResources("keyframe_events", path -> path.getPath().endsWith(".json")).forEach((id, resource) -> {
         try (InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            String girlID = json.get("girl_id").getAsString();

            for (JsonElement elem : json.getAsJsonArray("events")) {
               JsonObject scene = elem.getAsJsonObject();
               String key = scene.get("key").getAsString().toLowerCase();
               if (scene.has("sounds")) {
                  for (String soundId : jsonArrayToList(scene.getAsJsonArray("sounds"))) {
                     Identifier soundIdentifier = Identifier.of(soundId);
                     SoundEvent sound = Registries.SOUND_EVENT.get(soundIdentifier);
                     if (sound == null) {
                        sound = SoundEvent.of(soundIdentifier);
                     }

                     SceneKeyframeEventRegistry.registerSound(girlID, key, sound);
                  }
               }

               if (scene.has("random_sounds")) {
                  List<String> randomIds = jsonArrayToList(scene.getAsJsonArray("random_sounds"));
                  List<SoundEvent> soundEvents = new ArrayList<>();

                  for (String idStr : randomIds) {
                     Identifier randomSoundId = Identifier.of(idStr);
                     SoundEvent sound = Registries.SOUND_EVENT.get(randomSoundId);
                     if (sound == null) {
                        sound = SoundEvent.of(randomSoundId);
                     }

                     soundEvents.add(sound);
                  }

                  if (!soundEvents.isEmpty()) {
                     SceneKeyframeEventRegistry.registerSound(girlID, key, soundEvents);
                  }
               }

               if (scene.has("messages")) {
                  for (String message : jsonArrayToList(scene.getAsJsonArray("messages"))) {
                     SceneKeyframeEventRegistry.registerMessage(girlID, key, message);
                  }
               }

               if (scene.has("player_messages")) {
                  for (String message : jsonArrayToList(scene.getAsJsonArray("player_messages"))) {
                     SceneKeyframeEventRegistry.registerPlayerMessage(key, message);
                  }
               }
            }

            Heartbound.LOGGER.info("[SceneKeyframeEventLoader] Loaded scene keyframe events for {}", girlID);
         } catch (Exception var18) {
            Heartbound.LOGGER.error("[SceneKeyframeEventLoader] Failed to load scene keyframe events " + id, var18);
         }
      });
   }

   private static List<String> jsonArrayToList(JsonArray array) {
      List<String> list = new ArrayList<>();
      if (array != null) {
         for (JsonElement e : array) {
            list.add(e.getAsString());
         }
      }

      return list;
   }
}
