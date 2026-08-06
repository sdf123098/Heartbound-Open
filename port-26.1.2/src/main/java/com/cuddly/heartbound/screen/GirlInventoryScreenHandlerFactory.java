package com.cuddly.heartbound.screen;

import com.cuddly.heartbound.HeartboundClient;
import com.cuddly.heartbound.entity.base.GirlEntity;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

public class GirlInventoryScreenHandlerFactory implements ExtendedMenuProvider<HeartboundClient.GirlScreenData> {
   private final GirlEntity girl;

   public GirlInventoryScreenHandlerFactory(GirlEntity girl) {
      this.girl = girl;
   }

   public HeartboundClient.GirlScreenData getScreenOpeningData(ServerPlayer player) {
      return new HeartboundClient.GirlScreenData(this.girl.getId());
   }

   public Component getDisplayName() {
      return Component.literal("");
   }

   @Nullable
   public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
      return new GirlInventoryScreenHandler(syncId, playerInventory, this.girl.getId());
   }
}
