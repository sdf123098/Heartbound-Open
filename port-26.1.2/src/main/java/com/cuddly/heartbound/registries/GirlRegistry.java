package com.cuddly.heartbound.registries;

import com.cuddly.heartbound.Heartbound;
import com.cuddly.heartbound.entity.HeartboundEntities;
import com.cuddly.heartbound.entity.base.GirlEntity;
import com.cuddly.heartbound.entity.girls.AlyEntity;
import com.cuddly.heartbound.entity.girls.BiaEntity;
import com.cuddly.heartbound.entity.girls.CoppieEntity;
import com.cuddly.heartbound.entity.girls.CustomGirlEntity;
import com.cuddly.heartbound.entity.girls.EllieEntity;
import com.cuddly.heartbound.entity.girls.JennyEntity;
import com.cuddly.heartbound.entity.girls.KoboldEntity;
import com.cuddly.heartbound.entity.girls.SlimeEntity;
import net.minecraft.world.entity.EntityType;

public class GirlRegistry {
   public static EntityType<JennyEntity> JENNY;
   public static EntityType<EllieEntity> ELLIE;
   public static EntityType<BiaEntity> BIA;
   public static EntityType<SlimeEntity> SLIME;
   public static EntityType<KoboldEntity> KOBOLD;
   public static EntityType<CoppieEntity> COPPIE;
   public static EntityType<CustomGirlEntity> CUSTOM_GIRL;
   public static EntityType<AlyEntity> ALY;

   public static void registerGirls() {
      Heartbound.LOGGER.info("Registering Girls for Heartbound");
      HeartboundEntities.registerAttributes();
   }

   static {
      try {
         JENNY = HeartboundEntities.registerGirl("jenny", JennyEntity::new, 0.5F, 1.85F, 4930923, 9136967, GirlEntity::createDefaultAttributes);
         ELLIE = HeartboundEntities.registerGirl("ellie", EllieEntity::new, 0.5F, 1.95F, 1710638, 4853326, EllieEntity::createAttributes);
         BIA = HeartboundEntities.registerGirl("bia", BiaEntity::new, 0.5F, 1.65F, 9136404, 12886874, BiaEntity::createAttributes);
         SLIME = HeartboundEntities.registerGirl("slime", SlimeEntity::new, 0.5F, 1.85F, 3066993, 11267014, SlimeEntity::createAttributes);
         KOBOLD = HeartboundEntities.registerGirl("kobold", KoboldEntity::new, 0.5F, 1.55F, 12852794, 16737095, KoboldEntity::createAttributes);
         COPPIE = HeartboundEntities.registerGirl("coppie", CoppieEntity::new, 0.5F, 1.35F, 12088115, 15245926, CoppieEntity::createAttributes);
         CUSTOM_GIRL = HeartboundEntities.registerGirl("custom_girl", CustomGirlEntity::new, 0.5F, 1.95F, false, GirlEntity::createDefaultAttributes);
         ALY = HeartboundEntities.registerGirl("aly", AlyEntity::new, 0.5F, 1.65F, 8384495, 15132410, AlyEntity::createAttributes);
      } catch (Throwable var1) {
         Heartbound.LOGGER.warn("Failed to register some girl entities during static init (server environment?) - continuing without them:", var1);
      }
   }
}
