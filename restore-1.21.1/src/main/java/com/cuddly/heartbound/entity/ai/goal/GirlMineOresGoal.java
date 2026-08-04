package com.cuddly.heartbound.entity.ai.goal;

import com.cuddly.heartbound.entity.base.tamable.TameableGirlEntity;
import com.cuddly.heartbound.networking.S2C.PlayAttackAnimationS2CPacket;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.Goal.Control;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.world.World;

public class GirlMineOresGoal extends Goal {
   private final TameableGirlEntity girl;
   private final World world;
   private final double speed;
   private BlockPos targetOrePos;
   private int miningTicks;
   private static final int MINING_DURATION = 30;
   private int miningDurationThreshold = 30;
   private int searchCooldown;
   private static final int SEARCH_COOLDOWN_DURATION = 40;
   private static final int SEARCH_RANGE = 12;
   private boolean isMining;

   public GirlMineOresGoal(TameableGirlEntity girl, double speed) {
      this.girl = girl;
      this.world = girl.getWorld();
      this.speed = speed;
      this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
   }

   @Override
   public boolean canStart() {
      if (!this.girl.isTamed()
         || !this.girl.isMining()
         || !this.girl.isFollowing()
         || this.girl.isSitting()
         || this.girl.hasVehicle()
         || this.girl.isHavingSex()) {
         return false;
      } else if (this.searchCooldown > 0) {
         this.searchCooldown--;
         return false;
      } else {
         this.targetOrePos = this.findNearbyExposedOre();
         if (this.targetOrePos != null) {
            this.searchCooldown = 40;
            return true;
         } else {
            this.searchCooldown = 40;
            return false;
         }
      }
   }

   @Override
   public boolean shouldContinue() {
      if (this.targetOrePos == null) {
         return false;
      } else {
         return this.girl.isMining() && this.girl.isFollowing() && !this.girl.isSitting() && !this.girl.hasVehicle() && !this.girl.isHavingSex()
            ? this.isMining
               || !this.girl.getNavigation().isIdle()
               || this.girl.squaredDistanceTo((double)this.targetOrePos.getX() + 0.5, (double)this.targetOrePos.getY(), (double)this.targetOrePos.getZ() + 0.5)
                  > 6.25
            : false;
      }
   }

   @Override
   public void start() {
      this.miningTicks = 0;
      this.isMining = false;
      if (this.targetOrePos != null) {
         this.girl
            .getNavigation()
            .startMovingTo((double)this.targetOrePos.getX() + 0.5, (double)this.targetOrePos.getY(), (double)this.targetOrePos.getZ() + 0.5, this.speed);
      }
   }

   @Override
   public void tick() {
      if (this.targetOrePos != null) {
         this.girl.getLookControl().lookAt((double)this.targetOrePos.getX() + 0.5, (double)this.targetOrePos.getY(), (double)this.targetOrePos.getZ() + 0.5);
         double distance = this.girl
            .squaredDistanceTo((double)this.targetOrePos.getX() + 0.5, (double)this.targetOrePos.getY(), (double)this.targetOrePos.getZ() + 0.5);
         if (this.isMining) {
            this.miningTicks++;
            if (this.miningTicks >= this.miningDurationThreshold) {
               this.mineOre();
               this.isMining = false;
               this.miningTicks = 0;
               this.targetOrePos = this.findNearbyExposedOre();
               if (this.targetOrePos != null) {
                  this.girl
                     .getNavigation()
                     .startMovingTo(
                        (double)this.targetOrePos.getX() + 0.5, (double)this.targetOrePos.getY(), (double)this.targetOrePos.getZ() + 0.5, this.speed
                     );
               }
            }
         } else if (distance <= 6.25) {
            this.girl.getNavigation().stop();
            this.miningDurationThreshold = this.computeMiningDurationForPos(this.targetOrePos, this.girl.getMainHandStack());
            this.isMining = true;
            this.miningTicks = 0;
         } else if (this.girl.getNavigation().isIdle()) {
            this.girl
               .getNavigation()
               .startMovingTo((double)this.targetOrePos.getX() + 0.5, (double)this.targetOrePos.getY(), (double)this.targetOrePos.getZ() + 0.5, this.speed);
         }
      }
   }

   @Override
   public void stop() {
      this.targetOrePos = null;
      this.miningTicks = 0;
      this.isMining = false;
      this.girl.getNavigation().stop();
   }

