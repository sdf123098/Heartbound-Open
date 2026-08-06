package com.cuddly.heartbound.block.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class LittedMagicalPumpkinBlock extends HorizontalDirectionalBlock {
   public static final MapCodec<LittedMagicalPumpkinBlock> CODEC = simpleCodec(LittedMagicalPumpkinBlock::new);
   public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

   @Override
   public MapCodec<? extends LittedMagicalPumpkinBlock> codec() {
      return CODEC;
   }

   public LittedMagicalPumpkinBlock(Properties settings) {
      super(settings);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext ctx) {
      return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(FACING);
   }
}
