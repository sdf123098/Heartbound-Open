package com.cuddly.heartbound.client.gui.screen.customize;

import com.cuddly.heartbound.client.gui.screen.CustomizeScreen;
import com.cuddly.heartbound.entity.base.GirlSceneEntity;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public class Vec3dInputSection<T extends GirlSceneEntity> extends CustomizeSection<T> {
   private final Component label;
   private final Supplier<Vec3> valueGetter;
   private final Consumer<Vec3> valueSetter;
   private EditBox xField;
   private EditBox yField;
   private EditBox zField;

   public Vec3dInputSection(T entity, T previewEntity, Component label, Supplier<Vec3> valueGetter, Consumer<Vec3> valueSetter) {
      super(entity, previewEntity);
      this.label = label;
      this.valueGetter = valueGetter;
      this.valueSetter = valueSetter;
   }

   @Override
   public void init(CustomizeScreen<T> screen, CustomizeScreen.LayoutConfig layout, int startY) {
   }

   @Override
   public int render(CustomizeScreen<T> screen, CustomizeScreen.LayoutConfig layout, int currentY) {
      Vec3 currentValue = this.valueGetter.get();
      StringWidget textWidget = new StringWidget(this.label, screen.getTextRenderer());
      textWidget.setWidth(layout.contentWidth);
      textWidget.setX(layout.centerX);
      textWidget.setY(currentY);
      screen.addRenderableWidget(textWidget);
      currentY += 20;
      int fieldGap = 6;
      int fieldWidth = (layout.contentWidth - fieldGap * 2) / 3;
      int startX = layout.centerX;
      this.xField = this.createWarmField(screen, startX, currentY, fieldWidth, "X", String.valueOf(currentValue.x()));
      this.xField.setResponder(text -> this.onValueChanged());
      screen.addRenderableWidget(this.xField);
      this.yField = this.createWarmField(screen, startX + fieldWidth + fieldGap, currentY, fieldWidth, "Y", String.valueOf(currentValue.y()));
      this.yField.setResponder(text -> this.onValueChanged());
      screen.addRenderableWidget(this.yField);
      this.zField = this.createWarmField(screen, startX + (fieldWidth + fieldGap) * 2, currentY, fieldWidth, "Z", String.valueOf(currentValue.z()));
      this.zField.setResponder(text -> this.onValueChanged());
      screen.addRenderableWidget(this.zField);
      return currentY + 25;
   }

   private EditBox createWarmField(CustomizeScreen<T> screen, int x, int y, int w, String axis, String value) {
      WarmTextFieldWidget field = new WarmTextFieldWidget(screen.getTextRenderer(), x, y, w, 20, Component.literal(axis));
      field.setValue(value);
      field.setTooltip(Tooltip.create(Component.translatable("gui.heartbound.customize.offset", axis)));
      return field;
   }

   private void onValueChanged() {
      try {
         double x = Double.parseDouble(this.xField.getValue());
         double y = Double.parseDouble(this.yField.getValue());
         double z = Double.parseDouble(this.zField.getValue());
         this.valueSetter.accept(new Vec3(x, y, z));
      } catch (NumberFormatException var7) {
      }
   }
}
