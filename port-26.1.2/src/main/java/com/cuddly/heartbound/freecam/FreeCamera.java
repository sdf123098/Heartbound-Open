package com.cuddly.heartbound.freecam;

import com.cuddly.heartbound.config.ModConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.chat.ChatAbilities;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.ApiStatus.AvailableSince;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
@AvailableSince("0.4.0")
public class FreeCamera extends LocalPlayer {
   public FreeCamera(int id) {
      super(Freecam.MC, Freecam.MC.level, Freecam.MC.getConnection(), Freecam.MC.player.getStats(), Freecam.MC.player.getRecipeBook(), Input.EMPTY, false, ChatAbilities.NO_RESTRICTIONS);
      this.setId(id);
      this.setPose(Pose.SWIMMING);
      this.getAbilities().flying = true;
      this.input = new KeyboardInput(Freecam.MC.options);
   }

   @Override
   public void copyPosition(Entity entity) {
      this.applyPosition(new FreecamPosition(entity));
   }

   public void applyPosition(FreecamPosition position) {
      this.setPos(position.x, position.y, position.z);
      this.setYRot(position.yaw);
      this.setXRot(position.pitch);
      this.xBob = this.getXRot();
      this.yBob = this.getYRot();
      this.xBobO = this.xBob;
      this.yBobO = this.yBob;
   }

   public void applyPerspective(ModConfig.Perspective perspective) {
      FreecamPosition position = new FreecamPosition(this);
      switch (perspective) {
         case INSIDE:
         default:
            break;
         case FIRST_PERSON:
            this.moveForwardUntilCollision(position, 0.4);
            break;
         case THIRD_PERSON_MIRROR:
            position.mirrorRotation();
         case THIRD_PERSON:
            this.moveForwardUntilCollision(position, -4.0);
      }
   }

   private boolean moveForwardUntilCollision(FreecamPosition position, double distance, boolean checkCollision) {
      if (!checkCollision) {
         position.moveForward(distance);
         this.applyPosition(position);
         return true;
      } else {
         return this.moveForwardUntilCollision(position, distance);
      }
   }

   private boolean moveForwardUntilCollision(FreecamPosition position, double maxDistance) {
      boolean negative = maxDistance < 0.0;
      maxDistance = negative ? -1.0 * maxDistance : maxDistance;
      double increment = 0.1;

      for (double distance = 0.0; distance < maxDistance; distance += increment) {
         FreecamPosition oldPosition = new FreecamPosition(this);
         position.moveForward(negative ? -1.0 * increment : increment);
         this.applyPosition(position);
         if (!this.wouldNotSuffocateAtTargetPose(this.getPose())) {
            this.applyPosition(oldPosition);
            return distance > 0.0;
         }
      }

      return true;
   }

   public void spawn() {
      if (this.level() instanceof ClientLevel clientLevel) {
         clientLevel.addEntity(this);
      }
   }

   public void despawn() {
      if (this.level() instanceof ClientLevel clientLevel && clientLevel.getEntity(this.getId()) != null) {
         clientLevel.removeEntity(this.getId(), RemovalReason.DISCARDED);
      }
   }

   @Override
   protected void checkFallDamage(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
   }

   @Override
   public float getAttackAnim(float tickDelta) {
      return Freecam.MC.player.getAttackAnim(tickDelta);
   }

   @Override
   public int getUseItemRemainingTicks() {
      return Freecam.MC.player.getUseItemRemainingTicks();
   }

   @Override
   public boolean isUsingItem() {
      return Freecam.MC.player.isUsingItem();
   }

   @Override
   public boolean onClimbable() {
      return false;
   }

   @Override
   public boolean isInWater() {
      return false;
   }

   @Override
   public MobEffectInstance getEffect(Holder<MobEffect> effect) {
      return Freecam.MC.player.getEffect(effect);
   }

   @Override
   public PushReaction getPistonPushReaction() {
      return PushReaction.IGNORE;
   }

   @Override
   public boolean canCollideWith(Entity other) {
      return false;
   }

   @Override
   public void setPose(Pose pose) {
      super.setPose(Pose.SWIMMING);
   }

   @Override
   public boolean isMovingSlowly() {
      return false;
   }

   @Override
   protected boolean updateIsUnderwater() {
      this.wasUnderwater = this.isEyeInFluid(FluidTags.WATER);
      return this.wasUnderwater;
   }

   @Override
   protected void doWaterSplashEffect() {
   }

   @Override
   public void aiStep() {
      if (ModConfig.INSTANCE.movement.flightMode.equals(ModConfig.FlightMode.DEFAULT)) {
         this.getAbilities().setFlyingSpeed(0.0F);
         Motion.doMotion(this, ModConfig.INSTANCE.movement.horizontalSpeed, ModConfig.INSTANCE.movement.verticalSpeed);
      } else {
         this.getAbilities().setFlyingSpeed((float)ModConfig.INSTANCE.movement.verticalSpeed / 10.0F);
      }

      super.aiStep();
      this.getAbilities().flying = true;
      this.setOnGround(false);
   }
}
