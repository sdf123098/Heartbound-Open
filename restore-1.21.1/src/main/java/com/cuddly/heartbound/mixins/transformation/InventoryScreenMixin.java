package com.cuddly.heartbound.mixins.transformation;

import com.cuddly.heartbound.client.gui.screen.customize.WarmButtonWidget;
import com.cuddly.heartbound.client.rendering.TransformedPlayerAnimatable;
import com.cuddly.heartbound.client.rendering.renderers.AbstractGirlRenderer;
import com.cuddly.heartbound.transformation.TransformablePlayer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({InventoryScreen.class})
public abstract class InventoryScreenMixin extends AbstractInventoryScreen<PlayerScreenHandler> {
   protected InventoryScreenMixin() {
      super(null, null, null);
   }

   @Inject(
      method = {"drawEntity(Lnet/minecraft/client/gui/DrawContext;IIIIIFFFLnet/minecraft/entity/LivingEntity;)V"},
      at = {@At("HEAD")}
   )
   private static void heartbound$beforeDrawEntity(
      DrawContext context, int x1, int y1, int x2, int y2, int size, float yOffset, float mouseX, float mouseY, LivingEntity entity, CallbackInfo ci
   ) {
      AbstractGirlRenderer.IS_GUI_RENDERING = true;
   }

   @Inject(
      method = {"drawEntity(Lnet/minecraft/client/gui/DrawContext;IIIIIFFFLnet/minecraft/entity/LivingEntity;)V"},
      at = {@At("RETURN")}
   )
   private static void heartbound$afterDrawEntity(
      DrawContext context, int x1, int y1, int x2, int y2, int size, float yOffset, float mouseX, float mouseY, LivingEntity entity, CallbackInfo ci
   ) {
      AbstractGirlRenderer.IS_GUI_RENDERING = false;
   }

   @Inject(
      method = {"init()V"},
      at = {@At("TAIL")}
   )
   private void heartbound$addStripButton(CallbackInfo ci) {
      if (this.client != null && this.client.player != null) {
         PlayerEntity player = this.client.player;
         if (player instanceof TransformablePlayer tp && tp.heartbound$isTransformed()) {
            Text label = tp.heartbound$isStripped() ? Text.translatable("gui.heartbound.button.dressUp") : Text.translatable("gui.heartbound.button.strip");
            int buttonWidth = 80;
            int buttonHeight = 20;
            int buttonX = this.x - buttonWidth - 10;
            int buttonY = this.y + 10;
            WarmButtonWidget button = new WarmButtonWidget(
               this.textRenderer, buttonX, buttonY, buttonWidth, buttonHeight, label, btn -> TransformedPlayerAnimatable.INSTANCE.requestStripAnimation()
            );
            if (TransformedPlayerAnimatable.INSTANCE.isStripAnimPlaying()) {
               button.active = false;
            }

            this.addDrawableChild(button);
            return;
         }
      }
   }
}
