package com.cuddly.heartbound.client.models;

import com.cuddly.heartbound.config.ModConfig;
import com.cuddly.heartbound.entity.base.GirlSceneEntity;
import com.cuddly.heartbound.util.rendering.GeoBoneExtension;
import com.cuddly.heartbound.util.rendering.JigglePhysics;
import com.cuddly.heartbound.util.variables.JiggleBoneConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;

public abstract class AbstractGirlModel<T extends GirlSceneEntity> extends GeoModel<T> {
   private final Map<Long, Map<String, JigglePhysics>> jiggleMapByEntity = new HashMap<>();
   private final Map<Long, Map<String, Vec3d>> defaultRotationsByEntity = new HashMap<>();
   private final Map<Long, Long> lastUpdateTimeByEntity = new HashMap<>();
   private static final double FIXED_TIMESTEP = 0.04;
   private final Map<Long, Double> timeAccumulator = new HashMap<>();
   private static final List<AbstractGirlModel<?>> MODEL_INSTANCES = new ArrayList<>();

   public AbstractGirlModel() {
      MODEL_INSTANCES.add(this);
   }

   public static void refreshAllModels() {
      for (AbstractGirlModel<?> model : MODEL_INSTANCES) {
         model.refreshAllDefaults();
      }
   }

   public Identifier getModelResource(T animatable) {
      boolean stripped = animatable.isStripped();
      String girlID = animatable.getGirlID();
      String folder = stripped ? "nude/" : "dressed/";
      String filePath = "geo/" + folder + girlID + ".geo.json";
      return Identifier.of("heartbound", filePath);
   }

   public Identifier getTextureResource(T animatable) {
      String girlID = animatable.getGirlID();
      String filePath = "textures/entities/" + girlID + ".png";
      return Identifier.of("heartbound", filePath);
   }

   public Identifier getAnimationResource(T animatable) {
      return Identifier.of("heartbound", "animations/" + animatable.getGirlID() + ".animation.json");
   }

   public void setCustomAnimations(T animatable, long instanceId, AnimationState<T> animationState) {
      GeoBone head = this.getAnimationProcessor().getBone("head");
      boolean isSceneActive = animatable.isSceneActive();
      boolean hasVehicle = animatable.hasVehicle();
      boolean isSprinting = animatable.isSprinting();
      boolean isStripping = "strip".equals(animatable.getOverrideAnim());
      if (!isSceneActive && !hasVehicle && !isSprinting && !isStripping) {
         this.calculateJigglePhysics(animatable, instanceId, animationState);
      }

      if (head != null && !isSceneActive) {
         float pitch = animationState.getData(DataTickets.ENTITY_MODEL_DATA).headPitch();
         float yaw = animationState.getData(DataTickets.ENTITY_MODEL_DATA).netHeadYaw();
         head.setRotX(pitch * (float) (Math.PI / 180.0));
         head.setRotY(yaw * (float) (Math.PI / 180.0));
      }

      GeoBone headBone = this.getAnimationProcessor().getBone("Head2");
      if (headBone != null) {
         MinecraftClient client = MinecraftClient.getInstance();
         boolean isFirstPerson = client.options.getPerspective().isFirstPerson();
         boolean isPlayerRider = client.cameraEntity == animatable.getFirstPassenger();
         ((GeoBoneExtension)headBone).setHiddenWithoutHidingChildren(isFirstPerson && isPlayerRider);
      }

      GeoBone boobWindow = this.getAnimationProcessor().getBone("boobWindow");
      if (boobWindow != null) {
         boobWindow.setHidden(ModConfig.INSTANCE.girls.boobWindow);
      }

      if (animatable.boneSizeOverrides != null) {
         for (Entry<String, Vec3d> entry : animatable.boneSizeOverrides.entrySet()) {
            GeoBone bone = this.getAnimationProcessor().getBone(entry.getKey());
            if (bone != null) {
               Vec3d scale = entry.getValue();
               bone.setScaleX((float)scale.x);
               bone.setScaleY((float)scale.y);
               bone.setScaleZ((float)scale.z);
            }
         }
      }
   }

