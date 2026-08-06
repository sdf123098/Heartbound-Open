package com.cuddly.heartbound.client.gui.screen;

import com.cuddly.heartbound.entity.base.GirlEntity;
import com.cuddly.heartbound.networking.C2S.InventoryButtonC2SPacket;
import com.cuddly.heartbound.networking.C2S.SetGUIOpenStateC2SPacket;
import com.cuddly.heartbound.screen.FeatureGroup;
import java.util.List;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class GirlFeatureScreen extends Screen {
   private static final int COLS = 2;
   private static final int ROWS = 3;
   private static final int PER_PAGE = 6;
   private static final int BTN_W = 130;
   private static final int BTN_H = 24;
   private static final int BTN_GAP_X = 14;
   private static final int BTN_GAP_Y = 10;
   private static final int PADDING = 18;
   private static final int ARROW_W = 22;
   private static final int ARROW_H = 30;
   private static final int HEADER_H = 30;
   private static final int TEXT_MID = -9741750;
   private final GirlEntity girl;
   private final Player player;
   private final FeatureGroup group;
   private int page = 0;
   private static final int BACK_BTN_W = 50;
   private static final int BACK_BTN_H = 20;
   private int panelX;
   private int panelY;
   private int panelW;
   private int panelH;
   private int gridX;
   private int gridY;
   private int leftArrowX;
   private int leftArrowY;
   private int rightArrowX;
   private int rightArrowY;
   private int backBtnX;
   private int backBtnY;
   private String hoveredTooltip = null;
   private int tooltipX;
   private int tooltipY;

   public GirlFeatureScreen(GirlEntity girl, Player player, FeatureGroup group) {
      super(Component.translatable(group.titleKey()));
      this.girl = girl;
      this.player = player;
      this.group = group;
   }

   private int maxPage() {
      return Math.max(0, (this.group.entries().size() - 1) / 6);
   }

   @Override
   protected void init() {
      super.init();
      this.calcLayout();
      ClientPlayNetworking.send(new SetGUIOpenStateC2SPacket(this.girl.getId(), true));
   }

   private void calcLayout() {
      int gridW = 274;
      int gridH = 92;
      this.panelW = gridW + 36 + 64;
      this.panelH = 30 + gridH + 36 + 20 + 8;
      this.panelX = (this.width - this.panelW) / 2;
      this.panelY = (this.height - this.panelH) / 2;
      this.gridX = this.panelX + 18 + 22 + 10;
      this.gridY = this.panelY + 18 + 30;
      int contentMidY = this.gridY + (this.panelY + this.panelH - 18 - this.gridY) / 2;
      this.leftArrowX = this.panelX + 9;
      this.leftArrowY = contentMidY - 15;
      this.rightArrowX = this.panelX + this.panelW - 9 - 22;
      this.rightArrowY = this.leftArrowY;
      this.backBtnX = this.panelX + (this.panelW - 50) / 2;
      this.backBtnY = this.panelY + this.panelH - 20 - 8;
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      if (this.girl != null && !this.girl.isRemoved()) {
         ScreenDecorations.drawBackground(context, this.width, this.height);
         this.hoveredTooltip = null;
         ScreenDecorations.drawPanel(context, this.panelX, this.panelY, this.panelW, this.panelH);
         ScreenDecorations.drawTitleWithDecor(context, this.font, this.title, this.width / 2, this.panelY + 18);
         List<FeatureGroup.FeatureEntry> entries = this.group.entries();
         int startIdx = this.page * 6;

         for (int slot = 0; slot < 6; slot++) {
            int idx = startIdx + slot;
            if (idx >= entries.size()) {
               break;
            }

            int col = slot % 2;
            int row = slot / 2;
            int bx = this.gridX + col * 144;
            int by = this.gridY + row * 34;
            FeatureGroup.FeatureEntry entry = entries.get(idx);
            boolean active = this.girl.getCurrentRelationshipLevel() >= entry.requiredRelationshipLevel();
            Component label = entry.labelProvider().apply(this.girl);
            ScreenDecorations.drawButton(context, this.font, bx, by, 130, 24, label, mouseX, mouseY, active);
            if (!active && this.isInside((double)mouseX, (double)mouseY, bx, by, 130, 24)) {
               this.hoveredTooltip = Component.translatable("gui.heartbound.tooltip.requiresLevel", entry.requiredRelationshipLevel()).getString();
               this.tooltipX = mouseX;
               this.tooltipY = mouseY;
            }
         }

         boolean canLeft = this.page > 0;
         boolean canRight = this.page < this.maxPage();
         ScreenDecorations.drawButton(context, this.font, this.leftArrowX, this.leftArrowY, 22, 30, Component.literal("◀"), mouseX, mouseY, canLeft);
         ScreenDecorations.drawButton(context, this.font, this.rightArrowX, this.rightArrowY, 22, 30, Component.literal("▶"), mouseX, mouseY, canRight);
         if (this.maxPage() > 0) {
            String pageText = this.page + 1 + "/" + (this.maxPage() + 1);
            int pw = this.font.width(pageText);
            context.text(this.font, pageText, (this.width - pw) / 2, this.gridY + 102 + 2, -9741750, false);
         }

         ScreenDecorations.drawButton(
            context, this.font, this.backBtnX, this.backBtnY, 50, 20, Component.translatable("gui.heartbound.button.back"), mouseX, mouseY, true
         );
         if (this.hoveredTooltip != null) {
            context.setTooltipForNextFrame(Component.literal(this.hoveredTooltip), this.tooltipX, this.tooltipY);
         }
      } else {
         this.onClose();
      }
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean onPress) {
      if (event.button() == 0) {
         List<FeatureGroup.FeatureEntry> entries = this.group.entries();
         int startIdx = this.page * 6;

         for (int slot = 0; slot < 6; slot++) {
            int idx = startIdx + slot;
            if (idx >= entries.size()) {
               break;
            }

            int col = slot % 2;
            int row = slot / 2;
            int bx = this.gridX + col * 144;
            int by = this.gridY + row * 34;
            if (event.x() >= (double)bx && event.x() < (double)(bx + 130) && event.y() >= (double)by && event.y() < (double)(by + 24)) {
               FeatureGroup.FeatureEntry entry = entries.get(idx);
               if (this.girl.getCurrentRelationshipLevel() >= entry.requiredRelationshipLevel()) {
                  entry.action().accept(this.girl, this.player);
               }

               return true;
            }
         }

         if (this.isInside(event.x(), event.y(), this.backBtnX, this.backBtnY, 50, 20)) {
            this.goBack();
            return true;
         }

         if (this.page > 0 && this.isInside(event.x(), event.y(), this.leftArrowX, this.leftArrowY, 22, 30)) {
            this.page--;
            return true;
         }

         if (this.page < this.maxPage() && this.isInside(event.x(), event.y(), this.rightArrowX, this.rightArrowY, 22, 30)) {
            this.page++;
            return true;
         }
      }

      return super.mouseClicked(event, onPress);
   }

   @Override
   public boolean keyPressed(KeyEvent event) {
      if (event.key() == 263 && this.page > 0) {
         this.page--;
         return true;
      } else if (event.key() == 262 && this.page < this.maxPage()) {
         this.page++;
         return true;
      } else {
         return super.keyPressed(event);
      }
   }

   private void goBack() {
      ClientPlayNetworking.send(new InventoryButtonC2SPacket(this.girl.getId(), "openInventory"));
   }

   private boolean isInside(double mx, double my, int x, int y, int w, int h) {
      return mx >= (double)x && mx < (double)(x + w) && my >= (double)y && my < (double)(y + h);
   }

   @Override
   public void onClose() {
      super.onClose();
      ClientPlayNetworking.send(new SetGUIOpenStateC2SPacket(this.girl.getId(), false));
   }

   @Override
   public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }
}
