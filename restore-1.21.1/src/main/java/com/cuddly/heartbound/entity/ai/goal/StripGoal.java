package com.cuddly.heartbound.entity.ai.goal;

import com.cuddly.heartbound.entity.base.GirlSceneEntity;
import com.cuddly.heartbound.util.Utils;
import com.cuddly.heartbound.util.variables.Scene;
import java.util.EnumSet;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.Goal.Control;

public class StripGoal extends Goal {
   private final GirlSceneEntity girl;
   private boolean stripTrigged = false;
   private boolean started = false;
   private Scene scene = Scene.EMPTY;

   public StripGoal(GirlSceneEntity girl) {
      this.girl = girl;
      this.setControls(EnumSet.of(Control.MOVE, Control.LOOK, Control.JUMP));
   }

   @Override
   public boolean canStart() {
      return this.girl.getOverrideAnim().isEmpty() && this.girl.shouldStrip();
   }

   @Override
   public void start() {
      this.girl.setFreeze(true);
      if (this.girl.hasStripAnim()) {
         this.girl.playAnimation("strip", false, false);
      }

      if (!this.girl.stripOptions.equals(Scene.EMPTY)) {
         this.scene = this.girl.stripOptions;
         this.girl.stripOptions = Scene.EMPTY;
      }

      this.stripTrigged = false;
      this.started = true;
   }

   @Override
   public void tick() {
      if (this.girl.hasStripAnim()) {
         if (this.started) {
            if (!this.girl.isFrozenInPlace()) {
               this.girl.setFreeze(true);
            }

            if (Utils.isStringInQueue(this.girl.getAnimationKeyFrameEvent(), "becomeNude".toLowerCase()) && !this.stripTrigged) {
               this.girl.setStripped(!this.girl.isStripped());
               this.stripTrigged = true;
            }

            if (!this.stripTrigged && this.girl.getOverrideAnim().isEmpty()) {
               this.girl.setStripped(!this.girl.isStripped());
               this.stripTrigged = true;
            }
         }
      }
   }

   @Override
   public boolean shouldContinue() {
      return !this.girl.hasStripAnim() ? false : !this.stripTrigged || !this.girl.getOverrideAnim().isEmpty();
   }

   @Override
   public void stop() {
      if (this.girl.hasStripAnim()) {
         this.girl.setFreeze(false);
      } else {
         this.girl.setStripped(!this.girl.isStripped());
      }

      this.started = false;
      if (!this.scene.equals(Scene.EMPTY)) {
         this.girl.startScene(this.girl.getScenePlayer(), this.scene);
         this.scene = Scene.EMPTY;
      }
   }
}
