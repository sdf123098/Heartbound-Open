package com.cuddly.heartbound.entity.ai.goal;

import com.cuddly.heartbound.entity.base.tamable.TameableGirlEntity;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class GirlDepositItemsGoal extends Goal {
   private final TameableGirlEntity girl;
   private final double speed;
   private BlockPos targetChest;
   private int depositDelay;
   private int movingTicks;
   private static final int DEPOSIT_COOLDOWN = 100;
   private static final int MOVING_TIMEOUT = 100;
   private static final double SEARCH_RANGE = 12.0;
   private static final double DEPOSIT_RANGE = 3.0;
   private static final List<Item> VALUABLE_FOOD_ITEMS = List.of(
      Items.COOKED_BEEF,
      Items.COOKED_PORKCHOP,
      Items.COOKED_CHICKEN,
      Items.COOKED_MUTTON,
      Items.COOKED_RABBIT,
      Items.COOKED_COD,
      Items.COOKED_SALMON,
      Items.GOLDEN_APPLE,
      Items.ENCHANTED_GOLDEN_APPLE,
      Items.GOLDEN_CARROT
   );

   public GirlDepositItemsGoal(TameableGirlEntity girl, double speed) {
      this.girl = girl;
      this.speed = speed;
      this.setFlags(EnumSet.of(Flag.MOVE));
   }

   @Override
   public boolean canUse() {
      if (this.girl.isTamed()
         && this.girl.isRoaming()
         && !this.girl.isFollowing()
         && !this.girl.isSitting()
         && !this.girl.isPassenger()
         && !this.girl.isHavingSex()
         && this.depositDelay <= 0) {
         if (!this.hasItemsToDeposit()) {
            return false;
         } else {
            this.targetChest = this.findNearbyChest();
            return this.targetChest != null;
         }
      } else {
         if (this.depositDelay > 0) {
            this.depositDelay--;
         }

         return false;
      }
   }

   @Override
   public boolean canContinueToUse() {
      if (this.targetChest == null) {
         return false;
      } else {
         return this.girl.isRoaming() && !this.girl.isFollowing() && !this.girl.isSitting() && !this.girl.isPassenger() && !this.girl.isHavingSex()
            ? this.targetChest != null
            : false;
      }
   }

   @Override
   public void start() {
      this.movingTicks = 0;
      if (this.targetChest != null) {
         this.girl
            .getNavigation()
            .moveTo((double)this.targetChest.getX() + 0.5, (double)this.targetChest.getY(), (double)this.targetChest.getZ() + 0.5, this.speed);
      }
   }

   @Override
   public void tick() {
      if (this.targetChest != null) {
         this.girl
            .getLookControl()
            .setLookAt((double)this.targetChest.getX() + 0.5, (double)this.targetChest.getY() + 0.5, (double)this.targetChest.getZ() + 0.5, 30.0F, 30.0F);
         this.movingTicks++;
         double distance = this.girl
            .distanceToSqr((double)this.targetChest.getX() + 0.5, (double)this.targetChest.getY(), (double)this.targetChest.getZ() + 0.5);
         if (distance <= 9.0) {
            try {
               this.depositItems();
            } catch (Exception var4) {
               var4.printStackTrace();
               this.targetChest = null;
               this.depositDelay = 100;
            }
         } else if (this.movingTicks >= 100) {
            this.targetChest = null;
         } else if (this.girl.getNavigation().isDone()) {
            this.girl
               .getNavigation()
               .moveTo((double)this.targetChest.getX() + 0.5, (double)this.targetChest.getY(), (double)this.targetChest.getZ() + 0.5, this.speed);
         }
      }
   }

   @Override
   public void stop() {
      this.targetChest = null;
      this.movingTicks = 0;
      this.girl.getNavigation().stop();
      this.depositDelay = 100;
   }

   private boolean hasItemsToDeposit() {
      for (int i = 5; i <= 28; i++) {
         ItemStack stack = this.girl.inventory.getItem(i);
         if (!stack.isEmpty() && !this.isValuableFood(stack)) {
            return true;
         }
      }

      return false;
   }

   private BlockPos findNearbyChest() {
      Level world = this.girl.level();
      BlockPos girlPos = this.girl.blockPosition();
      AABB searchBox = new AABB(
         (double)girlPos.getX() - 12.0,
         (double)(girlPos.getY() - 2),
         (double)girlPos.getZ() - 12.0,
         (double)girlPos.getX() + 12.0,
         (double)(girlPos.getY() + 2),
         (double)girlPos.getZ() + 12.0
      );
      double yawRad = Math.toRadians((double)this.girl.getYRot());
      double lookX = -Math.sin(yawRad);
      double lookZ = Math.cos(yawRad);
      BlockPos bestChest = null;
      double bestScore = Double.MAX_VALUE;

      for (BlockPos pos : BlockPos.betweenClosed(
         (int)searchBox.minX, (int)searchBox.minY, (int)searchBox.minZ, (int)searchBox.maxX, (int)searchBox.maxY, (int)searchBox.maxZ
      )) {
         BlockState state = world.getBlockState(pos);
         if (state.getBlock() instanceof ChestBlock) {
            double dist = girlPos.distSqr(pos);
            double dx = (double)pos.getX() + 0.5 - this.girl.getX();
            double dz = (double)pos.getZ() + 0.5 - this.girl.getZ();
            double len = Math.sqrt(dx * dx + dz * dz);
            double dot = len > 0.01 ? (dx * lookX + dz * lookZ) / len : 0.0;
            double directionMultiplier = 1.0 - dot * 0.75;
            double score = dist * directionMultiplier;
            if (score < bestScore) {
               bestScore = score;
               bestChest = pos.immutable();
            }
         }
      }

      return bestChest;
   }

   private BlockPos findAnotherChest(Set<BlockPos> excludeChests) {
      Level world = this.girl.level();
      BlockPos girlPos = this.girl.blockPosition();
      AABB searchBox = new AABB(
         (double)girlPos.getX() - 12.0,
         (double)(girlPos.getY() - 2),
         (double)girlPos.getZ() - 12.0,
         (double)girlPos.getX() + 12.0,
         (double)(girlPos.getY() + 2),
         (double)girlPos.getZ() + 12.0
      );
      double yawRad = Math.toRadians((double)this.girl.getYRot());
      double lookX = -Math.sin(yawRad);
      double lookZ = Math.cos(yawRad);
      BlockPos bestChest = null;
      double bestScore = Double.MAX_VALUE;

      for (BlockPos pos : BlockPos.betweenClosed(
         (int)searchBox.minX, (int)searchBox.minY, (int)searchBox.minZ, (int)searchBox.maxX, (int)searchBox.maxY, (int)searchBox.maxZ
      )) {
         if (!excludeChests.contains(pos)) {
            BlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof ChestBlock) {
               double dist = girlPos.distSqr(pos);
               double dx = (double)pos.getX() + 0.5 - this.girl.getX();
               double dz = (double)pos.getZ() + 0.5 - this.girl.getZ();
               double len = Math.sqrt(dx * dx + dz * dz);
               double dot = len > 0.01 ? (dx * lookX + dz * lookZ) / len : 0.0;
               double directionMultiplier = 1.0 - dot * 0.75;
               double score = dist * directionMultiplier;
               if (score < bestScore) {
                  bestScore = score;
                  bestChest = pos.immutable();
               }
            }
         }
      }

      return bestChest;
   }

   private void depositItems() {
      if (this.targetChest != null) {
         Level world = this.girl.level();
         boolean depositedToThisChest = this.depositToChest(this.targetChest);
         if (depositedToThisChest) {
            this.playDepositSound(world, this.targetChest);
         }

         if (this.hasItemsToDeposit()) {
            Set<BlockPos> tried = new HashSet<>();
            tried.add(this.targetChest);
            BlockPos nextChest = this.findAnotherChest(tried);
            if (nextChest != null) {
               this.targetChest = nextChest;
               this.movingTicks = 0;
               this.girl.getNavigation().moveTo((double)nextChest.getX() + 0.5, (double)nextChest.getY(), (double)nextChest.getZ() + 0.5, this.speed);
               return;
            }
         }

         this.targetChest = null;
      }
   }

   private boolean depositToChest(BlockPos chestPos) {
      try {
         return !(this.girl.level().getBlockEntity(chestPos) instanceof ChestBlockEntity chest) ? false : this.tryDepositToChest(chest);
      } catch (Exception var4) {
         var4.printStackTrace();
         return false;
      }
   }

   private void playDepositSound(Level world, BlockPos chestPos) {
      world.playSound(
         null,
         (double)chestPos.getX() + 0.5,
         (double)chestPos.getY() + 0.5,
         (double)chestPos.getZ() + 0.5,
         SoundEvents.CHEST_CLOSE,
         this.girl.getSoundSource(),
         0.5F,
         world.getRandom().nextFloat() * 0.1F + 0.9F
      );
   }

   private boolean tryDepositToChest(Container chestInventory) {
      boolean depositedAny = false;

      for (int i = 5; i <= 28; i++) {
         ItemStack stack = this.girl.inventory.getItem(i);
         if (!stack.isEmpty() && !this.isValuableFood(stack)) {
            ItemStack remaining = this.depositStackToChest(chestInventory, stack);
            this.girl.inventory.setItem(i, remaining);
            if (remaining.getCount() < stack.getCount()) {
               depositedAny = true;
            }
         }
      }

      return depositedAny;
   }

   private ItemStack depositStackToChest(Container chestInventory, ItemStack stack) {
      ItemStack remaining = stack.copy();
      remaining = this.tryMergeWithExistingStacks(chestInventory, remaining);
      return remaining.isEmpty() ? ItemStack.EMPTY : this.tryPlaceInEmptySlots(chestInventory, remaining);
   }

   private ItemStack tryMergeWithExistingStacks(Container chestInventory, ItemStack stack) {
      ItemStack remaining = stack.copy();

      for (int i = 0; i < chestInventory.getContainerSize(); i++) {
         ItemStack chestStack = chestInventory.getItem(i);
         if (!chestStack.isEmpty() && ItemStack.isSameItemSameComponents(chestStack, remaining)) {
            int maxCount = Math.min(chestStack.getMaxStackSize(), remaining.getMaxStackSize());
            int available = maxCount - chestStack.getCount();
            if (available > 0) {
               int transfer = Math.min(available, remaining.getCount());
               chestStack.grow(transfer);
               remaining.shrink(transfer);
               chestInventory.setChanged();
               if (remaining.isEmpty()) {
                  return ItemStack.EMPTY;
               }
            }
         }
      }

      return remaining;
   }

   private boolean isValuableFood(ItemStack stack) {
      return stack != null && !stack.isEmpty() ? VALUABLE_FOOD_ITEMS.contains(stack.getItem()) : false;
   }

   private ItemStack tryPlaceInEmptySlots(Container chestInventory, ItemStack stack) {
      ItemStack remaining = stack.copy();

      for (int i = 0; i < chestInventory.getContainerSize(); i++) {
         ItemStack chestStack = chestInventory.getItem(i);
         if (chestStack.isEmpty()) {
            int transfer = Math.min(remaining.getCount(), remaining.getMaxStackSize());
            if (transfer > 0) {
               ItemStack toDeposit = remaining.copy();
               toDeposit.setCount(transfer);
               chestInventory.setItem(i, toDeposit);
               remaining.shrink(transfer);
               chestInventory.setChanged();
               if (remaining.isEmpty()) {
                  return ItemStack.EMPTY;
               }
            }
         }
      }

      return remaining;
   }
}
