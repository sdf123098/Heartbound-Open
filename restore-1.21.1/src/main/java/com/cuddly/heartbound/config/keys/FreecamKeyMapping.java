package com.cuddly.heartbound.config.keys;

import java.util.function.Consumer;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.texture.TextureTickListener;
import net.minecraft.client.util.InputUtil.Type;

public class FreecamKeyMapping extends KeyBinding implements TextureTickListener {
   private static final String CATEGORY = "key.categories.Heartbound";
   private static final String PREFIX = "key.freecam.";
   private final Consumer<FreecamKeyMapping> tickHandler;

   protected FreecamKeyMapping(String translationKey, Type type, int code) {
      this(translationKey, type, code, null);
   }

   FreecamKeyMapping(String translationKey, Type type, int code, Consumer<FreecamKeyMapping> onTick) {
      super("key.freecam." + translationKey, type, code, "key.categories.Heartbound");
      this.tickHandler = onTick;
   }

   @Override
   public void tick() {
      this.tickHandler.accept(this);
   }

   public void reset() {
      while (this.wasPressed()) {
      }
   }
}
