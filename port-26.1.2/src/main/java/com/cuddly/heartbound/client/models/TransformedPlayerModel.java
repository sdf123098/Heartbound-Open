package com.cuddly.heartbound.client.models;

import com.cuddly.heartbound.client.rendering.TransformedPlayerAnimatable;
import com.cuddly.heartbound.config.ModConfig;
import com.cuddly.heartbound.transformation.TransformablePlayer;
import com.cuddly.heartbound.util.rendering.JigglePhysics;
import com.cuddly.heartbound.util.variables.JiggleBoneConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import com.geckolib.animation.state.BoneSnapshot;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;

public class TransformedPlayerModel extends GeoModel<TransformedPlayerAnimatable> {
   private String currentGirlId = "jenny";
   private final Map<Long, Map<String, JigglePhysics>> jiggleMapByEntity = new HashMap<>();
   private final Map<Long, Map<String, Vec3>> defaultRotationsByEntity = new HashMap<>();
   private final Map<Long, Long> lastUpdateTimeByEntity = new HashMap<>();
   private final Map<Long, Double> timeAccumulator = new HashMap<>();
   private static final double FIXED_TIMESTEP = 0.04;
   private static final double MAX_FORCE = 0.15;
   private static final double MAX_DISPLACEMENT = 0.3;
   private final Map<Integer, Vec3> previousVelocityByEntity = new HashMap<>();
   private final Map<Integer, Float> previousYawByEntity = new HashMap<>();
   private final Map<Integer, Integer> lastTickByEntity = new HashMap<>();
   private final Map<Long, Boolean> lastStrippedByInstance = new HashMap<>();

   public void setGirlId(String girlId) {
      this.currentGirlId = girlId;
   }

   public String getGirlId() {
      return this.currentGirlId;
   }

   @Override
   public Identifier getModelResource(GeoRenderState renderState) {
      Player player = TransformedPlayerAnimatable.INSTANCE.getCurrentEntity();
      boolean stripped = false;
      if (player instanceof TransformablePlayer tp) {
         stripped = tp.heartbound$isStripped();
      }

      String folder = stripped ? "nude/" : "dressed/";
      return Identifier.fromNamespaceAndPath("heartbound", folder + this.currentGirlId);
   }

   @Override
   public Identifier getTextureResource(GeoRenderState renderState) {
      return Identifier.fromNamespaceAndPath("heartbound", "textures/entities/" + this.currentGirlId + ".png");
   }

   @Override
   public Identifier getAnimationResource(TransformedPlayerAnimatable animatable) {
      return Identifier.fromNamespaceAndPath("heartbound", this.currentGirlId);
   }

   public void applyFrameBoneTransforms(TransformedPlayerAnimatable animatable, BakedGeoModel model, BoneSnapshots snapshots, float headPitch, float headYaw) {
      Player player = animatable.getCurrentEntity();
      if (player == null) {
         return;
      }

      Minecraft client = Minecraft.getInstance();
      boolean isFirstPerson = client.options.getCameraType().isFirstPerson();
      boolean isLocalPlayer = client.getCameraEntity() == player;
      boolean sceneActive = false;
      if (player instanceof TransformablePlayer tp) {
         sceneActive = tp.heartbound$isTransformSceneActive();
      }

      if (!sceneActive) {
         snapshots.get("head").ifPresent(snap -> {
            snap.setRotX(headPitch * (float)(Math.PI / 180.0));
            snap.setRotY(headYaw * (float)(Math.PI / 180.0));
         });
      }

      boolean hideHead = isFirstPerson && isLocalPlayer && sceneActive;
      snapshots.get("head").ifPresent(snap -> snap.skipRender(hideHead));
      model.getBone("head").ifPresent(head -> {
         for (GeoBone child : head.children()) {
            String childName = child.name();
            if (!"girlCam".equals(childName) && !childName.startsWith("armor")) {
               snapshots.get(childName).ifPresent(childSnap -> {
                  childSnap.skipRender(hideHead);
                  childSnap.skipChildrenRender(hideHead);
               });
            }
         }
      });

      boolean isRiderLocalPlayer = !player.getPassengers().isEmpty() && client.getCameraEntity() == player.getPassengers().get(0);
      snapshots.get("Head2").ifPresent(snap -> snap.skipRender(isFirstPerson && isRiderLocalPlayer));

      snapshots.get("boobWindow").ifPresent(snap -> {
         boolean hide = ModConfig.INSTANCE.girls.boobWindow;
         snap.skipRender(hide);
         snap.skipChildrenRender(hide);
      });

      if (!player.isPassenger() && !player.isSprinting() && !TransformedPlayerAnimatable.INSTANCE.isStripAnimPlaying()) {
         boolean stripped = false;
         if (player instanceof TransformablePlayer tp) {
            stripped = tp.heartbound$isStripped();
         }

         this.calculateJigglePhysics(player, animatable.hashCode(), stripped, model, snapshots);
      }
   }

