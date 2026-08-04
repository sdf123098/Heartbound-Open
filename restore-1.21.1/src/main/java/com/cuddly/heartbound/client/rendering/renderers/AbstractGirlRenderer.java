package com.cuddly.heartbound.client.rendering.renderers;

import com.cuddly.heartbound.client.models.AbstractGirlModel;
import com.cuddly.heartbound.config.ModConfig;
import com.cuddly.heartbound.entity.base.GirlSceneEntity;
import com.cuddly.heartbound.networking.C2S.BonePosSyncC2SPacket;
import com.cuddly.heartbound.util.rendering.UVOffsetVertexConsumer;
import com.cuddly.heartbound.util.rendering.UnlitNormalVertexConsumer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

public abstract class AbstractGirlRenderer<T extends GirlSceneEntity> extends GeoEntityRenderer<T> {
   protected final Map<String, Boolean> boneVisibility = new HashMap<>();
   protected Identifier textureOverride = null;
   protected boolean shadingEnabled = true;
   private Set<String> steveBoneCache = null;
   private int syncTickCounter = 0;
   private static final int SYNC_INTERVAL = 1;
   private GeoBone trackedBoyCamBone = null;
   private T trackedAnimatable = (T)null;
   private boolean renderingArmorSubtree = false;
   private float armorSubtreeUOffset = 0.0F;
   private float armorSubtreeVOffset = 0.0F;
   private static Vec3d clientBoyCamPos = null;
   private static int trackedGirlEntityId = -1;
   public static boolean IS_SHADING_DISABLED = false;
   public static boolean IS_GUI_RENDERING = false;

   public static Vec3d getBoyCamPos() {
      return clientBoyCamPos;
   }

   public static int getTrackedGirlEntityId() {
      return trackedGirlEntityId;
   }

   public static void clearCameraPosition() {
      clientBoyCamPos = null;
      trackedGirlEntityId = -1;
   }

   public static void updateShadingState() {
      IS_SHADING_DISABLED = ModConfig.INSTANCE.girls.disableShading;
   }

   public AbstractGirlRenderer(Context ctx, GeoModel<T> model) {
      super(ctx, model);
      this.addRenderLayer(new BlockAndItemGeoLayer<T>(this) {
         protected ItemStack getStackForBone(GeoBone bone, T animatable) {
            String var3 = bone.getName();

            return switch (var3) {
               case "weapon" -> animatable.getEquippedStack(EquipmentSlot.MAINHAND);
               case "weapon2" -> animatable.getEquippedStack(EquipmentSlot.OFFHAND);
               default -> null;
            };
         }

         protected ModelTransformationMode getTransformTypeForStack(GeoBone bone, ItemStack stack, T animatable) {
            String var4 = bone.getName();

            return switch (var4) {
               case "weapon" -> ModelTransformationMode.THIRD_PERSON_RIGHT_HAND;
               case "weapon2" -> ModelTransformationMode.THIRD_PERSON_LEFT_HAND;
               default -> ModelTransformationMode.NONE;
            };
         }
      });
   }

   public void preRender(
      MatrixStack poseStack,
      T animatable,
      BakedGeoModel model,
      VertexConsumerProvider bufferSource,
      VertexConsumer buffer,
      boolean isReRender,
      float partialTick,
      int packedLight,
      int packedOverlay,
      int colour
   ) {
      super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
      this.trackedBoyCamBone = null;
      this.trackedAnimatable = null;
      if (this.steveBoneCache == null) {
         this.steveBoneCache = new HashSet<>();
         model.getBone("steve").ifPresent(steveBone -> this.collectBoneNames(steveBone, this.steveBoneCache));
      }

      if (!isReRender && animatable.isSceneActive() && animatable.passengerBoneName != null) {
         model.getBone(animatable.passengerBoneName).ifPresent(bone -> {
            bone.setTrackingMatrices(true);
            this.trackedBoyCamBone = bone;
            this.trackedAnimatable = animatable;
         });
      } else if (!isReRender && animatable.getId() == trackedGirlEntityId) {
         clearCameraPosition();
      }

      this.updateBoneVisibility(animatable, model);
   }

