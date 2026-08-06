package com.cuddly.heartbound.client.models;

import com.cuddly.heartbound.Heartbound;
import com.cuddly.heartbound.client.rendering.renderers.AbstractGirlRenderer;
import com.cuddly.heartbound.config.ModConfig;
import com.cuddly.heartbound.entity.base.GirlSceneEntity;
import com.cuddly.heartbound.util.rendering.JigglePhysics;
import com.cuddly.heartbound.util.variables.JiggleBoneConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;

public abstract class AbstractGirlModel<T extends GirlSceneEntity> extends GeoModel<T> {
   private final Map<Long, Map<String, JigglePhysics>> jiggleMapByEntity = new HashMap<>();
   private final Map<Long, Map<String, Vec3>> defaultRotationsByEntity = new HashMap<>();
   private final Map<Long, Long> lastUpdateTimeByEntity = new HashMap<>();
   private static final double FIXED_TIMESTEP = 0.04;
   private final Map<Long, Double> timeAccumulator = new HashMap<>();
   private static final List<AbstractGirlModel<?>> MODEL_INSTANCES = new ArrayList<>();
   private static final java.util.Set<String> LOGGED_RESOURCE_KEYS = new java.util.HashSet<>();

   public AbstractGirlModel() {
      MODEL_INSTANCES.add(this);
   }

   public static void refreshAllModels() {
      for (AbstractGirlModel<?> model : MODEL_INSTANCES) {
         model.refreshAllDefaults();
      }
   }

   protected GirlSceneEntity getAnimatableFrom(GeoRenderState renderState) {
      if (renderState instanceof AbstractGirlRenderer.GirlRenderState girlState) {
         return girlState.animatable;
      }

      return null;
   }

   @Override
   public Identifier getModelResource(GeoRenderState renderState) {
      GirlSceneEntity animatable = this.getAnimatableFrom(renderState);
      if (animatable == null) {
         return Identifier.fromNamespaceAndPath("heartbound", "dressed/jenny");
      }

      boolean stripped = animatable.isStripped();
      String girlID = animatable.getGirlID();
      String folder = stripped ? "nude/" : "dressed/";
      Identifier key = Identifier.fromNamespaceAndPath("heartbound", folder + girlID);
      String logKey = "model:" + key;
      if (LOGGED_RESOURCE_KEYS.add(logKey)) {
         Heartbound.LOGGER.info("[HB-DBG] {} (stripped={}, animatable={})", logKey, stripped, animatable.getId());
      }
      return key;
   }

   @Override
   public Identifier getTextureResource(GeoRenderState renderState) {
      GirlSceneEntity animatable = this.getAnimatableFrom(renderState);
      if (animatable == null) {
         return Identifier.fromNamespaceAndPath("heartbound", "textures/entities/jenny.png");
      }

      String girlID = animatable.getGirlID();
      String filePath = "textures/entities/" + girlID + ".png";
      Identifier key = Identifier.fromNamespaceAndPath("heartbound", filePath);
      String logKey = "texture:" + key;
      if (LOGGED_RESOURCE_KEYS.add(logKey)) {
         Heartbound.LOGGER.info("[HB-DBG] {} (animatable={})", logKey, animatable.getId());
      }
      return key;
   }

   @Override
   public Identifier getAnimationResource(T animatable) {
      return Identifier.fromNamespaceAndPath("heartbound", animatable.getGirlID());
   }

   public void applyFrameBoneTransforms(T animatable, BakedGeoModel model, BoneSnapshots snapshots, float headPitch, float headYaw) {
      boolean isSceneActive = animatable.isSceneActive();
      boolean hasVehicle = animatable.isPassenger();
      boolean isSprinting = animatable.isSprinting();
      boolean isStripping = "strip".equals(animatable.getOverrideAnim());
      if (!isSceneActive && !hasVehicle && !isSprinting && !isStripping) {
         this.calculateJigglePhysics(animatable, (long)animatable.getId(), model, snapshots);
      }

      if (!isSceneActive) {
         snapshots.get("head").ifPresent(snap -> {
            snap.setRotX(headPitch * (float)(Math.PI / 180.0));
            snap.setRotY(headYaw * (float)(Math.PI / 180.0));
         });
      }

      Minecraft client = Minecraft.getInstance();
      boolean isFirstPerson = client.options.getCameraType().isFirstPerson();
      boolean isPlayerRider = client.getCameraEntity() == animatable.getFirstPassenger();
      snapshots.get("Head2").ifPresent(snap -> snap.skipRender(isFirstPerson && isPlayerRider));

      snapshots.get("boobWindow").ifPresent(snap -> {
         boolean hide = ModConfig.INSTANCE.girls.boobWindow;
         snap.skipRender(hide);
         snap.skipChildrenRender(hide);
      });

      if (animatable.boneSizeOverrides != null) {
         for (Entry<String, Vec3> entry : animatable.boneSizeOverrides.entrySet()) {
            snapshots.get(entry.getKey()).ifPresent(snap -> {
               Vec3 scale = entry.getValue();
               snap.setScaleX((float)scale.x);
               snap.setScaleY((float)scale.y);
               snap.setScaleZ((float)scale.z);
            });
         }
      }
   }

