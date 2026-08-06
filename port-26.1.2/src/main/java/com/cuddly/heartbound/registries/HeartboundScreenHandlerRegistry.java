package com.cuddly.heartbound.registries;

import com.cuddly.heartbound.Heartbound;
import com.cuddly.heartbound.HeartboundClient;
import com.cuddly.heartbound.screen.FusionTableScreenHandler;
import com.cuddly.heartbound.screen.GirlInventoryScreenHandler;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public class HeartboundScreenHandlerRegistry {
   public static final Object EMPTY_DATA = new Object();
   public static final ExtendedMenuType<GirlInventoryScreenHandler, HeartboundClient.GirlScreenData> GIRL_INVENTORY_SCREEN_HANDLER = Registry.register(
      BuiltInRegistries.MENU,
      Identifier.fromNamespaceAndPath("heartbound", "girl_inventory_screen"),
      new ExtendedMenuType<>(GirlInventoryScreenHandler::new, HeartboundClient.GirlScreenData.PACKET_CODEC)
   );
   public static final ExtendedMenuType<FusionTableScreenHandler, Object> FUSION_TABLE_SCREEN_HANDLER = Registry.register(
      BuiltInRegistries.MENU,
      Identifier.fromNamespaceAndPath("heartbound", "fusion_table"),
      new ExtendedMenuType(FusionTableScreenHandler::new, StreamCodec.unit(EMPTY_DATA))
   );

   public static void registerScreenHandlers() {
      Heartbound.LOGGER.info("Registering Screen Handlers for Heartbound");
   }
}
