package com.cuddly.heartbound.registries;

import com.cuddly.heartbound.Heartbound;
import com.cuddly.heartbound.block.HeartboundBlocks;
import com.cuddly.heartbound.block.blocks.MagicalPumpkinBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class HeartboundDispenserBehavior {
   public static void registerDispenserBehavior() {
      Heartbound.LOGGER.info("Registering Dispenser Behavior for Heartbound");
      DispenserBlock.registerBehavior(HeartboundBlocks.MAGICAL_PUMPKIN, new FallibleItemDispenserBehavior() {
         @Override
         protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
            World world = pointer.world();
            BlockPos blockPos = pointer.pos().offset(pointer.state().get(DispenserBlock.FACING));
            MagicalPumpkinBlock magicalPumpkinBlock = (MagicalPumpkinBlock)HeartboundBlocks.MAGICAL_PUMPKIN;
            if (world.isAir(blockPos) && magicalPumpkinBlock.canDispense(world, blockPos)) {
               if (!world.isClient) {
                  world.setBlockState(blockPos, magicalPumpkinBlock.getDefaultState(), 3);
                  world.emitGameEvent(null, GameEvent.BLOCK_PLACE, blockPos);
               }

               stack.decrement(1);
               this.setSuccess(true);
            } else {
               this.setSuccess(ArmorItem.dispenseArmor(pointer, stack));
            }

            return stack;
         }
      });
   }
}
