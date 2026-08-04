package com.cuddly.heartbound.client.rendering.renderers;

import com.cuddly.heartbound.client.models.TransformedPlayerModel;
import com.cuddly.heartbound.client.rendering.TransformedPlayerAnimatable;
import com.cuddly.heartbound.config.ModConfig;
import com.cuddly.heartbound.networking.C2S.BonePosSyncC2SPacket;
import com.cuddly.heartbound.transformation.TransformablePlayer;
import com.cuddly.heartbound.util.rendering.UVOffsetVertexConsumer;
import com.cuddly.heartbound.util.rendering.UnlitNormalVertexConsumer;
import com.cuddly.heartbound.util.variables.Scene;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.SkinTextures.Model;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.GeoReplacedEntityRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

public class TransformedPlayerRenderer extends GeoReplacedEntityRenderer<PlayerEntity, TransformedPlayerAnimatable> {
   private final TransformedPlayerModel girlModel;
   private static final Map<EquipmentSlot, List<String>> ARMOR_BONES = Map.of(
      EquipmentSlot.HEAD,
      List.of("armorHelmet"),
      EquipmentSlot.CHEST,
      List.of("armorBoobs", "armorChest", "armorShoulderL", "armorShoulderR"),
      EquipmentSlot.LEGS,
      List.of("armorHip", "armorPantsLowL", "armorPantsUpL", "armorPantsLowR", "armorPantsUpR", "armorBootyL", "armorBootyR", "armorKneeL", "armorKneeR"),
      EquipmentSlot.FEET,
      List.of("armorShoesL", "armorShoesR")
   );
   private static final float ARMOR_UV_STEP = 0.017578125F;
   private final Map<String, Float> armorBoneUVOffsets = new HashMap<>();
   private final Map<String, Integer> armorBoneColors = new HashMap<>();
   private static final Set<String> ALL_ARMOR_BONE_NAMES;
   private static final Set<String> ARMOR_TINT_BONES = Set.of("armorBoobs", "armorBootyL", "armorBootyR");
   private boolean skipVisualRender = false;
   private boolean renderingSteveSubtree = false;
   private boolean renderingArmorSubtree = false;
   private float armorSubtreeUVOffset = 0.0F;
   private int armorSubtreeColour = -1;
   private static final VertexConsumer NO_OP_BUFFER = new VertexConsumer() {
      @Override
      public VertexConsumer vertex(float x, float y, float z) {
         return this;
      }

      @Override
      public VertexConsumer color(int red, int green, int blue, int alpha) {
         return this;
      }

      @Override
      public VertexConsumer texture(float u, float v) {
         return this;
      }

      @Override
      public VertexConsumer overlay(int u, int v) {
         return this;
      }

      @Override
      public VertexConsumer light(int u, int v) {
         return this;
      }

      @Override
      public VertexConsumer normal(float x, float y, float z) {
         return this;
      }
   };
   private GeoBone trackedBoyCamBone = null;
   private GeoBone trackedGirlCamBone = null;
   private int syncTickCounter = 0;
   private static final int SYNC_INTERVAL = 1;
   private Identifier cachedSkinTexture = null;
   private UUID cachedSkinPlayerUuid = null;
   private boolean cachedIsSlimModel = false;
   public static boolean isWorldRenderPass = false;
   private static Vec3d clientGirlCamPos = null;
   private static Vec3d clientBoyCamPos = null;
   private static int trackedSceneEntityId = -1;

   public static Vec3d getGirlCamPos() {
      return clientGirlCamPos;
   }

   public static Vec3d getBoyCamPos() {
      return clientBoyCamPos;
   }

   public static int getTrackedSceneEntityId() {
      return trackedSceneEntityId;
   }

   public static void clearCameraPositions() {
      clientGirlCamPos = null;
      clientBoyCamPos = null;
      trackedSceneEntityId = -1;
   }

