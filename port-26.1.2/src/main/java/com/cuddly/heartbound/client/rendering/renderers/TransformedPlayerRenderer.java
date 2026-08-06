package com.cuddly.heartbound.client.rendering.renderers;

import com.cuddly.heartbound.client.models.TransformedPlayerModel;
import com.cuddly.heartbound.client.rendering.TransformedPlayerAnimatable;
import com.cuddly.heartbound.networking.C2S.BonePosSyncC2SPacket;
import com.cuddly.heartbound.transformation.TransformablePlayer;
import com.cuddly.heartbound.util.variables.Scene;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoReplacedEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.builtin.BlockAndItemGeoLayer;
import com.geckolib.renderer.layer.builtin.BlockAndItemGeoLayer.RenderData;

public class TransformedPlayerRenderer extends GeoReplacedEntityRenderer<TransformedPlayerAnimatable, Player, TransformedPlayerRenderer.TransformedPlayerRenderState> {
   /**
    * 注意：GeckoLib 5.5.2 的 EntityRenderStateMixin 会给 EntityRenderState 注入独立的 geckolib$data map，
    * 若此处不复写 addGeckolibData/getDataMap 会落入 mixin 的 map，与 GeckoLib 内部读取不一致（ANIMATABLE_MANAGER null）。
    * 因此全部方法显式指向本类自己的 map（子类方法遮蔽 mixin 注入方法）。
    */
   public static class TransformedPlayerRenderState extends LivingEntityRenderState implements GeoRenderState {
      public TransformedPlayerAnimatable animatable;
      private final Map<DataTicket<?>, Object> geckolibDataMap = new it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap<>();

      @Override
      public Map<DataTicket<?>, Object> getDataMap() {
         return this.geckolibDataMap;
      }

      @Override
      public <D> void addGeckolibData(DataTicket<D> dataTicket, D data) {
         this.geckolibDataMap.put(dataTicket, data);
      }

      @Override
      public boolean hasGeckolibData(DataTicket<?> dataTicket) {
         return this.geckolibDataMap.containsKey(dataTicket);
      }

      @Override
      public <D> D getGeckolibData(DataTicket<D> dataTicket) {
         return (D)this.geckolibDataMap.get(dataTicket);
      }
   }

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
   private static final Set<String> ALL_ARMOR_BONE_NAMES;
   private boolean skipVisualRender = false;
   private boolean positionListenersRegistered = false;
   private int syncTickCounter = 0;
   private static final int SYNC_INTERVAL = 1;
   public static boolean isWorldRenderPass = false;
   private static Vec3 clientGirlCamPos = null;
   private static Vec3 clientBoyCamPos = null;
   private static int trackedSceneEntityId = -1;

   public static Vec3 getGirlCamPos() {
      return clientGirlCamPos;
   }

   public static Vec3 getBoyCamPos() {
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
      this.withRenderLayer(
         new BlockAndItemGeoLayer<TransformedPlayerAnimatable, Player, TransformedPlayerRenderState>(context, this) {
            @Override
            public void addRenderData(TransformedPlayerAnimatable animatable, Player player, TransformedPlayerRenderState renderState, float partialTick) {
               List<RenderData> data = new ArrayList<>();
               if (!TransformedPlayerRenderer.this.skipVisualRender && player != null) {
                  boolean sceneActive = false;
                  if (player instanceof TransformablePlayer tp) {
                     sceneActive = tp.heartbound$isTransformSceneActive();
                  }

                  if (!sceneActive) {
                     ItemStack mainHand = player.getItemBySlot(EquipmentSlot.MAINHAND);
                     if (!mainHand.isEmpty()) {
                        ItemStackRenderState mainHandState = new ItemStackRenderState();
                        this.itemModelResolver.updateForLiving(mainHandState, mainHand, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, player);
                        data.add(RenderData.item("weapon", ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, mainHandState));
                     }

                     ItemStack offHand = player.getItemBySlot(EquipmentSlot.OFFHAND);
                     if (!offHand.isEmpty()) {
                        ItemStackRenderState offHandState = new ItemStackRenderState();
                        this.itemModelResolver.updateForLiving(offHandState, offHand, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, player);
                        data.add(RenderData.item("weapon2", ItemDisplayContext.THIRD_PERSON_LEFT_HAND, offHandState));
                     }
                  }
               }

               renderState.addGeckolibData(CONTENTS, data);
            }

            @Override
            public List<RenderData> getRelevantBones(TransformedPlayerAnimatable animatable, Player player, TransformedPlayerRenderState renderState, float partialTick) {
               return renderState.hasGeckolibData(CONTENTS) ? renderState.getGeckolibData(CONTENTS) : List.of();
            }
         }
      );
   }

