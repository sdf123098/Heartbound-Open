package com.cuddly.heartbound.block.entity.entities;

import com.cuddly.heartbound.Heartbound;
import com.cuddly.heartbound.block.entity.HeartboundBlockEntities;
import com.cuddly.heartbound.registries.HeartboundScreenHandlerRegistry;
import com.cuddly.heartbound.screen.FusionTableScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class FusionTableBlockEntity extends BlockEntity {
   public FusionTableBlockEntity(BlockPos pos, BlockState state) {
      super(HeartboundBlockEntities.FUSION_TABLE_BLOCK_ENTITY, pos, state);
   }

   @Nullable
   @Override
   public Packet<ClientPlayPacketListener> toUpdatePacket() {
      return BlockEntityUpdateS2CPacket.create(this);
   }

   @Override
   public NbtCompound toInitialChunkDataNbt(WrapperLookup registryLookup) {
      return this.createNbt(registryLookup);
   }

   public void openGui(ServerWorld world, ServerPlayerEntity player) {
      Heartbound.LOGGER.info("FusionTableBlockEntity.openGui called for player: " + player.getName().getString());
      player.openHandledScreen(new ExtendedScreenHandlerFactory() {
         public Text getDisplayName() {
            return Text.translatable("screen.heartbound.fusion_table");
         }

         public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
            Heartbound.LOGGER.info("Creating FusionTableScreenHandler with syncId: " + syncId);
            return new FusionTableScreenHandler(syncId, playerInventory, null);
         }

         public Object getScreenOpeningData(ServerPlayerEntity player) {
            return HeartboundScreenHandlerRegistry.EMPTY_DATA;
         }
      });
   }

   public static void tick(World world, BlockPos pos, BlockState state, FusionTableBlockEntity be) {
   }
}
