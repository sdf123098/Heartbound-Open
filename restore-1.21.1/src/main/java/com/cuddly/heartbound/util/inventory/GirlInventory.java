package com.cuddly.heartbound.util.inventory;

import java.util.List;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public interface GirlInventory extends Inventory {
   int MAIN_HAND_SLOT = 0;
   int ARMOR_FEET_SLOT = 1;
   int ARMOR_LEGS_SLOT = 2;
   int ARMOR_CHEST_SLOT = 3;
   int ARMOR_HEAD_SLOT = 4;
   int ARMOR_START = 1;
   int ARMOR_END = 4;
   int BACKPACK_START = 5;
   int BACKPACK_END = 28;
   int OFF_HAND_SLOT = 29;
   int TOTAL_SLOTS = 30;

   DefaultedList<ItemStack> getItems();

   static GirlInventory of(DefaultedList<ItemStack> items) {
      return () -> items;
   }

   static GirlInventory ofSize() {
      return of(DefaultedList.ofSize(30, ItemStack.EMPTY));
   }

   default int slotForEquipment(EquipmentSlot slot) {
      return switch (slot) {
         case FEET -> 1;
         case LEGS -> 2;
         case CHEST -> 3;
         case HEAD -> 4;
         case MAINHAND -> 0;
         case OFFHAND -> 29;
         default -> -1;
      };
   }

   default ItemStack getEquipmentStack(EquipmentSlot slot) {
      int idx = this.slotForEquipment(slot);
      return idx >= 0 ? this.getStack(idx) : ItemStack.EMPTY;
   }

   default void setEquipmentStack(EquipmentSlot slot, ItemStack stack) {
      int idx = this.slotForEquipment(slot);
      if (idx >= 0) {
         this.setStack(idx, stack);
      }
   }

   @Override
   default int size() {
      return this.getItems().size();
   }

   @Override
   default boolean isEmpty() {
      for (ItemStack stack : this.getItems()) {
         if (!stack.isEmpty()) {
            return false;
         }
      }

      return true;
   }

   @Override
   default ItemStack getStack(int slot) {
      return this.getItems().get(slot);
   }

   @Override
   default ItemStack removeStack(int slot, int amount) {
      ItemStack result = Inventories.splitStack(this.getItems(), slot, amount);
      if (!result.isEmpty()) {
         this.markDirty();
      }

      return result;
   }

   @Override
   default ItemStack removeStack(int slot) {
      return Inventories.removeStack(this.getItems(), slot);
   }

   @Override
   default void setStack(int slot, ItemStack stack) {
      this.getItems().set(slot, stack);
      if (stack.getCount() > stack.getMaxCount()) {
         stack.setCount(stack.getMaxCount());
      }

      this.markDirty();
   }

   default void setItems(List<ItemStack> items) {
      DefaultedList<ItemStack> inv = this.getItems();
      int count = Math.min(items.size(), inv.size());

      for (int i = 0; i < count; i++) {
         inv.set(i, items.get(i));
      }
   }

   @Override
   default void markDirty() {
   }

   @Override
   default boolean canPlayerUse(PlayerEntity player) {
      return true;
   }

   @Override
   default void clear() {
      this.getItems().clear();
   }
}
