package com.cuddly.heartbound.client.gui.screen.customize;

import com.cuddly.heartbound.client.gui.screen.CustomizeScreen;
import com.cuddly.heartbound.entity.base.GirlSceneEntity;
import com.cuddly.heartbound.util.Utils;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;

public class ButtonSection<T extends GirlSceneEntity> extends CustomizeSection<T> {
   private final Component label;
   private final Supplier<Boolean> valueGetter;
   private final Consumer<Boolean> valueSetter;
   private Button toggleButton;
   ChatFormatting textColor;

   public ButtonSection(T entity, T previewEntity, Component label, Supplier<Boolean> valueGetter, Consumer<Boolean> valueSetter) {
      super(entity, previewEntity);
      this.label = label;
      this.valueGetter = valueGetter;
      this.valueSetter = valueSetter;
   }

   @Override
   public void init(CustomizeScreen<T> screen, CustomizeScreen.LayoutConfig layout, int startY) {
      this.textColor = this.valueGetter.get() ? ChatFormatting.GREEN : ChatFormatting.RED;
   }

   @Override
   public int render(CustomizeScreen<T> screen, CustomizeScreen.LayoutConfig layout, int currentY) {
      StringWidget textWidget = new StringWidget(this.label, screen.getTextRenderer());
      textWidget.setWidth(layout.contentWidth);
      textWidget.setX(layout.centerX);
      textWidget.setY(currentY);
      screen.addRenderableWidget(textWidget);
      currentY += 20;
      this.toggleButton = Button.builder(
            Component.literal(Utils.getFirstLetterCapitalized(String.valueOf(this.valueGetter.get()))).withStyle(ChatFormatting.BOLD, this.textColor), button -> {
               this.valueSetter.accept(!this.valueGetter.get());
               this.textColor = this.valueGetter.get() ? ChatFormatting.GREEN : ChatFormatting.RED;
               button.setMessage(
                  Component.literal(Utils.getFirstLetterCapitalized(String.valueOf(this.valueGetter.get()))).withStyle(ChatFormatting.BOLD, this.textColor)
               );
            }
         )
         .bounds(layout.centerX, currentY, layout.contentWidth, 20)
         .build();
      screen.addRenderableWidget(this.toggleButton);
      return currentY + 20;
   }
}