   private void collectBoneNames(GeoBone bone, Set<String> names) {
      names.add(bone.getName());

      for (GeoBone child : bone.getChildBones()) {
         this.collectBoneNames(child, names);
      }
   }

   public void renderRecursively(
      MatrixStack poseStack,
      T animatable,
      GeoBone bone,
      RenderLayer renderType,
      VertexConsumerProvider bufferSource,
      VertexConsumer buffer,
      boolean isReRender,
      float partialTick,
      int packedLight,
      int packedOverlay,
      int colour
   ) {
      int boneColour = colour;
      if (animatable.boneColorOverrides != null && animatable.boneColorOverrides.containsKey(bone.getName())) {
         boneColour = animatable.boneColorOverrides.get(bone.getName());
      }

      boolean forceUnlit = IS_SHADING_DISABLED || IS_GUI_RENDERING;
      int lightLevel = forceUnlit ? 15728880 : packedLight;
      if ("steve".equals(bone.getName()) && animatable.isSceneActive()) {
         Identifier skinTexture = animatable.boneTextureOverrides.get("steve");
         if (skinTexture != null) {
            RenderLayer skinRenderType = RenderLayer.getEntityTranslucent(skinTexture);
            VertexConsumer skinBuffer = bufferSource.getBuffer(skinRenderType);
            if (forceUnlit) {
               skinBuffer = new UnlitNormalVertexConsumer(skinBuffer);
            }

            super.renderRecursively(
               poseStack, animatable, bone, skinRenderType, bufferSource, skinBuffer, isReRender, partialTick, lightLevel, packedOverlay, boneColour
            );
            Identifier layer2Texture = animatable.boneTextureOverridesLayer2 != null ? animatable.boneTextureOverridesLayer2.get("steve") : null;
            if (layer2Texture != null) {
               RenderLayer layer2RenderType = RenderLayer.getEntityTranslucent(layer2Texture);
               VertexConsumer layer2Buffer = bufferSource.getBuffer(layer2RenderType);
               if (forceUnlit) {
                  layer2Buffer = new UnlitNormalVertexConsumer(layer2Buffer);
               }

               super.renderRecursively(
                  poseStack, animatable, bone, layer2RenderType, bufferSource, layer2Buffer, true, partialTick, lightLevel, packedOverlay, boneColour
               );
            }

            Identifier layer3Texture = animatable.boneTextureOverridesLayer3 != null ? animatable.boneTextureOverridesLayer3.get("steve") : null;
            if (layer3Texture != null) {
               RenderLayer layer3RenderType = RenderLayer.getEntityTranslucent(layer3Texture);
               VertexConsumer layer3Buffer = bufferSource.getBuffer(layer3RenderType);
               if (forceUnlit) {
                  layer3Buffer = new UnlitNormalVertexConsumer(layer3Buffer);
               }

               super.renderRecursively(
                  poseStack, animatable, bone, layer3RenderType, bufferSource, layer3Buffer, true, partialTick, lightLevel, packedOverlay, boneColour
               );
            }

            return;
         }
      }

      Vec2f uvOffset = animatable.boneUVOffsets != null ? animatable.boneUVOffsets.get(bone.getName()) : null;
      if (uvOffset != null) {
         VertexConsumer armorBuffer;
         if (forceUnlit) {
            armorBuffer = new UnlitNormalVertexConsumer(bufferSource.getBuffer(renderType));
         } else {
            armorBuffer = buffer;
         }

         if (uvOffset.x != 0.0F || uvOffset.y != 0.0F) {
            armorBuffer = new UVOffsetVertexConsumer(armorBuffer, uvOffset.x, uvOffset.y);
         }

         this.renderingArmorSubtree = true;
         this.armorSubtreeUOffset = uvOffset.x;
         this.armorSubtreeVOffset = uvOffset.y;
         super.renderRecursively(
            poseStack, animatable, bone, renderType, bufferSource, armorBuffer, isReRender, partialTick, lightLevel, packedOverlay, boneColour
         );
         this.renderingArmorSubtree = false;
      } else if (this.renderingArmorSubtree) {
         VertexConsumer armorBufferx;
         if (forceUnlit) {
            armorBufferx = new UnlitNormalVertexConsumer(bufferSource.getBuffer(renderType));
         } else {
            armorBufferx = buffer;
         }

         if (this.armorSubtreeUOffset != 0.0F || this.armorSubtreeVOffset != 0.0F) {
            armorBufferx = new UVOffsetVertexConsumer(armorBufferx, this.armorSubtreeUOffset, this.armorSubtreeVOffset);
         }

         super.renderRecursively(
            poseStack, animatable, bone, renderType, bufferSource, armorBufferx, isReRender, partialTick, lightLevel, packedOverlay, boneColour
         );
      } else {
         VertexConsumer renderBuffer;
         if (forceUnlit) {
            renderBuffer = new UnlitNormalVertexConsumer(bufferSource.getBuffer(renderType));
         } else {
            renderBuffer = buffer;
         }

         super.renderRecursively(
            poseStack, animatable, bone, renderType, bufferSource, renderBuffer, isReRender, partialTick, lightLevel, packedOverlay, boneColour
         );
      }
   }

