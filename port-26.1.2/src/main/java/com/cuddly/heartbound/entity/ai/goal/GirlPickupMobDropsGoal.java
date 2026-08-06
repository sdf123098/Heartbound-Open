package com.cuddly.heartbound.entity.ai.goal;

import com.cuddly.heartbound.entity.base.tamable.TameableGirlEntity;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

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
      this.setFlags(EnumSet.of(Flag.MOVE));
   }

   @Override
   public boolean canUse() {
      boolean canPickup = this.girl.isFollowing() || this.girl.isRoaming();
      if (this.girl.isTamed() && canPickup && !this.girl.isSitting() && !this.girl.isPassenger() && !this.girl.isHavingSex() && this.pickupDelay <= 0) {
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
   public boolean canContinueToUse() {
      if (this.targetItem != null && this.targetItem.isAlive()) {
         boolean canPickup = this.girl.isFollowing() || this.girl.isRoaming();
         if (canPickup && !this.girl.isSitting() && !this.girl.isPassenger() && !this.girl.isHavingSex()) {
            ItemStack stack = this.targetItem.getItem();
            return MOB_DROP_ITEMS.contains(stack.getItem()) || stack.get(DataComponents.FOOD) != null;
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

   private ItemEntity findNearbyMobDropItem() {
      AABB searchBox = this.girl.getBoundingBox().inflate(8.0);
      List<ItemEntity> items = this.girl.level().getEntitiesOfClass(ItemEntity.class, searchBox, itemx -> {
         if (itemx.isAlive() && itemx.getAge() >= 10) {
            ItemStack stack = itemx.getItem();
            return MOB_DROP_ITEMS.contains(stack.getItem()) || stack.get(DataComponents.FOOD) != null;
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
            double distance = this.girl.distanceToSqr(item);
            if (distance < closestDistance && this.girl.getNavigation().createPath(item, 1) != null) {
               closest = item;
               closestDistance = distance;
            }
         }

         return closest;
      }
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
