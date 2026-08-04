package com.cuddly.heartbound.screen;

import com.cuddly.heartbound.HeartboundClient;
import com.cuddly.heartbound.entity.base.GirlEntity;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class GirlInventoryScreenHandlerFactory implements ExtendedScreenHandlerFactory {
   private final GirlEntity girl;

   public GirlInventoryScreenHandlerFactory(GirlEntity girl) {
      this.girl = girl;
   }

   public Object getScreenOpeningData(ServerPlayerEntity player) {
      return new HeartboundClient.GirlScreenData(this.girl.getId());
   }

   public Text getDisplayName() {
      return Text.literal("");
   }

   @Nullable
   public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
      return new GirlInventoryScreenHandler(syncId, playerInventory, this.girl.getId());
   }
}
