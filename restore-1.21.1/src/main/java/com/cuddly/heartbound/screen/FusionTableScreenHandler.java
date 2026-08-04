package com.cuddly.heartbound.screen;

import com.cuddly.heartbound.Heartbound;
import com.cuddly.heartbound.component.HeartboundDataComponentTypes;
import com.cuddly.heartbound.item.HeartboundItems;
import com.cuddly.heartbound.registries.HeartboundScreenHandlerRegistry;
import com.cuddly.heartbound.util.json.CustomGirlLoader;
import com.cuddly.heartbound.util.variables.CustomGirlProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class FusionTableScreenHandler extends ScreenHandler {
   private final Inventory inventory = new SimpleInventory(4);

   public FusionTableScreenHandler(int syncId, PlayerInventory playerInventory, Object data) {
      super(HeartboundScreenHandlerRegistry.FUSION_TABLE_SCREEN_HANDLER, syncId);
      this.addSlot(new Slot(this.inventory, 0, 8, 48) {
         @Override
         public boolean canInsert(ItemStack stack) {
            return stack.getItem() == HeartboundItems.CUSTOM_GIRL_SPAWN_EGG;
         }

         @Override
         public void setStack(ItemStack stack) {
            super.setStack(stack);
            FusionTableScreenHandler.this.updateFusionResult();
         }

         @Override
         public void onTakeItem(PlayerEntity player, ItemStack stack) {
            super.onTakeItem(player, stack);
            FusionTableScreenHandler.this.updateFusionResult();
         }
      });
      this.addSlot(new Slot(this.inventory, 1, 26, 48) {
         @Override
         public void setStack(ItemStack stack) {
            super.setStack(stack);
            FusionTableScreenHandler.this.updateFusionResult();
         }

         @Override
         public void onTakeItem(PlayerEntity player, ItemStack stack) {
            super.onTakeItem(player, stack);
            FusionTableScreenHandler.this.updateFusionResult();
         }
      });
      this.addSlot(new Slot(this.inventory, 2, 44, 48) {
         @Override
         public void setStack(ItemStack stack) {
            super.setStack(stack);
            FusionTableScreenHandler.this.updateFusionResult();
         }

         @Override
         public void onTakeItem(PlayerEntity player, ItemStack stack) {
            super.onTakeItem(player, stack);
            FusionTableScreenHandler.this.updateFusionResult();
         }
      });
      this.addSlot(new Slot(this.inventory, 3, 98, 48) {
         @Override
         public boolean canInsert(ItemStack stack) {
            return false;
         }

         @Override
         public void onTakeItem(PlayerEntity player, ItemStack stack) {
            for (int i = 0; i < 3; i++) {
               ItemStack inputStack = this.inventory.getStack(i);
               if (!inputStack.isEmpty()) {
                  inputStack.decrement(1);
                  this.inventory.setStack(i, inputStack.getCount() > 0 ? inputStack : ItemStack.EMPTY);
               }
            }

            this.inventory.markDirty();
            FusionTableScreenHandler.this.updateFusionResult();
         }
      });

      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 9; j++) {
            this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
         }
      }

      for (int i = 0; i < 9; i++) {
         this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
      }

      this.updateFusionResult();
   }

   @Override
   public void onContentChanged(Inventory inventory) {
      super.onContentChanged(inventory);
      Heartbound.LOGGER.info("FusionTableScreenHandler.onContentChanged called for inventory: " + inventory);
      this.updateFusionResult();
   }

   private void updateFusionResult() {
      ItemStack baseEgg = this.inventory.getStack(0);
      ItemStack tameItem1 = this.inventory.getStack(1);
      ItemStack tameItem2 = this.inventory.getStack(2);
      Heartbound.LOGGER.info("Fusion check - Base: " + baseEgg.getItem() + ", Tame1: " + tameItem1.getItem() + ", Tame2: " + tameItem2.getItem());
      Heartbound.LOGGER.info("REGISTERED_PROFILES size: " + CustomGirlLoader.REGISTERED_PROFILES.size());
      if (baseEgg.getItem() == HeartboundItems.CUSTOM_GIRL_SPAWN_EGG
         && !tameItem1.isEmpty()
         && !tameItem2.isEmpty()
         && tameItem1.getItem() == tameItem2.getItem()) {
         Heartbound.LOGGER.info("Recipe matched! Looking up profile for: " + tameItem1.getItem());
         CustomGirlProfile profile = CustomGirlLoader.checkItem(tameItem1.getItem());
         if (profile != null) {
            Heartbound.LOGGER.info("Found profile: " + profile.id() + " for tame item: " + tameItem1.getItem());
            ItemStack result = new ItemStack(HeartboundItems.CUSTOM_GIRL_SPAWN_EGG);
            result.set(HeartboundDataComponentTypes.GIRL_PROFILE_ID, profile.id());
            this.inventory.setStack(3, result);
            this.inventory.markDirty();
            Heartbound.LOGGER.info("Set result in slot 3");
            return;
         }

         Heartbound.LOGGER.info("No profile found for tame item: " + tameItem1.getItem());
      } else {
         Heartbound.LOGGER.info("Recipe not matched");
      }

      this.inventory.setStack(3, ItemStack.EMPTY);
      this.inventory.markDirty();
      Heartbound.LOGGER.info("Cleared output slot");
   }

   @Override
   public void onClosed(PlayerEntity player) {
      super.onClosed(player);

      for (int i = 0; i < 3; i++) {
         ItemStack stack = this.inventory.getStack(i);
         if (!stack.isEmpty()) {
            if (!player.getInventory().insertStack(stack)) {
               player.dropItem(stack, false);
            }

            this.inventory.setStack(i, ItemStack.EMPTY);
         }
      }
   }

   @Override
   public ItemStack quickMove(PlayerEntity player, int slot) {
      Slot slotx = this.slots.get(slot);
      if (!slotx.hasStack()) {
         return ItemStack.EMPTY;
      } else {
         ItemStack slotStack = slotx.getStack();
         ItemStack originalStack = slotStack.copy();
         if (slot == 3) {
            if (!this.insertItem(slotStack, 4, 40, true)) {
               return ItemStack.EMPTY;
            }

            slotx.onQuickTransfer(slotStack, originalStack);
         } else if (slot >= 0 && slot <= 2) {
            if (!this.insertItem(slotStack, 4, 40, false)) {
               return ItemStack.EMPTY;
            }
         } else if (slot >= 4 && slot <= 39) {
            if (slotStack.getItem() == HeartboundItems.CUSTOM_GIRL_SPAWN_EGG) {
               if (!this.insertItem(slotStack, 0, 1, false) && !this.insertItem(slotStack, 1, 3, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (!this.insertItem(slotStack, 1, 3, false)) {
               if (slot < 31) {
                  if (!this.insertItem(slotStack, 31, 40, false)) {
                     return ItemStack.EMPTY;
                  }
               } else if (!this.insertItem(slotStack, 4, 31, false)) {
                  return ItemStack.EMPTY;
               }
            }
         }

         if (slotStack.isEmpty()) {
            slotx.setStack(ItemStack.EMPTY);
         } else {
            slotx.markDirty();
         }

         if (slotStack.getCount() == originalStack.getCount()) {
            return ItemStack.EMPTY;
         } else {
            slotx.onTakeItem(player, slotStack);
            return originalStack;
         }
      }
   }

   @Override
   public boolean canUse(PlayerEntity player) {
      return true;
   }

   public Inventory getInventory() {
      return this.inventory;
   }
}
