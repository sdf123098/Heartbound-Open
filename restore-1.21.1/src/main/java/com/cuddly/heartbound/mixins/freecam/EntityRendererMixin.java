package com.cuddly.heartbound.mixins.freecam;

import com.cuddly.heartbound.freecam.Freecam;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({EntityRenderer.class})
public class EntityRendererMixin<T extends Entity> {
   @Inject(
      method = {"renderLabelIfPresent(Lnet/minecraft/entity/Entity;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IF)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRenderLabel(T entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float tickDelta, CallbackInfo ci) {
      if (Freecam.isEnabled() && ((EntityRenderDispatcherAccessor)Freecam.MC.getEntityRenderDispatcher()).isRenderShadows()) {
         ci.cancel();
      }
   }
}