   private void calculateJigglePhysics(T animatable, long instanceId, BakedGeoModel model, BoneSnapshots snapshots) {
      boolean inGui = Minecraft.getInstance().screen != null;
      Vec3 velocity = inGui ? Vec3.ZERO : animatable.getDeltaMovement();
      Vec3 prevVelocity = inGui ? Vec3.ZERO : animatable.previousVelocity;
      float currentYaw = inGui ? 0.0F : animatable.getYRot();
      float prevYaw = inGui ? 0.0F : animatable.previousYaw;
      float yawDelta = currentYaw - prevYaw;
      if (yawDelta > 180.0F) {
         yawDelta -= 360.0F;
      }

      if (yawDelta < -180.0F) {
         yawDelta += 360.0F;
      }

      Vec3 deltaVelocity = velocity.subtract(prevVelocity);
      Vec3 inertiaForce = deltaVelocity.scale(1.2);
      double yawInfluenceX = Math.sin(Math.toRadians((double)currentYaw)) * (double)yawDelta * 0.05;
      double yawInfluenceZ = Math.cos(Math.toRadians((double)currentYaw)) * (double)yawDelta * 0.05;
      inertiaForce = inertiaForce.add(yawInfluenceX, 0.0, yawInfluenceZ);
      Vec3 finalInertia = inertiaForce;
      this.jiggleMapByEntity.putIfAbsent(instanceId, new HashMap<>());
      this.defaultRotationsByEntity.putIfAbsent(instanceId, new HashMap<>());
      this.timeAccumulator.putIfAbsent(instanceId, 0.0);
      Map<String, JigglePhysics> jiggleMap = this.jiggleMapByEntity.get(instanceId);
      Map<String, Vec3> defaultRotations = this.defaultRotationsByEntity.get(instanceId);
      long now = System.nanoTime();
      long lastUpdate = this.lastUpdateTimeByEntity.getOrDefault(instanceId, now);
      double deltaSec = (double)(now - lastUpdate) / 1.0E9;
      this.lastUpdateTimeByEntity.put(instanceId, now);
      double accumulator = this.timeAccumulator.get(instanceId) + deltaSec;
      accumulator = Math.min(accumulator, 0.2);

      while (accumulator >= 0.04) {
         for (JiggleBoneConfig config : this.JIGGLE_BONES(animatable)) {
            model.getBone(config.boneName()).flatMap(bone -> Optional.ofNullable(snapshots.get(bone))).ifPresent(snap -> {
               defaultRotations.putIfAbsent(config.boneName(), new Vec3((double)snap.getRotX(), (double)snap.getRotY(), (double)snap.getRotZ()));
               jiggleMap.putIfAbsent(config.boneName(), new JigglePhysics(config.stiffness(), config.damping()));
               jiggleMap.get(config.boneName()).update(finalInertia);
            });
         }

         accumulator -= 0.04;
         if (Double.isNaN(accumulator) || accumulator > 1.0) {
            accumulator = 0.0;
         }
      }

      this.timeAccumulator.put(instanceId, accumulator);
      double alpha = accumulator / 0.04;

      for (JiggleBoneConfig configx : this.JIGGLE_BONES(animatable)) {
         Vec3 defaultRot = defaultRotations.get(configx.boneName());
         JigglePhysics jiggle = jiggleMap.get(configx.boneName());
         if (defaultRot != null && jiggle != null) {
            Vec3 offset = jiggle.getInterpolatedDisplacement(alpha);
            Vec3 finalOffset = offset;
            model.getBone(configx.boneName()).flatMap(bone -> Optional.ofNullable(snapshots.get(bone))).ifPresent(snap -> {
               snap.setRotX((float)(defaultRot.x + finalOffset.x));
               snap.setRotY((float)(defaultRot.y + finalOffset.y));
               snap.setRotZ((float)(defaultRot.z + finalOffset.z));
            });
         }
      }
   }

   protected List<JiggleBoneConfig> JIGGLE_BONES(T animatable) {
      List<JiggleBoneConfig> bones = new ArrayList<>();
      bones.add(new JiggleBoneConfig("cheekL", 0.2, 0.2));
      bones.add(new JiggleBoneConfig("cheekR", 0.2, 0.2));
      bones.add(new JiggleBoneConfig("belly", 0.3, 0.4));
      if (!animatable.isStripped()) {
         bones.add(new JiggleBoneConfig("boobs", 0.2, 0.4));
      } else {
         bones.add(new JiggleBoneConfig("boobL", 0.2, 0.3));
         bones.add(new JiggleBoneConfig("boobR", 0.2, 0.3));
      }

      return bones;
   }

   public void refreshAllDefaults() {
      this.defaultRotationsByEntity.clear();
      this.jiggleMapByEntity.clear();
      this.timeAccumulator.clear();
      this.lastUpdateTimeByEntity.clear();
   }
}