   private List<JiggleBoneConfig> getJiggleBones(boolean stripped) {
      List<JiggleBoneConfig> bones = new ArrayList<>();
      bones.add(new JiggleBoneConfig("cheekL", 0.2, 0.2));
      bones.add(new JiggleBoneConfig("cheekR", 0.2, 0.2));
      bones.add(new JiggleBoneConfig("belly", 0.3, 0.4));
      if (!stripped) {
         bones.add(new JiggleBoneConfig("boobs", 0.2, 0.4));
      } else {
         bones.add(new JiggleBoneConfig("boobL", 0.2, 0.3));
         bones.add(new JiggleBoneConfig("boobR", 0.2, 0.3));
      }

      return bones;
   }

   private void calculateJigglePhysics(Player player, long instanceId, boolean stripped, BakedGeoModel model, BoneSnapshots snapshots) {
      boolean inGui = Minecraft.getInstance().screen != null;
      int entityId = player.getId();
      int currentTick = player.tickCount;
      Vec3 velocity = inGui ? Vec3.ZERO : player.getDeltaMovement();
      float currentYaw = inGui ? 0.0F : player.yBodyRot;
      int lastTick = this.lastTickByEntity.getOrDefault(entityId, -1);
      Vec3 prevVelocity;
      float prevYaw;
      if (lastTick == -1) {
         prevVelocity = velocity;
         prevYaw = currentYaw;
         this.previousVelocityByEntity.put(entityId, velocity);
         this.previousYawByEntity.put(entityId, currentYaw);
         this.lastTickByEntity.put(entityId, currentTick);
      } else {
         prevVelocity = inGui ? Vec3.ZERO : this.previousVelocityByEntity.getOrDefault(entityId, velocity);
         prevYaw = inGui ? 0.0F : this.previousYawByEntity.getOrDefault(entityId, currentYaw);
         if (currentTick != lastTick) {
            this.previousVelocityByEntity.put(entityId, velocity);
            this.previousYawByEntity.put(entityId, currentYaw);
            this.lastTickByEntity.put(entityId, currentTick);
         }
      }

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
      double forceMag = inertiaForce.length();
      if (forceMag > 0.15) {
         inertiaForce = inertiaForce.scale(0.15 / forceMag);
      }

      Vec3 finalInertia = inertiaForce;
      Boolean lastStripped = this.lastStrippedByInstance.get(instanceId);
      if (lastStripped != null && lastStripped != stripped) {
         this.jiggleMapByEntity.remove(instanceId);
         this.defaultRotationsByEntity.remove(instanceId);
      }

      this.lastStrippedByInstance.put(instanceId, stripped);
      List<JiggleBoneConfig> jiggleBones = this.getJiggleBones(stripped);
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
         for (JiggleBoneConfig config : jiggleBones) {
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

      for (JiggleBoneConfig configx : jiggleBones) {
         Vec3 defaultRot = defaultRotations.get(configx.boneName());
         JigglePhysics jiggle = jiggleMap.get(configx.boneName());
         if (defaultRot != null && jiggle != null) {
            Vec3 offset = jiggle.getInterpolatedDisplacement(alpha);
            double offsetMag = offset.length();
            if (offsetMag > 0.3) {
               offset = offset.scale(0.3 / offsetMag);
            }

            Vec3 finalOffset = offset;
            model.getBone(configx.boneName()).flatMap(bone -> Optional.ofNullable(snapshots.get(bone))).ifPresent(snap -> {
               snap.setRotX((float)(defaultRot.x + finalOffset.x));
               snap.setRotY((float)(defaultRot.y + finalOffset.y));
               snap.setRotZ((float)(defaultRot.z + finalOffset.z));
            });
         }
      }
   }
}
