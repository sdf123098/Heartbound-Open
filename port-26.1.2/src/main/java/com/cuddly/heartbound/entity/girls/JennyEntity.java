package com.cuddly.heartbound.entity.girls;

import com.cuddly.heartbound.entity.base.tamable.BaseGirlEntityAI;
import com.cuddly.heartbound.util.variables.Scene;
import java.util.List;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class JennyEntity extends BaseGirlEntityAI {
   public JennyEntity(EntityType<? extends BaseGirlEntityAI> entityType, Level world) {
      super(entityType, world);
   }

   @Override
   public Item isAttractedTo() {
      return Items.GOLD_INGOT;
   }

   @Override
   public String getGirlID() {
      return "jenny";
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
   public List<Scene> getScenes() {
      return List.of(
         Scene.stationary("scene.heartbound.masturbation", 4, "masturbating", 4, true, true),
         Scene.onPlayer(
            "scene.heartbound.paizuri", 6, List.of("paizuri_intro"), List.of("paizuri_slow"), List.of("paizuri_fast"), "paizuri_cum", 4.0F, true, false
         ),
         Scene.onPlayer(
            "scene.heartbound.blowJob", 8, List.of("blowjob_intro"), List.of("blowjob_slow"), List.of("blowjob_fast"), "blowjob_cum", 2.5F, false, false
         ),
         Scene.onBed(
            "scene.heartbound.doggy",
            10,
            List.of("doggy_intro"),
            List.of("doggy_slow"),
            List.of("doggy_fast1", "doggy_fast2"),
            "doggy_cum",
            4.5F,
            true,
            true,
            0.0F,
            "doggy_lay_on_bed",
            "doggy_bed_idle"
         )
      );
   }

   @Override
   public List<String> giftRepliesLike() {
      return List.of("msg.heartbound.jenny.gift.like.1", "msg.heartbound.jenny.gift.like.2", "msg.heartbound.jenny.gift.like.3");
   }

   @Override
   public List<String> giftRepliesLove() {
      return List.of("msg.heartbound.jenny.gift.love.1", "msg.heartbound.jenny.gift.love.2", "msg.heartbound.jenny.gift.love.3");
   }
}