   public void postRender(
      MatrixStack poseStack,
      T animatable,
      BakedGeoModel model,
      VertexConsumerProvider bufferSource,
      VertexConsumer buffer,
      boolean isReRender,
      float partialTick,
      int packedLight,
      int packedOverlay,
      int colour
   ) {
      super.postRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
      if (!isReRender && this.trackedBoyCamBone != null && this.trackedAnimatable == animatable) {
         Vector3d worldPos = this.trackedBoyCamBone.getWorldPosition();
         if (worldPos == null || Double.isNaN(worldPos.x) || Double.isNaN(worldPos.y) || Double.isNaN(worldPos.z)) {
            return;
         }

         if (Math.abs(worldPos.x) < 1.0E-6 && Math.abs(worldPos.y) < 1.0E-6 && Math.abs(worldPos.z) < 1.0E-6) {
            return;
         }

         clientBoyCamPos = new Vec3d(worldPos.x, worldPos.y, worldPos.z);
         trackedGirlEntityId = animatable.getId();
         Vec3d entityPos = animatable.getPos();
         Vec3d relativePos = new Vec3d(worldPos.x - entityPos.x, worldPos.y - entityPos.y, worldPos.z - entityPos.z);
         if (Math.abs(relativePos.x) > 50.0 || Math.abs(relativePos.y) > 50.0 || Math.abs(relativePos.z) > 50.0) {
            return;
         }

         this.syncBoyCamPosition(animatable, relativePos);
      }
   }

   private void syncBoyCamPosition(T animatable, Vec3d position) {
      if (++this.syncTickCounter >= 1) {
         this.syncTickCounter = 0;
         ClientPlayNetworking.send(new BonePosSyncC2SPacket(animatable.getId(), position));
      }
   }

   protected void updateBoneVisibility(T entity, BakedGeoModel bakedModel) {
      boolean isSceneActive = entity.isSceneActive();
      bakedModel.getBone("steve").ifPresent(bonex -> bonex.setHidden(!isSceneActive));
      if (entity.boneVisibility != null) {
         for (Entry<String, Boolean> entry : entity.boneVisibility.entrySet()) {
            bakedModel.getBone(entry.getKey()).ifPresent(bonex -> bonex.setHidden(!entry.getValue()));
         }
      }

      if (this.model instanceof AbstractGirlModel) {
         for (Entry<String, Boolean> entry : this.boneVisibility.entrySet()) {
            GeoBone bone = this.model.getBone(entry.getKey()).orElse(null);
            if (bone != null) {
               bone.setHidden(!entry.getValue());
            }
         }
      }
   }

   public void setBoneVisibility(String boneName, boolean visible) {
      this.boneVisibility.put(boneName, visible);
   }

   public void setTextureOverride(Identifier texture) {
      this.textureOverride = texture;
   }

   public void clearTextureOverride() {
      this.textureOverride = null;
   }

   public Identifier getTextureLocation(T animatable) {
      return this.textureOverride != null ? this.textureOverride : super.getTextureLocation(animatable);
   }

   public void setShadingEnabled(boolean enabled) {
      this.shadingEnabled = enabled;
   }
}
