package com.cuddly.heartbound.config.gui;

import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Supplier;
import me.shedaniel.autoconfig.gui.registry.GuiRegistry;
import me.shedaniel.autoconfig.util.Utils;
import net.minecraft.network.chat.Component;

class BoundedContinuousImpl {
   private BoundedContinuousImpl() {
   }

   static void apply(GuiRegistry registry) {
      registry.registerAnnotationProvider(
         (i18n, field, config, defaults, guiProvider) -> {
            Consumer<Double> save = newValue -> Utils.setUnsafely(field, config, newValue);
            Supplier<Double> defaultValue = () -> Utils.getUnsafely(field, defaults);
            double value = Utils.getUnsafely(field, config, defaultValue.get());
            BoundedContinuous bounds = field.getAnnotation(BoundedContinuous.class);
            return Collections.singletonList(
               new DoubleSliderEntry(
                  Component.translatable(i18n), bounds.precision(), bounds.min(), bounds.max(), value, AutoConfigExtensions.RESET_TEXT, defaultValue, save
               )
            );
         },
         field -> field.getType() == double.class || field.getType() == Double.class,
         BoundedContinuous.class
      );
   }
}
