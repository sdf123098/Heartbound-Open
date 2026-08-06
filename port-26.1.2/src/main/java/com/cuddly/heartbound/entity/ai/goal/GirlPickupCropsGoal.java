package com.cuddly.heartbound.entity.ai.goal;

import com.cuddly.heartbound.entity.base.tamable.TameableGirlEntity;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.phys.AABB;

public class GirlPickupCropsGoal extends Goal {
   private final TameableGirlEntity girl;
   private ItemEntity targetItem;
   private final double speed;
   private int pickupDelay;
   private int searchDelay;
   private static final int PICKUP_COOLDOWN = 10;
   private static final int SEARCH_COOLDOWN = 5;
   private static final double SEARCH_RANGE = 8.0;
   private static final double PICKUP_RANGE = 2.0;
   private static final List<Item> COLLECTIBLE_ITEMS = List.of(
      Items.WHEAT,
      Items.WHEAT_SEEDS,
      Items.CARROT,
      Items.POTATO,
      Items.BEETROOT,
      Items.BEETROOT_SEEDS,
      Items.NETHER_WART,
      Items.SWEET_BERRIES,
      Items.COCOA_BEANS,
      Items.MELON_SLICE,
      Items.PUMPKIN,
      Items.SUGAR_CANE,
      Items.BAMBOO,
      Items.OAK_LOG,
      Items.SPRUCE_LOG,
      Items.BIRCH_LOG,
      Items.JUNGLE_LOG,
      Items.ACACIA_LOG,
      Items.DARK_OAK_LOG,
      Items.MANGROVE_LOG,
      Items.CHERRY_LOG,
      Items.CRIMSON_STEM,
      Items.WARPED_STEM,
      Items.OAK_SAPLING,
      Items.SPRUCE_SAPLING,
      Items.BIRCH_SAPLING,
      Items.JUNGLE_SAPLING,
      Items.ACACIA_SAPLING,
      Items.DARK_OAK_SAPLING,
      Items.MANGROVE_PROPAGULE,
      Items.CHERRY_SAPLING,
      Items.CRIMSON_FUNGUS,
      Items.WARPED_FUNGUS,
      Items.APPLE,
      Items.STICK
   );

   public GirlPickupCropsGoal(TameableGirlEntity girl, double speed) {
      this.girl = girl;
      this.speed = speed;
      this.setFlags(EnumSet.of(Flag.MOVE));
   }

   @Override
   public boolean canUse() {
      if (this.girl.isTamed()
         && !this.girl.isFollowing()
         && !this.girl.isSitting()
         && !this.girl.isPassenger()
         && !this.girl.isHavingSex()
         && this.pickupDelay <= 0) {
         if (this.searchDelay > 0) {
            this.searchDelay--;
            return false;
         } else {
            this.targetItem = this.findNearbyFarmItem();
            if (this.targetItem != null) {
               this.searchDelay = 5;
               return true;
            } else {
               this.searchDelay = 5;
               return false;
            }
         }
      } else {
         if (this.pickupDelay > 0) {
            this.pickupDelay--;
         }

         if (this.searchDelay > 0) {
            this.searchDelay--;
         }

         return false;
      }
   }

   @Override
   public boolean canContinueToUse() {
      if (this.targetItem == null || !this.targetItem.isAlive()) {
         return false;
      } else {
         return !this.girl.isFollowing() && !this.girl.isSitting() && !this.girl.isPassenger() && !this.girl.isHavingSex()
            ? this.isCollectibleItem(this.targetItem)
            : false;
      }
   }

   @Override
   public void start() {
      if (this.targetItem != null) {
         this.girl.getNavigation().moveTo(this.targetItem, this.speed);
      }
   }

   @Override
   public void tick() {
      if (this.targetItem != null && this.targetItem.isAlive()) {
         this.girl.getLookControl().setLookAt(this.targetItem, 30.0F, 30.0F);
         double distance = this.girl.distanceToSqr(this.targetItem);
         if (distance <= 4.0) {
            this.pickupItem();
         } else if (this.girl.getNavigation().isDone()) {
            this.girl.getNavigation().moveTo(this.targetItem, this.speed);
         }
      }
   }

