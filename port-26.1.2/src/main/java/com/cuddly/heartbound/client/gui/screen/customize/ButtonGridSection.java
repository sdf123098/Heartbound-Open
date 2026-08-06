package com.cuddly.heartbound.client.gui.screen.customize;

import com.cuddly.heartbound.client.gui.screen.CustomizeScreen;
import com.cuddly.heartbound.entity.base.GirlSceneEntity;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;

public class ButtonGridSection<T extends GirlSceneEntity, V> extends CustomizeSection<T> {
   private final Component title;
   private final String groupId;
   private final V[] options;
   private final int columns;
   private final Function<V, Component> labelProvider;
   private final Consumer<V> onSelect;
   private final Supplier<V> currentValue;

   public ButtonGridSection(
      T entity,
      T previewEntity,
      Component title,
      String groupId,
      V[] options,
      int columns,
      Function<V, Component> labelProvider,
      Consumer<V> onSelect,
      Supplier<V> currentValue
   ) {
      super(entity, previewEntity);
      this.title = title;
      this.groupId = groupId;
      this.options = options;
      this.columns = columns;
      this.labelProvider = labelProvider;
      this.onSelect = onSelect;
      this.currentValue = currentValue;
   }

   @Override
   public void init(CustomizeScreen<T> screen, CustomizeScreen.LayoutConfig layout, int startY) {
   }

   @Override
   public int render(CustomizeScreen<T> screen, CustomizeScreen.LayoutConfig layout, int currentY) {
      StringWidget textWidget = new StringWidget(this.title, screen.getTextRenderer());
      textWidget.setWidth(layout.contentWidth);
      textWidget.setX(layout.centerX);
      textWidget.setY(currentY);
      screen.addRenderableWidget(textWidget);
      currentY += 20;
      int totalGaps = (this.columns - 1) * 5;
      int buttonWidth = (layout.contentWidth - totalGaps) / this.columns;
      int rows = (this.options.length + this.columns - 1) / this.columns;
      V current = this.currentValue.get();

      for (int i = 0; i < this.options.length; i++) {
         int row = i / this.columns;
         int col = i % this.columns;
         int btnX = layout.centerX + col * (buttonWidth + 5);
         int btnY = currentY + row * 25;
         V option = this.options[i];
         Button button = screen.createSelectableButton(
            this.groupId, this.labelProvider.apply(option), btnX, btnY, buttonWidth, 20, btn -> this.onSelect.accept(option)
         );
         screen.addRenderableWidget(button);
         if (option.equals(current)) {
            screen.markAsSelected(this.groupId, button);
         }
      }

      return currentY + rows * 25 + 10;
   }
}