   private void calculateJigglePhysics(T animatable, long instanceId, AnimationState<T> animationState) {
      boolean inGui = MinecraftClient.getInstance().currentScreen != null;
      Vec3d velocity = inGui ? Vec3d.ZERO : animatable.getVelocity();
      Vec3d prevVelocity = inGui ? Vec3d.ZERO : animatable.previousVelocity;
      float currentYaw = inGui ? 0.0F : animatable.getYaw();
      float prevYaw = inGui ? 0.0F : animatable.previousYaw;
      float yawDelta = currentYaw - prevYaw;
      if (yawDelta > 180.0F) {
         yawDelta -= 360.0F;
      }

      if (yawDelta < -180.0F) {
         yawDelta += 360.0F;
      }

      Vec3d deltaVelocity = velocity.subtract(prevVelocity);
      Vec3d inertiaForce = deltaVelocity.multiply(1.2);
      double yawInfluenceX = Math.sin(Math.toRadians((double)currentYaw)) * (double)yawDelta * 0.05;
      double yawInfluenceZ = Math.cos(Math.toRadians((double)currentYaw)) * (double)yawDelta * 0.05;
      inertiaForce = inertiaForce.add(yawInfluenceX, 0.0, yawInfluenceZ);
      this.jiggleMapByEntity.putIfAbsent(instanceId, new HashMap<>());
      this.defaultRotationsByEntity.putIfAbsent(instanceId, new HashMap<>());
      this.timeAccumulator.putIfAbsent(instanceId, 0.0);
      Map<String, JigglePhysics> jiggleMap = this.jiggleMapByEntity.get(instanceId);
      Map<String, Vec3d> defaultRotations = this.defaultRotationsByEntity.get(instanceId);
      long now = System.nanoTime();
      long lastUpdate = this.lastUpdateTimeByEntity.getOrDefault(instanceId, now);
      double deltaSec = (double)(now - lastUpdate) / 1.0E9;
      this.lastUpdateTimeByEntity.put(instanceId, now);
      double accumulator = this.timeAccumulator.get(instanceId) + deltaSec;
      accumulator = Math.min(accumulator, 0.2);

      while (accumulator >= 0.04) {
         for (JiggleBoneConfig config : this.JIGGLE_BONES(animatable)) {
            GeoBone bone = this.getAnimationProcessor().getBone(config.boneName());
            if (bone != null) {
               defaultRotations.putIfAbsent(config.boneName(), new Vec3d((double)bone.getRotX(), (double)bone.getRotY(), (double)bone.getRotZ()));
               jiggleMap.putIfAbsent(config.boneName(), new JigglePhysics(config.stiffness(), config.damping()));
               jiggleMap.get(config.boneName()).update(inertiaForce);
            }
         }

         accumulator -= 0.04;
         if (Double.isNaN(accumulator) || accumulator > 1.0) {
            accumulator = 0.0;
         }
      }

      this.timeAccumulator.put(instanceId, accumulator);
      double alpha = accumulator / 0.04;

      for (JiggleBoneConfig configx : this.JIGGLE_BONES(animatable)) {
         GeoBone bone = this.getAnimationProcessor().getBone(configx.boneName());
         if (bone != null) {
            Vec3d defaultRot = defaultRotations.get(configx.boneName());
            JigglePhysics jiggle = jiggleMap.get(configx.boneName());
            if (defaultRot != null && jiggle != null) {
               Vec3d offset = jiggle.getInterpolatedDisplacement(alpha);
               bone.setRotX((float)(defaultRot.x + offset.x));
               bone.setRotY((float)(defaultRot.y + offset.y));
               bone.setRotZ((float)(defaultRot.z + offset.z));
            }
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
