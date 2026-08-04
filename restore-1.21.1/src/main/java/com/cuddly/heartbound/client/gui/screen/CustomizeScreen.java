package com.cuddly.heartbound.client.gui.screen;

import com.cuddly.heartbound.client.gui.screen.customize.CustomizeSection;
import com.cuddly.heartbound.entity.base.GirlSceneEntity;
import com.cuddly.heartbound.networking.C2S.RemovePreviewEntityC2SPacket;
import com.cuddly.heartbound.networking.C2S.SetGUIOpenStateC2SPacket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget.PressAction;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public abstract class CustomizeScreen<T extends GirlSceneEntity> extends Screen {
   public static final int PANEL_BG = -252647716;
   public static final int PANEL_BORDER = -3889014;
   public static final int TEXT_DARK = -11913432;
   public static final int TEXT_MID = -9741750;
   public static final int FIELD_BG = -659996;
   public static final int FIELD_BORDER = -3889014;
   public static final int FIELD_BORDER_FOCUS = -4679568;
   public static final int SCROLLBAR_BG = 1086630026;
   public static final int SCROLLBAR_THUMB = -3889014;
   public static final int BTN_BG = -2308956;
   public static final int BTN_BG_HOVER = -1518408;
   public static final int BTN_BG_DISABLED = -3358540;
   public static final int BTN_BORDER = -4679568;
   public static final int BTN_BORDER_HOVER = -6258608;
   public static final int TEXT_DISABLED = -6649222;
   protected final int entityId;
   protected final T previewEntity;
   protected final T entity;
   protected final CustomizeScreen.LayoutConfig layout = new CustomizeScreen.LayoutConfig();
   protected final List<CustomizeSection<T>> sections = new ArrayList<>();
   protected final Map<String, List<ButtonWidget>> buttonGroups = new HashMap<>();
   protected final Map<ButtonWidget, String> buttonToGroup = new HashMap<>();
   protected final Map<String, ButtonWidget> selectedButtons = new HashMap<>();
   protected double scrollOffset = 0.0;
   private int computedContentHeight = 0;
   private int confirmBtnX;
   private int confirmBtnY;
   private int confirmBtnW;
   private int confirmBtnH;
   private int cancelBtnX;
   private int cancelBtnY;
   private int cancelBtnW;
   private int cancelBtnH;
   private boolean hasActionButtons = false;
   private final List<CustomizeScreen.WarmButtonEntry> warmButtons = new ArrayList<>();

   public CustomizeScreen(Text title, int entityId, int previewEntityId, Class<T> entityClass) {
      super(title);
      this.entityId = entityId;
      World world = MinecraftClient.getInstance().world;
      this.previewEntity = entityClass.cast(world.getEntityById(previewEntityId));
      this.entity = entityClass.cast(world.getEntityById(entityId));
   }

   protected abstract void addSections();

   protected abstract void onConfirm();

   protected abstract void applyToPreview();

   public <W extends Element & Drawable & Selectable> W addWidget(W widget) {
      return this.addDrawableChild(widget);
   }

   public void addWarmButton(int x, int y, int w, int h, Text label, int textColor, Runnable action) {
      this.warmButtons.add(new CustomizeScreen.WarmButtonEntry(x, y, w, h, label, textColor, action));
   }

   @Override
   protected void init() {
      super.init();
      this.clearChildren();
      this.buttonGroups.clear();
      this.buttonToGroup.clear();
      this.selectedButtons.clear();
      this.sections.clear();
      this.warmButtons.clear();
      this.hasActionButtons = false;
      this.layout.calculate(this.width, this.height);
      this.addSections();
      this.buildUI();
   }

   private void buildUI() {
      int currentY = this.layout.startY - (int)this.scrollOffset;
      currentY += 30;

      for (CustomizeSection<T> section : this.sections) {
         section.init(this, this.layout, currentY);
         currentY = section.render(this, this.layout, currentY);
         currentY += section.getSpacing();
      }

      int btnW = (this.layout.contentWidth - 10) / 2;
      this.confirmBtnX = this.layout.centerX;
      this.confirmBtnY = currentY;
      this.confirmBtnW = btnW;
      this.confirmBtnH = 22;
      this.cancelBtnX = this.layout.centerX + btnW + 10;
      this.cancelBtnY = currentY;
      this.cancelBtnW = btnW;
      this.cancelBtnH = 22;
      this.hasActionButtons = true;
      this.computedContentHeight = currentY + 22 + 10 + (int)this.scrollOffset;
   }

   @Override
   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      ScreenDecorations.drawBackground(context, this.width, this.height);
      if (this.previewEntity != null) {
         int ppX = 4;
         int ppY = 20;
         int ppW = this.layout.previewWidth - 8;
         int ppH = this.height - 40;
         ScreenDecorations.drawPanel(context, ppX, ppY, ppW, ppH);
         this.renderEntityPreview(context, mouseX, mouseY);
      }

      int mpX = this.layout.menuStartX - 14;
      int mpY = 10;
      int mpW = this.width - mpX - 10;
      int mpH = this.height - 20;
      ScreenDecorations.drawPanel(context, mpX, mpY, mpW, mpH);
      int titleY = this.layout.startY - (int)this.scrollOffset;
      int titleW = this.textRenderer.getWidth(this.getTitle());
      int titleX = this.layout.centerX + (this.layout.contentWidth - titleW) / 2;
      context.drawText(this.textRenderer, this.getTitle(), titleX, titleY + 4, -11913432, false);
      super.render(context, mouseX, mouseY, delta);
      if (this.hasActionButtons) {
         ScreenDecorations.drawWarmButton(
            context,
            this.textRenderer,
            this.confirmBtnX,
            this.confirmBtnY,
            this.confirmBtnW,
            this.confirmBtnH,
            Text.translatable("gui.heartbound.button.confirm"),
            mouseX,
            mouseY,
            -10843590
         );
         ScreenDecorations.drawWarmButton(
            context,
            this.textRenderer,
            this.cancelBtnX,
            this.cancelBtnY,
            this.cancelBtnW,
            this.cancelBtnH,
            Text.translatable("gui.heartbound.button.cancel"),
            mouseX,
            mouseY,
            -7718342
         );
      }

      for (CustomizeScreen.WarmButtonEntry wb : this.warmButtons) {
         ScreenDecorations.drawWarmButton(context, this.textRenderer, wb.x(), wb.y(), wb.w(), wb.h(), wb.label(), mouseX, mouseY, wb.textColor());
      }

      if (this.computedContentHeight > this.height) {
         int maxScroll = Math.max(0, this.computedContentHeight - this.height);
         int scrollBarH = Math.max(20, this.height * this.height / (this.height + maxScroll));
         int scrollBarY = maxScroll > 0 ? (int)((double)(this.height - scrollBarH) * (this.scrollOffset / (double)maxScroll)) : 0;
         int sbX = this.width - 6;
         context.fill(sbX, 0, sbX + 4, this.height, 1086630026);
         context.fill(sbX, scrollBarY, sbX + 4, scrollBarY + scrollBarH, -3889014);
      }

      if (this.previewEntity != null) {
         this.applyToPreview();
      }
   }

   public static void outlineRect(DrawContext ctx, int x, int y, int w, int h, int color) {
      ScreenDecorations.outlineRect(ctx, x, y, w, h, color);
   }

   public static void drawThemedButton(DrawContext ctx, TextRenderer tr, int x, int y, int w, int h, Text label, int mx, int my, boolean active) {
      ScreenDecorations.drawButton(ctx, tr, x, y, w, h, label, mx, my, active);
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 0 && this.hasActionButtons) {
         if (this.isInside(mouseX, mouseY, this.confirmBtnX, this.confirmBtnY, this.confirmBtnW, this.confirmBtnH)) {
            this.onConfirm();
            this.close();
            return true;
         }

         if (this.isInside(mouseX, mouseY, this.cancelBtnX, this.cancelBtnY, this.cancelBtnW, this.cancelBtnH)) {
            this.close();
            return true;
         }
      }

      if (button == 0) {
         for (CustomizeScreen.WarmButtonEntry wb : this.warmButtons) {
            if (this.isInside(mouseX, mouseY, wb.x(), wb.y(), wb.w(), wb.h())) {
               wb.action().run();
               return true;
            }
         }
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   private boolean isInside(double mx, double my, int x, int y, int w, int h) {
      return mx >= (double)x && mx < (double)(x + w) && my >= (double)y && my < (double)(y + h);
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      if (mouseX > (double)this.layout.previewWidth && this.computedContentHeight > this.height) {
         int maxScroll = Math.max(0, this.computedContentHeight - this.height);
         this.scrollOffset = Math.max(0.0, Math.min((double)maxScroll, this.scrollOffset - verticalAmount * (double)this.layout.scrollSpeed));
         this.init();
         return true;
      } else {
         return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
      }
   }

   private void renderEntityPreview(DrawContext context, int mouseX, int mouseY) {
      int x1 = 10;
      int y1 = 5;
      int x2 = this.layout.previewWidth - 10;
      int y2 = this.height - 20;
      InventoryScreen.drawEntity(context, x1, y1, x2, y2, this.layout.previewSize, 0.0625F, (float)mouseX, (float)mouseY, this.previewEntity);
   }

   @Override
   public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
   }

   @Override
   public boolean shouldPause() {
      return false;
   }

   @Override
   public void close() {
      if (this.previewEntity != null) {
         ClientPlayNetworking.send(new RemovePreviewEntityC2SPacket(this.entityId, this.previewEntity.getId()));
      }

      super.close();
      ClientPlayNetworking.send(new SetGUIOpenStateC2SPacket(this.entityId, false));
   }

   public ButtonWidget createSelectableButton(String groupId, Text message, int x, int y, int width, int height, PressAction onPress) {
      ButtonWidget button = ButtonWidget.builder(message, btn -> {
         this.selectButton(groupId, btn);
         onPress.onPress(btn);
      }).dimensions(x, y, width, height).build();
      this.buttonGroups.computeIfAbsent(groupId, k -> new ArrayList<>()).add(button);
      this.buttonToGroup.put(button, groupId);
      return button;
   }

   public void selectButton(String groupId, ButtonWidget button) {
      ButtonWidget previouslySelected = this.selectedButtons.get(groupId);
      if (previouslySelected != null) {
         previouslySelected.active = true;
      }

      button.active = false;
      this.selectedButtons.put(groupId, button);
   }

   public void markAsSelected(String groupId, ButtonWidget button) {
      button.active = false;
      this.selectedButtons.put(groupId, button);
   }

   public TextRenderer getTextRenderer() {
      return this.textRenderer;
   }

   public static class LayoutConfig {
      public int previewWidth;
      public int menuWidth;
      public int menuStartX;
      public int startY;
      public int contentWidth;
      public int centerX;
      public int previewSize = 75;
      public int scrollSpeed = 20;

      public void calculate(int screenWidth, int screenHeight) {
         this.previewWidth = screenWidth / 4;
         this.menuStartX = this.previewWidth + 20;
         this.menuWidth = screenWidth - this.menuStartX;
         this.startY = 20;
         int panelX = this.menuStartX - 14;
         int panelW = screenWidth - panelX - 10;
         int padding = 14;
         int innerWidth = panelW - padding * 2;
         this.contentWidth = Math.min(400, innerWidth);
         this.centerX = panelX + padding + (innerWidth - this.contentWidth) / 2;
      }
   }

   private static record WarmButtonEntry(int x, int y, int w, int h, Text label, int textColor, Runnable action) {
   }
}
