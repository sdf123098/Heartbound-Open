package com.cuddly.heartbound.entity.ai.goal;

import com.cuddly.heartbound.Heartbound;
import com.cuddly.heartbound.entity.base.GirlSceneEntity;
import com.cuddly.heartbound.util.Utils;
import com.cuddly.heartbound.util.variables.ScenePhase;
import java.util.EnumSet;
import java.util.UUID;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class BedGoal extends Goal {
   private final GirlSceneEntity entity;
   private final double speed;
   private final PathNavigation navigation;
   private Direction bedFacing;
   private Vec3 snapPos;
   private Vec3 scenePos;
   private Path pathToBed;

   public BedGoal(GirlSceneEntity entity, double speed) {
      this.entity = entity;
      this.speed = speed;
      this.navigation = entity.getNavigation();
      this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
      if (!(entity.getNavigation() instanceof GroundPathNavigation) && !(entity.getNavigation() instanceof FlyingPathNavigation)) {
         throw new IllegalArgumentException("Unsupported mob type for BedGoal");
      }
   }

   @Override
   public boolean canUse() {
      boolean use = this.entity.shouldMoveToBed() && this.entity.targetBedPos != null;
      if (use) {
         Heartbound.LOGGER.info(
            "[HB-DBG] BedGoal.canUse=true girl={} targetBed={} scenePlayer={} phase={}",
            this.entity.getGirlID(), this.entity.targetBedPos, this.entity.getScenePlayer(), this.entity.getCurrentScenePhase()
         );
      }
      return use;
   }

   @Override
   public boolean canContinueToUse() {
      boolean cont = this.entity.targetBedPos != null && Utils.checkForBlockAt(this.entity.level(), this.entity.targetBedPos, null, BlockTags.BEDS);
      if (!cont) {
         Heartbound.LOGGER.info("[HB-DBG] BedGoal.canContinue=false girl={} targetBed={}", this.entity.getGirlID(), this.entity.targetBedPos);
      }
      return cont;
   }

   @Override
   public void start() {
      Heartbound.LOGGER.info("[HB-DBG] BedGoal.start girl={} targetBed={}", this.entity.getGirlID(), this.entity.targetBedPos);
      Heartbound.usedBeds.put(this.entity.getUUID(), this.entity.targetBedPos);
      BlockState state = this.entity.level().getBlockState(this.entity.targetBedPos);
      if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
         this.bedFacing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
      } else {
         this.bedFacing = Direction.NORTH;
      }

      if (this.bedFacing == Direction.NORTH) {
         this.snapPos = new Vec3(
            (double)this.entity.targetBedPos.getX() + 0.5, (double)this.entity.targetBedPos.getY(), (double)this.entity.targetBedPos.getZ() + 1.5
         );
         this.scenePos = new Vec3(this.snapPos.x(), this.snapPos.y(), this.snapPos.z() - (double)this.entity.getBedOffset());
      } else if (this.bedFacing == Direction.EAST) {
         this.snapPos = new Vec3(
            (double)this.entity.targetBedPos.getX() - 0.5, (double)this.entity.targetBedPos.getY(), (double)this.entity.targetBedPos.getZ() + 0.5
         );
         this.scenePos = new Vec3(this.snapPos.x() + (double)this.entity.getBedOffset(), this.snapPos.y(), this.snapPos.z());
      } else if (this.bedFacing == Direction.SOUTH) {
         this.snapPos = new Vec3(
            (double)this.entity.targetBedPos.getX() + 0.5, (double)this.entity.targetBedPos.getY(), (double)this.entity.targetBedPos.getZ() - 0.5
         );
         this.scenePos = new Vec3(this.snapPos.x(), this.snapPos.y(), this.snapPos.z() + (double)this.entity.getBedOffset());
      } else if (this.bedFacing == Direction.WEST) {
         this.snapPos = new Vec3(
            (double)this.entity.targetBedPos.getX() + 1.5, (double)this.entity.targetBedPos.getY(), (double)this.entity.targetBedPos.getZ() + 0.5
         );
         this.scenePos = new Vec3(this.snapPos.x() - (double)this.entity.getBedOffset(), this.snapPos.y(), this.snapPos.z());
      }

      this.pathToBed = this.navigation.createPath(this.entity.targetBedPos, 1);
   }

   @Override
   public void tick() {
      this.handleMovement();
      this.startOnContact();
   }

   private void handleMovement() {
      if (this.entity.targetBedPos != null && this.entity.distanceToSqr(this.entity.targetBedPos.getCenter()) <= 3.0) {
         if (this.entity.getScenePlayer() != null) {
            this.navigation.stop();
            float targetYaw = this.entity.getYRot();
            if (this.bedFacing != null) {
               targetYaw = this.bedFacing.toYRot();
            }

            this.entity.setWaitingAtBedState(true);
            Heartbound.LOGGER.info(
               "[HB-DBG] BedGoal arrived girl={} waitingAtBed=true phase={} snapPos={}",
               this.entity.getGirlID(), this.entity.getCurrentScenePhase(), this.snapPos
            );
            if (!this.entity.level().isClientSide()) {
               this.entity.teleportTo(this.snapPos.x, this.snapPos.y, this.snapPos.z);
               this.entity.setYRot(targetYaw);
            }

            this.entity.setYHeadRot(targetYaw);
            this.entity.setYBodyRot(targetYaw);
            if (!this.entity.isSceneActive()) {
               this.entity.playPhase(ScenePhase.LAYING_DOWN);
            }
         }
      } else if (!this.entity.isWaitingAtBed()) {
         this.navigation.moveTo(this.pathToBed, this.speed);
      }
   }

   private void startOnContact() {
      if (this.entity.isWaitingAtBed()) {
         if (this.entity.getScenePlayer() != null) {
            UUID playerId = this.entity.getScenePlayer().getUUID();
            Heartbound.LOGGER.info(
               "[HB-DBG] BedGoal.startOnContact girl={} dist={} phase={} activeScenes={}",
               this.entity.getGirlID(),
               this.entity.distanceToSqr(this.entity.getScenePlayer()),
               this.entity.getCurrentScenePhase(),
               Heartbound.activeScenes
            );
            if (!Heartbound.activeScenes.containsKey(playerId)) {
               if (this.entity.distanceToSqr(this.entity.getScenePlayer()) <= 1.5 && this.entity.getCurrentScenePhase().equals(ScenePhase.BED_IDLE)) {
                  Heartbound.activeScenes.put(playerId, this.entity.getUUID());
                  this.entity.setPos(this.scenePos);
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
