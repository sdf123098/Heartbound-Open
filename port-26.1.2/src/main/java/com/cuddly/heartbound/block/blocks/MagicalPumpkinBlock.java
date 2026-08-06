package com.cuddly.heartbound.block.blocks;

import com.cuddly.heartbound.block.HeartboundBlocks;
import com.cuddly.heartbound.entity.girls.CoppieEntity;
import com.cuddly.heartbound.registries.GirlRegistry;
import com.mojang.serialization.MapCodec;
import java.util.function.Predicate;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPattern.BlockPatternMatch;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import org.jetbrains.annotations.Nullable;

public class MagicalPumpkinBlock extends HorizontalDirectionalBlock {
   public static final MapCodec<MagicalPumpkinBlock> CODEC = simpleCodec(MagicalPumpkinBlock::new);
   public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
   @Nullable
   private BlockPattern copperGirlDispenserPattern;
   @Nullable
   private BlockPattern copperGirlPattern;
   private static final Predicate<BlockState> IS_GOLEM_HEAD_PREDICATE = state -> state != null && state.is(HeartboundBlocks.MAGICAL_PUMPKIN);

   @Override
   public MapCodec<? extends MagicalPumpkinBlock> codec() {
      return CODEC;
   }

   public MagicalPumpkinBlock(Properties settings) {
      super(settings);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
   }

   @Override
   protected void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify) {
      if (!oldState.is(state.getBlock())) {
         this.trySpawnEntity(world, pos);
      }
   }

   public boolean canDispense(LevelReader world, BlockPos pos) {
      return this.getCopperGirlDispenserPattern().find(world, pos) != null;
   }

   private void trySpawnEntity(Level world, BlockPos pos) {
      BlockPatternMatch result = this.getCopperGirlPattern().find(world, pos);
      if (result != null) {
         CoppieEntity coppie = GirlRegistry.COPPIE.create(world, EntitySpawnReason.STRUCTURE);
         if (coppie != null) {
            spawnEntity(world, result, coppie, result.getBlock(0, 1, 0).getPos());
         }
      }
   }

   private static void spawnEntity(Level world, BlockPatternMatch patternResult, Entity entity, BlockPos pos) {
      breakPatternBlocks(world, patternResult);
      entity.teleportTo((double)pos.getX() + 0.5, (double)pos.getY() + 0.05, (double)pos.getZ() + 0.5);
      world.addFreshEntity(entity);

      for (ServerPlayer serverPlayerEntity : world.getEntitiesOfClass(ServerPlayer.class, entity.getBoundingBox().inflate(5.0))) {
         CriteriaTriggers.SUMMONED_ENTITY.trigger(serverPlayerEntity, entity);
      }

      updatePatternBlocks(world, patternResult);
   }

   public static void breakPatternBlocks(Level world, BlockPatternMatch patternResult) {
      for (int i = 0; i < patternResult.getWidth(); i++) {
         for (int j = 0; j < patternResult.getHeight(); j++) {
            BlockInWorld cachedBlockPosition = patternResult.getBlock(i, j, 0);
            world.setBlock(cachedBlockPosition.getPos(), Blocks.AIR.defaultBlockState(), 2);
            world.levelEvent(2001, cachedBlockPosition.getPos(), Block.getId(cachedBlockPosition.getState()));
         }
      }
   }

   public static void updatePatternBlocks(Level world, BlockPatternMatch patternResult) {
      for (int i = 0; i < patternResult.getWidth(); i++) {
         for (int j = 0; j < patternResult.getHeight(); j++) {
            BlockInWorld cachedBlockPosition = patternResult.getBlock(i, j, 0);
            world.updateNeighborsAt(cachedBlockPosition.getPos(), Blocks.AIR, Orientation.of(Direction.NORTH, Direction.UP, Orientation.SideBias.LEFT));
         }
      }
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext ctx) {
      return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(FACING);
   }

   private BlockPattern getCopperGirlDispenserPattern() {
      if (this.copperGirlDispenserPattern == null) {
         this.copperGirlDispenserPattern = BlockPatternBuilder.start()
            .aisle(" ", "#")
            .where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.COPPER_BLOCK)))
            .build();
      }

      return this.copperGirlDispenserPattern;
   }

   private BlockPattern getCopperGirlPattern() {
      if (this.copperGirlPattern == null) {
         this.copperGirlPattern = BlockPatternBuilder.start()
            .aisle("^", "#")
            .where('^', BlockInWorld.hasState(IS_GOLEM_HEAD_PREDICATE))
            .where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.COPPER_BLOCK)))
            .build();
      }

      return this.copperGirlPattern;
   }
}
