package com.cuddly.heartbound.entity.ai.goal;

import com.cuddly.heartbound.entity.base.tamable.TameableGirlEntity;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.Goal.Control;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

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
      this.setControls(EnumSet.of(Control.MOVE));
   }

   @Override
   public boolean canStart() {
      if (this.girl.isTamed()
         && this.girl.isRoaming()
         && !this.girl.isFollowing()
         && !this.girl.isSitting()
         && !this.girl.hasVehicle()
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
   public boolean shouldContinue() {
      if (this.targetChest == null) {
         return false;
      } else {
         return this.girl.isRoaming() && !this.girl.isFollowing() && !this.girl.isSitting() && !this.girl.hasVehicle() && !this.girl.isHavingSex()
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
            .startMovingTo((double)this.targetChest.getX() + 0.5, (double)this.targetChest.getY(), (double)this.targetChest.getZ() + 0.5, this.speed);
      }
   }

   @Override
   public void tick() {
      if (this.targetChest != null) {
         this.girl
            .getLookControl()
            .lookAt((double)this.targetChest.getX() + 0.5, (double)this.targetChest.getY() + 0.5, (double)this.targetChest.getZ() + 0.5, 30.0F, 30.0F);
         this.movingTicks++;
         double distance = this.girl
            .squaredDistanceTo((double)this.targetChest.getX() + 0.5, (double)this.targetChest.getY(), (double)this.targetChest.getZ() + 0.5);
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
         } else if (this.girl.getNavigation().isIdle()) {
            this.girl
               .getNavigation()
               .startMovingTo((double)this.targetChest.getX() + 0.5, (double)this.targetChest.getY(), (double)this.targetChest.getZ() + 0.5, this.speed);
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
         ItemStack stack = this.girl.inventory.getStack(i);
         if (!stack.isEmpty() && !this.isValuableFood(stack)) {
            return true;
         }
      }

      return false;
   }

   private BlockPos findNearbyChest() {
      World world = this.girl.getWorld();
      BlockPos girlPos = this.girl.getBlockPos();
      Box searchBox = new Box(
         (double)girlPos.getX() - 12.0,
         (double)(girlPos.getY() - 2),
         (double)girlPos.getZ() - 12.0,
         (double)girlPos.getX() + 12.0,
         (double)(girlPos.getY() + 2),
         (double)girlPos.getZ() + 12.0
      );
      double yawRad = Math.toRadians((double)this.girl.getYaw());
      double lookX = -Math.sin(yawRad);
      double lookZ = Math.cos(yawRad);
      BlockPos bestChest = null;
      double bestScore = Double.MAX_VALUE;

      for (BlockPos pos : BlockPos.iterate(
         (int)searchBox.minX, (int)searchBox.minY, (int)searchBox.minZ, (int)searchBox.maxX, (int)searchBox.maxY, (int)searchBox.maxZ
      )) {
         BlockState state = world.getBlockState(pos);
         if (state.getBlock() instanceof ChestBlock) {
            double dist = girlPos.getSquaredDistance(pos);
            double dx = (double)pos.getX() + 0.5 - this.girl.getX();
            double dz = (double)pos.getZ() + 0.5 - this.girl.getZ();
            double len = Math.sqrt(dx * dx + dz * dz);
            double dot = len > 0.01 ? (dx * lookX + dz * lookZ) / len : 0.0;
            double directionMultiplier = 1.0 - dot * 0.75;
            double score = dist * directionMultiplier;
            if (score < bestScore) {
               bestScore = score;
               bestChest = pos.toImmutable();
            }
         }
      }

      return bestChest;
   }

   private BlockPos findAnotherChest(Set<BlockPos> excludeChests) {
      World world = this.girl.getWorld();
      BlockPos girlPos = this.girl.getBlockPos();
      Box searchBox = new Box(
         (double)girlPos.getX() - 12.0,
         (double)(girlPos.getY() - 2),
         (double)girlPos.getZ() - 12.0,
         (double)girlPos.getX() + 12.0,
         (double)(girlPos.getY() + 2),
         (double)girlPos.getZ() + 12.0
      );
      double yawRad = Math.toRadians((double)this.girl.getYaw());
      double lookX = -Math.sin(yawRad);
      double lookZ = Math.cos(yawRad);
      BlockPos bestChest = null;
      double bestScore = Double.MAX_VALUE;

      for (BlockPos pos : BlockPos.iterate(
         (int)searchBox.minX, (int)searchBox.minY, (int)searchBox.minZ, (int)searchBox.maxX, (int)searchBox.maxY, (int)searchBox.maxZ
      )) {
         if (!excludeChests.contains(pos)) {
            BlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof ChestBlock) {
               double dist = girlPos.getSquaredDistance(pos);
               double dx = (double)pos.getX() + 0.5 - this.girl.getX();
               double dz = (double)pos.getZ() + 0.5 - this.girl.getZ();
               double len = Math.sqrt(dx * dx + dz * dz);
               double dot = len > 0.01 ? (dx * lookX + dz * lookZ) / len : 0.0;
               double directionMultiplier = 1.0 - dot * 0.75;
               double score = dist * directionMultiplier;
               if (score < bestScore) {
                  bestScore = score;
                  bestChest = pos.toImmutable();
               }
            }
         }
      }

      return bestChest;
   }

   private void depositItems() {
      if (this.targetChest != null) {
         World world = this.girl.getWorld();
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
               this.girl.getNavigation().startMovingTo((double)nextChest.getX() + 0.5, (double)nextChest.getY(), (double)nextChest.getZ() + 0.5, this.speed);
               return;
            }
         }

         this.targetChest = null;
      }
   }

   private boolean depositToChest(BlockPos chestPos) {
      try {
         return !(this.girl.getWorld().getBlockEntity(chestPos) instanceof ChestBlockEntity chest) ? false : this.tryDepositToChest(chest);
      } catch (Exception var4) {
         var4.printStackTrace();
         return false;
      }
   }

   private void playDepositSound(World world, BlockPos chestPos) {
      world.playSound(
         null,
         (double)chestPos.getX() + 0.5,
         (double)chestPos.getY() + 0.5,
         (double)chestPos.getZ() + 0.5,
         SoundEvents.BLOCK_CHEST_CLOSE,
         this.girl.getSoundCategory(),
         0.5F,
         world.random.nextFloat() * 0.1F + 0.9F
      );
   }

   private boolean tryDepositToChest(Inventory chestInventory) {
      boolean depositedAny = false;

      for (int i = 5; i <= 28; i++) {
         ItemStack stack = this.girl.inventory.getStack(i);
         if (!stack.isEmpty() && !this.isValuableFood(stack)) {
            ItemStack remaining = this.depositStackToChest(chestInventory, stack);
            this.girl.inventory.setStack(i, remaining);
            if (remaining.getCount() < stack.getCount()) {
               depositedAny = true;
            }
         }
      }

      return depositedAny;
   }

   private ItemStack depositStackToChest(Inventory chestInventory, ItemStack stack) {
      ItemStack remaining = stack.copy();
      remaining = this.tryMergeWithExistingStacks(chestInventory, remaining);
      return remaining.isEmpty() ? ItemStack.EMPTY : this.tryPlaceInEmptySlots(chestInventory, remaining);
   }

   private ItemStack tryMergeWithExistingStacks(Inventory chestInventory, ItemStack stack) {
      ItemStack remaining = stack.copy();

      for (int i = 0; i < chestInventory.size(); i++) {
         ItemStack chestStack = chestInventory.getStack(i);
         if (!chestStack.isEmpty() && ItemStack.areItemsAndComponentsEqual(chestStack, remaining)) {
            int maxCount = Math.min(chestStack.getMaxCount(), remaining.getMaxCount());
            int available = maxCount - chestStack.getCount();
            if (available > 0) {
               int transfer = Math.min(available, remaining.getCount());
               chestStack.increment(transfer);
               remaining.decrement(transfer);
               chestInventory.markDirty();
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

   private ItemStack tryPlaceInEmptySlots(Inventory chestInventory, ItemStack stack) {
      ItemStack remaining = stack.copy();

      for (int i = 0; i < chestInventory.size(); i++) {
         ItemStack chestStack = chestInventory.getStack(i);
         if (chestStack.isEmpty()) {
            int transfer = Math.min(remaining.getCount(), remaining.getMaxCount());
            if (transfer > 0) {
               ItemStack toDeposit = remaining.copy();
               toDeposit.setCount(transfer);
               chestInventory.setStack(i, toDeposit);
               remaining.decrement(transfer);
               chestInventory.markDirty();
               if (remaining.isEmpty()) {
                  return ItemStack.EMPTY;
               }
            }
         }
      }

      return remaining;
   }
}
