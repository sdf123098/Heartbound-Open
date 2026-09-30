package com.cuddly.heartbound.client.rendering.renderers;

import com.cuddly.heartbound.Heartbound;
import com.cuddly.heartbound.client.models.AbstractGirlModel;
import com.cuddly.heartbound.client.rendering.layers.BoneOverrideRenderLayer;
import com.cuddly.heartbound.config.ModConfig;
import com.cuddly.heartbound.entity.base.GirlSceneEntity;
import com.cuddly.heartbound.networking.C2S.BonePosSyncC2SPacket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.builtin.BlockAndItemGeoLayer;
import com.geckolib.renderer.layer.builtin.BlockAndItemGeoLayer.RenderData;
import com.geckolib.util.RenderUtil;

public abstract class AbstractGirlRenderer<T extends GirlSceneEntity> extends GeoEntityRenderer<T, AbstractGirlRenderer.GirlRenderState> {
   /**
    * 注意：GeckoLib 5.5.2 的 EntityRenderStateMixin 会给 EntityRenderState 注入独立的 geckolib$data map，
    * 若此处不复写 addGeckolibData/getDataMap 会落入 mixin 的 map，与 GeckoLib 内部读取不一致（ANIMATABLE_MANAGER null）。
    * 因此全部方法显式指向本类自己的 map（子类方法遮蔽 mixin 注入方法）。
    */
   public static class GirlRenderState extends LivingEntityRenderState implements GeoRenderState {
      public GirlSceneEntity animatable;
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

   protected final Map<String, Boolean> boneVisibility = new HashMap<>();
   protected Identifier textureOverride = null;
   protected boolean shadingEnabled = true;
   private int syncTickCounter = 0;
   private static final int SYNC_INTERVAL = 1;
   private static Vec3 clientBoyCamPos = null;
   private static int trackedGirlEntityId = -1;
   public static boolean IS_SHADING_DISABLED = false;
   public static boolean IS_GUI_RENDERING = false;

   public static Vec3 getBoyCamPos() {
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
      this.withRenderLayer(new BoneOverrideRenderLayer<>(this));
      this.withRenderLayer(
         new BlockAndItemGeoLayer<T, Void, GirlRenderState>(ctx, this) {
            @Override
            public void addRenderData(T animatable, Void unused, GirlRenderState renderState, float partialTick) {
               List<RenderData> data = new ArrayList<>();
               ItemStack mainHand = animatable.getItemBySlot(EquipmentSlot.MAINHAND);
               if (!mainHand.isEmpty()) {
                  ItemStackRenderState mainHandState = new ItemStackRenderState();
                  this.itemModelResolver.updateForLiving(mainHandState, mainHand, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, animatable);
                  data.add(RenderData.item("weapon", ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, mainHandState));
               }

               ItemStack offHand = animatable.getItemBySlot(EquipmentSlot.OFFHAND);
               if (!offHand.isEmpty()) {
                  ItemStackRenderState offHandState = new ItemStackRenderState();
                  this.itemModelResolver.updateForLiving(offHandState, offHand, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, animatable);
                  data.add(RenderData.item("weapon2", ItemDisplayContext.THIRD_PERSON_LEFT_HAND, offHandState));
               }

               renderState.addGeckolibData(CONTENTS, data);
            }

            @Override
            public List<RenderData> getRelevantBones(T animatable, Void unused, GirlRenderState renderState, float partialTick) {
               return renderState.hasGeckolibData(CONTENTS) ? renderState.getGeckolibData(CONTENTS) : List.of();
            }
         }
      );
   }

   @Override
   public GirlRenderState createRenderState(T animatable, Void unused) {
      return new GirlRenderState();
   }

   @Override
   public void extractRenderState(T entity, GirlRenderState state, float partialTick) {
      super.extractRenderState(entity, state, partialTick);
      state.animatable = entity;
   }

