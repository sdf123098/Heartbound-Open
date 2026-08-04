package com.cuddly.heartbound.client.models;

import com.cuddly.heartbound.client.rendering.TransformedPlayerAnimatable;
import com.cuddly.heartbound.config.ModConfig;
import com.cuddly.heartbound.transformation.TransformablePlayer;
import com.cuddly.heartbound.util.rendering.GeoBoneExtension;
import com.cuddly.heartbound.util.rendering.JigglePhysics;
import com.cuddly.heartbound.util.variables.JiggleBoneConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.state.BoneSnapshot;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;

public class TransformedPlayerModel extends GeoModel<TransformedPlayerAnimatable> {
   private String currentGirlId = "jenny";
   private final Map<Long, Map<String, JigglePhysics>> jiggleMapByEntity = new HashMap<>();
   private final Map<Long, Map<String, Vec3d>> defaultRotationsByEntity = new HashMap<>();
   private final Map<Long, Long> lastUpdateTimeByEntity = new HashMap<>();
   private final Map<Long, Double> timeAccumulator = new HashMap<>();
   private static final double FIXED_TIMESTEP = 0.04;
   private static final double MAX_FORCE = 0.15;
   private static final double MAX_DISPLACEMENT = 0.3;
   private final Map<Integer, Vec3d> previousVelocityByEntity = new HashMap<>();
   private final Map<Integer, Float> previousYawByEntity = new HashMap<>();
   private final Map<Integer, Integer> lastTickByEntity = new HashMap<>();
   private final Map<Long, Boolean> lastStrippedByInstance = new HashMap<>();
   private final Map<Identifier, BakedGeoModel> separateModelCache = new HashMap<>();
   private final Map<Identifier, BakedGeoModel> sourceModelRef = new HashMap<>();
   private BakedGeoModel activeModel = null;

   public void setGirlId(String girlId) {
      this.currentGirlId = girlId;
   }

   public String getGirlId() {
      return this.currentGirlId;
   }

   public BakedGeoModel getBakedModel(Identifier location) {
      BakedGeoModel original = GeckoLibCache.getBakedModels().get(location);
      if (original == null) {
         return super.getBakedModel(location);
      } else {
         BakedGeoModel lastSource = this.sourceModelRef.get(location);
         BakedGeoModel separate = this.separateModelCache.get(location);
         if (separate == null || original != lastSource) {
            separate = deepCopyModel(original);
            this.separateModelCache.put(location, separate);
            this.sourceModelRef.put(location, original);
            if (separate == this.activeModel) {
               this.activeModel = null;
            }
         }

         if (separate != this.activeModel) {
            this.getAnimationProcessor().setActiveModel(separate);
            this.activeModel = separate;
         }

         return separate;
      }
   }

