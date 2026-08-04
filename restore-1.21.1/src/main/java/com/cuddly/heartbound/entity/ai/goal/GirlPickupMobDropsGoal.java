package com.cuddly.heartbound.entity.ai.goal;

import com.cuddly.heartbound.entity.base.tamable.TameableGirlEntity;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.Goal.Control;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;

public class GirlPickupMobDropsGoal extends Goal {
   private final TameableGirlEntity girl;
   private ItemEntity targetItem;
   private final double speed;
   private int pickupDelay;
   private int searchDelay;
   private static final int PICKUP_COOLDOWN = 10;
   private static final int SEARCH_COOLDOWN = 5;
   private static final double SEARCH_RANGE = 8.0;
   private static final double PICKUP_RANGE = 2.0;
   private static final List<Item> MOB_DROP_ITEMS = List.of(
      Items.BONE,
      Items.ROTTEN_FLESH,
      Items.GUNPOWDER,
      Items.STRING,
      Items.SPIDER_EYE,
      Items.ENDER_PEARL,
      Items.BLAZE_ROD,
      Items.GHAST_TEAR,
      Items.PHANTOM_MEMBRANE,
      Items.PRISMARINE_SHARD,
      Items.PRISMARINE_CRYSTALS,
      Items.RABBIT_HIDE,
      Items.RABBIT_FOOT,
      Items.MAGMA_CREAM,
      Items.SLIME_BALL,
      Items.ARROW,
      Items.BOW,
      Items.CROSSBOW,
      Items.SHIELD,
      Items.LEATHER,
      Items.FEATHER,
      Items.EMERALD,
      Items.GOLD_INGOT,
      Items.IRON_INGOT,
      Items.DIAMOND,
      Items.NETHERITE_INGOT,
      Items.EXPERIENCE_BOTTLE,
      Items.COAL,
      Items.RAW_IRON,
      Items.RAW_GOLD,
      Items.RAW_COPPER,
      Items.REDSTONE,
      Items.LAPIS_LAZULI,
      Items.QUARTZ,
      Blocks.COAL_ORE.asItem(),
      Blocks.IRON_ORE.asItem(),
      Blocks.GOLD_ORE.asItem(),
      Blocks.DIAMOND_ORE.asItem(),
      Blocks.COPPER_ORE.asItem(),
      Blocks.LAPIS_ORE.asItem(),
      Blocks.REDSTONE_ORE.asItem(),
      Blocks.EMERALD_ORE.asItem(),
      Blocks.NETHER_QUARTZ_ORE.asItem()
   );

   public GirlPickupMobDropsGoal(TameableGirlEntity girl, double speed) {
      this.girl = girl;
      this.speed = speed;
      this.setControls(EnumSet.of(Control.MOVE));
   }

   @Override
   public boolean canStart() {
      boolean canPickup = this.girl.isFollowing() || this.girl.isRoaming();
      if (this.girl.isTamed() && canPickup && !this.girl.isSitting() && !this.girl.hasVehicle() && !this.girl.isHavingSex() && this.pickupDelay <= 0) {
         if (this.searchDelay > 0) {
            this.searchDelay--;
            return false;
         } else {
            this.targetItem = this.findNearbyMobDropItem();
            this.searchDelay = 5;
            return this.targetItem != null;
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
   public boolean shouldContinue() {
      if (this.targetItem != null && this.targetItem.isAlive()) {
         boolean canPickup = this.girl.isFollowing() || this.girl.isRoaming();
         if (canPickup && !this.girl.isSitting() && !this.girl.hasVehicle() && !this.girl.isHavingSex()) {
            ItemStack stack = this.targetItem.getStack();
            return MOB_DROP_ITEMS.contains(stack.getItem()) || stack.get(DataComponentTypes.FOOD) != null;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public void start() {
      if (this.targetItem != null) {
         this.girl.getNavigation().startMovingTo(this.targetItem, this.speed);
      }
   }

   @Override
   public void tick() {
      if (this.targetItem != null && this.targetItem.isAlive()) {
         this.girl.getLookControl().lookAt(this.targetItem, 30.0F, 30.0F);
         double distance = this.girl.squaredDistanceTo(this.targetItem);
         if (distance <= 4.0) {
            this.pickupItem();
         } else if (this.girl.getNavigation().isIdle()) {
            this.girl.getNavigation().startMovingTo(this.targetItem, this.speed);
         }
      }
   }

   @Override
   public void stop() {
      this.targetItem = null;
      this.girl.getNavigation().stop();
      this.pickupDelay = 10;
   }

   private ItemEntity findNearbyMobDropItem() {
      Box searchBox = this.girl.getBoundingBox().expand(8.0);
      List<ItemEntity> items = this.girl.getWorld().getEntitiesByClass(ItemEntity.class, searchBox, itemx -> {
         if (itemx.isAlive() && itemx.getItemAge() >= 10) {
            ItemStack stack = itemx.getStack();
            return MOB_DROP_ITEMS.contains(stack.getItem()) || stack.get(DataComponentTypes.FOOD) != null;
         } else {
            return false;
         }
      });
      if (items.isEmpty()) {
         return null;
      } else {
         ItemEntity closest = null;
         double closestDistance = Double.MAX_VALUE;

         for (ItemEntity item : items) {
            double distance = this.girl.squaredDistanceTo(item);
            if (distance < closestDistance && this.girl.getNavigation().findPathTo(item, 1) != null) {
               closest = item;
               closestDistance = distance;
            }
         }

         return closest;
      }
   }

   private void pickupItem() {
      if (this.targetItem != null && this.targetItem.isAlive()) {
         ItemStack stack = this.targetItem.getStack().copy();
         boolean added = this.addToInventory(stack);
         if (added || stack.isEmpty()) {
            this.targetItem.discard();
            this.targetItem = null;
            this.girl
               .getWorld()
               .playSound(
                  null,
                  this.girl.getX(),
                  this.girl.getY(),
                  this.girl.getZ(),
                  SoundEvents.ENTITY_ITEM_PICKUP,
                  this.girl.getSoundCategory(),
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
