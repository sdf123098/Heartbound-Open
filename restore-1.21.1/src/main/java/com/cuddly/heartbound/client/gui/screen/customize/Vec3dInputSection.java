package com.cuddly.heartbound.client.gui.screen.customize;

import com.cuddly.heartbound.client.gui.screen.CustomizeScreen;
import com.cuddly.heartbound.entity.base.GirlSceneEntity;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public class Vec3dInputSection<T extends GirlSceneEntity> extends CustomizeSection<T> {
   private final Text label;
   private final Supplier<Vec3d> valueGetter;
   private final Consumer<Vec3d> valueSetter;
   private TextFieldWidget xField;
   private TextFieldWidget yField;
   private TextFieldWidget zField;

   public Vec3dInputSection(T entity, T previewEntity, Text label, Supplier<Vec3d> valueGetter, Consumer<Vec3d> valueSetter) {
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
      Vec3d currentValue = this.valueGetter.get();
      TextWidget textWidget = new TextWidget(this.label, screen.getTextRenderer());
      textWidget.setWidth(layout.contentWidth);
      textWidget.setPosition(layout.centerX, currentY);
      screen.addWidget(textWidget);
      currentY += 20;
      int fieldGap = 6;
      int fieldWidth = (layout.contentWidth - fieldGap * 2) / 3;
      int startX = layout.centerX;
      this.xField = this.createWarmField(screen, startX, currentY, fieldWidth, "X", String.valueOf(currentValue.getX()));
      this.xField.setChangedListener(text -> this.onValueChanged());
      screen.addWidget(this.xField);
      this.yField = this.createWarmField(screen, startX + fieldWidth + fieldGap, currentY, fieldWidth, "Y", String.valueOf(currentValue.getY()));
      this.yField.setChangedListener(text -> this.onValueChanged());
      screen.addWidget(this.yField);
      this.zField = this.createWarmField(screen, startX + (fieldWidth + fieldGap) * 2, currentY, fieldWidth, "Z", String.valueOf(currentValue.getZ()));
      this.zField.setChangedListener(text -> this.onValueChanged());
      screen.addWidget(this.zField);
      return currentY + 25;
   }

   private TextFieldWidget createWarmField(CustomizeScreen<T> screen, int x, int y, int w, String axis, String value) {
      WarmTextFieldWidget field = new WarmTextFieldWidget(screen.getTextRenderer(), x, y, w, 20, Text.literal(axis));
      field.setText(value);
      field.setTooltip(Tooltip.of(Text.translatable("gui.heartbound.customize.offset", axis)));
      return field;
   }

   private void onValueChanged() {
      try {
         double x = Double.parseDouble(this.xField.getText());
         double y = Double.parseDouble(this.yField.getText());
         double z = Double.parseDouble(this.zField.getText());
         this.valueSetter.accept(new Vec3d(x, y, z));
      } catch (NumberFormatException var7) {
      }
   }
}
