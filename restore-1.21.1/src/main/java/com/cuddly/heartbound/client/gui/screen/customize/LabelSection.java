package com.cuddly.heartbound.client.gui.screen.customize;

import com.cuddly.heartbound.client.gui.screen.CustomizeScreen;
import com.cuddly.heartbound.entity.base.GirlSceneEntity;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;

public class LabelSection<T extends GirlSceneEntity> extends CustomizeSection<T> {
   private final Text text;

   public LabelSection(T entity, T previewEntity, Text text) {
      super(entity, previewEntity);
      this.text = text;
   }

   @Override
   public void init(CustomizeScreen<T> screen, CustomizeScreen.LayoutConfig layout, int startY) {
   }

   @Override
   public int render(CustomizeScreen<T> screen, CustomizeScreen.LayoutConfig layout, int currentY) {
      TextWidget textWidget = new TextWidget(this.text, screen.getTextRenderer());
      textWidget.setWidth(layout.contentWidth);
      textWidget.setPosition(layout.centerX, currentY);
      textWidget.setTextColor(-9741750);
      screen.addWidget(textWidget);
      return currentY + 20;
   }

   @Override
   public int getSpacing() {
      return 5;
   }
}
