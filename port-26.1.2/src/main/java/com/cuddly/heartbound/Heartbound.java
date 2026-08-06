package com.cuddly.heartbound;

import com.cuddly.heartbound.advancement.criterion.HeartboundCriteria;
import com.cuddly.heartbound.block.HeartboundBlocks;
import com.cuddly.heartbound.block.entity.HeartboundBlockEntities;
import com.cuddly.heartbound.command.Commands;
import com.cuddly.heartbound.component.HeartboundDataComponentTypes;
import com.cuddly.heartbound.entity.HeartboundEntities;
import com.cuddly.heartbound.entity.ai.brain.GirlMemoryTypes;
import com.cuddly.heartbound.item.HeartboundItemGroups;
import com.cuddly.heartbound.item.HeartboundItems;
import com.cuddly.heartbound.networking.HeartboundPackets;
import com.cuddly.heartbound.registries.GirlRegistry;
import com.cuddly.heartbound.registries.HeartboundDispenserBehavior;
import com.cuddly.heartbound.registries.HeartboundScreenHandlerRegistry;
import com.cuddly.heartbound.registries.HeartboundSoundEventRegistry;
import com.cuddly.heartbound.registries.HeartboundTrackedDataRegistry;
import com.cuddly.heartbound.transformation.GirlTransformationInfo;
import com.cuddly.heartbound.util.json.CustomGirlLoader;
import com.cuddly.heartbound.util.managers.TamedGirlManager;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.Filter.Result;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.filter.RegexFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Heartbound implements ModInitializer {
   public static final String MOD_ID = "heartbound";
   public static final String MOD_NAME = "Pleasure Horizons";
   public static final Logger LOGGER;
   public static Map<UUID, BlockPos> usedBeds;
   public static Map<UUID, UUID> activeScenes;

   public void onInitialize() {
      LOGGER.info("Heartbound onInitialize called");

      try {
         LoggerContext ctx = LoggerContext.getContext(false);
         Configuration config = ctx.getConfiguration();
         String regex = ".*No data fixer registered for .*";
         RegexFilter filter = RegexFilter.createFilter(regex, null, false, Result.DENY, Result.NEUTRAL);
         LoggerConfig mcLoggerConfig = config.getLoggerConfig("Minecraft");
         mcLoggerConfig.addFilter(filter);
         LoggerConfig utilLoggerConfig = config.getLoggerConfig("net.minecraft.util.Util");
         utilLoggerConfig.addFilter(filter);
         ctx.updateLoggers();
      } catch (Throwable var7) {
      }

      ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> server.execute(() -> HeartboundCriteria.FIRST_LOGIN.trigger(handler.getPlayer())));
      ServerTickEvents.END_LEVEL_TICK.register(world -> {
         if (!world.isClientSide()) {
            TamedGirlManager.get(world).cleanupDeadGirls(world);
            if (world.getServer().getTickCount() == 1) {
               HeartboundEntities.runSelfChecks();
            }
         }
      });
      HeartboundPackets.registerPackets();
      HeartboundPackets.registerC2SPackets();
      LOGGER.info("About to call CustomGirlLoader.register()");
      CustomGirlLoader.register();
      LOGGER.info("CustomGirlLoader.register() completed");
      HeartboundItemGroups.registerItemGroups();
      HeartboundDataComponentTypes.registerDataComponentsTypes();
      HeartboundItems.registerItems();
      HeartboundBlockEntities.registerBlockEntities();
      HeartboundBlocks.registerBlocks();
      HeartboundCriteria.registerAdvancementCriteria();
      HeartboundTrackedDataRegistry.registerTrackedData();
      HeartboundSoundEventRegistry.registerSoundEvents();
      HeartboundScreenHandlerRegistry.registerScreenHandlers();
      HeartboundDispenserBehavior.registerDispenserBehavior();
      GirlMemoryTypes.registerMemoryTypes();
      GirlRegistry.registerGirls();
      GirlTransformationInfo.init();
      Commands.register();
   }

   static {
      try {
         Configurator.setLevel("Minecraft", Level.WARN);
         Configurator.setLevel("net.minecraft.util.Util", Level.WARN);
      } catch (Throwable var1) {
      }

      LOGGER = LoggerFactory.getLogger("Pleasure Horizons");
      usedBeds = new HashMap<>();
      activeScenes = new HashMap<>();
   }
}
