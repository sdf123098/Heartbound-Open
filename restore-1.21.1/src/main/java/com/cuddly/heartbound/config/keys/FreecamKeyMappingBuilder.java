package com.cuddly.heartbound.config.keys;

import net.minecraft.client.util.InputUtil.Type;

public class FreecamKeyMappingBuilder {
   private final String key;
   private Type inputType = Type.KEYSYM;
   private int code = -1;
   private Runnable onPress;
   private HoldAction onHold;
   private long holdLimit = 10L;

   private FreecamKeyMappingBuilder(String key) {
      this.key = key;
   }

   public static FreecamKeyMappingBuilder builder(String translationKey) {
      return new FreecamKeyMappingBuilder(translationKey);
   }

   public FreecamKeyMappingBuilder type(Type type) {
      this.inputType = type;
      return this;
   }

   public FreecamKeyMappingBuilder maxHoldTicks(long ticks) {
      this.holdLimit = ticks;
      return this;
   }

   public FreecamKeyMappingBuilder defaultKey(int keyCode) {
      this.code = keyCode;
      return this;
   }

   public FreecamKeyMappingBuilder action(Runnable action) {
      this.onPress = action;
      return this;
   }

   public FreecamKeyMappingBuilder holdAction(HoldAction action) {
      this.onHold = action;
      return this;
   }

   public FreecamKeyMapping build() {
      if (this.onPress != null && this.onHold != null) {
         return new FreecamComboKeyMapping(this.key, this.inputType, this.code, this.onPress, this.onHold, this.holdLimit);
      } else if (this.onPress != null) {
         Runnable tap = this.onPress;
         return new FreecamKeyMapping(this.key, this.inputType, this.code, self -> {
            while (self.wasPressed()) {
               tap.run();
            }
         });
      } else if (this.onHold != null) {
         HoldAction hold = this.onHold;
         return new FreecamKeyMapping(this.key, this.inputType, this.code, self -> {
            if (self.isPressed()) {
               hold.run();
            }
         });
      } else {
         throw new IllegalStateException("No action defined.");
      }
   }
}