   @Override
   public void preRenderPass(RenderPassInfo<GirlRenderState> renderPassInfo, net.minecraft.client.renderer.SubmitNodeCollector nodeCollector) {
      T animatable = (T)renderPassInfo.renderState().animatable;
      Minecraft client = Minecraft.getInstance();
      if (animatable != null
         && animatable.isHavingSex()
         && animatable.passengerBoneName != null
         && client.player != null
         && client.player.getVehicle() == animatable) {
         renderPassInfo.model().getBone(animatable.passengerBoneName).ifPresent(bone -> renderPassInfo.addPerBoneRender(bone, (passInfo, b, tasks) -> {
            RenderUtil.providePositionsToListeners(
               passInfo.poseStack(),
               passInfo,
               new RenderPassInfo.BonePositionListener[]{(position, rotation, scale) -> this.onBoyCamPosition(animatable, position)}
            );
         }));
      }
   }

   @Override
   public void adjustModelBonesForRender(RenderPassInfo<GirlRenderState> renderPassInfo, BoneSnapshots snapshots) {
      T animatable = (T)renderPassInfo.renderState().animatable;
      if (animatable == null) {
         return;
      }

      if ((!animatable.isHavingSex() || animatable.passengerBoneName == null) && animatable.getId() == trackedGirlEntityId) {
         clearCameraPosition();
      }

      this.updateBoneVisibility(animatable, renderPassInfo.model(), snapshots);
      if (this.model instanceof AbstractGirlModel) {
         ((AbstractGirlModel<T>)this.model).applyFrameBoneTransforms(animatable, renderPassInfo.model(), snapshots, renderPassInfo.renderState().xRot, renderPassInfo.renderState().yRot);
      }
   }

   private void onBoyCamPosition(T animatable, Vec3 position) {
      if (position == null || Double.isNaN(position.x) || Double.isNaN(position.y) || Double.isNaN(position.z)) {
         return;
      }

      if (Math.abs(position.x) < 1.0E-6 && Math.abs(position.y) < 1.0E-6 && Math.abs(position.z) < 1.0E-6) {
         return;
      }

      Vec3 entityPos = animatable.position();
      Vec3 relativePos = position.subtract(entityPos);
      if (Math.abs(relativePos.x) > 50.0 || Math.abs(relativePos.y) > 50.0 || Math.abs(relativePos.z) > 50.0) {
         return;
      }

      clientBoyCamPos = position;
      trackedGirlEntityId = animatable.getId();
      this.syncBoyCamPosition(animatable, relativePos);
   }

   private void syncBoyCamPosition(T animatable, Vec3 position) {
      if (++this.syncTickCounter >= SYNC_INTERVAL) {
         this.syncTickCounter = 0;
         ClientPlayNetworking.send(new BonePosSyncC2SPacket(animatable.getId(), position));
      }
   }

   protected void updateBoneVisibility(T entity, BakedGeoModel bakedModel, BoneSnapshots snapshots) {
      boolean isSceneActive = entity.isSceneActive();
      boolean steveHasOverride = entity.boneTextureOverrides != null && entity.boneTextureOverrides.get("steve") != null;
      snapshots.get("steve").ifPresent(snap -> {
         boolean hide = !isSceneActive || steveHasOverride;
         snap.skipRender(hide);
         snap.skipChildrenRender(hide);
      });
      if (entity.boneVisibility != null) {
         for (Map.Entry<String, Boolean> entry : entity.boneVisibility.entrySet()) {
            snapshots.get(entry.getKey()).ifPresent(snap -> {
               snap.skipRender(!entry.getValue());
               snap.skipChildrenRender(!entry.getValue());
            });
         }
      }

      for (Map.Entry<String, Boolean> entry : this.boneVisibility.entrySet()) {
         snapshots.get(entry.getKey()).ifPresent(snap -> {
            snap.skipRender(!entry.getValue());
            snap.skipChildrenRender(!entry.getValue());
         });
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

   @Override
   public Identifier getTextureLocation(GirlRenderState state) {
      return this.textureOverride != null ? this.textureOverride : super.getTextureLocation(state);
   }

   public void setShadingEnabled(boolean enabled) {
      this.shadingEnabled = enabled;
   }
}
