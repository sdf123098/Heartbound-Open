package com.cuddly.heartbound.registries;

import com.cuddly.heartbound.Heartbound;
import com.cuddly.heartbound.HeartboundClient;
import com.cuddly.heartbound.screen.FusionTableScreenHandler;
import com.cuddly.heartbound.screen.GirlInventoryScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class HeartboundScreenHandlerRegistry {
   public static final Object EMPTY_DATA = new Object();
   public static final ExtendedScreenHandlerType<GirlInventoryScreenHandler, HeartboundClient.GirlScreenData> GIRL_INVENTORY_SCREEN_HANDLER = Registry.register(
      Registries.SCREEN_HANDLER,
      Identifier.of("heartbound", "girl_inventory_screen"),
      new ExtendedScreenHandlerType<>(GirlInventoryScreenHandler::new, HeartboundClient.GirlScreenData.PACKET_CODEC)
   );
   public static final ExtendedScreenHandlerType<FusionTableScreenHandler, Object> FUSION_TABLE_SCREEN_HANDLER = Registry.register(
      Registries.SCREEN_HANDLER,
      Identifier.of("heartbound", "fusion_table"),
      new ExtendedScreenHandlerType(FusionTableScreenHandler::new, PacketCodec.unit(EMPTY_DATA))
   );

   public static void registerScreenHandlers() {
      Heartbound.LOGGER.info("Registering Screen Handlers for Heartbound");
   }
}
