package com.cuddly.heartbound.client.gui.screen;

import com.cuddly.heartbound.item.HeartboundItems;
import com.cuddly.heartbound.screen.FusionTableScreenHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class FusionTableScreen extends AbstractContainerScreen<FusionTableScreenHandler> {
   private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("minecraft", "textures/gui/container/smithing.png");
   private static final ItemStack EGG_PLACEHOLDER = new ItemStack(HeartboundItems.CUSTOM_GIRL_SPAWN_EGG);

   public FusionTableScreen(FusionTableScreenHandler handler, Inventory inventory, Component title) {
      super(handler, inventory, title, 176, 166);
   }

   @Override
   public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      int x = (this.width - this.imageWidth) / 2;
      int y = (this.height - this.imageHeight) / 2;
      context.blit(TEXTURE, x, y, this.imageWidth, this.imageHeight, 0.0F, 0.0F, 1.0F, 1.0F);
      if (this.menu.getSlot(0).getItem().isEmpty()) {
         context.item(EGG_PLACEHOLDER, x + 8, y + 48);
      }
   }

   @Override
   protected void init() {
      super.init();
      this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
   }
}
