package com.cuddly.heartbound.config;

import com.cuddly.heartbound.config.gui.AutoConfigExtensions;
import com.cuddly.heartbound.config.gui.BoundedContinuous;
import com.cuddly.heartbound.config.gui.ModBindingsConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry.ColorPicker;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.CollapsibleObject;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.EnumHandler;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.Excluded;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.Tooltip;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.EnumHandler.EnumDisplayOption;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.clothconfig2.gui.entries.SelectionListEntry.Translatable;
import org.jetbrains.annotations.NotNull;

@Config(
   name = "heartbound"
)
public class ModConfig implements ConfigData {
   @Excluded
   public static ModConfig INSTANCE;
   @CollapsibleObject
   public ModConfig.GirlConfig girls = new ModConfig.GirlConfig();
   @CollapsibleObject
   public ModConfig.PlayerConfig player = new ModConfig.PlayerConfig();
   @Excluded
   public ModConfig.ControlsConfig controls = new ModConfig.ControlsConfig();
   @Excluded
   public ModConfig.MovementConfig movement = new ModConfig.MovementConfig();
   @Excluded
   public ModConfig.VisualConfig visual = new ModConfig.VisualConfig();
   @Excluded
   public ModConfig.UtilityConfig utility = new ModConfig.UtilityConfig();
   @Excluded
   public ModConfig.NotificationConfig notification = new ModConfig.NotificationConfig();

   public static void init() {
      ConfigHolder<ModConfig> holder = AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new);
      AutoConfigExtensions.apply(ModConfig.class);
      INSTANCE = holder.getConfig();
   }

   public static class ControlsConfig {
      @ModBindingsConfig
      private Object keys;
   }

   public static enum FlightMode implements Translatable {
      CREATIVE("creative"),
      DEFAULT("default");

      private final String key;

      private FlightMode(String name) {
         this.key = "text.autoconfig.heartbound.option.movement.flightMode." + name;
      }

      @NotNull
      @Override
      public String getKey() {
         return this.key;
      }
   }

   public static class GirlConfig {
      @Tooltip
      public boolean boobWindow = false;
      @Tooltip
      public boolean disableShading = false;
   }

   public static enum InteractionMode implements Translatable {
      CAMERA("camera"),
      PLAYER("player");

      private final String key;

      private InteractionMode(String name) {
         this.key = "text.autoconfig.heartbound.option.utility.interactionMode." + name;
      }

      @NotNull
      @Override
      public String getKey() {
         return this.key;
      }
   }

   public static class MovementConfig {
      @Tooltip
      @EnumHandler(
         option = EnumDisplayOption.BUTTON
      )
      public ModConfig.FlightMode flightMode = ModConfig.FlightMode.DEFAULT;
      @Tooltip
      @BoundedContinuous(
         max = 10.0
      )
      public double horizontalSpeed = 1.0;
      @Tooltip
      @BoundedContinuous(
         max = 10.0
      )
      public double verticalSpeed = 1.0;
   }

   public static class NotificationConfig {
      @Tooltip
      public boolean notifyFreecam = true;
      @Tooltip
      public boolean notifyTripod = true;
   }

   public static enum Perspective implements Translatable {
      FIRST_PERSON("firstPerson"),
      THIRD_PERSON("thirdPerson"),
      THIRD_PERSON_MIRROR("thirdPersonMirror"),
      INSIDE("inside");

      private final String key;

      private Perspective(String name) {
         this.key = "text.autoconfig.heartbound.option.visual.perspective." + name;
      }

      @NotNull
      @Override
      public String getKey() {
         return this.key;
      }
   }

   public static class PlayerConfig {
      @Tooltip
      @ColorPicker
      public int penisShaftColor = 16107173;
      @Tooltip
      @ColorPicker
      public int penisHeadColor = 16099241;
   }

   public static class UtilityConfig {
      @Tooltip
      public boolean disableOnDamage = true;
      public boolean freezePlayer = true;
      public boolean allowInteract = false;
      @Tooltip
      @EnumHandler(
         option = EnumDisplayOption.BUTTON
      )
      public ModConfig.InteractionMode interactionMode = ModConfig.InteractionMode.CAMERA;
   }

   public static class VisualConfig {
      @Tooltip
      @EnumHandler(
         option = EnumDisplayOption.BUTTON
      )
      public ModConfig.Perspective perspective = ModConfig.Perspective.INSIDE;
      @Tooltip
      public boolean hidePlayer = true;
      @Tooltip
      public boolean showHand = false;
      @Tooltip
      public boolean fullBright = false;
      @Tooltip
      public boolean showSubmersion = false;
   }
}
