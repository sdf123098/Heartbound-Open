package com.cuddly.heartbound.util.json;

import com.cuddly.heartbound.util.variables.CustomGirlProfile;
import com.cuddly.heartbound.util.variables.Scene;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

public class CustomGirlParser {
   public static CustomGirlProfile parse(JsonObject json) throws Exception {
      String id = json.get("id").getAsString();
      String name = json.get("name").getAsString();
      float hitboxHeight = json.has("hitbox_height") ? json.get("hitbox_height").getAsFloat() : 1.65F;
      int guiSize = json.has("gui_size") ? json.get("gui_size").getAsInt() : 30;
      float guiYOffset = json.has("gui_y_offset") ? json.get("gui_y_offset").getAsFloat() : 0.0625F;
      float weaponBoneRotation = json.has("weapon_bone_rotation") ? json.get("weapon_bone_rotation").getAsFloat() : 150.0F;
      String tameItemId = json.has("tame_item") ? json.get("tame_item").getAsString() : null;
      if (tameItemId == null) {
         throw new Exception("Girl does not have a tame_item property");
      } else {
         Item tameItem = BuiltInRegistries.ITEM.getValue(Identifier.parse(tameItemId));
         JsonObject attr = json.getAsJsonObject("attributes");
         double health = attr.has("health") ? attr.get("health").getAsDouble() : 20.0;
         double speed = attr.has("speed") ? attr.get("speed").getAsDouble() : 0.2;
         double damage = attr.has("damage") ? attr.get("damage").getAsDouble() : 2.0;
         List<Scene> scenes = new ArrayList<>();
         if (json.has("scenes")) {
            for (JsonElement e : json.getAsJsonArray("scenes")) {
               scenes.add(parseScene(e.getAsJsonObject()));
            }
         }

         return new CustomGirlProfile(id, name, hitboxHeight, guiSize, guiYOffset, weaponBoneRotation, tameItem, health, speed, damage, scenes);
      }
   }

   private static Scene parseScene(JsonObject s) {
      String name = s.get("name").getAsString();
      int level = s.get("required_level").getAsInt();
      boolean needsStrip = s.has("needs_to_strip") && s.get("needs_to_strip").getAsBoolean();
      String type = s.has("scene_type") ? s.get("scene_type").getAsString() : "on_player";
      switch (type) {
         case "on_bed":
            return Scene.onBed(
               name,
               level,
               jsonArrayToList(s.getAsJsonArray("intro_anim")),
               jsonArrayToList(s.getAsJsonArray("slow_anim")),
               jsonArrayToList(s.getAsJsonArray("fast_anim")),
               s.get("cum_anim").getAsString(),
               s.get("cum_threshold").getAsFloat(),
               needsStrip,
               s.has("use_keyframe") && s.get("use_keyframe").getAsBoolean(),
               s.has("bed_offset") ? s.get("bed_offset").getAsFloat() : 0.0F,
               s.has("lay_on_bed_anim") ? s.get("lay_on_bed_anim").getAsString() : "",
               s.has("bed_idle_anim") ? s.get("bed_idle_anim").getAsString() : ""
            );
         case "on_player":
            return Scene.onPlayer(
               name,
               level,
               jsonArrayToList(s.getAsJsonArray("intro_anim")),
               jsonArrayToList(s.getAsJsonArray("slow_anim")),
               jsonArrayToList(s.getAsJsonArray("fast_anim")),
               s.get("cum_anim").getAsString(),
               s.get("cum_threshold").getAsFloat(),
               needsStrip,
               s.has("use_keyframe") && s.get("use_keyframe").getAsBoolean()
            );
         case "stationary_contact":
            return Scene.stationaryContact(
               name,
               level,
               jsonArrayToList(s.getAsJsonArray("intro_anim")),
               jsonArrayToList(s.getAsJsonArray("slow_anim")),
               jsonArrayToList(s.getAsJsonArray("fast_anim")),
               s.get("cum_anim").getAsString(),
               s.get("cum_threshold").getAsFloat(),
               needsStrip,
               s.has("use_keyframe") && s.get("use_keyframe").getAsBoolean(),
               s.has("lay_down_anim") ? s.get("lay_down_anim").getAsString() : "",
               s.has("idle_anim") ? s.get("idle_anim").getAsString() : ""
            );
         case "stationary_intro":
            return Scene.stationaryIntro(
               name,
               level,
               jsonArrayToList(s.getAsJsonArray("intro_anim")),
               s.get("anim").getAsString(),
               s.get("amount_of_loops").getAsInt(),
               needsStrip,
               s.has("hide_player") && s.get("hide_player").getAsBoolean()
            );
         case "stationary":
            return Scene.stationary(
               name,
               level,
               s.get("anim").getAsString(),
               s.get("amount_of_loops").getAsInt(),
               needsStrip,
               s.has("hide_player") && s.get("hide_player").getAsBoolean()
            );
         default:
            return Scene.onPlayer(
               name,
               level,
               jsonArrayToList(s.getAsJsonArray("intro_anim")),
               jsonArrayToList(s.getAsJsonArray("slow_anim")),
               jsonArrayToList(s.getAsJsonArray("fast_anim")),
               s.get("cum_anim").getAsString(),
               s.get("cum_threshold").getAsFloat(),
               needsStrip,
               s.has("use_keyframe") && s.get("use_keyframe").getAsBoolean()
            );
      }
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
