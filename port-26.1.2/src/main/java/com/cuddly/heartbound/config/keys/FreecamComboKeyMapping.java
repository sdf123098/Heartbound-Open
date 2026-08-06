package com.cuddly.heartbound.config.keys;

import com.mojang.blaze3d.platform.InputConstants.Type;

class FreecamComboKeyMapping extends FreecamKeyMapping {
   private final Runnable pressAction;
   private final HoldAction holdAction;
   private final long holdThreshold;
   private long ticksSinceUsed;
   private long ticksSinceDown;
   private long pendingActions;

   FreecamComboKeyMapping(String translationKey, Type type, int code, Runnable pressAction, HoldAction holdAction, long holdThreshold) {
      super(translationKey, type, code);
      this.pressAction = pressAction;
      this.holdAction = holdAction;
      this.holdThreshold = holdThreshold;
   }

   @Override
   public void tick() {
      this.ticksSinceUsed++;
      this.ticksSinceDown++;
      if (this.isDown()) {
         if (this.holdAction.run()) {
            this.consumeUse();
         }
      } else {
         while (this.pendingActions > 0L) {
            this.pendingActions--;
            this.pressAction.run();
         }
      }
   }

   @Override
   public void setDown(boolean pressed) {
      boolean wasDown = this.isDown();
      super.setDown(pressed);
      if (pressed && !wasDown) {
         this.onKeyDown();
      } else if (!pressed && wasDown) {
         this.onKeyUp();
      }
   }

   private void onKeyDown() {
      this.ticksSinceDown = 0L;
   }

   private void onKeyUp() {
      if (this.ticksSinceUsed > this.ticksSinceDown && this.ticksSinceDown < this.holdThreshold) {
         this.pendingActions++;
      }

      this.consumeUse();
   }

   private void consumeUse() {
      this.ticksSinceUsed = 0L;
   }
}
