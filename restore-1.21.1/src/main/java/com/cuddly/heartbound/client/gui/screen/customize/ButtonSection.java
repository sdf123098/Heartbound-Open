package com.cuddly.heartbound.client.gui.screen.customize;

import com.cuddly.heartbound.client.gui.screen.CustomizeScreen;
import com.cuddly.heartbound.entity.base.GirlSceneEntity;
import com.cuddly.heartbound.util.Utils;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ButtonSection<T extends GirlSceneEntity> extends CustomizeSection<T> {
   private final Text label;
   private final Supplier<Boolean> valueGetter;
   private final Consumer<Boolean> valueSetter;
   private ButtonWidget toggleButton;
   Formatting textColor;

   public ButtonSection(T entity, T previewEntity, Text label, Supplier<Boolean> valueGetter, Consumer<Boolean> valueSetter) {
      super(entity, previewEntity);
      this.label = label;
      this.valueGetter = valueGetter;
      this.valueSetter = valueSetter;
   }

   @Override
   public void init(CustomizeScreen<T> screen, CustomizeScreen.LayoutConfig layout, int startY) {
      this.textColor = this.valueGetter.get() ? Formatting.GREEN : Formatting.RED;
   }

   @Override
   public int render(CustomizeScreen<T> screen, CustomizeScreen.LayoutConfig layout, int currentY) {
      TextWidget textWidget = new TextWidget(this.label, screen.getTextRenderer());
      textWidget.setWidth(layout.contentWidth);
      textWidget.setPosition(layout.centerX, currentY);
      textWidget.setTextColor(-9741750);
      screen.addWidget(textWidget);
      currentY += 20;
      this.toggleButton = ButtonWidget.builder(
            Text.literal(Utils.getFirstLetterCapitalized(String.valueOf(this.valueGetter.get()))).formatted(Formatting.BOLD, this.textColor), button -> {
               this.valueSetter.accept(!this.valueGetter.get());
               this.textColor = this.valueGetter.get() ? Formatting.GREEN : Formatting.RED;
               button.setMessage(
                  Text.literal(Utils.getFirstLetterCapitalized(String.valueOf(this.valueGetter.get()))).formatted(Formatting.BOLD, this.textColor)
               );
            }
         )
         .dimensions(layout.centerX, currentY, layout.contentWidth, 20)
         .build();
      screen.addWidget(this.toggleButton);
      return currentY + 20;
   }
}
