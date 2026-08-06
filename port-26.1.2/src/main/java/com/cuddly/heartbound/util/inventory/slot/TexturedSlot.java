package com.cuddly.heartbound.util.inventory.slot;

import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

public class TexturedSlot extends Slot {
   private final Identifier backgroundSprite;

   public TexturedSlot(Container inventory, int index, int x, int y, Identifier backgroundSprite) {
      super(inventory, index, x, y);
      this.backgroundSprite = backgroundSprite;
   }

   @Override
   public Identifier getNoItemIcon() {
      return this.backgroundSprite;
   }
}