   public TransformedPlayerRenderer(Context context) {
      super(context, new TransformedPlayerModel(), TransformedPlayerAnimatable.INSTANCE);
      this.girlModel = (TransformedPlayerModel)this.getGeoModel();
      this.addRenderLayer(
         new BlockAndItemGeoLayer<TransformedPlayerAnimatable>(this) {
            protected ItemStack getStackForBone(GeoBone bone, TransformedPlayerAnimatable animatable) {
               if (TransformedPlayerRenderer.this.skipVisualRender) {
                  return null;
               } else {
                  PlayerEntity player = animatable.getCurrentEntity();
                  if (player == null) {
                     return null;
                  } else {
                     if (player instanceof TransformablePlayer tp && tp.heartbound$isTransformSceneActive()) {
                        return null;
                     }

                     String var6 = bone.getName();

                     return switch (var6) {
                        case "weapon" -> player.getEquippedStack(EquipmentSlot.MAINHAND);
                        case "weapon2" -> player.getEquippedStack(EquipmentSlot.OFFHAND);
                        default -> null;
                     };
                  }
               }
            }

            protected ModelTransformationMode getTransformTypeForStack(GeoBone bone, ItemStack stack, TransformedPlayerAnimatable animatable) {
               String var4 = bone.getName();

               return switch (var4) {
                  case "weapon" -> ModelTransformationMode.THIRD_PERSON_RIGHT_HAND;
                  case "weapon2" -> ModelTransformationMode.THIRD_PERSON_LEFT_HAND;
                  default -> ModelTransformationMode.NONE;
               };
            }

            protected void renderStackForBone(
               MatrixStack poseStack,
               GeoBone bone,
               ItemStack stack,
               TransformedPlayerAnimatable animatable,
               VertexConsumerProvider bufferSource,
               float partialTick,
               int packedLight,
               int packedOverlay
            ) {
               PlayerEntity player = animatable.getCurrentEntity();
               if (player != null) {
                  MinecraftClient.getInstance()
                     .getItemRenderer()
                     .renderItem(
                        player,
                        stack,
                        this.getTransformTypeForStack(bone, stack, animatable),
                        false,
                        poseStack,
                        bufferSource,
                        player.getWorld(),
                        packedLight,
                        packedOverlay,
                        player.getId()
                     );
               }
            }
         }
      );
   }

