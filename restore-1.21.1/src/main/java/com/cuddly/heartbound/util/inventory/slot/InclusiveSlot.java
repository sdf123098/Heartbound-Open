package com.cuddly.heartbound.util.inventory.slot;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class InclusiveSlot extends TexturedSlot {
   private final Item insertItem;

   public InclusiveSlot(Inventory inventory, int index, int x, int y, Identifier backgroundSpite, Item insertItem) {
      super(inventory, index, x, y, backgroundSpite);
      this.insertItem = insertItem;
   }

   @Override
   public boolean canInsert(ItemStack stack) {
      return stack.isOf(this.insertItem);
   }
}
