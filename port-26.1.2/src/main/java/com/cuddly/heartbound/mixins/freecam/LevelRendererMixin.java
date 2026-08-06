package com.cuddly.heartbound.mixins.freecam;

import com.cuddly.heartbound.client.rendering.renderers.TransformedPlayerRenderer;
import com.cuddly.heartbound.freecam.Freecam;
import com.cuddly.heartbound.transformation.TransformablePlayer;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LevelRenderer.class})
public abstract class LevelRendererMixin {
   private static final String RENDER_LEVEL_DESC = "renderLevel(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/renderer/state/level/CameraRenderState;Lorg/joml/Matrix4fc;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;ZLnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;)V";
   private static final String EXTRACT_VISIBLE_ENTITIES_DESC = "extractVisibleEntities(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;Lnet/minecraft/client/DeltaTracker;Lnet/minecraft/client/renderer/state/level/LevelRenderState;)V";
   @Shadow
   @Final
   private EntityRenderDispatcher entityRenderDispatcher;

   @Inject(
      method = RENDER_LEVEL_DESC,
      at = {@At("HEAD")}
   )
   private void heartbound$startWorldRender(CallbackInfo ci) {
      TransformedPlayerRenderer.isWorldRenderPass = true;
   }

   @Inject(
      method = RENDER_LEVEL_DESC,
      at = {@At("RETURN")}
   )
   private void heartbound$endWorldRender(CallbackInfo ci) {
      TransformedPlayerRenderer.isWorldRenderPass = false;
   }

   @Redirect(
      method = EXTRACT_VISIBLE_ENTITIES_DESC,
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/Camera;isDetached()Z"
      )
   )
   private boolean heartbound$allowTransformedFirstPerson(Camera camera) {
      if (camera.isDetached()) {
         return true;
      } else {
         if (camera.entity() instanceof TransformablePlayer tp && tp.heartbound$isTransformed()) {
            return true;
         }

         return Freecam.isEnabled() && Freecam.MC.player instanceof TransformablePlayer tp && tp.heartbound$isTransformed();
      }
   }

   @Inject(
      method = EXTRACT_VISIBLE_ENTITIES_DESC,
      at = {@At("TAIL")}
   )
   private void heartbound$renderPlayerDuringFreecam(
      Camera camera, Frustum frustum, DeltaTracker tickCounter, LevelRenderState levelRenderState, CallbackInfo ci
   ) {
      if (Freecam.isEnabled() && Freecam.MC.player != null) {
         Vec3 pos = camera.position();
         Player player = Freecam.MC.player;
         EntityRenderState renderState = null;

         for (EntityRenderState state : levelRenderState.entityRenderStates) {
            if (state.x == player.getX() && state.y == player.getY() && state.z == player.getZ()) {
               renderState = state;
               break;
            }
         }

         if (renderState == null) {
            renderState = this.entityRenderDispatcher.extractEntity(player, tickCounter.getGameTimeDeltaPartialTick(false));
            levelRenderState.entityRenderStates.add(renderState);
         }

         renderState.x = pos.x;
         renderState.y = pos.y;
         renderState.z = pos.z;
      }
   }
}
