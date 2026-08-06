package com.cuddly.heartbound.registries;

import com.cuddly.heartbound.Heartbound;
import com.cuddly.heartbound.block.HeartboundBlocks;
import com.cuddly.heartbound.block.blocks.MagicalPumpkinBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.EquipmentDispenseItemBehavior;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.gameevent.GameEvent;

public class HeartboundDispenserBehavior {
   public static void registerDispenserBehavior() {
      Heartbound.LOGGER.info("Registering Dispenser Behavior for Heartbound");
      DispenserBlock.registerBehavior(HeartboundBlocks.MAGICAL_PUMPKIN, new OptionalDispenseItemBehavior() {
         @Override
         protected ItemStack execute(BlockSource pointer, ItemStack stack) {
            Level world = pointer.level();
            BlockPos blockPos = pointer.pos().relative(pointer.state().getValue(DispenserBlock.FACING));
            MagicalPumpkinBlock magicalPumpkinBlock = (MagicalPumpkinBlock)HeartboundBlocks.MAGICAL_PUMPKIN;
            if (world.isEmptyBlock(blockPos) && magicalPumpkinBlock.canDispense(world, blockPos)) {
               if (!world.isClientSide()) {
                  world.setBlock(blockPos, magicalPumpkinBlock.defaultBlockState(), 3);
                  world.gameEvent(null, GameEvent.BLOCK_PLACE, blockPos);
               }

               stack.shrink(1);
               this.setSuccess(true);
            } else {
               this.setSuccess(EquipmentDispenseItemBehavior.dispenseEquipment(pointer, stack));
            }

            return stack;
         }
      });
   }
}
