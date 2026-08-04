package com.cuddly.heartbound.client.gui.screen;

import com.cuddly.heartbound.item.HeartboundItems;
import com.cuddly.heartbound.screen.FusionTableScreenHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class FusionTableScreen extends HandledScreen<FusionTableScreenHandler> {
   private static final Identifier TEXTURE = Identifier.of("minecraft", "textures/gui/container/smithing.png");
   private static final ItemStack EGG_PLACEHOLDER = new ItemStack(HeartboundItems.CUSTOM_GIRL_SPAWN_EGG);

   public FusionTableScreen(FusionTableScreenHandler handler, PlayerInventory inventory, Text title) {
      super(handler, inventory, title);
      this.backgroundWidth = 176;
      this.backgroundHeight = 166;
   }

   @Override
   protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
      int x = (this.width - this.backgroundWidth) / 2;
      int y = (this.height - this.backgroundHeight) / 2;
      context.drawTexture(TEXTURE, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);
      if (this.handler.getSlot(0).getStack().isEmpty()) {
         RenderSystem.enableBlend();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.15F);
         context.drawItem(EGG_PLACEHOLDER, x + 8, y + 48);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         RenderSystem.disableBlend();
      }
   }

   @Override
   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.renderBackground(context, mouseX, mouseY, delta);
      super.render(context, mouseX, mouseY, delta);
      this.drawMouseoverTooltip(context, mouseX, mouseY);
   }

   @Override
   protected void init() {
      super.init();
      this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;
   }
}
