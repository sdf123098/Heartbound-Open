package com.cuddly.heartbound.entity.ai.goal;

import com.cuddly.heartbound.Heartbound;
import com.cuddly.heartbound.entity.base.GirlSceneEntity;
import com.cuddly.heartbound.util.Utils;
import com.cuddly.heartbound.util.variables.ScenePhase;
import java.util.EnumSet;
import java.util.UUID;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.Goal.Control;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class BedGoal extends Goal {
   private final GirlSceneEntity entity;
   private final double speed;
   private final EntityNavigation navigation;
   private Direction bedFacing;
   private Vec3d snapPos;
   private Vec3d scenePos;
   private Path pathToBed;

   public BedGoal(GirlSceneEntity entity, double speed) {
      this.entity = entity;
      this.speed = speed;
      this.navigation = entity.getNavigation();
      this.setControls(EnumSet.of(Control.MOVE, Control.LOOK, Control.JUMP));
      if (!(entity.getNavigation() instanceof MobNavigation) && !(entity.getNavigation() instanceof BirdNavigation)) {
         throw new IllegalArgumentException("Unsupported mob type for BedGoal");
      }
   }

   @Override
   public boolean canStart() {
      return this.entity.shouldMoveToBed() && this.entity.targetBedPos != null;
   }

   @Override
   public boolean shouldContinue() {
      return this.entity.targetBedPos != null && Utils.checkForBlockAt(this.entity.getWorld(), this.entity.targetBedPos, null, BlockTags.BEDS);
   }

   @Override
   public void start() {
      Heartbound.usedBeds.put(this.entity.getUuid(), this.entity.targetBedPos);
      BlockState state = this.entity.getWorld().getBlockState(this.entity.targetBedPos);
      if (state.contains(Properties.HORIZONTAL_FACING)) {
         this.bedFacing = state.get(Properties.HORIZONTAL_FACING);
      } else {
         this.bedFacing = Direction.NORTH;
      }

      if (this.bedFacing == Direction.NORTH) {
         this.snapPos = new Vec3d(
            (double)this.entity.targetBedPos.getX() + 0.5, (double)this.entity.targetBedPos.getY(), (double)this.entity.targetBedPos.getZ() + 1.5
         );
         this.scenePos = new Vec3d(this.snapPos.getX(), this.snapPos.getY(), this.snapPos.getZ() - (double)this.entity.getBedOffset());
      } else if (this.bedFacing == Direction.EAST) {
         this.snapPos = new Vec3d(
            (double)this.entity.targetBedPos.getX() - 0.5, (double)this.entity.targetBedPos.getY(), (double)this.entity.targetBedPos.getZ() + 0.5
         );
         this.scenePos = new Vec3d(this.snapPos.getX() + (double)this.entity.getBedOffset(), this.snapPos.getY(), this.snapPos.getZ());
      } else if (this.bedFacing == Direction.SOUTH) {
         this.snapPos = new Vec3d(
            (double)this.entity.targetBedPos.getX() + 0.5, (double)this.entity.targetBedPos.getY(), (double)this.entity.targetBedPos.getZ() - 0.5
         );
         this.scenePos = new Vec3d(this.snapPos.getX(), this.snapPos.getY(), this.snapPos.getZ() + (double)this.entity.getBedOffset());
      } else if (this.bedFacing == Direction.WEST) {
         this.snapPos = new Vec3d(
            (double)this.entity.targetBedPos.getX() + 1.5, (double)this.entity.targetBedPos.getY(), (double)this.entity.targetBedPos.getZ() + 0.5
         );
         this.scenePos = new Vec3d(this.snapPos.getX() - (double)this.entity.getBedOffset(), this.snapPos.getY(), this.snapPos.getZ());
      }

      this.pathToBed = this.navigation.findPathTo(this.entity.targetBedPos, 1);
   }

   @Override
   public void tick() {
      this.handleMovement();
      this.startOnContact();
   }

   private void handleMovement() {
      if (this.entity.targetBedPos != null && this.entity.squaredDistanceTo(this.entity.targetBedPos.toCenterPos()) <= 3.0) {
         if (this.entity.getScenePlayer() != null) {
            this.navigation.stop();
            float targetYaw = this.entity.getYaw();
            if (this.bedFacing != null) {
               targetYaw = this.bedFacing.asRotation();
            }

            this.entity.setWaitingAtBedState(true);
            if (!this.entity.getWorld().isClient()) {
               this.entity.refreshPositionAndAngles(this.snapPos, targetYaw, this.entity.getPitch());
            }

            this.entity.setHeadYaw(targetYaw);
            this.entity.setBodyYaw(targetYaw);
            if (!this.entity.isSceneActive()) {
               this.entity.playPhase(ScenePhase.LAYING_DOWN);
            }
         }
      } else if (!this.entity.isWaitingAtBed()) {
         this.navigation.startMovingAlong(this.pathToBed, this.speed);
      }
   }

   private void startOnContact() {
      if (this.entity.isWaitingAtBed()) {
         if (this.entity.getScenePlayer() != null) {
            UUID playerId = this.entity.getScenePlayer().getUuid();
            if (!Heartbound.activeScenes.containsKey(playerId)) {
               if (this.entity.squaredDistanceTo(this.entity.getScenePlayer()) <= 1.5 && this.entity.getCurrentScenePhase().equals(ScenePhase.BED_IDLE)) {
                  Heartbound.activeScenes.put(playerId, this.entity.getUuid());
                  this.entity.setPosition(this.scenePos);
                  this.entity.startRidingScene(this.entity.getScenePlayer());
               }
            }
         }
      }
   }

   @Override
   public void stop() {
      this.navigation.stop();
      this.entity.setWaitingAtBedState(false);
   }
}
