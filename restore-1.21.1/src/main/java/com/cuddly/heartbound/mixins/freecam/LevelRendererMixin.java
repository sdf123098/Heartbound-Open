package com.cuddly.heartbound.mixins.freecam;

import com.cuddly.heartbound.client.rendering.renderers.TransformedPlayerRenderer;
import com.cuddly.heartbound.freecam.Freecam;
import com.cuddly.heartbound.transformation.TransformablePlayer;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({WorldRenderer.class})
public abstract class LevelRendererMixin {
   @Shadow
   @Final
   private BufferBuilderStorage bufferBuilders;

   @Shadow
   protected abstract void renderEntity(Entity var1, double var2, double var4, double var6, float var8, MatrixStack var9, VertexConsumerProvider var10);

   @Inject(
      method = {"render(Lnet/minecraft/client/render/RenderTickCounter;ZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V"},
      at = {@At("HEAD")}
   )
   private void heartbound$startWorldRender(CallbackInfo ci) {
      TransformedPlayerRenderer.isWorldRenderPass = true;
   }

   @Inject(
      method = {"render(Lnet/minecraft/client/render/RenderTickCounter;ZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V"},
      at = {@At("RETURN")}
   )
   private void heartbound$endWorldRender(CallbackInfo ci) {
      TransformedPlayerRenderer.isWorldRenderPass = false;
   }

   @Redirect(
      method = {"render(Lnet/minecraft/client/render/RenderTickCounter;ZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/Camera;isThirdPerson()Z"
      )
   )
   private boolean heartbound$allowTransformedFirstPerson(Camera camera) {
      if (camera.isThirdPerson()) {
         return true;
      } else {
         if (camera.getFocusedEntity() instanceof TransformablePlayer tp && tp.heartbound$isTransformed()) {
            return true;
         }

         return Freecam.isEnabled() && Freecam.MC.player instanceof TransformablePlayer tp && tp.heartbound$isTransformed();
      }
   }

   @Inject(
      method = {"render(Lnet/minecraft/client/render/RenderTickCounter;ZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;drawCurrentLayer()V",
         ordinal = 0
      )}
   )
   private void heartbound$renderPlayerDuringFreecam(
      RenderTickCounter tickCounter,
      boolean renderBlockOutline,
      Camera camera,
      GameRenderer gameRenderer,
      LightmapTextureManager lightmapTextureManager,
      Matrix4f matrix4f,
      Matrix4f matrix4f2,
      CallbackInfo ci
   ) {
      if (Freecam.isEnabled() && Freecam.MC.player != null) {
         Vec3d pos = camera.getPos();
         float partialTick = tickCounter.getTickDelta(false);
         MatrixStack matrixStack = new MatrixStack();
         this.renderEntity(Freecam.MC.player, pos.x, pos.y, pos.z, partialTick, matrixStack, this.bufferBuilders.getEntityVertexConsumers());
      }
   }
}
