package com.cuddly.heartbound.freecam;

import com.cuddly.heartbound.config.ModConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.ApiStatus.AvailableSince;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
@AvailableSince("0.4.0")
public class FreeCamera extends ClientPlayerEntity {
   public FreeCamera(int id) {
      super(Freecam.MC, Freecam.MC.world, Freecam.MC.getNetworkHandler(), Freecam.MC.player.getStatHandler(), Freecam.MC.player.getRecipeBook(), false, false);
      this.setId(id);
      this.setPose(EntityPose.SWIMMING);
      this.getAbilities().flying = true;
      this.input = new KeyboardInput(Freecam.MC.options);
   }

   @Override
   public void copyPositionAndRotation(Entity entity) {
      this.applyPosition(new FreecamPosition(entity));
   }

   public void applyPosition(FreecamPosition position) {
      this.refreshPositionAndAngles(position.x, position.y, position.z, position.yaw, position.pitch);
      this.renderPitch = this.getPitch();
      this.renderYaw = this.getYaw();
      this.lastRenderPitch = this.renderPitch;
      this.lastRenderYaw = this.renderYaw;
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
         if (!this.wouldNotSuffocateInPose(this.getPose())) {
            this.applyPosition(oldPosition);
            return distance > 0.0;
         }
      }

      return true;
   }

   public void spawn() {
      if (this.clientWorld != null) {
         this.clientWorld.addEntity(this);
      }
   }

   public void despawn() {
      if (this.clientWorld != null && this.clientWorld.getEntityById(this.getId()) != null) {
         this.clientWorld.removeEntity(this.getId(), RemovalReason.DISCARDED);
      }
   }

   @Override
   protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
   }

   @Override
   public float getHandSwingProgress(float tickDelta) {
      return Freecam.MC.player.getHandSwingProgress(tickDelta);
   }

   @Override
   public int getItemUseTimeLeft() {
      return Freecam.MC.player.getItemUseTimeLeft();
   }

   @Override
   public boolean isUsingItem() {
      return Freecam.MC.player.isUsingItem();
   }

   @Override
   public boolean isClimbing() {
      return false;
   }

   @Override
   public boolean isTouchingWater() {
      return false;
   }

   @Override
   public StatusEffectInstance getStatusEffect(RegistryEntry<StatusEffect> effect) {
      return Freecam.MC.player.getStatusEffect(effect);
   }

   @Override
   public PistonBehavior getPistonBehavior() {
      return PistonBehavior.IGNORE;
   }

   @Override
   public boolean collidesWith(Entity other) {
      return false;
   }

   @Override
   public void setPose(EntityPose pose) {
      super.setPose(EntityPose.SWIMMING);
   }

   @Override
   public boolean shouldSlowDown() {
      return false;
   }

   @Override
   protected boolean updateWaterSubmersionState() {
      this.isSubmergedInWater = this.isSubmergedIn(FluidTags.WATER);
      return this.isSubmergedInWater;
   }

   @Override
   protected void onSwimmingStart() {
   }

   @Override
   public void tickMovement() {
      if (ModConfig.INSTANCE.movement.flightMode.equals(ModConfig.FlightMode.DEFAULT)) {
         this.getAbilities().setFlySpeed(0.0F);
         Motion.doMotion(this, ModConfig.INSTANCE.movement.horizontalSpeed, ModConfig.INSTANCE.movement.verticalSpeed);
      } else {
         this.getAbilities().setFlySpeed((float)ModConfig.INSTANCE.movement.verticalSpeed / 10.0F);
      }

      super.tickMovement();
      this.getAbilities().flying = true;
      this.setOnGround(false);
   }
}
