package com.cuddly.heartbound.client;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil.Type;

public class HeartboundKeybinds {
   public static KeyBinding thrustKey;
   public static KeyBinding cumKey;
   public static KeyBinding transformSceneKey;

   public static void register() {
      thrustKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.Heartbound.thrust", Type.KEYSYM, 90, "key.categories.Heartbound"));
      cumKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.Heartbound.cum", Type.KEYSYM, 86, "key.categories.Heartbound"));
      transformSceneKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.Heartbound.transformScene", Type.KEYSYM, 71, "key.categories.Heartbound"));
   }
}
