package com.cuddly.heartbound.entity.girls;

import com.cuddly.heartbound.entity.base.GirlEntity;
import com.cuddly.heartbound.entity.base.tamable.BaseGirlEntityAI;
import com.cuddly.heartbound.util.variables.Scene;
import java.util.List;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class AlyEntity extends BaseGirlEntityAI {
   public AlyEntity(EntityType<? extends BaseGirlEntityAI> entityType, World world) {
      super(entityType, world);
      this.setNoGravity(true);
      this.moveControl = new FlightMoveControl(this, 20, true);
   }

   public void setFastFlying(boolean fast) {
      EntityAttributeInstance inst = this.getAttributeInstance(EntityAttributes.GENERIC_FLYING_SPEED);
      if (inst != null) {
         if (fast) {
            inst.setBaseValue(0.25);
         } else {
            inst.setBaseValue(0.1);
         }
      }
   }

   @Override
   protected boolean supportsDoorInteraction() {
      return false;
   }

   @Override
   public void setSprinting(boolean sprinting) {
      super.setSprinting(sprinting);
      this.setFastFlying(sprinting);
   }

   @Override
   public boolean damage(DamageSource source, float amount) {
      return source.isIn(DamageTypeTags.IS_FALL) ? false : super.damage(source, amount);
   }

   @Override
   public Item isAttractedTo() {
      return Items.AMETHYST_SHARD;
   }

   @Override
   public String getGirlID() {
      return "aly";
   }

   @Override
   public int getSizeGUI() {
      return 30;
   }

   @Override
   public float getYAxisGUI() {
      return 0.0625F;
   }

   @Override
   public List<Scene> getScenes() {
      return List.of(
         Scene.onPlayer("scene.heartbound.sex", 4, List.of("sex_start"), List.of("sex_slow"), List.of("sex_fast"), "sex_cum", 5.0F, true, false),
         Scene.onPlayer(
            "scene.heartbound.blowJob", 6, List.of("blowjob_start"), List.of("blowjob_slow"), List.of("blowjob_fast"), "blowjob_cum", 5.0F, false, false
         ),
         Scene.onPlayer("scene.heartbound.nelson", 8, List.of("nelson_start"), List.of("nelson_slow"), List.of("nelson_fast"), "nelson_cum", 5.0F, true, false)
      );
   }

   @Override
   public boolean isAerialEntity() {
      return true;
   }

   @Override
   public boolean hasBackwardsWalkAnim() {
      return false;
   }

   @Override
   protected String mapDefaultAnimation(String animation) {
      if (animation == null) {
         return "idle";
      } else {
         byte var3 = -1;
         switch (animation.hashCode()) {
            case -2000825176:
               if (animation.equals("fly_fast")) {
                  var3 = 0;
               }
            default:
               return switch (var3) {
                  case 0 -> "flyfast";
                  default -> animation;
               };
         }
      }
   }

   @Override
   public void travel(Vec3d movementInput) {
      if (this.isLogicalSideForUpdatingMovement()) {
         if (this.isTouchingWater()) {
            this.updateVelocity(0.02F, movementInput);
            this.move(MovementType.SELF, this.getVelocity());
            this.setVelocity(this.getVelocity().multiply(0.8));
         } else if (this.isInLava()) {
            this.updateVelocity(0.02F, movementInput);
            this.move(MovementType.SELF, this.getVelocity());
            this.setVelocity(this.getVelocity().multiply(0.5));
         } else {
            this.updateVelocity(this.getMovementSpeed(), movementInput);
            this.move(MovementType.SELF, this.getVelocity());
            this.setVelocity(this.getVelocity().multiply(0.91));
         }
      }

      this.updateLimbs(false);
   }

   public static Builder createAttributes() {
      return GirlEntity.createDefaultAttributes()
         .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
         .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.1)
         .add(EntityAttributes.GENERIC_FLYING_SPEED, 0.1)
         .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0);
   }

   @Override
   protected EntityNavigation createNavigation(World world) {
      return new BirdNavigation(this, world);
   }
}