   public Identifier getModelResource(TransformedPlayerAnimatable animatable) {
      PlayerEntity player = animatable.getCurrentEntity();
      boolean stripped = false;
      if (player instanceof TransformablePlayer tp) {
         stripped = tp.heartbound$isStripped();
      }

      String folder = stripped ? "nude/" : "dressed/";
      return Identifier.of("heartbound", "geo/" + folder + this.currentGirlId + ".geo.json");
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

   public Identifier getTextureResource(TransformedPlayerAnimatable animatable) {
      return Identifier.of("heartbound", "textures/entities/" + this.currentGirlId + ".png");
   }

   public Identifier getAnimationResource(TransformedPlayerAnimatable animatable) {
      return Identifier.of("heartbound", "animations/" + this.currentGirlId + ".animation.json");
   }

   public void setCustomAnimations(TransformedPlayerAnimatable animatable, long instanceId, AnimationState<TransformedPlayerAnimatable> animationState) {
      PlayerEntity player = animatable.getCurrentEntity();
      if (player != null) {
         GeoBone head = this.getAnimationProcessor().getBone("head");
         if (head != null) {
            TransformablePlayer tp = (TransformablePlayer)player;
            boolean sceneActive = tp.heartbound$isTransformSceneActive();
            if (!sceneActive) {
               float pitch = animationState.getData(DataTickets.ENTITY_MODEL_DATA).headPitch();
               float yaw = animationState.getData(DataTickets.ENTITY_MODEL_DATA).netHeadYaw();
               head.setRotX(pitch * (float) (Math.PI / 180.0));
               head.setRotY(yaw * (float) (Math.PI / 180.0));
            }

            MinecraftClient client = MinecraftClient.getInstance();
            boolean isFirstPerson = client.options.getPerspective().isFirstPerson();
            boolean isLocalPlayer = client.cameraEntity == player;
            boolean hideHead = isFirstPerson && isLocalPlayer && sceneActive;
            ((GeoBoneExtension)head).setHiddenWithoutHidingChildren(hideHead);

            for (GeoBone child : head.getChildBones()) {
               String childName = child.getName();
               if (!"girlCam".equals(childName) && !childName.startsWith("armor")) {
                  child.setHidden(hideHead);
               }
            }
         }

         GeoBone headBone = this.getAnimationProcessor().getBone("Head2");
         if (headBone != null) {
            MinecraftClient client = MinecraftClient.getInstance();
            boolean isFirstPerson = client.options.getPerspective().isFirstPerson();
            boolean isRiderLocalPlayer = !player.getPassengerList().isEmpty() && client.cameraEntity == player.getPassengerList().get(0);
            ((GeoBoneExtension)headBone).setHiddenWithoutHidingChildren(isFirstPerson && isRiderLocalPlayer);
         }

         GeoBone boobWindow = this.getAnimationProcessor().getBone("boobWindow");
         if (boobWindow != null) {
            boobWindow.setHidden(ModConfig.INSTANCE.girls.boobWindow);
         }

         if (!player.hasVehicle() && !player.isSprinting() && !TransformedPlayerAnimatable.INSTANCE.isStripAnimPlaying()) {
            boolean var10000;
            label52: {
               if (player instanceof TransformablePlayer tp && tp.heartbound$isStripped()) {
                  var10000 = true;
                  break label52;
               }

               var10000 = false;
            }

            boolean stripped = var10000;
            this.calculateJigglePhysics(player, instanceId, stripped);
         }
      }
   }

   private void calculateJigglePhysics(PlayerEntity player, long instanceId, boolean stripped) {
      boolean inGui = MinecraftClient.getInstance().currentScreen != null;
      int entityId = player.getId();
      int currentTick = player.age;
      Vec3d velocity = inGui ? Vec3d.ZERO : player.getVelocity();
      float currentYaw = inGui ? 0.0F : player.bodyYaw;
      int lastTick = this.lastTickByEntity.getOrDefault(entityId, -1);
      Vec3d prevVelocity;
      float prevYaw;
      if (lastTick == -1) {
         prevVelocity = velocity;
         prevYaw = currentYaw;
         this.previousVelocityByEntity.put(entityId, velocity);
         this.previousYawByEntity.put(entityId, currentYaw);
         this.lastTickByEntity.put(entityId, currentTick);
      } else {
         prevVelocity = inGui ? Vec3d.ZERO : this.previousVelocityByEntity.getOrDefault(entityId, velocity);
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

      Vec3d deltaVelocity = velocity.subtract(prevVelocity);
      Vec3d inertiaForce = deltaVelocity.multiply(1.2);
      double yawInfluenceX = Math.sin(Math.toRadians((double)currentYaw)) * (double)yawDelta * 0.05;
      double yawInfluenceZ = Math.cos(Math.toRadians((double)currentYaw)) * (double)yawDelta * 0.05;
      inertiaForce = inertiaForce.add(yawInfluenceX, 0.0, yawInfluenceZ);
      double forceMag = inertiaForce.length();
      if (forceMag > 0.15) {
         inertiaForce = inertiaForce.multiply(0.15 / forceMag);
      }

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
      Map<String, Vec3d> defaultRotations = this.defaultRotationsByEntity.get(instanceId);
      long now = System.nanoTime();
      long lastUpdate = this.lastUpdateTimeByEntity.getOrDefault(instanceId, now);
      double deltaSec = (double)(now - lastUpdate) / 1.0E9;
      this.lastUpdateTimeByEntity.put(instanceId, now);
      double accumulator = this.timeAccumulator.get(instanceId) + deltaSec;
      accumulator = Math.min(accumulator, 0.2);

      while (accumulator >= 0.04) {
         for (JiggleBoneConfig config : jiggleBones) {
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

      for (JiggleBoneConfig configx : jiggleBones) {
         GeoBone bone = this.getAnimationProcessor().getBone(configx.boneName());
         if (bone != null) {
            Vec3d defaultRot = defaultRotations.get(configx.boneName());
            JigglePhysics jiggle = jiggleMap.get(configx.boneName());
            if (defaultRot != null && jiggle != null) {
               Vec3d offset = jiggle.getInterpolatedDisplacement(alpha);
               double offsetMag = offset.length();
               if (offsetMag > 0.3) {
                  offset = offset.multiply(0.3 / offsetMag);
               }

               bone.setRotX((float)(defaultRot.x + offset.x));
               bone.setRotY((float)(defaultRot.y + offset.y));
               bone.setRotZ((float)(defaultRot.z + offset.z));
            }
         }
      }
   }

   private static BakedGeoModel deepCopyModel(BakedGeoModel original) {
      List<GeoBone> copiedBones = new ArrayList<>();

      for (GeoBone bone : original.topLevelBones()) {
         copiedBones.add(deepCopyBone(bone, null));
      }

      return new BakedGeoModel(copiedBones, original.properties());
   }

   private static GeoBone deepCopyBone(GeoBone original, GeoBone newParent) {
      GeoBone copy = new GeoBone(newParent, original.getName(), original.getMirror(), original.getInflate(), original.shouldNeverRender(), original.getReset());
      copy.updatePivot(original.getPivotX(), original.getPivotY(), original.getPivotZ());
      BoneSnapshot snapshot = original.getInitialSnapshot();
      if (snapshot != null) {
         copy.updateRotation(snapshot.getRotX(), snapshot.getRotY(), snapshot.getRotZ());
         copy.updatePosition(snapshot.getOffsetX(), snapshot.getOffsetY(), snapshot.getOffsetZ());
         copy.updateScale(snapshot.getScaleX(), snapshot.getScaleY(), snapshot.getScaleZ());
      } else {
         copy.updateRotation(original.getRotX(), original.getRotY(), original.getRotZ());
         copy.updatePosition(original.getPosX(), original.getPosY(), original.getPosZ());
         copy.updateScale(original.getScaleX(), original.getScaleY(), original.getScaleZ());
      }

      copy.resetStateChanges();
      copy.getCubes().addAll(original.getCubes());

      for (GeoBone child : original.getChildBones()) {
         copy.getChildBones().add(deepCopyBone(child, copy));
      }

      return copy;
   }
}
