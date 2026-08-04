package com.cuddly.heartbound.entity.girls;

import com.cuddly.heartbound.entity.base.GirlEntity;
import com.cuddly.heartbound.entity.base.tamable.BaseGirlEntityAI;
import com.cuddly.heartbound.util.variables.Scene;
import java.util.List;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.world.World;

public class SlimeEntity extends BaseGirlEntityAI {
   public SlimeEntity(EntityType<? extends BaseGirlEntityAI> entityType, World world) {
      super(entityType, world);
   }

   @Override
   public Item isAttractedTo() {
      return Items.SLIME_BALL;
   }

   @Override
   public String getGirlID() {
      return "slime";
   }

   @Override
   public int getSizeGUI() {
      return 29;
   }

   @Override
   public float getYAxisGUI() {
      return 0.0525F;
   }

   @Override
   public float getWeaponBoneXRotation() {
      return -100.0F;
   }

   @Override
   public List<Scene> getScenes() {
      return List.of(
         Scene.onPlayer(
            "scene.heartbound.blowJob", 4, List.of("blowjob_intro"), List.of("blowjob_slow"), List.of("blowjob_fast"), "blowjob_cum", 2.5F, false, false
         ),
         Scene.stationaryContact(
            "scene.heartbound.doggy",
            6,
            List.of("doggy_intro"),
            List.of("doggy_slow"),
            List.of("doggy_fast"),
            "doggy_cum",
            4.5F,
            true,
            false,
            "doggy_lay_on_bed",
            "doggy_bed_idle"
         )
      );
   }

   @Override
   public boolean damage(DamageSource source, float amount) {
      return source.isIn(DamageTypeTags.IS_FALL) ? false : super.damage(source, amount);
   }

   public static Builder createAttributes() {
      return GirlEntity.createDefaultAttributes()
         .add(EntityAttributes.GENERIC_MAX_HEALTH, 15.0)
         .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2)
         .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0);
   }
}
