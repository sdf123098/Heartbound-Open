package com.cuddly.heartbound.util.inventory;

import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface GirlInventory extends Container {
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

   NonNullList<ItemStack> getItems();

   static GirlInventory of(NonNullList<ItemStack> items) {
      return () -> items;
   }

   static GirlInventory ofSize() {
      return of(NonNullList.withSize(30, ItemStack.EMPTY));
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
      return idx >= 0 ? this.getItem(idx) : ItemStack.EMPTY;
   }

   default void setEquipmentStack(EquipmentSlot slot, ItemStack stack) {
      int idx = this.slotForEquipment(slot);
      if (idx >= 0) {
         this.setItem(idx, stack);
      }
   }

   @Override
   default int getContainerSize() {
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
   default ItemStack getItem(int slot) {
      return this.getItems().get(slot);
   }

   @Override
   default ItemStack removeItem(int slot, int amount) {
      ItemStack result = ContainerHelper.removeItem(this.getItems(), slot, amount);
      if (!result.isEmpty()) {
         this.setChanged();
      }

      return result;
   }

   @Override
   default ItemStack removeItemNoUpdate(int slot) {
      return ContainerHelper.takeItem(this.getItems(), slot);
   }

   @Override
   default void setItem(int slot, ItemStack stack) {
      this.getItems().set(slot, stack);
      if (stack.getCount() > stack.getMaxStackSize()) {
         stack.setCount(stack.getMaxStackSize());
      }

      this.setChanged();
   }

   default void setItems(List<ItemStack> items) {
      NonNullList<ItemStack> inv = this.getItems();
      int count = Math.min(items.size(), inv.size());

      for (int i = 0; i < count; i++) {
         inv.set(i, items.get(i));
      }
   }

   @Override
   default void setChanged() {
   }

   @Override
   default boolean stillValid(Player player) {
      return true;
   }

   @Override
   default void clearContent() {
      this.getItems().clear();
   }
}
