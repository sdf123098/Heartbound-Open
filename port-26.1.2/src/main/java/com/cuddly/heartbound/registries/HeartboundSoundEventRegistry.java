package com.cuddly.heartbound.registries;

import com.cuddly.heartbound.Heartbound;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class HeartboundSoundEventRegistry {
   public static final SoundEvent BEDRUSTLE = sound("misc.bedrustle");
   public static final SoundEvent BELLJINGLE = sound("misc.belljingle");
   public static final SoundEvent CLAP = sound("misc.clap");
   public static final SoundEvent CUMINFLATION = sound("misc.cuminflation");
   public static final SoundEvent FLAP = sound("misc.flap");
   public static final SoundEvent INSERTS = sound("misc.inserts");
   public static final SoundEvent POUNDING = sound("misc.pounding");
   public static final SoundEvent SLAP = sound("misc.slap");
   public static final SoundEvent SLIDE = sound("misc.slide");
   public static final SoundEvent SMALLINSERTS = sound("misc.smallinserts");
   public static final SoundEvent TOUCH = sound("misc.touch");
   public static final SoundEvent PLOB = sound("misc.plob");
   public static final SoundEvent JENNY_AFTERSSESSIONMOAN = sound("jenny.aftersessionmoan");
   public static final SoundEvent JENNY_AHH = sound("jenny.ahh");
   public static final SoundEvent JENNY_BJMOAN = sound("jenny.bjmoan");
   public static final SoundEvent JENNY_GIGGLE = sound("jenny.giggle");
   public static final SoundEvent JENNY_HAPPOH = sound("jenny.happyoh");
   public static final SoundEvent JENNY_HEAVYBREATHING = sound("jenny.heavybreathing");
   public static final SoundEvent JENNY_HMPH = sound("jenny.hmph");
   public static final SoundEvent JENNY_HUH = sound("jenny.huh");
   public static final SoundEvent JENNY_LIGHTBREATHING = sound("jenny.lightbreathing");
   public static final SoundEvent JENNY_LIPSOUND = sound("jenny.lipsound");
   public static final SoundEvent JENNY_MMM = sound("jenny.mmm");
   public static final SoundEvent JENNY_MOAN = sound("jenny.moan");
   public static final SoundEvent JENNY_SADOH = sound("jenny.sadoh");
   public static final SoundEvent JENNY_SIGH = sound("jenny.sigh");
   public static final SoundEvent ELLIE_AFTERSSESSIONMOAN = sound("ellie.aftersessionmoan");
   public static final SoundEvent ELLIE_AHH = sound("ellie.ahh");
   public static final SoundEvent ELLIE_BJMOAN = sound("ellie.bjmoan");
   public static final SoundEvent ELLIE_COMETOMOMMY = sound("ellie.cometomommy");
   public static final SoundEvent ELLIE_GIGGLE = sound("ellie.giggle");
   public static final SoundEvent ELLIE_GOODBOY = sound("ellie.goodboy");
   public static final SoundEvent ELLIE_HAPPYOH = sound("ellie.happyoh");
   public static final SoundEvent ELLIE_HEAVYBREATHING = sound("ellie.heavybreathing");
   public static final SoundEvent ELLIE_HMPH = sound("ellie.hmph");
   public static final SoundEvent ELLIE_HUH = sound("ellie.huh");
   public static final SoundEvent ELLIE_LIGHTBREATHING = sound("ellie.lightbreathing");
   public static final SoundEvent ELLIE_LIPSOUND = sound("ellie.lipsound");
   public static final SoundEvent ELLIE_MMM = sound("ellie.mmm");
   public static final SoundEvent ELLIE_MOAN = sound("ellie.moan");
   public static final SoundEvent ELLIE_MOMMYSHORNNY = sound("ellie.mommyhorny");
   public static final SoundEvent ELLIE_SADOH = sound("ellie.sadoh");
   public static final SoundEvent ELLIE_SIGH = sound("ellie.sigh");
   public static final SoundEvent BIA_AHH = sound("bia.ahh");
   public static final SoundEvent BIA_BJMOAN = sound("bia.bjmoan");
   public static final SoundEvent BIA_MOAN = sound("bia.moan");
   public static final SoundEvent BIA_GIGGLE = sound("bia.giggle");
   public static final SoundEvent BIA_HAPPYOH = sound("bia.happyoh");
   public static final SoundEvent BIA_SADOH = sound("bia.sadoh");
   public static final SoundEvent BIA_SIGH = sound("bia.sigh");
   public static final SoundEvent BIA_HEY = sound("bia.hey");
   public static final SoundEvent BIA_HMPH = sound("bia.hmph");
   public static final SoundEvent BIA_HUH = sound("bia.huh");
   public static final SoundEvent BIA_MMM = sound("bia.mmm");
   public static final SoundEvent BIA_LIGHTBREATHING = sound("bia.lightbreathing");

   public static void registerSoundEvents() {
      Heartbound.LOGGER.info("Registering SoundEvents for Heartbound");
      SceneKeyframeEventRegistry.registerSoundEvents();
   }

   private static SoundEvent sound(String path) {
      Identifier id = Identifier.fromNamespaceAndPath("heartbound", path);
      return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
   }

   public static enum SoundGroup {
      AFTERSSESSIONMOAN(Map.of("jenny", HeartboundSoundEventRegistry.JENNY_AFTERSSESSIONMOAN, "ellie", HeartboundSoundEventRegistry.ELLIE_AFTERSSESSIONMOAN)),
      AHH(Map.of("jenny", HeartboundSoundEventRegistry.JENNY_AHH, "ellie", HeartboundSoundEventRegistry.ELLIE_AHH, "bia", HeartboundSoundEventRegistry.BIA_AHH)),
      GIGGLE(
         Map.of(
            "jenny",
            HeartboundSoundEventRegistry.JENNY_GIGGLE,
            "ellie",
            HeartboundSoundEventRegistry.ELLIE_GIGGLE,
            "bia",
            HeartboundSoundEventRegistry.BIA_GIGGLE
         )
      ),
      HAPPOH(
         Map.of(
            "jenny",
            HeartboundSoundEventRegistry.JENNY_HAPPOH,
            "ellie",
            HeartboundSoundEventRegistry.ELLIE_HAPPYOH,
            "bia",
            HeartboundSoundEventRegistry.BIA_HAPPYOH
         )
      ),
      HEAVYBREATHING(Map.of("jenny", HeartboundSoundEventRegistry.JENNY_HEAVYBREATHING, "ellie", HeartboundSoundEventRegistry.ELLIE_HEAVYBREATHING)),
      HMPH(
         Map.of(
            "jenny", HeartboundSoundEventRegistry.JENNY_HMPH, "ellie", HeartboundSoundEventRegistry.ELLIE_HMPH, "bia", HeartboundSoundEventRegistry.BIA_HMPH
         )
      ),
      HUH(Map.of("jenny", HeartboundSoundEventRegistry.JENNY_HUH, "ellie", HeartboundSoundEventRegistry.ELLIE_HUH, "bia", HeartboundSoundEventRegistry.BIA_HUH)),
      LIGHTBREATHING(
         Map.of(
            "jenny",
            HeartboundSoundEventRegistry.JENNY_LIGHTBREATHING,
            "ellie",
            HeartboundSoundEventRegistry.ELLIE_LIGHTBREATHING,
            "bia",
            HeartboundSoundEventRegistry.BIA_LIGHTBREATHING
         )
      ),
      LIPSOUND(Map.of("jenny", HeartboundSoundEventRegistry.JENNY_LIPSOUND, "ellie", HeartboundSoundEventRegistry.ELLIE_LIPSOUND)),
      MMM(Map.of("jenny", HeartboundSoundEventRegistry.JENNY_MMM, "ellie", HeartboundSoundEventRegistry.ELLIE_MMM, "bia", HeartboundSoundEventRegistry.BIA_MMM)),
      MOAN(
         Map.of(
            "jenny", HeartboundSoundEventRegistry.JENNY_MOAN, "ellie", HeartboundSoundEventRegistry.ELLIE_MOAN, "bia", HeartboundSoundEventRegistry.BIA_MOAN
         )
      ),
      SADOH(
         Map.of(
            "jenny", HeartboundSoundEventRegistry.JENNY_SADOH, "ellie", HeartboundSoundEventRegistry.ELLIE_SADOH, "bia", HeartboundSoundEventRegistry.BIA_SADOH
         )
      ),
      SIGH(
         Map.of(
            "jenny", HeartboundSoundEventRegistry.JENNY_SIGH, "ellie", HeartboundSoundEventRegistry.ELLIE_SIGH, "bia", HeartboundSoundEventRegistry.BIA_SIGH
         )
      ),
      HEY(Map.of("bia", HeartboundSoundEventRegistry.BIA_HEY)),
      COMETOMOMMY(Map.of("ellie", HeartboundSoundEventRegistry.ELLIE_COMETOMOMMY)),
      MOMMYSHORNNY(Map.of("ellie", HeartboundSoundEventRegistry.ELLIE_MOMMYSHORNNY)),
      GOODBOY(Map.of("ellie", HeartboundSoundEventRegistry.ELLIE_GOODBOY)),
      BJMOAN(
         Map.of(
            "jenny",
            HeartboundSoundEventRegistry.JENNY_BJMOAN,
            "ellie",
            HeartboundSoundEventRegistry.ELLIE_BJMOAN,
            "bia",
            HeartboundSoundEventRegistry.BIA_BJMOAN
         )
      );

      private final Map<String, SoundEvent> sounds;

      private SoundGroup(Map<String, SoundEvent> sounds) {
         this.sounds = sounds;
      }

      public SoundEvent getSound(String girlId) {
         return this.sounds.get(girlId.toLowerCase());
      }
   }
}
