package com.cuddly.heartbound.config.gui;

import com.cuddly.heartbound.freecam.Freecam;
import com.google.common.util.concurrent.AtomicDouble;
import com.mojang.blaze3d.platform.Window;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class DoubleSliderEntry extends TooltipListEntry<Double> {
   private final DoubleSliderEntry.Slider sliderWidget;
   private final Button resetButton;
   private final AtomicDouble value;
   private final double original;
   private final int precision;
   private final double minimum;
   private final double maximum;
   private final Supplier<Double> defaultValue;
   private final List<AbstractWidget> widgets;

   DoubleSliderEntry(
      Component fieldName,
      int precision,
      double minimum,
      double maximum,
      double value,
      Component resetText,
      Supplier<Double> defaultValue,
      @Nullable Consumer<Double> save
   ) {
      super(fieldName, null);
      this.value = new AtomicDouble(value);
      this.original = value;
      this.defaultValue = defaultValue;
      this.maximum = maximum;
      this.minimum = minimum;
      this.precision = precision;
      this.saveCallback = save;
      this.sliderWidget = new DoubleSliderEntry.Slider(0, 0, 152, 20, (this.value.get() - minimum) / (maximum - minimum));
      this.sliderWidget.updateMessage();
      this.resetButton = Button.builder(resetText, widget -> this.setValue(this.defaultValue.get()))
         .width(Freecam.MC.font.width(resetText) + 6)
         .build();
      this.widgets = List.of(this.sliderWidget, this.resetButton);
   }

   public Double getValue() {
      return this.value.get();
   }

   public void setValue(double value) {
      double clamped = Mth.clamp(value, this.minimum, this.maximum);
      this.value.set(clamped);
      this.sliderWidget.setValue((clamped - this.minimum) / (this.maximum - this.minimum));
      this.sliderWidget.updateMessage();
   }

   @Override
   public boolean isEdited() {
      return super.isEdited() || this.getValue() != this.original;
   }

   @Override
   public Optional<Double> getDefaultValue() {
      return Optional.ofNullable(this.defaultValue).map(Supplier::get);
   }

   @NotNull
   public List<? extends GuiEventListener> children() {
      return this.widgets;
   }

   @Override
   public List<? extends NarratableEntry> narratables() {
      return this.widgets;
   }

   public void extractRenderState(GuiGraphicsExtractor graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
      super.extractRenderState(graphics, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isHovered, delta);
      Window window = Freecam.MC.getWindow();
      this.resetButton.active = this.isEditable() && this.getDefaultValue().isPresent() && this.defaultValue.get() != this.value.get();
      this.resetButton.setY(y);
      this.sliderWidget.active = this.isEditable();
      this.sliderWidget.setY(y);
      Component name = this.getDisplayedFieldName();
      if (Freecam.MC.font.isBidirectional()) {
         graphics.text(
            Freecam.MC.font,
            name.getVisualOrderText(),
            window.getGuiScaledWidth() - x - Freecam.MC.font.width(name),
            y + 6,
            this.getPreferredTextColor()
         );
         this.resetButton.setX(x);
         this.sliderWidget.setX(x + this.resetButton.getWidth() + 1);
      } else {
         graphics.text(Freecam.MC.font, name.getVisualOrderText(), x, y + 6, this.getPreferredTextColor());
         this.resetButton.setX(x + entryWidth - this.resetButton.getWidth());
         this.sliderWidget.setX(x + entryWidth - 150);
      }

      this.sliderWidget.setWidth(150 - this.resetButton.getWidth() - 2);
      this.resetButton.extractRenderState(graphics, mouseX, mouseY, delta);
      this.sliderWidget.extractRenderState(graphics, mouseX, mouseY, delta);
   }

   private final class Slider extends AbstractSliderButton {
      private Slider(int x, int y, int width, int height, double value) {
         super(x, y, width, height, Component.empty(), value);
      }

      @Override
      public void updateMessage() {
         NumberFormat fmt = DecimalFormat.getInstance();
         fmt.setMinimumIntegerDigits(1);
         fmt.setMinimumFractionDigits(DoubleSliderEntry.this.precision);
         fmt.setMaximumFractionDigits(DoubleSliderEntry.this.precision);
         this.setMessage(Component.literal("Value: " + fmt.format(DoubleSliderEntry.this.value.get())));
      }

      @Override
      protected void applyValue() {
         double rounded = BigDecimal.valueOf(DoubleSliderEntry.this.minimum + (DoubleSliderEntry.this.maximum - DoubleSliderEntry.this.minimum) * this.value)
            .setScale(DoubleSliderEntry.this.precision, RoundingMode.HALF_UP)
            .doubleValue();
         DoubleSliderEntry.this.value.set(rounded);
      }

      @Override
      public boolean keyPressed(KeyEvent event) {
         return DoubleSliderEntry.this.isEditable() && super.keyPressed(event);
      }

      @Override
      public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
         return DoubleSliderEntry.this.isEditable() && super.mouseDragged(event, deltaX, deltaY);
      }

      public void setValue(double value) {
         this.value = value;
      }
   }
}
