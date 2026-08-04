package com.cuddly.heartbound.screen;

import com.cuddly.heartbound.HeartboundClient;
import com.cuddly.heartbound.entity.base.tamable.TameableGirlEntity;
import com.cuddly.heartbound.registries.HeartboundScreenHandlerRegistry;
import com.cuddly.heartbound.util.inventory.GirlInventory;
import com.cuddly.heartbound.util.inventory.slot.ExclusiveSlot;
import com.cuddly.heartbound.util.inventory.slot.InclusiveSlot;
import com.cuddly.heartbound.util.inventory.slot.PublicArmorSlot;
import java.util.Map;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class GirlInventoryScreenHandler extends ScreenHandler {
   private final Inventory inventory;
   private final TameableGirlEntity girl;
   public static final Identifier EMPTY_HELMET_SLOT_TEXTURE = Identifier.ofVanilla("item/empty_armor_slot_helmet");
   public static final Identifier EMPTY_CHESTPLATE_SLOT_TEXTURE = Identifier.ofVanilla("item/empty_armor_slot_chestplate");
   public static final Identifier EMPTY_LEGGINGS_SLOT_TEXTURE = Identifier.ofVanilla("item/empty_armor_slot_leggings");
   public static final Identifier EMPTY_BOOTS_SLOT_TEXTURE = Identifier.ofVanilla("item/empty_armor_slot_boots");
   public static final Identifier EMPTY_SWORD_TEXTURE = Identifier.ofVanilla("item/empty_slot_sword");
   public static final Identifier EMPTY_BOW_TEXTURE = Identifier.ofVanilla("item/empty_armor_slot_shield");
   public static final Map<EquipmentSlot, Identifier> EMPTY_ARMOR_SLOT_TEXTURES = Map.of(
      EquipmentSlot.FEET,
      EMPTY_BOOTS_SLOT_TEXTURE,
      EquipmentSlot.LEGS,
      EMPTY_LEGGINGS_SLOT_TEXTURE,
      EquipmentSlot.CHEST,
      EMPTY_CHESTPLATE_SLOT_TEXTURE,
      EquipmentSlot.HEAD,
      EMPTY_HELMET_SLOT_TEXTURE
   );
   public static final EquipmentSlot[] EQUIPMENT_SLOT_ORDER = new EquipmentSlot[]{
      EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
   };
   private static final int GIRL_BACKPACK_START = 0;
   private static final int GIRL_BACKPACK_END = 23;
   private static final int GIRL_MAIN_HAND = 24;
   private static final int GIRL_OFF_HAND = 25;
   private static final int GIRL_ARMOR_START = 26;
   private static final int GIRL_ARMOR_END = 29;
   private static final int PLAYER_INV_START = 30;
   private static final int PLAYER_HOTBAR_END = 65;

   public GirlInventoryScreenHandler(int syncId, PlayerInventory playerInventory, HeartboundClient.GirlScreenData data) {
      this(syncId, playerInventory, data.entityId());
   }

   public GirlInventoryScreenHandler(int syncId, PlayerInventory playerInventory, int girlId) {
      super(HeartboundScreenHandlerRegistry.GIRL_INVENTORY_SCREEN_HANDLER, syncId);
      PlayerEntity player = playerInventory.player;
      World world = player.getWorld();
      if (!(world.getEntityById(girlId) instanceof TameableGirlEntity girlEntity)) {
         throw new IllegalStateException("Girl not found or mismatched entity ID");
      } else {
         this.girl = girlEntity;
         GirlInventory inventory = this.girl.getInventory();
         this.inventory = inventory;
         checkSize(inventory, 30);
         int slotIndex = 5;

         for (int m = 0; m < 4; m++) {
            for (int col = 0; col < 6; col++) {
               this.addSlot(new Slot(inventory, slotIndex++, 98 + col * 18, 8 + m * 18));
            }
         }

         this.addSlot(new ExclusiveSlot(inventory, 0, 27, 62, EMPTY_SWORD_TEXTURE, Items.BOW));
         this.addSlot(new InclusiveSlot(inventory, 29, 64, 62, EMPTY_BOW_TEXTURE, Items.BOW));

         for (int i = 0; i < 4; i++) {
            EquipmentSlot equipmentSlot = EQUIPMENT_SLOT_ORDER[i];
            Identifier identifier = EMPTY_ARMOR_SLOT_TEXTURES.get(equipmentSlot);
            this.addSlot(new PublicArmorSlot(inventory, this.girl, equipmentSlot, 4 - i, 8, 8 + i * 18, identifier));
         }

         for (int m = 0; m < 3; m++) {
            for (int l = 0; l < 9; l++) {
               this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 26 + l * 18, 106 + m * 18));
            }
         }

         for (int m = 0; m < 9; m++) {
            this.addSlot(new Slot(playerInventory, m, 26 + m * 18, 164));
         }
      }
   }

   @Override
   public boolean canUse(PlayerEntity player) {
      return this.inventory.canPlayerUse(player);
   }

   @Override
   public ItemStack quickMove(PlayerEntity player, int slot) {
      Slot slotx = this.slots.get(slot);
      if (!slotx.hasStack()) {
         return ItemStack.EMPTY;
      } else {
         ItemStack originalStack = slotx.getStack();
         ItemStack copyStack = originalStack.copy();
         if (slot <= 29) {
            if (!this.insertItem(originalStack, 30, 66, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.insertItem(originalStack, 26, 30, false)
            && !this.insertItem(originalStack, 24, 25, false)
            && !this.insertItem(originalStack, 25, 26, false)
            && !this.insertItem(originalStack, 0, 24, false)) {
            return ItemStack.EMPTY;
         }

         if (originalStack.isEmpty()) {
            slotx.setStack(ItemStack.EMPTY);
         } else {
            slotx.markDirty();
         }

         if (originalStack.getCount() == copyStack.getCount()) {
            return ItemStack.EMPTY;
         } else {
            slotx.onTakeItem(player, originalStack);
            return copyStack;
         }
      }
   }

   public TameableGirlEntity getGirl() {
      return this.girl;
   }
}
