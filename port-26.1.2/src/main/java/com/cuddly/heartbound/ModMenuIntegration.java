package com.cuddly.heartbound;

import com.cuddly.heartbound.config.ModConfig;
import com.cuddly.heartbound.config.gui.AutoConfigExtensions;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigManager;
import me.shedaniel.autoconfig.gui.ConfigScreenProvider;
import me.shedaniel.autoconfig.gui.registry.ComposedGuiRegistryAccess;
import me.shedaniel.autoconfig.gui.registry.DefaultGuiRegistryAccess;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;

@Environment(EnvType.CLIENT)
public class ModMenuIntegration implements ModMenuApi {
   @Override
   public ConfigScreenFactory<?> getModConfigScreenFactory() {
      return parent -> {
         ConfigManager<ModConfig> manager = (ConfigManager<ModConfig>)AutoConfig.getConfigHolder(ModConfig.class);
         ComposedGuiRegistryAccess access = new ComposedGuiRegistryAccess(new DefaultGuiRegistryAccess(), AutoConfigExtensions.apply(ModConfig.class));
         return (Screen)new ConfigScreenProvider<>(manager, access, parent).get();
      };
   }
}
