package com.cuddly.heartbound.entity.ai.goal;

import com.cuddly.heartbound.Heartbound;
import com.cuddly.heartbound.entity.base.GirlSceneEntity;
import com.cuddly.heartbound.util.variables.ScenePhase;
import java.util.UUID;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.EntityNavigation;

public class StationaryContactGoal extends Goal {
   private final GirlSceneEntity entity;
   private final EntityNavigation navigation;
   private boolean stop = false;

   public StationaryContactGoal(GirlSceneEntity entity) {
      this.entity = entity;
      this.navigation = entity.getNavigation();
   }

   @Override
   public boolean canStart() {
      return this.entity.shouldWaitForPlayer();
   }

   @Override
   public void start() {
      this.stop = false;
   }

   @Override
   public void tick() {
      this.handleMovement();
      this.startOnContact();
   }

   private void handleMovement() {
      if (this.entity.getScenePlayer() != null) {
         this.navigation.stop();
         float targetYaw = this.entity.getYaw();
         this.entity.setWaitingAtBedState(true);
         this.entity.setYaw(targetYaw);
         this.entity.setHeadYaw(targetYaw);
         this.entity.setBodyYaw(targetYaw);
         this.entity.getLookControl().lookAt(this.entity.getX(), this.entity.getEyeY(), this.entity.getZ());
         this.entity.setWaitingForPlayerState(true);
         if (!this.entity.isSceneActive()) {
            this.entity.playPhase(ScenePhase.LAYING_DOWN);
         }
      }
   }

   private void startOnContact() {
      if (this.entity.isWaitingForPlayer()) {
         if (this.entity.getScenePlayer() != null) {
            UUID playerId = this.entity.getScenePlayer().getUuid();
            if (!Heartbound.activeScenes.containsKey(playerId)) {
               if (this.entity.squaredDistanceTo(this.entity.getScenePlayer()) <= 1.5 && this.entity.getCurrentScenePhase().equals(ScenePhase.BED_IDLE)) {
                  Heartbound.activeScenes.put(playerId, this.entity.getUuid());
                  this.entity.startRidingScene(this.entity.getScenePlayer());
                  this.stop = true;
               }
            }
         }
      }
   }

   @Override
   public void stop() {
      this.navigation.stop();
      this.entity.setWaitingForPlayerState(false);
   }

   @Override
   public boolean shouldContinue() {
      return !this.stop;
   }
}
