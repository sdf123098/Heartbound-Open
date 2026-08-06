package com.cuddly.heartbound.registries;

import com.cuddly.heartbound.Heartbound;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import net.minecraft.sounds.SoundEvent;
import java.util.Map.Entry;

public class SceneKeyframeEventRegistry {
   private static final Map<SceneKeyframeEventRegistry.SceneKey, List<SoundEvent>> SOUND_EVENTS = new HashMap<>();
   private static final Map<SceneKeyframeEventRegistry.SceneKey, List<SoundEvent>> RANDOM_SOUNDS = new HashMap<>();
   private static final Map<SceneKeyframeEventRegistry.SceneKey, List<String>> CHAT_MESSAGES = new HashMap<>();
   private static final Map<String, List<String>> PLAYER_MESSAGES = new HashMap<>();
   private static final Random RANDOM = new Random();

   public static void registerSoundEvents() {
      Heartbound.LOGGER.info("Registering Scene Keyframe Events for Heartbound");
   }

   public static void clearAll() {
      SOUND_EVENTS.clear();
      RANDOM_SOUNDS.clear();
      CHAT_MESSAGES.clear();
      PLAYER_MESSAGES.clear();
   }

   public static void registerSound(String girlID, String frameKey, SoundEvent event) {
      frameKey = frameKey.toLowerCase();
      SceneKeyframeEventRegistry.SceneKey key = new SceneKeyframeEventRegistry.SceneKey(girlID, frameKey);
      SOUND_EVENTS.computeIfAbsent(key, k -> new ArrayList<>()).add(event);
   }

   public static void registerSound(String girlID, String frameKey, List<SoundEvent> events) {
      frameKey = frameKey.toLowerCase();
      SceneKeyframeEventRegistry.SceneKey key = new SceneKeyframeEventRegistry.SceneKey(girlID, frameKey);
      RANDOM_SOUNDS.computeIfAbsent(key, k -> new ArrayList<>()).addAll(events);
   }

   public static List<SoundEvent> getSound(String girlID, String keyframe) {
      keyframe = keyframe.toLowerCase(Locale.ROOT);
      String[] tokens = keyframe.strip().split(",");
      List<SoundEvent> result = new ArrayList<>();

      for (Entry<SceneKeyframeEventRegistry.SceneKey, List<SoundEvent>> entry : SOUND_EVENTS.entrySet()) {
         SceneKeyframeEventRegistry.SceneKey sk = entry.getKey();
         if (sk.girlID().equals(girlID)) {
            for (String token : tokens) {
               token = token.strip();
               if (token.equals(sk.key())) {
                  result.addAll(entry.getValue());
               }
            }
         }
      }

      for (Entry<SceneKeyframeEventRegistry.SceneKey, List<SoundEvent>> entryx : RANDOM_SOUNDS.entrySet()) {
         SceneKeyframeEventRegistry.SceneKey sk = entryx.getKey();
         if (sk.girlID().equals(girlID)) {
            for (String tokenx : tokens) {
               tokenx = tokenx.strip();
               if (tokenx.equals(sk.key())) {
                  List<SoundEvent> pool = entryx.getValue();
                  if (!pool.isEmpty()) {
                     result.add(pool.get(RANDOM.nextInt(pool.size())));
                  }
               }
            }
         }
      }

      return result;
   }

   public static void registerMessage(String girlID, String frameKey, String message) {
      frameKey = frameKey.toLowerCase();
      SceneKeyframeEventRegistry.SceneKey key = new SceneKeyframeEventRegistry.SceneKey(girlID, frameKey);
      CHAT_MESSAGES.computeIfAbsent(key, k -> new ArrayList<>()).add(message);
   }

   public static void registerPlayerMessage(String frameKey, String message) {
      frameKey = frameKey.toLowerCase();
      PLAYER_MESSAGES.computeIfAbsent(frameKey, k -> new ArrayList<>()).add(message);
   }

   public static List<String> getPlayerMessage(String key) {
      key = key.toLowerCase();
      return PLAYER_MESSAGES.getOrDefault(key, Collections.emptyList());
   }

   public static List<String> getMessage(String girlID, String keyframe) {
      keyframe = keyframe.toLowerCase(Locale.ROOT);
      String[] tokens = keyframe.strip().split(",");
      List<String> result = new ArrayList<>();

      for (Entry<SceneKeyframeEventRegistry.SceneKey, List<String>> entry : CHAT_MESSAGES.entrySet()) {
         SceneKeyframeEventRegistry.SceneKey sk = entry.getKey();
         if (sk.girlID().equals(girlID)) {
            for (String token : tokens) {
               token = token.strip();
               if (token.equals(sk.key())) {
                  result.addAll(entry.getValue());
               }
            }
         }
      }

      return result;
   }

   public static record SceneKey(String girlID, String key) {
      public SceneKey(String girlID, String key) {
         key = key.toLowerCase(Locale.ROOT);
         this.girlID = girlID;
         this.key = key;
      }
   }
}
