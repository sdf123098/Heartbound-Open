package com.cuddly.heartbound.entity.girls;

import com.cuddly.heartbound.entity.base.GirlEntity;
import com.cuddly.heartbound.entity.base.tamable.BaseGirlEntityAI;
import com.cuddly.heartbound.util.variables.Scene;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class AlyEntity extends BaseGirlEntityAI {
   public AlyEntity(EntityType<? extends BaseGirlEntityAI> entityType, Level world) {
      super(entityType, world);
      this.setNoGravity(true);
      this.moveControl = new FlyingMoveControl(this, 20, true);
   }

   public void setFastFlying(boolean fast) {
      AttributeInstance inst = this.getAttribute(Attributes.FLYING_SPEED);
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
   public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount) {
      return source.is(DamageTypeTags.IS_FALL) ? false : super.hurtServer(serverLevel, source, amount);
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
   public void travel(Vec3 movementInput) {
      if (this.isEffectiveAi()) {
         if (this.isInWater()) {
            this.moveRelative(0.02F, movementInput);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.8));
         } else if (this.isInLava()) {
            this.moveRelative(0.02F, movementInput);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.5));
         } else {
            this.moveRelative(this.getSpeed(), movementInput);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.91));
         }
      }

      this.calculateEntityAnimation(false);
   }

   public static Builder createAttributes() {
      return GirlEntity.createDefaultAttributes()
         .add(Attributes.MAX_HEALTH, 20.0)
         .add(Attributes.MOVEMENT_SPEED, 0.1)
         .add(Attributes.FLYING_SPEED, 0.1)
         .add(Attributes.ATTACK_DAMAGE, 2.0);
   }

   @Override
   protected PathNavigation createNavigation(Level world) {
      return new FlyingPathNavigation(this, world);
   }
}
