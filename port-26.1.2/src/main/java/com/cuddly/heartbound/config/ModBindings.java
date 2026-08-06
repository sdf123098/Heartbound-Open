package com.cuddly.heartbound.config;

import com.cuddly.heartbound.config.keys.FreecamKeyMapping;
import com.cuddly.heartbound.config.keys.FreecamKeyMappingBuilder;
import com.cuddly.heartbound.freecam.Freecam;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import me.shedaniel.autoconfig.AutoConfigClient;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;

public enum ModBindings {
   KEY_TOGGLE(() -> FreecamKeyMappingBuilder.builder("toggle").action(Freecam::toggle).holdAction(Freecam::activateTripodHandler).defaultKey(293).build()),
   KEY_PLAYER_CONTROL(() -> FreecamKeyMappingBuilder.builder("playerControl").action(Freecam::switchControls).build()),
   KEY_TRIPOD_RESET(() -> FreecamKeyMappingBuilder.builder("tripodReset").holdAction(Freecam::resetTripodHandler).build()),
    KEY_CONFIG_GUI(
       () -> FreecamKeyMappingBuilder.builder("configGui")
             .action(() -> Freecam.MC.setScreen(AutoConfigClient.getConfigScreen(ModConfig.class, Freecam.MC.screen).get()))
             .build()
    );

   private final Supplier<FreecamKeyMapping> lazyMapping;

   private ModBindings(Supplier<FreecamKeyMapping> mappingSupplier) {
      this.lazyMapping = Suppliers.memoize(mappingSupplier);
   }

   public FreecamKeyMapping get() {
      return (FreecamKeyMapping)this.lazyMapping.get();
   }

   public static void forEach(@NotNull Consumer<FreecamKeyMapping> action) {
      Objects.requireNonNull(action);
      iterator().forEachRemaining(action);
   }

   @NotNull
   public static Iterator<FreecamKeyMapping> iterator() {
      return stream().iterator();
   }

   @NotNull
   public static Spliterator<FreecamKeyMapping> spliterator() {
      return stream().spliterator();
   }

   @NotNull
   public static Stream<FreecamKeyMapping> stream() {
      return Arrays.stream(values()).map(ModBindings::get);
   }
}