   private BlockPos findNearbyExposedOre() {
      BlockPos girlPos = this.girl.getBlockPos();
      Mutable mutablePos = new Mutable();
      List<BlockPos> ores = new ArrayList<>();

      for (int x = -12; x <= 12; x++) {
         for (int y = -2; y <= 4; y++) {
            for (int z = -12; z <= 12; z++) {
               mutablePos.set(girlPos.getX() + x, girlPos.getY() + y, girlPos.getZ() + z);
               if (this.isExposedOre(mutablePos)) {
                  ores.add(mutablePos.toImmutable());
               }
            }
         }
      }

      if (ores.isEmpty()) {
         return null;
      } else {
         BlockPos closest = null;
         double bestDist = Double.MAX_VALUE;

         for (BlockPos pos : ores) {
            double dist = this.girl.squaredDistanceTo((double)pos.getX() + 0.5, (double)pos.getY(), (double)pos.getZ() + 0.5);
            if (dist < bestDist && this.girl.getNavigation().findPathTo(pos, 1) != null) {
               closest = pos;
               bestDist = dist;
            }
         }

         return closest;
      }
   }

   private boolean isExposedOre(BlockPos pos) {
      BlockState state = this.world.getBlockState(pos);
      Block block = state.getBlock();
      return !this.isOreBlock(block)
         ? false
         : this.world.isAir(pos.up())
            || this.world.isAir(pos.north())
            || this.world.isAir(pos.south())
            || this.world.isAir(pos.east())
            || this.world.isAir(pos.west());
   }

   private boolean isOreBlock(Block block) {
      return block == Blocks.COAL_ORE
         || block == Blocks.IRON_ORE
         || block == Blocks.GOLD_ORE
         || block == Blocks.DIAMOND_ORE
         || block == Blocks.COPPER_ORE
         || block == Blocks.REDSTONE_ORE
         || block == Blocks.LAPIS_ORE
         || block == Blocks.EMERALD_ORE
         || block == Blocks.NETHER_QUARTZ_ORE
         || block == Blocks.DEEPSLATE_COAL_ORE
         || block == Blocks.DEEPSLATE_IRON_ORE
         || block == Blocks.DEEPSLATE_GOLD_ORE
         || block == Blocks.DEEPSLATE_DIAMOND_ORE
         || block == Blocks.DEEPSLATE_COPPER_ORE
         || block == Blocks.DEEPSLATE_REDSTONE_ORE
         || block == Blocks.DEEPSLATE_LAPIS_ORE
         || block == Blocks.DEEPSLATE_EMERALD_ORE;
   }

   private int computeMiningDurationForPos(BlockPos pos, ItemStack stack) {
      int base = 30;
      if (stack != null && !stack.isEmpty()) {
         if (stack.getItem() == Items.DIAMOND_PICKAXE || stack.getItem() == Items.NETHERITE_PICKAXE) {
            base = 20;
         }

         return base;
      } else {
         return base;
      }
   }

   private void mineOre() {
      if (this.world instanceof ServerWorld serverWorld) {
         if (this.targetOrePos != null) {
            BlockState state = this.world.getBlockState(this.targetOrePos);
            if (this.isOreBlock(state.getBlock())) {
               this.girl.triggerSwing();

               for (ServerPlayerEntity player : serverWorld.getPlayers()) {
                  ServerPlayNetworking.send(player, new PlayAttackAnimationS2CPacket(this.girl.getId()));
               }

               for (ItemStack drop : Block.getDroppedStacks(state, serverWorld, this.targetOrePos, null, this.girl, this.girl.getMainHandStack())) {
                  ItemStack stack = drop.copy();
                  boolean added = this.addToInventory(stack);
                  if (!added && !stack.isEmpty()) {
                     ItemEntity itemEntity = new ItemEntity(
                        this.world,
                        (double)this.targetOrePos.getX() + 0.5,
                        (double)this.targetOrePos.getY() + 0.5,
                        (double)this.targetOrePos.getZ() + 0.5,
                        stack
                     );
                     itemEntity.setVelocity(0.0, 0.05, 0.0);
                     itemEntity.setPickupDelay(10);
                     this.world.spawnEntity(itemEntity);
                  }
               }

               this.world.setBlockState(this.targetOrePos, Blocks.AIR.getDefaultState(), 3);
               this.world.playSound(null, this.targetOrePos, SoundEvents.BLOCK_STONE_BREAK, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }
         }
      }
   }

   private boolean addToInventory(ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      } else {
         for (int i = 5; i <= 28; i++) {
            ItemStack slotStack = this.girl.inventory.getStack(i);
            if (!slotStack.isEmpty() && ItemStack.areItemsAndComponentsEqual(slotStack, stack)) {
               int maxCount = Math.min(stack.getMaxCount(), this.girl.inventory.getMaxCount(stack));
               int available = maxCount - slotStack.getCount();
               if (available > 0) {
                  int transfer = Math.min(available, stack.getCount());
                  slotStack.increment(transfer);
                  stack.decrement(transfer);
                  this.girl.inventory.markDirty();
                  if (stack.isEmpty()) {
                     return true;
                  }
               }
            }
         }

         for (int ix = 5; ix <= 28; ix++) {
            ItemStack slotStack = this.girl.inventory.getStack(ix);
            if (slotStack.isEmpty()) {
               this.girl.inventory.setStack(ix, stack.copy());
               stack.setCount(0);
               return true;
            }
         }

         return false;
      }
   }
}
