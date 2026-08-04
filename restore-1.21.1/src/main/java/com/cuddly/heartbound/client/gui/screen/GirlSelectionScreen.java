package com.cuddly.heartbound.client.gui.screen;

import com.cuddly.heartbound.Heartbound;
import com.cuddly.heartbound.client.gui.screen.customize.WarmTextFieldWidget;
import com.cuddly.heartbound.entity.base.GirlEntity;
import com.cuddly.heartbound.entity.girls.AlyEntity;
import com.cuddly.heartbound.entity.girls.BiaEntity;
import com.cuddly.heartbound.entity.girls.CoppieEntity;
import com.cuddly.heartbound.entity.girls.CustomGirlEntity;
import com.cuddly.heartbound.entity.girls.EllieEntity;
import com.cuddly.heartbound.entity.girls.JennyEntity;
import com.cuddly.heartbound.entity.girls.KoboldEntity;
import com.cuddly.heartbound.entity.girls.SlimeEntity;
import com.cuddly.heartbound.networking.C2S.TransformRequestC2SPacket;
import com.cuddly.heartbound.registries.GirlRegistry;
import com.cuddly.heartbound.transformation.GirlTransformationInfo;
import com.cuddly.heartbound.util.json.CustomGirlLoader;
import com.cuddly.heartbound.util.variables.CustomGirlProfile;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public class GirlSelectionScreen extends Screen {
   private static final int CARD_W = 90;
   private static final int CARD_H = 140;
   private static final int CARD_GAP = 20;
   private static final int PER_PAGE = 3;
   private static final int GLOW_LAYERS = 3;
   private static final int PREVIEW_CACHE_SIZE = 7;
   private static final String[] ARMOR_BONES = new String[]{
      "armorHelmet",
      "armorBoobs",
      "armorChest",
      "armorShoulderL",
      "armorShoulderR",
      "armorHip",
      "armorPantsLowL",
      "armorPantsUpL",
      "armorPantsLowR",
      "armorPantsUpR",
      "armorBootyL",
      "armorBootyR",
      "armorShoesL",
      "armorShoesR"
   };
   private static final int BORDER_DIM = -3889014;
   private static final int BORDER_HOVER = -2506584;
   private static final int CARD_BG = -1516078;
   private static final int CARD_BG_SEL = -659996;
   private static final int TEXT_DARK = -11913432;
   private static final int TEXT_MID = -9741750;
   private final List<GirlTransformationInfo> allGirls;
   private List<GirlTransformationInfo> filteredGirls;
   private final LinkedHashMap<String, LivingEntity> previews = new LinkedHashMap<String, LivingEntity>(16, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Entry<String, LivingEntity> eldest) {
         return this.size() > 7;
      }
   };
   private int page = 0;
   private int selected = 0;
   private String searchQuery = "";
   private int panelX;
   private int panelY;
   private int panelW;
   private int panelH;
   private int cardsX;
   private int cardsY;
   private int leftArrowX;
   private int leftArrowY;
   private int arrowW;
   private int arrowH;
   private int rightArrowX;
   private int rightArrowY;
   private int selectBtnX;
   private int selectBtnY;
   private int selectBtnW;
   private int selectBtnH;
   private int backBtnX;
   private int backBtnY;
   private int backBtnW;
   private int backBtnH;
   private WarmTextFieldWidget searchField;

   public GirlSelectionScreen(boolean showCustom) {
      super(Text.translatable("gui.heartbound.girl_selection"));
      this.allGirls = new ArrayList<>();

      for (GirlTransformationInfo info : GirlTransformationInfo.REGISTRY.values()) {
         if (info.isCustom() == showCustom) {
            this.allGirls.add(info);
         }
      }

      this.filteredGirls = new ArrayList<>(this.allGirls);
   }

   @Override
   protected void init() {
      super.init();
      this.calcLayout();
      int searchW = 90;
      this.searchField = new WarmTextFieldWidget(
         this.textRenderer, this.panelX + 10, this.selectBtnY, searchW, this.selectBtnH, Text.translatable("gui.heartbound.search")
      );
      this.searchField.setMaxLength(50);
      this.searchField.setText(this.searchQuery);
      this.searchField.setPlaceholder(Text.translatable("gui.heartbound.search.placeholder"));
      this.searchField.setChangedListener(this::onSearchChanged);
      this.addDrawableChild(this.searchField);
   }

   private void onSearchChanged(String query) {
      this.searchQuery = query;
      String lower = query.toLowerCase().trim();
      this.filteredGirls = new ArrayList<>();

      for (GirlTransformationInfo info : this.allGirls) {
         String name = Text.translatable(info.displayNameKey()).getString().toLowerCase();
         if (lower.isEmpty() || name.contains(lower)) {
            this.filteredGirls.add(info);
         }
      }

      this.page = 0;
      this.selected = 0;
   }

   private int totalPages() {
      return Math.max(1, (this.filteredGirls.size() + 3 - 1) / 3);
   }

   private void calcLayout() {
      int totalCards = 310;
      this.panelW = totalCards + 100;
      this.panelH = 240;
      this.panelX = (this.width - this.panelW) / 2;
      this.panelY = (this.height - this.panelH) / 2;
      this.cardsX = this.panelX + (this.panelW - totalCards) / 2;
      this.cardsY = this.panelY + 35;
      this.arrowW = 20;
      this.arrowH = 30;
      this.leftArrowX = this.panelX + 6;
      this.leftArrowY = this.cardsY + 70 - this.arrowH / 2;
      this.rightArrowX = this.panelX + this.panelW - this.arrowW - 6;
      this.rightArrowY = this.leftArrowY;
      this.selectBtnW = 70;
      this.selectBtnH = 20;
      this.selectBtnX = this.panelX + this.panelW - this.selectBtnW - 10;
      this.selectBtnY = this.panelY + this.panelH - this.selectBtnH - 8;
      this.backBtnW = 50;
      this.backBtnH = 20;
      this.backBtnX = this.panelX + 10 + 90 + 8;
      this.backBtnY = this.selectBtnY;
   }

   private LivingEntity getOrCreatePreview(String girlId) {
      LivingEntity cached = this.previews.get(girlId);
      if (cached != null) {
         return cached;
      } else {
         World world = MinecraftClient.getInstance().world;
         if (world == null) {
            return null;
         } else {
            try {
               LivingEntity entity = this.createPreview(girlId, world);
               if (entity != null) {
                  if (entity instanceof GirlEntity girl) {
                     for (String bone : ARMOR_BONES) {
                        girl.boneVisibility.put(bone, false);
                     }
                  }

                  entity.setCustomNameVisible(false);
                  entity.setCustomName(Text.empty());
                  this.previews.put(girlId, entity);
               }

               return entity;
            } catch (Exception var10) {
               Heartbound.LOGGER.warn("Failed to create preview for '{}'", girlId);
               return null;
            }
         }
      }
   }

   private LivingEntity createPreview(String id, World world) {
      return (LivingEntity)(switch (id) {
         case "jenny" -> new JennyEntity(GirlRegistry.JENNY, world);
         case "ellie" -> new EllieEntity(GirlRegistry.ELLIE, world);
         case "bia" -> new BiaEntity(GirlRegistry.BIA, world);
         case "slime" -> new SlimeEntity(GirlRegistry.SLIME, world);
         case "kobold" -> new KoboldEntity(GirlRegistry.KOBOLD, world);
         case "coppie" -> new CoppieEntity(GirlRegistry.COPPIE, world);
         case "aly" -> new AlyEntity(GirlRegistry.ALY, world);
         default -> this.createCustomPreview(id, world);
      });
   }

   private LivingEntity createCustomPreview(String id, World world) {
      if (GirlRegistry.CUSTOM_GIRL == null) {
         return null;
      } else {
         CustomGirlEntity entity = new CustomGirlEntity(GirlRegistry.CUSTOM_GIRL, world);
         CustomGirlProfile profile = CustomGirlLoader.LOADED_PROFILES.get(id);
         if (profile != null) {
            entity.setProfile(profile, false);
         }

         return entity;
      }
   }

   @Override
   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      ScreenDecorations.drawBackground(context, this.width, this.height);
      ScreenDecorations.drawPanel(context, this.panelX, this.panelY, this.panelW, this.panelH);
      ScreenDecorations.drawTitleWithDecor(context, this.textRenderer, this.title, this.width / 2, this.panelY + 12);
      int startIndex = this.page * 3;

      for (int i = 0; i < 3; i++) {
         int gi = startIndex + i;
         int cx = this.cardsX + i * 110;
         if (gi < this.filteredGirls.size()) {
            boolean sel = gi == this.selected;
            boolean hov = !sel && mouseX >= cx && mouseX < cx + 90 && mouseY >= this.cardsY && mouseY < this.cardsY + 140;
            this.drawCard(context, cx, this.cardsY, this.filteredGirls.get(gi), sel, hov, mouseX, mouseY);
         } else {
            this.drawEmptySlot(context, cx, this.cardsY);
         }
      }

      String pageText = this.page + 1 + " / " + this.totalPages();
      int ptw = this.textRenderer.getWidth(pageText);
      int pageY = this.selectBtnY + (this.selectBtnH - 9) / 2;
      context.drawText(this.textRenderer, pageText, this.panelX + (this.panelW - ptw) / 2, pageY, -9741750, false);
      boolean canPrev = this.page > 0;
      boolean canNext = this.page < this.totalPages() - 1;
      ScreenDecorations.drawButton(
         context, this.textRenderer, this.leftArrowX, this.leftArrowY, this.arrowW, this.arrowH, Text.literal("◀"), mouseX, mouseY, canPrev
      );
      ScreenDecorations.drawButton(
         context, this.textRenderer, this.rightArrowX, this.rightArrowY, this.arrowW, this.arrowH, Text.literal("▶"), mouseX, mouseY, canNext
      );
      ScreenDecorations.drawButton(
         context,
         this.textRenderer,
         this.selectBtnX,
         this.selectBtnY,
         this.selectBtnW,
         this.selectBtnH,
         Text.translatable("gui.heartbound.button.select"),
         mouseX,
         mouseY,
         !this.filteredGirls.isEmpty()
      );
      ScreenDecorations.drawButton(
         context,
         this.textRenderer,
         this.backBtnX,
         this.backBtnY,
         this.backBtnW,
         this.backBtnH,
         Text.translatable("gui.heartbound.button.back"),
         mouseX,
         mouseY,
         true
      );
      super.render(context, mouseX, mouseY, delta);
   }

   private void drawEmptySlot(DrawContext ctx, int x, int y) {
      ctx.fill(x, y, x + 90, y + 140, -1516078);
      ScreenDecorations.outlineRect(ctx, x, y, 90, 140, -3889014);
   }

   private void drawCard(DrawContext ctx, int x, int y, GirlTransformationInfo info, boolean sel, boolean hov, int mx, int my) {
      if (sel) {
         for (int g = 3; g > 0; g--) {
            int a = (int)(60.0F * (1.0F - (float)g / 4.0F));
            ctx.fill(x - g, y - g, x + 90 + g, y + 140 + g, a << 24 | 13936746);
         }
      }

      ctx.fill(x, y, x + 90, y + 140, sel ? -659996 : -1516078);
      int borderColor = sel ? -7640812 : (hov ? -2506584 : -3889014);
      ScreenDecorations.outlineRect(ctx, x, y, 90, 140, borderColor);
      LivingEntity entity = this.getOrCreatePreview(info.girlId());
      if (entity != null) {
         int size;
         float yOff;
         if (entity instanceof GirlEntity girl) {
            size = (int)((float)girl.getSizeGUI() * 1.5F);
            yOff = girl.getYAxisGUI();
         } else {
            size = 35;
            yOff = 0.0F;
         }

         InventoryScreen.drawEntity(ctx, x + 5, y + 5, x + 90 - 5, y + 140 - 5, size, yOff, (float)mx, (float)my, entity);
      }

      String name = Text.translatable(info.displayNameKey()).getString();
      int nw = this.textRenderer.getWidth(name);
      ctx.drawText(this.textRenderer, name, x + (90 - nw) / 2, y + 140 + 5, sel ? -11913432 : -9741750, false);
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 0) {
         int startIndex = this.page * 3;

         for (int i = 0; i < 3; i++) {
            int gi = startIndex + i;
            if (gi >= this.filteredGirls.size()) {
               break;
            }

            int cx = this.cardsX + i * 110;
            if (mouseX >= (double)cx && mouseX < (double)(cx + 90) && mouseY >= (double)this.cardsY && mouseY < (double)(this.cardsY + 140)) {
               if (gi == this.selected) {
                  this.confirm();
               } else {
                  this.selected = gi;
               }

               return true;
            }
         }

         if (this.page > 0 && this.isInside(mouseX, mouseY, this.leftArrowX, this.leftArrowY, this.arrowW, this.arrowH)) {
            this.page--;
            return true;
         }

         if (this.page < this.totalPages() - 1 && this.isInside(mouseX, mouseY, this.rightArrowX, this.rightArrowY, this.arrowW, this.arrowH)) {
            this.page++;
            return true;
         }

         if (this.isInside(mouseX, mouseY, this.selectBtnX, this.selectBtnY, this.selectBtnW, this.selectBtnH)) {
            this.confirm();
            return true;
         }

         if (this.isInside(mouseX, mouseY, this.backBtnX, this.backBtnY, this.backBtnW, this.backBtnH)) {
            MinecraftClient.getInstance().setScreen(new CategorySelectionScreen());
            return true;
         }
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   private boolean isInside(double mx, double my, int x, int y, int w, int h) {
      return mx >= (double)x && mx < (double)(x + w) && my >= (double)y && my < (double)(y + h);
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      if (verticalAmount > 0.0 && this.page > 0) {
         this.page--;
      } else if (verticalAmount < 0.0 && this.page < this.totalPages() - 1) {
         this.page++;
      }

      return true;
   }

   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.searchField.isFocused()) {
         if (keyCode == 256) {
            this.searchField.setFocused(false);
            return true;
         } else {
            super.keyPressed(keyCode, scanCode, modifiers);
            return true;
         }
      } else if (keyCode == 263 && this.selected > 0) {
         this.selected--;
         this.ensureVisible();
         return true;
      } else if (keyCode == 262 && this.selected < this.filteredGirls.size() - 1) {
         this.selected++;
         this.ensureVisible();
         return true;
      } else if (keyCode != 257 && keyCode != 335) {
         return super.keyPressed(keyCode, scanCode, modifiers);
      } else {
         this.confirm();
         return true;
      }
   }

   private void ensureVisible() {
      this.page = this.selected / 3;
   }

   private void confirm() {
      if (this.selected >= 0 && this.selected < this.filteredGirls.size()) {
         ClientPlayNetworking.send(new TransformRequestC2SPacket(this.filteredGirls.get(this.selected).girlId()));
         this.close();
      }
   }

   @Override
   public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
   }

   @Override
   public boolean shouldPause() {
      return false;
   }
}
