package com.cuddly.heartbound.block.entity.entities;

import com.cuddly.heartbound.Heartbound;
import com.cuddly.heartbound.block.entity.HeartboundBlockEntities;
import com.cuddly.heartbound.registries.HeartboundScreenHandlerRegistry;
import com.cuddly.heartbound.screen.FusionTableScreenHandler;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FusionTableBlockEntity extends BlockEntity {
   public FusionTableBlockEntity(BlockPos pos, BlockState state) {
      super(HeartboundBlockEntities.FUSION_TABLE_BLOCK_ENTITY, pos, state);
   }

   @Nullable
   @Override
   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   @Override
   public CompoundTag getUpdateTag(Provider registryLookup) {
      return this.saveWithoutMetadata(registryLookup);
   }

   public void openGui(ServerLevel world, ServerPlayer player) {
      Heartbound.LOGGER.info("FusionTableBlockEntity.openGui called for player: " + player.getName().getString());
      player.openMenu(new ExtendedMenuProvider<Object>() {
         public Component getDisplayName() {
            return Component.translatable("screen.heartbound.fusion_table");
         }

         public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
            Heartbound.LOGGER.info("Creating FusionTableScreenHandler with syncId: " + syncId);
            return new FusionTableScreenHandler(syncId, playerInventory, null);
         }

         public Object getScreenOpeningData(ServerPlayer player) {
            return HeartboundScreenHandlerRegistry.EMPTY_DATA;
         }
      });
   }

   public static void tick(Level world, BlockPos pos, BlockState state, FusionTableBlockEntity be) {
   }
}
