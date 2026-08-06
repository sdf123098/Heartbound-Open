package com.cuddly.heartbound.util.inventory.slot;

import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class InclusiveSlot extends TexturedSlot {
   private final Item insertItem;

   public InclusiveSlot(Container inventory, int index, int x, int y, Identifier backgroundSpite, Item insertItem) {
      super(inventory, index, x, y, backgroundSpite);
      this.insertItem = insertItem;
   }

   @Override
   public boolean mayPlace(ItemStack stack) {
      return stack.is(this.insertItem);
   }
}
