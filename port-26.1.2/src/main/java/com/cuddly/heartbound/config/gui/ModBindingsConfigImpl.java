package com.cuddly.heartbound.config.gui;

import com.cuddly.heartbound.config.ModBindings;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.CollapsibleObject;
import me.shedaniel.autoconfig.gui.registry.GuiRegistry;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.impl.builders.KeyCodeBuilder;
import net.minecraft.network.chat.Component;

class ModBindingsConfigImpl {
   private ModBindingsConfigImpl() {
   }

   static void apply(GuiRegistry registry) {
      registry.registerAnnotationProvider(
         (i18n, field, config, defaults, guiProvider) -> modBindingEntries(),
         field -> !field.isAnnotationPresent(CollapsibleObject.class),
         ModBindingsConfig.class
      );
      registry.registerAnnotationProvider(
         (i18n, field, config, defaults, guiProvider) -> {
            AutoConfigExtensions.ENTRY_BUILDER
               .startSubCategory(Component.translatable(i18n), modBindingEntries())
               .setExpanded(field.getDeclaredAnnotation(CollapsibleObject.class).startExpanded())
               .build();
            return new ArrayList<>(modBindingEntries());
         },
         field -> field.isAnnotationPresent(CollapsibleObject.class),
         ModBindingsConfig.class
      );
   }

   private static List<AbstractConfigListEntry> modBindingEntries() {
      return ModBindings.stream()
         .map(bind -> AutoConfigExtensions.ENTRY_BUILDER.fillKeybindingField(Component.translatable(bind.getName()), bind))
         .map(KeyCodeBuilder::build)
         .map(AbstractConfigListEntry.class::cast)
         .collect(Collectors.toList());
   }
}
