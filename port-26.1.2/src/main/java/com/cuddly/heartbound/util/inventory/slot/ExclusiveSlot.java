package com.cuddly.heartbound.util.inventory.slot;

import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ExclusiveSlot extends TexturedSlot {
   private final Item canNotInsertItem;

   public ExclusiveSlot(Container inventory, int index, int x, int y, Identifier backgroundSpite, Item canNotInsertItem) {
      super(inventory, index, x, y, backgroundSpite);
      this.canNotInsertItem = canNotInsertItem;
   }

   @Override
   public boolean mayPlace(ItemStack stack) {
      return !stack.is(this.canNotInsertItem);
   }
}
