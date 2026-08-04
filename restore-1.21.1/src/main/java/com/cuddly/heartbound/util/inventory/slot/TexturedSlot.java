package com.cuddly.heartbound.util.inventory.slot;

import com.mojang.datafixers.util.Pair;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class TexturedSlot extends Slot {
   private final Identifier backgroundSprite;

   public TexturedSlot(Inventory inventory, int index, int x, int y, Identifier backgroundSprite) {
      super(inventory, index, x, y);
      this.backgroundSprite = backgroundSprite;
   }

   @Nullable
   @Override
   public Pair<Identifier, Identifier> getBackgroundSprite() {
      return Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, this.backgroundSprite);
   }
}
