package com.cuddly.heartbound.client.gui.screen;

import com.cuddly.heartbound.client.gui.screen.customize.ButtonGridSection;
import com.cuddly.heartbound.client.gui.screen.customize.CustomizeSection;
import com.cuddly.heartbound.client.gui.screen.customize.SliderSection;
import com.cuddly.heartbound.entity.base.GirlEntity;
import com.cuddly.heartbound.entity.girls.KoboldEntity;
import com.cuddly.heartbound.networking.C2S.KoboldCustomizeC2SPacket;
import com.cuddly.heartbound.util.Utils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class KoboldCustomizeScreen extends CustomizeScreen<KoboldEntity> {
   private int bodySize;
   private int breastSize;
   private int primaryColor;
   private int secondaryColor;
   private int irisColor;
   private int topHornType;
   private int bottomHornType;

   public KoboldCustomizeScreen(int entityId, int previewEntityId) {
      super(Component.translatable("gui.heartbound.customizeKobold").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), entityId, previewEntityId, KoboldEntity.class);
      if (this.entity != null) {
         this.bodySize = this.entity.getBodySize();
         this.breastSize = this.entity.getKoboldBreastSize();
         this.primaryColor = this.entity.getPrimaryColor();
         this.secondaryColor = this.entity.getSecondaryColor();
         this.irisColor = this.entity.getIrisColor();
         this.topHornType = this.entity.getTopHornType();
         this.bottomHornType = this.entity.getBottomHornType();
      }
   }

   @Override
   protected void addSections() {
      this.sections
         .add(
            new SliderSection<>(
               this.entity,
               this.previewEntity,
               Component.translatable("gui.heartbound.customize.bodySize"),
               65,
               115,
               () -> this.bodySize,
               value -> this.bodySize = value,
               Component.translatable("gui.heartbound.customize.bodySize.tooltip")
            )
         );
      this.sections
         .add(
            new SliderSection<>(
               this.entity,
               this.previewEntity,
               Component.translatable("gui.heartbound.customize.breastSize"),
               60,
               160,
               () -> this.breastSize,
               value -> this.breastSize = value,
               Component.translatable("gui.heartbound.customize.breastSize.tooltip")
            )
         );
      this.sections
         .add(
            new ButtonGridSection<>(
               this.entity,
               this.previewEntity,
               Component.translatable("gui.heartbound.customize.colorPattern").withStyle(ChatFormatting.YELLOW),
               "color_preset",
               KoboldEntity.PatternPresets.values(),
               2,
               preset -> Component.literal(Utils.getFormattedByUnderscore(preset.name())),
               preset -> {
                  this.primaryColor = preset.primary;
                  this.secondaryColor = preset.secondary;
               },
               () -> {
                  for (KoboldEntity.PatternPresets preset : KoboldEntity.PatternPresets.values()) {
                     if (preset.primary == this.primaryColor && preset.secondary == this.secondaryColor) {
                        return preset;
                     }
                  }

                  return null;
               }
            )
         );
      Integer[] irisColors = new Integer[]{-7876885, -16711936, -65536, -8388480, -23296, -256, -16181, -16711681, -13447886, -1, -8355712, -16777216};
      this.sections
         .add(
            new ButtonGridSection<>(
               this.entity,
               this.previewEntity,
               Component.translatable("gui.heartbound.customize.irisColor").withStyle(ChatFormatting.YELLOW),
               "iris_color",
               irisColors,
               3,
               color -> Component.literal("■").withStyle(style -> style.withColor(color)),
               color -> this.irisColor = color,
               () -> this.irisColor
            )
         );
      Integer[] topHornTypes = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7};
      this.sections
         .add(
            new ButtonGridSection<>(
               this.entity,
               this.previewEntity,
               Component.translatable("gui.heartbound.customize.topHorns").withStyle(ChatFormatting.YELLOW),
               "top_horn",
               topHornTypes,
               4,
               type -> Component.translatable("gui.heartbound.customize.type", type),
               type -> this.topHornType = type,
               () -> this.topHornType
            )
         );
      Integer[] bottomHornTypes = new Integer[]{0, 1, 2};
      this.sections
         .add(
            new ButtonGridSection<>(
               this.entity,
               this.previewEntity,
               Component.translatable("gui.heartbound.customize.bottomHorns").withStyle(ChatFormatting.YELLOW),
               "bottom_horn",
               bottomHornTypes,
               3,
               type -> Component.translatable("gui.heartbound.customize.type", type),
               type -> this.bottomHornType = type,
               () -> this.bottomHornType
            )
         );
      this.sections
         .add(
            new CustomizeSection<KoboldEntity>(this.entity, this.previewEntity) {
               @Override
               public void init(CustomizeScreen<KoboldEntity> screen, CustomizeScreen.LayoutConfig layout, int startY) {
               }

               @Override
               public int render(CustomizeScreen<KoboldEntity> screen, CustomizeScreen.LayoutConfig layout, int currentY) {
                  Button randomizeBtn = Button.builder(
                        Component.translatable("gui.heartbound.button.randomize").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD),
                        button -> KoboldCustomizeScreen.this.randomizeAll()
                     )
                     .bounds(layout.centerX, currentY, layout.contentWidth, 20)
                     .build();
                  screen.addRenderableWidget(randomizeBtn);
                  return currentY + 25;
               }
            }
         );
   }

   private void randomizeAll() {
      this.bodySize = GirlEntity.RANDOM.nextInt(65, 116);
      this.breastSize = GirlEntity.RANDOM.nextInt(60, 161);
      KoboldEntity.PatternPresets preset = KoboldEntity.PatternPresets.values()[GirlEntity.RANDOM.nextInt(KoboldEntity.PatternPresets.values().length)];
      this.primaryColor = preset.primary;
      this.secondaryColor = preset.secondary;
      Integer[] colors = new Integer[]{-7876885, -16711936, -65536, -8388480, -23296, -256, -16181, -16711681, -13447886, -1, -8355712, -16777216};
      this.irisColor = colors[GirlEntity.RANDOM.nextInt(colors.length)];
      this.topHornType = GirlEntity.RANDOM.nextInt(0, 8);
      this.bottomHornType = GirlEntity.RANDOM.nextInt(0, 3);
      this.init();
   }

   @Override
   protected void applyToPreview() {
      this.previewEntity.setBodySize(this.bodySize);
      this.previewEntity.setKoboldBreastSize(this.breastSize);
      this.previewEntity.setPrimaryColor(this.primaryColor);
      this.previewEntity.setSecondaryColor(this.secondaryColor);
      this.previewEntity.setIrisColor(this.irisColor);
      this.previewEntity.setTopHornType(this.topHornType);
      this.previewEntity.setBottomHornType(this.bottomHornType);
   }

   @Override
   protected void onConfirm() {
      ClientPlayNetworking.send(
         new KoboldCustomizeC2SPacket(
            this.entityId, this.bodySize, this.breastSize, this.primaryColor, this.secondaryColor, this.irisColor, this.topHornType, this.bottomHornType
         )
      );
   }
}
