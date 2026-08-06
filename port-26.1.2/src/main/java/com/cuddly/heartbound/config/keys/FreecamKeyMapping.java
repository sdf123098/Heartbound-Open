package com.cuddly.heartbound.config.keys;

import com.cuddly.heartbound.client.HeartboundKeybinds;
import com.mojang.blaze3d.platform.InputConstants.Type;
import java.util.function.Consumer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.texture.TickableTexture;

public class FreecamKeyMapping extends KeyMapping implements TickableTexture {
   private static final String PREFIX = "key.freecam.";
   private final Consumer<FreecamKeyMapping> tickHandler;

   protected FreecamKeyMapping(String translationKey, Type type, int code) {
      this(translationKey, type, code, null);
   }

   FreecamKeyMapping(String translationKey, Type type, int code, Consumer<FreecamKeyMapping> onTick) {
      super("key.freecam." + translationKey, type, code, HeartboundKeybinds.CATEGORY);
      this.tickHandler = onTick;
   }

   @Override
   public void tick() {
      this.tickHandler.accept(this);
   }

   public void release() {
      while (this.consumeClick()) {
      }
   }
}