   protected void applyRotations(
      TransformedPlayerAnimatable animatable, MatrixStack poseStack, float ageInTicks, float rotationYaw, float partialTick, float nativeScale
   ) {
      if (this.isShaking(animatable)) {
         rotationYaw += (float)(Math.cos((double)((PlayerEntity)this.currentEntity).age * 3.25) * Math.PI * 0.4);
      }

      if (!((PlayerEntity)this.currentEntity).isInPose(EntityPose.SLEEPING)) {
         poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - rotationYaw));
      }
   }

   public void setGirlId(String girlId) {
      this.girlModel.setGirlId(girlId);
   }

   public void preRender(
      MatrixStack poseStack,
      TransformedPlayerAnimatable animatable,
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
      PlayerEntity player = animatable.getCurrentEntity();
      boolean sceneActive = false;
      boolean showSteve = false;
      if (player != null) {
         TransformablePlayer tp = (TransformablePlayer)player;
         sceneActive = tp.heartbound$isTransformSceneActive();
         if (sceneActive) {
            Scene scene = tp.heartbound$getTransformScene();
            boolean hasPartner = !player.getPassengerList().isEmpty() || tp.heartbound$getTransformScenePartner().isPresent();
            showSteve = !scene.hidePlayer() && hasPartner;
         }
      }

      boolean steveVisible = showSteve;
      this.trackedBoyCamBone = null;
      this.trackedGirlCamBone = null;
      model.getBone("steve").ifPresent(bone -> bone.setHidden(!steveVisible));
      this.armorBoneUVOffsets.clear();
      this.armorBoneColors.clear();
      if (player != null) {
         TransformablePlayer tp2 = (TransformablePlayer)player;
         boolean stripped = tp2.heartbound$isStripped();

         for (Entry<EquipmentSlot, List<String>> entry : ARMOR_BONES.entrySet()) {
            EquipmentSlot slot = entry.getKey();
            ItemStack stack = player.getEquippedStack(slot);
            boolean hasArmor = !stack.isEmpty() && !stripped;

            for (String boneName : entry.getValue()) {
               model.getBone(boneName).ifPresent(bone -> bone.setHidden(!hasArmor));
            }

            if (hasArmor) {
               float uvOffset = this.getArmorUVOffset(stack);
               int dyeColor = this.getArmorDyeColor(stack);
               int materialColor = this.getArmorMaterialColor(stack);

               for (String boneName : entry.getValue()) {
                  this.armorBoneUVOffsets.put(boneName, uvOffset);
                  if (dyeColor != -1) {
                     this.armorBoneColors.put(boneName, dyeColor | 0xFF000000);
                  } else if (ARMOR_TINT_BONES.contains(boneName)) {
                     this.armorBoneColors.put(boneName, materialColor | 0xFF000000);
                  }
               }
            }
         }
      } else {
         ALL_ARMOR_BONE_NAMES.forEach(boneNamex -> model.getBone(boneNamex).ifPresent(bone -> bone.setHidden(true)));
      }

      if (steveVisible) {
         boolean slim = this.cachedIsSlimModel;
         model.getBone("leftArmSteve").ifPresent(b -> b.setHidden(slim));
         model.getBone("leftLowerArmSteve").ifPresent(b -> b.setHidden(slim));
         model.getBone("rightArmSteve").ifPresent(b -> b.setHidden(slim));
         model.getBone("rightLowerArmSteve").ifPresent(b -> b.setHidden(slim));
         model.getBone("leftArmAlex").ifPresent(b -> b.setHidden(!slim));
         model.getBone("leftLowerArmAlex").ifPresent(b -> b.setHidden(!slim));
         model.getBone("rightArmAlex").ifPresent(b -> b.setHidden(!slim));
         model.getBone("rightLowerArmAlex").ifPresent(b -> b.setHidden(!slim));
      }

      if (!isReRender && sceneActive) {
         model.getBone("girlCam").ifPresent(bone -> {
            bone.setTrackingMatrices(true);
            this.trackedGirlCamBone = bone;
         });
         if (steveVisible) {
            model.getBone("boyCam").ifPresent(bone -> {
               bone.setTrackingMatrices(true);
               this.trackedBoyCamBone = bone;
            });
         }

         if (player != null && showSteve) {
            this.applySkinToSteve(player);
         }
      } else if (!isReRender && player != null && player.getId() == trackedSceneEntityId) {
         clearCameraPositions();
      }

      if (!isReRender) {
         if (player != null && isWorldRenderPass) {
            MinecraftClient client = MinecraftClient.getInstance();
            boolean isFirstPerson = client.options.getPerspective().isFirstPerson();
            boolean isLocalPlayer = client.cameraEntity == player;
            this.skipVisualRender = isFirstPerson && isLocalPlayer && !sceneActive;
         } else {
            this.skipVisualRender = false;
         }
      }
   }

   public void renderRecursively(
      MatrixStack poseStack,
      TransformedPlayerAnimatable animatable,
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
      boolean forceUnlit = AbstractGirlRenderer.IS_SHADING_DISABLED || AbstractGirlRenderer.IS_GUI_RENDERING;
      int lightLevel = forceUnlit ? 15728880 : packedLight;
      if ("steve".equals(bone.getName()) && this.cachedSkinTexture != null) {
         PlayerEntity player = animatable.getCurrentEntity();
         if (player != null) {
            TransformablePlayer tp = (TransformablePlayer)player;
            if (tp.heartbound$isTransformSceneActive()) {
               RenderLayer skinRenderType = RenderLayer.getEntityTranslucent(this.cachedSkinTexture);
               VertexConsumer skinBuffer = bufferSource.getBuffer(skinRenderType);
               if (forceUnlit) {
                  skinBuffer = new UnlitNormalVertexConsumer(skinBuffer);
               }

               this.renderingSteveSubtree = true;
               super.renderRecursively(
                  poseStack, animatable, bone, skinRenderType, bufferSource, skinBuffer, isReRender, partialTick, lightLevel, packedOverlay, colour
               );
               if (!isReRender) {
                  Identifier penisTexture = Identifier.of("heartbound", "textures/player/penis.png");
                  RenderLayer penisRenderType = RenderLayer.getEntityTranslucent(penisTexture);
                  VertexConsumer penisBuffer = bufferSource.getBuffer(penisRenderType);
                  if (forceUnlit) {
                     penisBuffer = new UnlitNormalVertexConsumer(penisBuffer);
                  }

                  super.renderRecursively(
                     poseStack, animatable, bone, penisRenderType, bufferSource, penisBuffer, true, partialTick, lightLevel, packedOverlay, colour
                  );
               }

               this.renderingSteveSubtree = false;
               return;
            }
         }
      }

      if (this.renderingSteveSubtree) {
         int steveColour = colour;
         String steveBoneName = bone.getName();
         if ("nut".equals(steveBoneName)) {
            steveColour = ModConfig.INSTANCE.player.penisHeadColor | 0xFF000000;
         } else if ("shaft".equals(steveBoneName) || "ballL".equals(steveBoneName) || "ballR".equals(steveBoneName)) {
            steveColour = ModConfig.INSTANCE.player.penisShaftColor | 0xFF000000;
         }

         VertexConsumer renderBuffer;
         if (forceUnlit) {
            renderBuffer = new UnlitNormalVertexConsumer(bufferSource.getBuffer(renderType));
         } else {
            renderBuffer = buffer;
         }

         super.renderRecursively(
            poseStack, animatable, bone, renderType, bufferSource, renderBuffer, isReRender, partialTick, lightLevel, packedOverlay, steveColour
         );
      } else if (this.skipVisualRender) {
         super.renderRecursively(
            poseStack, animatable, bone, renderType, bufferSource, NO_OP_BUFFER, isReRender, partialTick, packedLight, packedOverlay, colour
         );
      } else {
         String boneName = bone.getName();
         if (ALL_ARMOR_BONE_NAMES.contains(boneName)) {
            Float uvOffset = this.armorBoneUVOffsets.get(boneName);
            if (uvOffset != null) {
               int armorColour = this.armorBoneColors.getOrDefault(boneName, colour);
               VertexConsumer armorBuffer;
               if (forceUnlit) {
                  armorBuffer = new UnlitNormalVertexConsumer(bufferSource.getBuffer(renderType));
               } else {
                  armorBuffer = buffer;
               }

               if (uvOffset != 0.0F) {
                  armorBuffer = new UVOffsetVertexConsumer(armorBuffer, uvOffset, 0.0F);
               }

               this.renderingArmorSubtree = true;
               this.armorSubtreeUVOffset = uvOffset;
               this.armorSubtreeColour = armorColour;
               super.renderRecursively(
                  poseStack, animatable, bone, renderType, bufferSource, armorBuffer, isReRender, partialTick, lightLevel, packedOverlay, armorColour
               );
               this.renderingArmorSubtree = false;
            }
         } else if (this.renderingArmorSubtree) {
            VertexConsumer armorBufferx;
            if (forceUnlit) {
               armorBufferx = new UnlitNormalVertexConsumer(bufferSource.getBuffer(renderType));
            } else {
               armorBufferx = buffer;
            }

            if (this.armorSubtreeUVOffset != 0.0F) {
               armorBufferx = new UVOffsetVertexConsumer(armorBufferx, this.armorSubtreeUVOffset, 0.0F);
            }

            super.renderRecursively(
               poseStack, animatable, bone, renderType, bufferSource, armorBufferx, isReRender, partialTick, lightLevel, packedOverlay, this.armorSubtreeColour
            );
         } else {
            int boneColour = colour;
            if ("nut".equals(boneName)) {
               boneColour = ModConfig.INSTANCE.player.penisHeadColor | 0xFF000000;
            } else if ("shaft".equals(boneName) || "ballL".equals(boneName) || "ballR".equals(boneName)) {
               boneColour = ModConfig.INSTANCE.player.penisShaftColor | 0xFF000000;
            }

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
   }

   public void postRender(
      MatrixStack poseStack,
      TransformedPlayerAnimatable animatable,
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
      if (!isReRender) {
         PlayerEntity player = animatable.getCurrentEntity();
         if (player != null) {
            if (this.trackedGirlCamBone != null) {
               Vector3d gPos = this.trackedGirlCamBone.getWorldPosition();
               if (gPos != null && !Double.isNaN(gPos.x) && !Double.isNaN(gPos.y) && !Double.isNaN(gPos.z)) {
                  clientGirlCamPos = new Vec3d(gPos.x, gPos.y, gPos.z);
                  trackedSceneEntityId = player.getId();
               }
            }

            if (this.trackedBoyCamBone != null) {
               Vector3d worldPos = this.trackedBoyCamBone.getWorldPosition();
               if (worldPos == null || Double.isNaN(worldPos.x) || Double.isNaN(worldPos.y) || Double.isNaN(worldPos.z)) {
                  return;
               }

               if (Math.abs(worldPos.x) < 1.0E-6 && Math.abs(worldPos.y) < 1.0E-6 && Math.abs(worldPos.z) < 1.0E-6) {
                  return;
               }

               clientBoyCamPos = new Vec3d(worldPos.x, worldPos.y, worldPos.z);
               Vec3d entityPos = player.getPos();
               Vec3d relativePos = new Vec3d(worldPos.x - entityPos.x, worldPos.y - entityPos.y, worldPos.z - entityPos.z);
               if (Math.abs(relativePos.x) > 50.0 || Math.abs(relativePos.y) > 50.0 || Math.abs(relativePos.z) > 50.0) {
                  return;
               }

               this.syncBoyCamPosition(player, relativePos);
            }
         }
      }
   }

   private void syncBoyCamPosition(PlayerEntity player, Vec3d position) {
      if (++this.syncTickCounter >= 1) {
         this.syncTickCounter = 0;
         ClientPlayNetworking.send(new BonePosSyncC2SPacket(player.getId(), position));
      }
   }

   private void applySkinToSteve(PlayerEntity transformedPlayer) {
      TransformablePlayer tp = (TransformablePlayer)transformedPlayer;
      Optional<UUID> partnerUuid = tp.heartbound$getTransformScenePartner();
      if (!partnerUuid.isEmpty()) {
         UUID targetUuid = partnerUuid.get();
         if (!targetUuid.equals(this.cachedSkinPlayerUuid) || this.cachedSkinTexture == null) {
            this.cachedSkinTexture = Identifier.ofVanilla("textures/entity/player/wide/steve.png");
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world != null) {
               for (PlayerEntity worldPlayer : client.world.getPlayers()) {
                  if (worldPlayer.getUuid().equals(targetUuid)) {
                     this.fetchAndCacheSkin(worldPlayer);
                     return;
                  }
               }
            }
         }
      } else {
         if (!transformedPlayer.getPassengerList().isEmpty() && transformedPlayer.getPassengerList().get(0) instanceof PlayerEntity rider) {
            this.fetchAndCacheSkin(rider);
         } else {
            this.cachedSkinTexture = Identifier.ofVanilla("textures/entity/player/wide/steve.png");
            this.cachedSkinPlayerUuid = null;
         }
      }
   }

   private void fetchAndCacheSkin(PlayerEntity player) {
      if (player instanceof AbstractClientPlayerEntity clientPlayer) {
         SkinTextures skin = clientPlayer.getSkinTextures();
         Identifier texture = skin.texture();
         if (texture != null) {
            this.cachedSkinTexture = texture;
            this.cachedSkinPlayerUuid = player.getUuid();
            this.cachedIsSlimModel = skin.model() == Model.SLIM;
         }
      }
   }

   private float getArmorUVOffset(ItemStack stack) {
      String armorType = stack.toString().toLowerCase();
      if (armorType.contains("diamond")) {
         return 0.017578125F;
      } else if (armorType.contains("gold")) {
         return 0.03515625F;
      } else if (armorType.contains("iron")) {
         return 0.052734375F;
      } else if (armorType.contains("copper")) {
         return 0.0703125F;
      } else if (armorType.contains("chain")) {
         return 0.087890625F;
      } else if (armorType.contains("leather")) {
         return 0.10546875F;
      } else {
         return armorType.contains("turtle") ? 0.123046875F : 0.0F;
      }
   }

   private int getArmorDyeColor(ItemStack stack) {
      if (stack.isEmpty()) {
         return -1;
      } else {
         String armorType = stack.toString().toLowerCase();
         if (!armorType.contains("leather")) {
            return -1;
         } else {
            DyedColorComponent dyed = stack.get(DataComponentTypes.DYED_COLOR);
            return dyed != null ? dyed.rgb() : 10511680;
         }
      }
   }

   private int getArmorMaterialColor(ItemStack stack) {
      String armorType = stack.toString().toLowerCase();
      if (armorType.contains("netherite")) {
         return 5787725;
      } else if (armorType.contains("diamond")) {
         return 7200992;
      } else if (armorType.contains("gold")) {
         return 14597695;
      } else if (armorType.contains("iron")) {
         return 13684944;
      } else if (armorType.contains("copper")) {
         return 11823181;
      } else if (armorType.contains("chain")) {
         return 8421504;
      } else if (armorType.contains("leather")) {
         return 10511680;
      } else {
         return armorType.contains("turtle") ? 3843642 : 16777215;
      }
   }

   static {
      Set<String> names = new HashSet<>();
      ARMOR_BONES.values().forEach(names::addAll);
      ALL_ARMOR_BONE_NAMES = Set.copyOf(names);
   }
}
