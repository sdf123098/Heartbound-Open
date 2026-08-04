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

public class CoppieEntity extends BaseGirlEntityAI {
   public CoppieEntity(EntityType<? extends BaseGirlEntityAI> entityType, World world) {
      super(entityType, world);
   }

   @Override
   public Item isAttractedTo() {
      return Items.COPPER_INGOT;
   }

   @Override
   public String getGirlID() {
      return "coppie";
   }

   @Override
   public int getSizeGUI() {
      return 35;
   }

   @Override
   public List<Scene> getScenes() {
      return List.of(
         Scene.onBed(
            "scene.heartbound.anal",
            6,
            List.of("anal_intro"),
            List.of("anal_slow"),
            List.of("anal_fast"),
            "anal_cum",
            6.0F,
            true,
            false,
            0.0F,
            "anal_lay_on_bed",
            "anal_bed_idle"
         ),
         Scene.onBed(
            "scene.heartbound.doggy",
            8,
            List.of("prone_doggy_intro", "prone_doggy_insert"),
            List.of("prone_doggy_slow"),
            List.of("prone_doggy_hard1", "prone_doggy_hard2", "prone_doggy_hard3"),
            "prone_doggy_cum",
            6.0F,
            true,
            true,
            1.0F,
            "sit_down",
            "sit_down_idle"
         )
      );
   }

   @Override
   public List<String> giftRepliesLike() {
      return List.of("msg.heartbound.coppie.gift.like.1", "msg.heartbound.coppie.gift.like.2", "msg.heartbound.coppie.gift.like.3");
   }

   @Override
   public List<String> giftRepliesLove() {
      return List.of("msg.heartbound.coppie.gift.love.1", "msg.heartbound.coppie.gift.love.2", "msg.heartbound.coppie.gift.love.3");
   }

   public static Builder createAttributes() {
      return GirlEntity.createDefaultAttributes()
         .add(EntityAttributes.GENERIC_MAX_HEALTH, 15.0)
         .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2)
         .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0);
   }
}