   @Override
   public void stop() {
      this.targetItem = null;
      this.girl.getNavigation().stop();
      this.pickupDelay = 10;
   }

   private ItemEntity findNearbyFarmItem() {
      AABB searchBox = this.girl.getBoundingBox().inflate(8.0);
      List<ItemEntity> items = this.girl
         .level()
         .getEntitiesOfClass(
            ItemEntity.class, searchBox, itemx -> itemx.isAlive() && this.isCollectibleItem(itemx) && itemx.getAge() >= 10 && this.isItemOnGround(itemx)
         );
      if (items.isEmpty()) {
         return null;
      } else {
         items.sort(Comparator.comparingDouble(itemx -> this.girl.distanceToSqr(itemx)));
         int maxPathChecks = Math.min(3, items.size());

         for (int i = 0; i < maxPathChecks; i++) {
            ItemEntity item = items.get(i);
            if (this.girl.getNavigation().createPath(item, 1) != null) {
               return item;
            }
         }

         return null;
      }
   }

   private boolean isCollectibleItem(ItemEntity item) {
      if (COLLECTIBLE_ITEMS.contains(item.getItem().getItem())) {
         return true;
      } else {
         if (item.getItem().getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof CropBlock) {
            return true;
         }

         BlockPos itemPos = item.blockPosition();
         Block at = item.level().getBlockState(itemPos).getBlock();
         Block below = item.level().getBlockState(itemPos.below()).getBlock();
         Block above = item.level().getBlockState(itemPos.above()).getBlock();
         return !(at instanceof FarmlandBlock) && !(below instanceof FarmlandBlock) && at != Blocks.SOUL_SAND && below != Blocks.SOUL_SAND
            ? at instanceof CropBlock || above instanceof CropBlock
            : true;
      }
   }

   private boolean isItemOnGround(ItemEntity item) {
      double heightDifference = item.getY() - this.girl.getY();
      return heightDifference <= 2.0 && heightDifference >= -1.0;
   }

   private void pickupItem() {
      if (this.targetItem != null && this.targetItem.isAlive()) {
         ItemStack stack = this.targetItem.getItem().copy();
         boolean added = this.addToInventory(stack);
         if (added || stack.isEmpty()) {
            this.targetItem.discard();
            this.targetItem = null;
            this.girl
               .level()
               .playSound(
                  null,
                  this.girl.getX(),
                  this.girl.getY(),
                  this.girl.getZ(),
                  SoundEvents.ITEM_PICKUP,
                  this.girl.getSoundSource(),
                  0.2F,
                  (this.girl.getRandom().nextFloat() - this.girl.getRandom().nextFloat()) * 1.4F + 2.0F
               );
         }
      }
   }

   private boolean addToInventory(ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      } else {
         for (int i = 5; i <= 28; i++) {
            ItemStack slotStack = this.girl.inventory.getItem(i);
            if (!slotStack.isEmpty() && ItemStack.isSameItemSameComponents(slotStack, stack)) {
               int maxCount = Math.min(stack.getMaxStackSize(), this.girl.inventory.getMaxStackSize(stack));
               int available = maxCount - slotStack.getCount();
               if (available > 0) {
                  int transfer = Math.min(available, stack.getCount());
                  slotStack.grow(transfer);
                  stack.shrink(transfer);
                  this.girl.inventory.setChanged();
                  if (stack.isEmpty()) {
                     return true;
                  }
               }
            }
         }

         for (int ix = 5; ix <= 28; ix++) {
            ItemStack slotStack = this.girl.inventory.getItem(ix);
            if (slotStack.isEmpty()) {
               this.girl.inventory.setItem(ix, stack.copy());
               stack.setCount(0);
               return true;
            }
         }

         return false;
      }
   }
}
