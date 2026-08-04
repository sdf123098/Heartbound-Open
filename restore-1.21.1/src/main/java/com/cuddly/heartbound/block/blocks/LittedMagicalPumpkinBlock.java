package com.cuddly.heartbound.block.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.math.Direction;

public class LittedMagicalPumpkinBlock extends HorizontalFacingBlock {
   public static final MapCodec<LittedMagicalPumpkinBlock> CODEC = createCodec(LittedMagicalPumpkinBlock::new);
   public static final EnumProperty<Direction> FACING = HorizontalFacingBlock.FACING;

   @Override
   public MapCodec<? extends LittedMagicalPumpkinBlock> getCodec() {
      return CODEC;
   }

   public LittedMagicalPumpkinBlock(Settings settings) {
      super(settings);
      this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
   }

   @Override
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
   }

   @Override
   protected void appendProperties(Builder<Block, BlockState> builder) {
      builder.add(FACING);
   }
}
