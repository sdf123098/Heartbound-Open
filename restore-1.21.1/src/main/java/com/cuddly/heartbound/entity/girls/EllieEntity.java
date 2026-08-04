package com.cuddly.heartbound.entity.girls;

import com.cuddly.heartbound.entity.base.GirlEntity;
import com.cuddly.heartbound.entity.base.tamable.BaseGirlEntityAI;
import com.cuddly.heartbound.util.variables.Scene;
import java.util.List;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.world.World;

public class EllieEntity extends BaseGirlEntityAI {
   public EllieEntity(EntityType<? extends BaseGirlEntityAI> entityType, World world) {
      super(entityType, world);
   }

   @Override
   public Item isAttractedTo() {
      return Items.WITHER_ROSE;
   }

   @Override
   public String getGirlID() {
      return "ellie";
   }

   @Override
   public int getSizeGUI() {
      return 25;
   }

   @Override
   public float getWeaponBoneXRotation() {
      return -80.0F;
   }

   @Override
   public List<Scene> getScenes() {
      return List.of(
         Scene.onPlayer("scene.heartbound.faceFuck", 6, List.of("carry_intro"), List.of("carry_slow1"), List.of("carry_fast"), "carry_cum", 2.5F, false, false),
         Scene.onBed(
            "scene.heartbound.missionary",
            8,
            List.of("missionary_intro"),
            List.of("missionary_slow"),
            List.of("missionary_fast"),
            "missionary_cum",
            3.0F,
            true,
            false,
            0.5F,
            "sit_down",
            "sit_down_idle"
         ),
         Scene.onBed(
            "scene.heartbound.cowgirl",
            10,
            List.of("cowgirl_intro"),
            List.of("cowgirl_slow"),
            List.of("cowgirl_fast"),
            "cowgirl_cum",
            3.0F,
            true,
            false,
            0.5F,
            "sit_down",
            "sit_down_idle"
         )
      );
   }

   @Override
   public List<String> giftRepliesLike() {
      return List.of("msg.heartbound.ellie.gift.like.1", "msg.heartbound.ellie.gift.like.2", "msg.heartbound.ellie.gift.like.3");
   }

   @Override
   public List<String> giftRepliesLove() {
      return List.of("msg.heartbound.ellie.gift.love.1", "msg.heartbound.ellie.gift.love.2", "msg.heartbound.ellie.gift.love.3");
   }

   public static Builder createAttributes() {
      return GirlEntity.createDefaultAttributes()
         .add(EntityAttributes.GENERIC_MAX_HEALTH, 30.0)
         .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.18)
         .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0);
   }
}
