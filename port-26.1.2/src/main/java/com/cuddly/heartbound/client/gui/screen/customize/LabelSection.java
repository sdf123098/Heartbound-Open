package com.cuddly.heartbound.client.gui.screen.customize;

import com.cuddly.heartbound.client.gui.screen.CustomizeScreen;
import com.cuddly.heartbound.entity.base.GirlSceneEntity;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;

public class LabelSection<T extends GirlSceneEntity> extends CustomizeSection<T> {
   private final Component text;

   public LabelSection(T entity, T previewEntity, Component text) {
      super(entity, previewEntity);
      this.text = text;
   }

   @Override
   public void init(CustomizeScreen<T> screen, CustomizeScreen.LayoutConfig layout, int startY) {
   }

   @Override
   public int render(CustomizeScreen<T> screen, CustomizeScreen.LayoutConfig layout, int currentY) {
      StringWidget textWidget = new StringWidget(this.text, screen.getTextRenderer());
      textWidget.setWidth(layout.contentWidth);
      textWidget.setX(layout.centerX);
      textWidget.setY(currentY);
      screen.addRenderableWidget(textWidget);
      return currentY + 20;
   }

   @Override
   public int getSpacing() {
      return 5;
   }
}
