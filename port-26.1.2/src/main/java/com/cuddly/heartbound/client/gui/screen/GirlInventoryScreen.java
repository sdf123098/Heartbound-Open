package com.cuddly.heartbound.client.gui.screen;

import com.cuddly.heartbound.client.gui.screen.customize.WarmButtonWidget;
import com.cuddly.heartbound.entity.base.tamable.TameableGirlEntity;
import com.cuddly.heartbound.networking.C2S.SetGUIOpenStateC2SPacket;
import com.cuddly.heartbound.registries.InventoryButtonRegistry;
import com.cuddly.heartbound.screen.GirlInventoryScreenHandler;
import com.cuddly.heartbound.screen.InventoryButtonAction;
import com.cuddly.heartbound.util.HeartboundIcons;
import com.cuddly.heartbound.util.ScreenUtils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class GirlInventoryScreen extends AbstractContainerScreen<GirlInventoryScreenHandler> {
   private static final int GUI_WIDTH = 214;
   private static final int GUI_HEIGHT = 186;
   private static final int SLOT_BG = -2833232;
   private static final int SLOT_SHADOW = -4679568;
   private static final int SLOT_HIGHLIGHT = -659996;
   private static final int SECTION_BG = -1187114;
   private static final int SECTION_BORDER = -3426144;
   private static final String KEY_SIT = "gui.heartbound.button.sit";
   private static final String KEY_FOLLOW = "gui.heartbound.button.followMe";
   private static final String KEY_STRIP = "gui.heartbound.button.strip";
   private final TameableGirlEntity girl;
   private final Player player;

   private static boolean hasKey(Component text, String key) {
      if (text.getContents() instanceof TranslatableContents t && t.getKey().equals(key)) {
         return true;
      }

      return false;
   }

   public GirlInventoryScreen(GirlInventoryScreenHandler handler, Inventory inventory, Component title) {
      super(handler, inventory, title, 214, 186);
      this.girl = handler.getGirl();
      this.player = inventory.player;
   }

   @Override
   protected void extractLabels(GuiGraphicsExtractor context, int mouseX, int mouseY) {
   }

   @Override
   public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      ScreenDecorations.drawBackground(context, this.width, this.height);
      int cx = this.leftPos;
      int cy = this.topPos;
      ScreenDecorations.drawPanel(context, cx, cy, 214, 186);
      this.drawSection(context, cx + 5, cy + 5, 78, 76);
      int cardX = cx + 26;
      int cardY = cy + 7;
      int cardW = 55;
      int cardH = 72;

      for (int g = 3; g > 0; g--) {
         int a = (int)(60.0F * (1.0F - (float)g / 4.0F));
         context.fill(cardX - g, cardY - g, cardX + cardW + g, cardY + cardH + g, a << 24 | 13936746);
      }

      context.fill(cardX, cardY, cardX + cardW, cardY + cardH, -659996);
      ScreenDecorations.outlineRect(context, cardX, cardY, cardW, cardH, -7640812);
      this.drawSection(context, cx + 94, cy + 5, 114, 76);
      int dividerY = cy + 91;
      int midX = cx + 107;
      context.fill(cx + 10, dividerY, midX - 4, dividerY + 1, -2840470);
      context.fill(midX + 4, dividerY, cx + 214 - 10, dividerY + 1, -2840470);
      context.fill(midX - 2, dividerY - 1, midX + 2, dividerY + 2, -2840470);
      this.drawSection(context, cx + 5, cy + 102, 204, 82);

      for (int row = 0; row < 4; row++) {
         for (int col = 0; col < 6; col++) {
            this.drawSlotBg(context, cx + 97 + col * 18, cy + 7 + row * 18);
         }
      }

      for (int i = 0; i < 4; i++) {
         this.drawSlotBg(context, cx + 7, cy + 7 + i * 18);
      }

      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 9; col++) {
            this.drawSlotBg(context, cx + 25 + col * 18, cy + 105 + row * 18);
         }
      }

      for (int col = 0; col < 9; col++) {
         this.drawSlotBg(context, cx + 25 + col * 18, cy + 163);
      }

      InventoryScreen.extractEntityInInventoryFollowsMouse(
         context, cx + 26, cy + 8, cx + 80, cy + 78, this.girl.getSizeGUI(), this.girl.getYAxisGUI(), (float)mouseX, (float)mouseY, this.girl
      );
      this.drawSlotBg(context, cx + 26, cy + 61);
      this.drawSlotBg(context, cx + 63, cy + 61);
      int iconY = cy - 22;
      int iconSize = 18;
      int relLevel = this.girl.getCurrentRelationshipLevel();
      int relMax = this.girl.maxRelationshipLevel();
      String relText = relLevel + "/" + relMax;
      context.blit(HeartboundIcons.HEART_ICON, cx, iconY, iconSize, iconSize, 0.0F, 0.0F, 1.0F, 1.0F);
      context.text(this.font, Component.literal(relText), cx + 20, iconY + 5, -1, true);
      if (ScreenUtils.isMouseOverHere((double)mouseX, (double)mouseY, cx, iconY, 18, 18)) {
         context.setTooltipForNextFrame(Component.translatable("screen.heartbound.girl_inventory.relationship_tooltip"), mouseX, mouseY);
      }
   }

   private void drawSection(GuiGraphicsExtractor ctx, int x, int y, int w, int h) {
      ctx.fill(x, y, x + w, y + h, -1187114);
      ScreenDecorations.outlineRect(ctx, x, y, w, h, -3426144);
   }

   private void drawSlotBg(GuiGraphicsExtractor ctx, int x, int y) {
      ctx.fill(x, y, x + 18, y + 1, -4679568);
      ctx.fill(x, y, x + 1, y + 18, -4679568);
      ctx.fill(x + 1, y + 17, x + 18, y + 18, -659996);
      ctx.fill(x + 17, y + 1, x + 18, y + 18, -659996);
      ctx.fill(x + 1, y + 1, x + 17, y + 17, -2833232);
   }

   @Override
   public void onClose() {
      super.onClose();
      ClientPlayNetworking.send(new SetGUIOpenStateC2SPacket(this.girl.getId(), false));
   }

   private void drawButton(Component label, InventoryButtonAction action, int x, int y, int buttonWidth, int buttonHeight) {
      WarmButtonWidget button = new WarmButtonWidget(this.font, x, y, buttonWidth, buttonHeight, label, btn -> {
         if (this.girl != null && this.minecraft != null && this.player != null) {
            action.action().accept(this.girl, this.player);
            if (action.closesScreen()) {
               this.onClose();
            }
         }
      });
      if (this.girl.getCurrentRelationshipLevel() < action.requiredRelationshipLevel()) {
         button.active = false;
      }

      if (!button.active) {
         button.setTooltip(Tooltip.create(Component.translatable("gui.heartbound.tooltip.requiresLevel", action.requiredRelationshipLevel())));
      }

      this.addRenderableWidget(button);
   }

   @Override
   protected void init() {
      super.init();
      int centerX = (this.width - 214) / 2;
      int centerY = (this.height - 186) / 2;
      int buttonHeight = 22;
      int buttonWidth = 80;
      int paddingX = 10;
      int paddingY = 4;
      int startX = centerX - (buttonWidth + paddingX);
      int startY = centerY + 15;
      if (this.girl.isTamed()) {
         for (int i = 0; i < InventoryButtonRegistry.BUTTONS_LEFT.size(); i++) {
            InventoryButtonAction action = InventoryButtonRegistry.BUTTONS_LEFT.get(i);
            int y = startY + i * (buttonHeight + paddingY);
            this.drawButton(action.label(), action, startX, y, buttonWidth, buttonHeight);
         }

         for (int i = 0; i < InventoryButtonRegistry.BUTTONS_RIGHT.size(); i++) {
            InventoryButtonAction action = InventoryButtonRegistry.BUTTONS_RIGHT.get(i);
            int y = startY + i * (buttonHeight + paddingY);
            Component dynamicLabel = action.label();
            if (hasKey(action.label(), "gui.heartbound.button.sit") && this.girl.isSitting()) {
               dynamicLabel = Component.translatable("gui.heartbound.button.stand");
            } else if (hasKey(action.label(), "gui.heartbound.button.followMe") && this.girl.isFollowing()) {
               dynamicLabel = Component.translatable("gui.heartbound.button.stopFollowing");
            }

            if (hasKey(action.label(), "gui.heartbound.button.strip") && this.girl.isStripped()) {
               dynamicLabel = Component.translatable("gui.heartbound.button.dressUp");
            }

            this.drawButton(dynamicLabel, action, centerX + 214 + paddingX, y, buttonWidth, buttonHeight);
         }
      }
   }
}
