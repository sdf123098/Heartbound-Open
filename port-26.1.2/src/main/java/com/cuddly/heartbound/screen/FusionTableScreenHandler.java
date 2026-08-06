package com.cuddly.heartbound.screen;

import com.cuddly.heartbound.Heartbound;
import com.cuddly.heartbound.component.HeartboundDataComponentTypes;
import com.cuddly.heartbound.item.HeartboundItems;
import com.cuddly.heartbound.registries.HeartboundScreenHandlerRegistry;
import com.cuddly.heartbound.util.json.CustomGirlLoader;
import com.cuddly.heartbound.util.variables.CustomGirlProfile;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class FusionTableScreenHandler extends AbstractContainerMenu {
   private final Container inventory = new SimpleContainer(4);

   public FusionTableScreenHandler(int syncId, Inventory playerInventory, Object data) {
      super(HeartboundScreenHandlerRegistry.FUSION_TABLE_SCREEN_HANDLER, syncId);
      this.addSlot(new Slot(this.inventory, 0, 8, 48) {
         @Override
         public boolean mayPlace(ItemStack stack) {
            return stack.getItem() == HeartboundItems.CUSTOM_GIRL_SPAWN_EGG;
         }

         @Override
         public void setByPlayer(ItemStack stack) {
            super.setByPlayer(stack);
            FusionTableScreenHandler.this.updateFusionResult();
         }

         @Override
         public void onTake(Player player, ItemStack stack) {
            super.onTake(player, stack);
            FusionTableScreenHandler.this.updateFusionResult();
         }
      });
      this.addSlot(new Slot(this.inventory, 1, 26, 48) {
         @Override
         public void setByPlayer(ItemStack stack) {
            super.setByPlayer(stack);
            FusionTableScreenHandler.this.updateFusionResult();
         }

         @Override
         public void onTake(Player player, ItemStack stack) {
            super.onTake(player, stack);
            FusionTableScreenHandler.this.updateFusionResult();
         }
      });
      this.addSlot(new Slot(this.inventory, 2, 44, 48) {
         @Override
         public void setByPlayer(ItemStack stack) {
            super.setByPlayer(stack);
            FusionTableScreenHandler.this.updateFusionResult();
         }

         @Override
         public void onTake(Player player, ItemStack stack) {
            super.onTake(player, stack);
            FusionTableScreenHandler.this.updateFusionResult();
         }
      });
      this.addSlot(new Slot(this.inventory, 3, 98, 48) {
         @Override
         public boolean mayPlace(ItemStack stack) {
            return false;
         }

         @Override
         public void onTake(Player player, ItemStack stack) {
            for (int i = 0; i < 3; i++) {
               ItemStack inputStack = this.container.getItem(i);
               if (!inputStack.isEmpty()) {
                  inputStack.shrink(1);
                  this.container.setItem(i, inputStack.getCount() > 0 ? inputStack : ItemStack.EMPTY);
               }
            }

            this.container.setChanged();
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
   public void slotsChanged(Container inventory) {
      super.slotsChanged(inventory);
      Heartbound.LOGGER.info("FusionTableScreenHandler.onContentChanged called for inventory: " + inventory);
      this.updateFusionResult();
   }

   private void updateFusionResult() {
      ItemStack baseEgg = this.inventory.getItem(0);
      ItemStack tameItem1 = this.inventory.getItem(1);
      ItemStack tameItem2 = this.inventory.getItem(2);
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
            this.inventory.setItem(3, result);
            this.inventory.setChanged();
            Heartbound.LOGGER.info("Set result in slot 3");
            return;
         }

         Heartbound.LOGGER.info("No profile found for tame item: " + tameItem1.getItem());
      } else {
         Heartbound.LOGGER.info("Recipe not matched");
      }

      this.inventory.setItem(3, ItemStack.EMPTY);
      this.inventory.setChanged();
      Heartbound.LOGGER.info("Cleared output slot");
   }

   @Override
   public void removed(Player player) {
      super.removed(player);

      for (int i = 0; i < 3; i++) {
         ItemStack stack = this.inventory.getItem(i);
         if (!stack.isEmpty()) {
            if (!player.getInventory().add(stack)) {
               player.drop(stack, false);
            }

            this.inventory.setItem(i, ItemStack.EMPTY);
         }
      }
   }

   @Override
   public ItemStack quickMoveStack(Player player, int slot) {
      Slot slotx = this.slots.get(slot);
      if (!slotx.hasItem()) {
         return ItemStack.EMPTY;
      } else {
         ItemStack slotStack = slotx.getItem();
         ItemStack originalStack = slotStack.copy();
         if (slot == 3) {
            if (!this.moveItemStackTo(slotStack, 4, 40, true)) {
               return ItemStack.EMPTY;
            }

            slotx.onQuickCraft(slotStack, originalStack);
         } else if (slot >= 0 && slot <= 2) {
            if (!this.moveItemStackTo(slotStack, 4, 40, false)) {
               return ItemStack.EMPTY;
            }
         } else if (slot >= 4 && slot <= 39) {
            if (slotStack.getItem() == HeartboundItems.CUSTOM_GIRL_SPAWN_EGG) {
               if (!this.moveItemStackTo(slotStack, 0, 1, false) && !this.moveItemStackTo(slotStack, 1, 3, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (!this.moveItemStackTo(slotStack, 1, 3, false)) {
               if (slot < 31) {
                  if (!this.moveItemStackTo(slotStack, 31, 40, false)) {
                     return ItemStack.EMPTY;
                  }
               } else if (!this.moveItemStackTo(slotStack, 4, 31, false)) {
                  return ItemStack.EMPTY;
               }
            }
         }

         if (slotStack.isEmpty()) {
            slotx.setByPlayer(ItemStack.EMPTY);
         } else {
            slotx.setChanged();
         }

         if (slotStack.getCount() == originalStack.getCount()) {
            return ItemStack.EMPTY;
         } else {
            slotx.onTake(player, slotStack);
            return originalStack;
         }
      }
   }

   @Override
   public boolean stillValid(Player player) {
      return true;
   }

   public Container getInventory() {
      return this.inventory;
   }
}