   public void setGirlId(String girlId) {
      this.girlModel.setGirlId(girlId);
   }

   @Override
   public TransformedPlayerRenderState createRenderState(TransformedPlayerAnimatable animatable, Player player) {
      return new TransformedPlayerRenderState();
   }

   @Override
   public void extractRenderState(Player player, TransformedPlayerRenderState state, float partialTick) {
      super.extractRenderState(player, state, partialTick);
      state.animatable = this.animatable;
      this.positionListenersRegistered = false;
   }

   @Override
   protected void applyRotations(RenderPassInfo<TransformedPlayerRenderState> renderPassInfo, PoseStack poseStack, float partialTick) {
      Player player = this.animatable.getCurrentEntity();
      if (player != null && !player.hasPose(Pose.SLEEPING)) {
         poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - renderPassInfo.renderState().yRot));
      }
   }

   @Override
   public void adjustModelBonesForRender(RenderPassInfo<TransformedPlayerRenderState> renderPassInfo, BoneSnapshots snapshots) {
      Player player = this.animatable.getCurrentEntity();
      boolean sceneActive = false;
      boolean showSteve = false;
      if (player != null) {
         TransformablePlayer tp = (TransformablePlayer)player;
         sceneActive = tp.heartbound$isTransformSceneActive();
         if (sceneActive) {
            Scene scene = tp.heartbound$getTransformScene();
            boolean hasPartner = !player.getPassengers().isEmpty() || tp.heartbound$getTransformScenePartner().isPresent();
            showSteve = !scene.hidePlayer() && hasPartner;
         }
      }

      boolean steveVisible = showSteve;
      snapshots.get("steve").ifPresent(snap -> snap.skipRender(!steveVisible));
      if (player != null) {
         TransformablePlayer tp = (TransformablePlayer)player;
         boolean stripped = tp.heartbound$isStripped();

         for (Map.Entry<EquipmentSlot, List<String>> entry : ARMOR_BONES.entrySet()) {
            boolean hasArmor = !player.getItemBySlot(entry.getKey()).isEmpty() && !stripped;

            for (String boneName : entry.getValue()) {
               snapshots.get(boneName).ifPresent(snap -> snap.skipRender(!hasArmor));
            }
         }
      } else {
         for (String boneName : ALL_ARMOR_BONE_NAMES) {
            snapshots.get(boneName).ifPresent(snap -> snap.skipRender(true));
         }
      }

      if (steveVisible) {
         this.setArmVisibility(snapshots, this.computeSteveSlim(player));
      }

      if (sceneActive && player != null) {
         this.skipVisualRender = false;
         if (!this.positionListenersRegistered) {
            this.positionListenersRegistered = true;
            renderPassInfo.addBonePositionListener("girlCam", (position, rotation, scale) -> this.onGirlCamPosition(player, position));
            if (steveVisible) {
               renderPassInfo.addBonePositionListener("boyCam", (position, rotation, scale) -> this.onBoyCamPosition(player, position));
            }
         }
      } else {
         if (player != null && player.getId() == trackedSceneEntityId) {
            clearCameraPositions();
         }

         if (player != null && isWorldRenderPass) {
            Minecraft client = Minecraft.getInstance();
            boolean isFirstPerson = client.options.getCameraType().isFirstPerson();
            this.skipVisualRender = isFirstPerson && client.getCameraEntity() == player && !sceneActive;
         } else {
            this.skipVisualRender = false;
         }

         if (this.skipVisualRender) {
            for (GeoBone bone : renderPassInfo.model().topLevelBones()) {
               Optional.ofNullable(snapshots.get(bone)).ifPresent(snap -> {
                  snap.skipRender(true);
                  snap.skipChildrenRender(true);
               });
            }
         }
      }
   }

   private void setArmVisibility(BoneSnapshots snapshots, boolean slim) {
      snapshots.get("leftArmSteve").ifPresent(s -> s.skipRender(slim));
      snapshots.get("leftLowerArmSteve").ifPresent(s -> s.skipRender(slim));
      snapshots.get("rightArmSteve").ifPresent(s -> s.skipRender(slim));
      snapshots.get("rightLowerArmSteve").ifPresent(s -> s.skipRender(slim));
      snapshots.get("leftArmAlex").ifPresent(s -> s.skipRender(!slim));
      snapshots.get("leftLowerArmAlex").ifPresent(s -> s.skipRender(!slim));
      snapshots.get("rightArmAlex").ifPresent(s -> s.skipRender(!slim));
      snapshots.get("rightLowerArmAlex").ifPresent(s -> s.skipRender(!slim));
   }

   private boolean computeSteveSlim(Player transformedPlayer) {
      TransformablePlayer tp = (TransformablePlayer)transformedPlayer;
      Optional<UUID> partnerUuid = tp.heartbound$getTransformScenePartner();
      if (partnerUuid.isPresent()) {
         UUID targetUuid = partnerUuid.get();
         Minecraft client = Minecraft.getInstance();
         if (client.level != null) {
            for (Player worldPlayer : client.level.players()) {
               if (worldPlayer.getUUID().equals(targetUuid) && worldPlayer instanceof AbstractClientPlayer cp) {
                  return cp.getSkin().model() == PlayerModelType.SLIM;
               }
            }
         }

         return false;
      }

      if (!transformedPlayer.getPassengers().isEmpty() && transformedPlayer.getPassengers().get(0) instanceof AbstractClientPlayer rider) {
         return rider.getSkin().model() == PlayerModelType.SLIM;
      }

      return false;
   }

   private void onGirlCamPosition(Player player, Vec3 position) {
      if (position != null && !Double.isNaN(position.x) && !Double.isNaN(position.y) && !Double.isNaN(position.z)) {
         clientGirlCamPos = position;
         trackedSceneEntityId = player.getId();
      }
   }

   private void onBoyCamPosition(Player player, Vec3 position) {
      if (position == null || Double.isNaN(position.x) || Double.isNaN(position.y) || Double.isNaN(position.z)) {
         return;
      }

      if (Math.abs(position.x) < 1.0E-6 && Math.abs(position.y) < 1.0E-6 && Math.abs(position.z) < 1.0E-6) {
         return;
      }

      clientBoyCamPos = position;
      Vec3 entityPos = player.position();
      Vec3 relativePos = position.subtract(entityPos);
      if (Math.abs(relativePos.x) > 50.0 || Math.abs(relativePos.y) > 50.0 || Math.abs(relativePos.z) > 50.0) {
         return;
      }

      this.syncBoyCamPosition(player, relativePos);
   }

   private void syncBoyCamPosition(Player player, Vec3 position) {
      if (++this.syncTickCounter >= SYNC_INTERVAL) {
         this.syncTickCounter = 0;
         ClientPlayNetworking.send(new BonePosSyncC2SPacket(player.getId(), position));
      }
   }

   static {
      Set<String> names = new HashSet<>();
      ARMOR_BONES.values().forEach(names::addAll);
      ALL_ARMOR_BONE_NAMES = Set.copyOf(names);
   }
}
