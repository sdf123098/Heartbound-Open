package com.cuddly.heartbound.mixins.transformation;

import com.cuddly.heartbound.client.gui.screen.customize.WarmButtonWidget;
import com.cuddly.heartbound.client.rendering.TransformedPlayerAnimatable;
import com.cuddly.heartbound.client.rendering.renderers.AbstractGirlRenderer;
import com.cuddly.heartbound.transformation.TransformablePlayer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({InventoryScreen.class})
public abstract class InventoryScreenMixin extends AbstractRecipeBookScreen<InventoryMenu> {
   protected InventoryScreenMixin() {
      super(null, null, null, null);
   }

   @Inject(
      method = {"extractEntityInInventoryFollowsMouse(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIIIIFFFLnet/minecraft/world/entity/LivingEntity;)V"},
      at = {@At("HEAD")}
   )
   private static void heartbound$beforeDrawEntity(
      GuiGraphicsExtractor context, int x1, int y1, int x2, int y2, int size, float yOffset, float mouseX, float mouseY, LivingEntity entity, CallbackInfo ci
   ) {
      AbstractGirlRenderer.IS_GUI_RENDERING = true;
   }

   @Inject(
      method = {"extractEntityInInventoryFollowsMouse(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIIIIFFFLnet/minecraft/world/entity/LivingEntity;)V"},
      at = {@At("RETURN")}
   )
   private static void heartbound$afterDrawEntity(
      GuiGraphicsExtractor context, int x1, int y1, int x2, int y2, int size, float yOffset, float mouseX, float mouseY, LivingEntity entity, CallbackInfo ci
   ) {
      AbstractGirlRenderer.IS_GUI_RENDERING = false;
   }

   @Inject(
      method = {"init()V"},
      at = {@At("TAIL")}
   )
   private void heartbound$addStripButton(CallbackInfo ci) {
      if (this.minecraft != null && this.minecraft.player != null) {
         Player player = this.minecraft.player;
         if (player instanceof TransformablePlayer tp && tp.heartbound$isTransformed()) {
            Component label = tp.heartbound$isStripped() ? Component.translatable("gui.heartbound.button.dressUp") : Component.translatable("gui.heartbound.button.strip");
            int buttonWidth = 80;
            int buttonHeight = 20;
            int buttonX = this.leftPos - buttonWidth - 10;
            int buttonY = this.topPos + 10;
            WarmButtonWidget button = new WarmButtonWidget(
               this.font, buttonX, buttonY, buttonWidth, buttonHeight, label, btn -> TransformedPlayerAnimatable.INSTANCE.requestStripAnimation()
            );
            if (TransformedPlayerAnimatable.INSTANCE.isStripAnimPlaying()) {
               button.active = false;
            }

            this.addRenderableWidget(button);
            return;
         }
      }
   }
}
