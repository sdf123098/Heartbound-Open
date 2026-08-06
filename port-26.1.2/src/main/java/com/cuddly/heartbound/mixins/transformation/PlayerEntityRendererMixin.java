package com.cuddly.heartbound.mixins.transformation;

import com.cuddly.heartbound.client.rendering.TransformedPlayerRenderManager;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({AvatarRenderer.class})
public abstract class PlayerEntityRendererMixin {
   @Inject(
      method = {"<init>(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;Z)V"},
      at = {@At("TAIL")}
   )
   private void heartbound$captureContext(Context ctx, boolean slim, CallbackInfo ci) {
      TransformedPlayerRenderManager.captureContext(ctx);
   }
}
