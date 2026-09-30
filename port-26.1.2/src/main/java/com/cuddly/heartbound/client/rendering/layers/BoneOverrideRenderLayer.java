package com.cuddly.heartbound.client.rendering.layers;

import com.cuddly.heartbound.client.rendering.renderers.AbstractGirlRenderer.GirlRenderState;
import com.cuddly.heartbound.entity.base.GirlSceneEntity;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.cache.model.cuboid.CuboidGeoBone;
import com.geckolib.cache.model.cuboid.GeoCube;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.PerBoneRender;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;
import com.geckolib.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.BiConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

/**
 * GeckoLib 5 移植版骨骼纹理覆盖层（原 1.21.1 renderRecursively 逻辑的对应实现）。
 * 机制：GeckoLib 5 的 PerBoneRender —— addPerBoneRender 注册骨骼级渲染任务，
 * submitPerBoneRenderTasks 在骨骼变换下调用 submitRenderTask，闭包经
 * submitCustomGeometry 延迟渲染。steve 骨（玩家形象）在场景中需用玩家皮肤纹理
 * 渲染（+ penis 纹理 layer2/layer3、boneColorOverrides 颜色），默认渲染在
 * adjustModelBonesForRender 里通过 BoneSnapshot.skipRender 隐藏。
 */
public class BoneOverrideRenderLayer<T extends GirlSceneEntity> extends GeoRenderLayer<T, Void, GirlRenderState> {
   public BoneOverrideRenderLayer(GeoRenderer<T, Void, GirlRenderState> renderer) {
      super(renderer);
   }

   @Override
   public void addPerBoneRender(RenderPassInfo<GirlRenderState> renderPassInfo, BiConsumer<GeoBone, PerBoneRender<GirlRenderState>> consumer) {
      GirlSceneEntity animatable = renderPassInfo.renderState().animatable;
      if (animatable == null || !animatable.isSceneActive()) {
         return;
      }

      // LAYING_DOWN and BED_IDLE are pre-contact animation phases, but the server
      // currently also exposes them as scene-active. Do not render the player's
      // replacement mesh until that player has actually mounted the girl.
      if (!animatable.isHavingSex()
         || !(animatable.getFirstPassenger() instanceof Player passenger)
         || !animatable.isCurrentScenePlayer(passenger)) {
         return;
      }

      Identifier skin = animatable.boneTextureOverrides != null && animatable.boneTextureOverrides.get("steve") != null
         ? animatable.boneTextureOverrides.get("steve")
         : Identifier.withDefaultNamespace("textures/entity/player/wide/steve.png");
      Identifier layer2 = animatable.boneTextureOverridesLayer2 != null ? animatable.boneTextureOverridesLayer2.get("steve") : null;
      Identifier layer3 = animatable.boneTextureOverridesLayer3 != null ? animatable.boneTextureOverridesLayer3.get("steve") : null;
      Minecraft client = Minecraft.getInstance();
      boolean hideHeadForCamera = client.options.getCameraType().isFirstPerson() && client.getCameraEntity() == passenger;
      renderPassInfo.model().getBone("steve").ifPresent(bone -> consumer.accept(bone, (passInfo, b, tasks) -> {
         this.submitSteveSubtree(passInfo, b, tasks, animatable, skin, hideHeadForCamera);
         if (layer2 != null) {
            this.submitSteveSubtree(passInfo, b, tasks, animatable, layer2, hideHeadForCamera);
         }

         if (layer3 != null) {
            this.submitSteveSubtree(passInfo, b, tasks, animatable, layer3, hideHeadForCamera);
         }
      }));
   }

   private void submitSteveSubtree(
      RenderPassInfo<GirlRenderState> passInfo,
      GeoBone bone,
      SubmitNodeCollector tasks,
      GirlSceneEntity animatable,
      Identifier texture,
      boolean hideHeadForCamera
   ) {
      int boneColor = animatable.boneColorOverrides != null ? animatable.boneColorOverrides.getOrDefault("steve", -1) : -1;
      RenderType renderType = RenderTypes.entityTranslucentCullItemTarget(texture);
      tasks.submitCustomGeometry(passInfo.poseStack(), renderType, (pose, vertexConsumer) -> {
         PoseStack poseStack = passInfo.poseStack();
         poseStack.pushPose();
         // Per-bone tasks receive an attachment pose: GeoLib has already moved to the
         // requested bone's pivot. Cubes, however, must render after moving back from
         // that pivot, exactly as GeoBone.positionAndRender does. Without this inverse
         // translation the root steve mesh and its children use different spaces.
         poseStack.last().set(pose);
         bone.translateAwayFromPivotPoint(poseStack);
         this.renderBoneSubtree(
            passInfo, bone, poseStack, vertexConsumer, animatable, boneColor, hideHeadForCamera
         );
         poseStack.popPose();
      });
   }

   private void renderBoneSubtree(
      RenderPassInfo<GirlRenderState> passInfo, GeoBone bone, PoseStack poseStack, com.mojang.blaze3d.vertex.VertexConsumer vertexConsumer,
      GirlSceneEntity animatable, int inheritedColor, boolean hideHeadForCamera
   ) {
      if (animatable.boneVisibility != null && Boolean.FALSE.equals(animatable.boneVisibility.get(bone.name()))) {
         return;
      }

      int color = inheritedColor;
      if (animatable.boneColorOverrides != null) {
         color = animatable.boneColorOverrides.getOrDefault(bone.name(), inheritedColor);
      }

      boolean hideBoneGeometry = hideHeadForCamera && "Head2".equals(bone.name());
      if (!hideBoneGeometry && bone instanceof CuboidGeoBone cuboid) {
         for (GeoCube cube : cuboid.cubes) {
            cube.render(poseStack, vertexConsumer, passInfo.packedLight(), passInfo.packedOverlay(), color);
         }
      }

      for (GeoBone child : bone.children()) {
         poseStack.pushPose();
         RenderUtil.prepMatrixForBoneAndUpdateListeners(poseStack, child, passInfo);
         child.translateAwayFromPivotPoint(poseStack);
         this.renderBoneSubtree(
            passInfo, child, poseStack, vertexConsumer, animatable, color, hideHeadForCamera
         );
         poseStack.popPose();
      }
   }
}
