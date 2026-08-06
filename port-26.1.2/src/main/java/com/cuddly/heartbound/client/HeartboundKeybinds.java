package com.cuddly.heartbound.client;

import com.mojang.blaze3d.platform.InputConstants.Type;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class HeartboundKeybinds {
   public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("heartbound", "key.categories.heartbound"));
   public static KeyMapping thrustKey;
   public static KeyMapping cumKey;
   public static KeyMapping transformSceneKey;

   public static void register() {
      thrustKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.Heartbound.thrust", Type.KEYSYM, 90, CATEGORY));
      cumKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.Heartbound.cum", Type.KEYSYM, 86, CATEGORY));
      transformSceneKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.Heartbound.transformScene", Type.KEYSYM, 71, CATEGORY));
   }
}
