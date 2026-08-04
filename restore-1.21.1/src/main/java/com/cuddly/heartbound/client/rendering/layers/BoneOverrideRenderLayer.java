package com.cuddly.heartbound.client.rendering.layers;

import com.cuddly.heartbound.entity.base.GirlSceneEntity;
import java.util.Map;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class BoneOverrideRenderLayer<T extends GirlSceneEntity> extends GeoRenderLayer<T> {
   public BoneOverrideRenderLayer(GeoRenderer<T> renderer) {
      super(renderer);
   }

   public void render(
      MatrixStack poseStack,
      T animatable,
      BakedGeoModel model,
      RenderLayer renderType,
      VertexConsumerProvider bufferSource,
      VertexConsumer buffer,
      float partialTick,
      int packedLight,
      int packedOverlay
   ) {
      if (renderType != null) {
         if (poseStack != null && poseStack.peek() != null) {
            Map<String, Identifier> layer1 = animatable.boneTextureOverrides;
            Map<String, Identifier> layer2 = animatable.boneTextureOverridesLayer2;
            Map<String, Identifier> layer3 = animatable.boneTextureOverridesLayer3;
            this.renderOverrideLayer(model, poseStack, bufferSource, animatable, layer1, partialTick, packedLight, packedOverlay);
            this.renderOverrideLayer(model, poseStack, bufferSource, animatable, layer2, partialTick, packedLight, packedOverlay);
            this.renderOverrideLayer(model, poseStack, bufferSource, animatable, layer3, partialTick, packedLight, packedOverlay);
         }
      }
   }

   private void renderOverrideLayer(
      BakedGeoModel model,
      MatrixStack poseStack,
      VertexConsumerProvider buffers,
      T animatable,
      Map<String, Identifier> map,
      float partialTick,
      int packedLight,
      int packedOverlay
   ) {
      if (map != null && !map.isEmpty()) {
         map.forEach((boneName, tex) -> {
            if (tex != null) {
               model.getBone(boneName).ifPresent(bone -> {
                  RenderLayer rl = RenderLayer.getEntityTranslucent(tex);
                  VertexConsumer bc = buffers.getBuffer(rl);
                  if (bc != null) {
                     boolean oldHidden = bone.isHidden();
                     bone.setHidden(false);
                     bone.setChildrenHidden(false);
                     int colour = -1;
                     if (animatable.boneColorOverrides != null && animatable.boneColorOverrides.containsKey(boneName)) {
                        colour = animatable.boneColorOverrides.get(boneName);
                     }

                     this.getRenderer().renderRecursively(poseStack, animatable, bone, rl, buffers, bc, false, partialTick, packedLight, packedOverlay, colour);
                     bone.setHidden(oldHidden);
                  }
               });
            }
         });
      }
   }
}
