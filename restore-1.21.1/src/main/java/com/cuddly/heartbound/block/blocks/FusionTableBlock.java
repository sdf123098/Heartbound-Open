package com.cuddly.heartbound.block.blocks;

import com.cuddly.heartbound.Heartbound;
import com.cuddly.heartbound.block.entity.HeartboundBlockEntities;
import com.cuddly.heartbound.block.entity.entities.FusionTableBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class FusionTableBlock extends BlockWithEntity implements BlockEntityProvider {
   public static final MapCodec<FusionTableBlock> CODEC = createCodec(FusionTableBlock::new);

   public FusionTableBlock(Settings settings) {
      super(settings);
   }

   @Override
   protected MapCodec<? extends BlockWithEntity> getCodec() {
      return CODEC;
   }

   @Nullable
   @Override
   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return new FusionTableBlockEntity(pos, state);
   }

   @Nullable
   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
      return validateTicker(type, HeartboundBlockEntities.FUSION_TABLE_BLOCK_ENTITY, FusionTableBlockEntity::tick);
   }

   @Override
   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.MODEL;
   }

   @Override
   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
      Heartbound.LOGGER.info("Fusion Table onUse called at position: " + pos);
      if (world.isClient) {
         return ActionResult.SUCCESS;
      } else {
         BlockEntity be = world.getBlockEntity(pos);
         Heartbound.LOGGER.info("Block entity: " + be);
         if (be instanceof FusionTableBlockEntity fusionTable) {
            Heartbound.LOGGER.info("Opening Fusion Table GUI");
            fusionTable.openGui((ServerWorld)world, (ServerPlayerEntity)player);
         }

         return ActionResult.SUCCESS;
      }
   }
}
