package com.cuddly.heartbound.block.blocks;

import com.cuddly.heartbound.Heartbound;
import com.cuddly.heartbound.block.entity.HeartboundBlockEntities;
import com.cuddly.heartbound.block.entity.entities.FusionTableBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class FusionTableBlock extends BaseEntityBlock implements EntityBlock {
   public static final MapCodec<FusionTableBlock> CODEC = simpleCodec(FusionTableBlock::new);

   public FusionTableBlock(Properties settings) {
      super(settings);
   }

   @Override
   protected MapCodec<? extends BaseEntityBlock> codec() {
      return CODEC;
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new FusionTableBlockEntity(pos, state);
   }

   @Nullable
   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
      return createTickerHelper(type, HeartboundBlockEntities.FUSION_TABLE_BLOCK_ENTITY, FusionTableBlockEntity::tick);
   }

   @Override
   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   @Override
   public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
      Heartbound.LOGGER.info("Fusion Table onUse called at position: " + pos);
      if (world.isClientSide()) {
         return InteractionResult.SUCCESS;
      } else {
         BlockEntity be = world.getBlockEntity(pos);
         Heartbound.LOGGER.info("Block entity: " + be);
         if (be instanceof FusionTableBlockEntity fusionTable) {
            Heartbound.LOGGER.info("Opening Fusion Table GUI");
            fusionTable.openGui((ServerLevel)world, (ServerPlayer)player);
         }

         return InteractionResult.SUCCESS;
      }
   }
}
