package com.cuddly.heartbound.block.blocks;

import com.cuddly.heartbound.block.HeartboundBlocks;
import com.cuddly.heartbound.entity.girls.CoppieEntity;
import com.cuddly.heartbound.registries.GirlRegistry;
import com.mojang.serialization.MapCodec;
import java.util.function.Predicate;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.block.pattern.BlockPattern.Result;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.predicate.block.BlockStatePredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class MagicalPumpkinBlock extends HorizontalFacingBlock {
   public static final MapCodec<MagicalPumpkinBlock> CODEC = createCodec(MagicalPumpkinBlock::new);
   public static final EnumProperty<Direction> FACING = HorizontalFacingBlock.FACING;
   @Nullable
   private BlockPattern copperGirlDispenserPattern;
   @Nullable
   private BlockPattern copperGirlPattern;
   private static final Predicate<BlockState> IS_GOLEM_HEAD_PREDICATE = state -> state != null && state.isOf(HeartboundBlocks.MAGICAL_PUMPKIN);

   @Override
   public MapCodec<? extends MagicalPumpkinBlock> getCodec() {
      return CODEC;
   }

   public MagicalPumpkinBlock(Settings settings) {
      super(settings);
      this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
   }

   @Override
   protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
      if (!oldState.isOf(state.getBlock())) {
         this.trySpawnEntity(world, pos);
      }
   }

   public boolean canDispense(WorldView world, BlockPos pos) {
      return this.getCopperGirlDispenserPattern().searchAround(world, pos) != null;
   }

   private void trySpawnEntity(World world, BlockPos pos) {
      Result result = this.getCopperGirlPattern().searchAround(world, pos);
      if (result != null) {
         CoppieEntity coppie = GirlRegistry.COPPIE.create(world);
         if (coppie != null) {
            spawnEntity(world, result, coppie, result.translate(0, 1, 0).getBlockPos());
         }
      }
   }

   private static void spawnEntity(World world, Result patternResult, Entity entity, BlockPos pos) {
      breakPatternBlocks(world, patternResult);
      entity.refreshPositionAndAngles((double)pos.getX() + 0.5, (double)pos.getY() + 0.05, (double)pos.getZ() + 0.5, 0.0F, 0.0F);
      world.spawnEntity(entity);

      for (ServerPlayerEntity serverPlayerEntity : world.getNonSpectatingEntities(ServerPlayerEntity.class, entity.getBoundingBox().expand(5.0))) {
         Criteria.SUMMONED_ENTITY.trigger(serverPlayerEntity, entity);
      }

      updatePatternBlocks(world, patternResult);
   }

   public static void breakPatternBlocks(World world, Result patternResult) {
      for (int i = 0; i < patternResult.getWidth(); i++) {
         for (int j = 0; j < patternResult.getHeight(); j++) {
            CachedBlockPosition cachedBlockPosition = patternResult.translate(i, j, 0);
            world.setBlockState(cachedBlockPosition.getBlockPos(), Blocks.AIR.getDefaultState(), 2);
            world.syncWorldEvent(2001, cachedBlockPosition.getBlockPos(), Block.getRawIdFromState(cachedBlockPosition.getBlockState()));
         }
      }
   }

   public static void updatePatternBlocks(World world, Result patternResult) {
      for (int i = 0; i < patternResult.getWidth(); i++) {
         for (int j = 0; j < patternResult.getHeight(); j++) {
            CachedBlockPosition cachedBlockPosition = patternResult.translate(i, j, 0);
            world.updateNeighbors(cachedBlockPosition.getBlockPos(), Blocks.AIR);
         }
      }
   }

   @Override
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
   }

   @Override
   protected void appendProperties(Builder<Block, BlockState> builder) {
      builder.add(FACING);
   }

   private BlockPattern getCopperGirlDispenserPattern() {
      if (this.copperGirlDispenserPattern == null) {
         this.copperGirlDispenserPattern = BlockPatternBuilder.start()
            .aisle(" ", "#")
            .where('#', CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(Blocks.COPPER_BLOCK)))
            .build();
      }

      return this.copperGirlDispenserPattern;
   }

   private BlockPattern getCopperGirlPattern() {
      if (this.copperGirlPattern == null) {
         this.copperGirlPattern = BlockPatternBuilder.start()
            .aisle("^", "#")
            .where('^', CachedBlockPosition.matchesBlockState(IS_GOLEM_HEAD_PREDICATE))
            .where('#', CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(Blocks.COPPER_BLOCK)))
            .build();
      }

      return this.copperGirlPattern;
   }
}
