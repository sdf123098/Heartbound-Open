package com.cuddly.heartbound.util.inventory.slot;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class ExclusiveSlot extends TexturedSlot {
   private final Item canNotInsertItem;

   public ExclusiveSlot(Inventory inventory, int index, int x, int y, Identifier backgroundSpite, Item canNotInsertItem) {
      super(inventory, index, x, y, backgroundSpite);
      this.canNotInsertItem = canNotInsertItem;
   }

   @Override
   public boolean canInsert(ItemStack stack) {
      return !stack.isOf(this.canNotInsertItem);
   }
}
