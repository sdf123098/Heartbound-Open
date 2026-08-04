package com.cuddly.heartbound.client.gui.screen.customize;

import com.cuddly.heartbound.client.gui.screen.CustomizeScreen;
import com.cuddly.heartbound.entity.base.GirlSceneEntity;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class SliderSection<T extends GirlSceneEntity> extends CustomizeSection<T> {
   private final Text label;
   private final int minValue;
   private final int maxValue;
   private final Supplier<Integer> valueGetter;
   private final Consumer<Integer> valueSetter;
   private final Text tooltipText;

   public SliderSection(
      T entity, T previewEntity, Text label, int minValue, int maxValue, Supplier<Integer> valueGetter, Consumer<Integer> valueSetter, Text tooltipText
   ) {
      super(entity, previewEntity);
      this.label = label;
      this.minValue = minValue;
      this.maxValue = maxValue;
      this.valueGetter = valueGetter;
      this.valueSetter = valueSetter;
      this.tooltipText = tooltipText;
   }

   @Override
   public void init(CustomizeScreen<T> screen, CustomizeScreen.LayoutConfig layout, int startY) {
   }

   @Override
   public int render(CustomizeScreen<T> screen, CustomizeScreen.LayoutConfig layout, int currentY) {
      int value = this.valueGetter.get();
      SliderWidget slider = new SliderWidget(
         layout.centerX,
         currentY,
         layout.contentWidth,
         20,
         Text.literal(this.label.getString() + ": " + value),
         (double)(((float)value - (float)this.minValue) / (float)(this.maxValue - this.minValue))
      ) {
         @Override
         protected void updateMessage() {
            int currentValue = SliderSection.this.minValue + (int)(this.value * (double)(SliderSection.this.maxValue - SliderSection.this.minValue));
            this.setMessage(Text.literal(SliderSection.this.label.getString() + ": " + currentValue));
         }

         @Override
         protected void applyValue() {
            int newValue = SliderSection.this.minValue + (int)(this.value * (double)(SliderSection.this.maxValue - SliderSection.this.minValue));
            SliderSection.this.valueSetter.accept(newValue);
         }

         @Override
         public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            int x = this.getX();
            int y = this.getY();
            int w = this.getWidth();
            int h = this.getHeight();
            context.fill(x, y, x + w, y + h, -659996);
            CustomizeScreen.outlineRect(context, x, y, w, h, -3889014);
            int fillW = (int)(this.value * (double)(w - 4));
            if (fillW > 0) {
               context.fill(x + 2, y + 2, x + 2 + fillW, y + h - 2, -2836330);
            }

            int handleX = x + 2 + fillW - 3;
            if (handleX < x + 2) {
               handleX = x + 2;
            }

            context.fill(handleX, y + 1, handleX + 6, y + h - 1, -2308956);
            CustomizeScreen.outlineRect(context, handleX, y + 1, 6, h - 2, -4679568);
            String msg = this.getMessage().getString();
            int tw = screen.getTextRenderer().getWidth(msg);
            context.drawText(screen.getTextRenderer(), msg, x + (w - tw) / 2, y + (h - 9) / 2, -11913432, false);
         }
      };
      if (this.tooltipText != null) {
         slider.setTooltip(Tooltip.of(this.tooltipText));
      }

      screen.addWidget(slider);
      return currentY + 25;
   }
}
