package com.cuddly.heartbound.client.rendering;

import com.cuddly.heartbound.networking.C2S.PlayerAnimLockC2SPacket;
import com.cuddly.heartbound.networking.C2S.PlayerStripStartC2SPacket;
import com.cuddly.heartbound.networking.C2S.PlayerStripToggleC2SPacket;
import com.cuddly.heartbound.networking.C2S.TransformAnimationFinishC2SPacket;
import com.cuddly.heartbound.networking.C2S.TransformThrustKeyframeC2SPacket;
import com.cuddly.heartbound.registries.SceneKeyframeEventRegistry;
import com.cuddly.heartbound.transformation.TransformablePlayer;
import com.cuddly.heartbound.util.variables.Scene;
import com.cuddly.heartbound.util.variables.ScenePhase;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoReplacedEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.animation.Animation.LoopType;
import software.bernie.geckolib.animation.AnimationController.State;
import software.bernie.geckolib.animation.keyframe.event.SoundKeyframeEvent;
import software.bernie.geckolib.util.GeckoLibUtil;

public class TransformedPlayerAnimatable implements GeoReplacedEntity {
   public static final TransformedPlayerAnimatable INSTANCE = new TransformedPlayerAnimatable();
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
   private String currentGirlId = "";
   private PlayerEntity currentEntity;
   private boolean stripRequested = false;
   private boolean stripAnimPlaying = false;
   private boolean stripKeyframeFired = false;
   private final Map<UUID, String> lastSceneAnimMap = new HashMap<>();
   private final Map<UUID, Boolean> lastKnownStripped = new HashMap<>();
   private final Map<UUID, Boolean> playingAttackMap = new HashMap<>();

   private TransformedPlayerAnimatable() {
   }

   private boolean isLocalPlayer(PlayerEntity player) {
      MinecraftClient client = MinecraftClient.getInstance();
      return client.player != null && client.player == player;
   }

   public void setCurrentGirlId(String girlId) {
      this.currentGirlId = girlId;
   }

   public void setCurrentEntity(PlayerEntity entity) {
      this.currentEntity = entity;
   }

   public PlayerEntity getCurrentEntity() {
      return this.currentEntity;
   }

   public void requestStripAnimation() {
      this.stripRequested = true;
   }

   public boolean isStripAnimPlaying() {
      return this.stripAnimPlaying;
   }

   public void resetAnimationState(long instanceId) {
      this.lastSceneAnimMap.clear();
      this.lastKnownStripped.clear();
      this.stripRequested = false;
      this.stripAnimPlaying = false;
      this.stripKeyframeFired = false;
      this.playingAttackMap.clear();
      AnimatableManager<GeoAnimatable> manager = this.cache.getManagerForId(instanceId);
      manager.clearSnapshotCache();

      for (AnimationController<?> controller : manager.getAnimationControllers().values()) {
         controller.forceAnimationReset();
         controller.stop();
      }
   }

   @Override
   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(new AnimationController<>(this, "transform_movement", 4, this::handleMovement).setSoundKeyframeHandler(this::handleSoundKeyframe));
      controllers.add(new AnimationController<>(this, "transform_attack", 4, this::handleAttack).setSoundKeyframeHandler(this::handleSoundKeyframe));
      controllers.add(new AnimationController<>(this, "transform_face", 4, this::handleFace).setSoundKeyframeHandler(this::handleSoundKeyframe));
      controllers.add(new AnimationController<>(this, "transform_strip", 4, this::handleStrip).setSoundKeyframeHandler(this::handleStripSoundKeyframe));
      controllers.add(new AnimationController<>(this, "transform_scene", 4, this::handleScene).setSoundKeyframeHandler(this::handleSceneSoundKeyframe));
   }

   private PlayState handleMovement(AnimationState<TransformedPlayerAnimatable> state) {
      PlayerEntity player = this.currentEntity;
      if (player == null) {
         return state.setAndContinue(RawAnimation.begin().thenLoop(this.animPath("idle")));
      } else {
         TransformablePlayer tp = (TransformablePlayer)player;
         if (tp.heartbound$isTransformSceneActive()) {
            return PlayState.STOP;
         } else {
            return !player.isDead() && !(player.getHealth() <= 0.0F)
               ? state.setAndContinue(RawAnimation.begin().thenLoop(this.animPath(this.getMovementAnimName(player))))
               : state.setAndContinue(RawAnimation.begin().then(this.animPath("downed"), LoopType.PLAY_ONCE));
         }
      }
   }

   private PlayState handleAttack(AnimationState<TransformedPlayerAnimatable> state) {
      PlayerEntity player = this.currentEntity;
      if (player == null) {
         return PlayState.STOP;
      } else {
         UUID playerId = player.getUuid();
         boolean playingAttack = this.playingAttackMap.getOrDefault(playerId, false);
         if (!player.isDead() && !(player.getHealth() <= 0.0F)) {
            TransformablePlayer tp = (TransformablePlayer)player;
            if (tp.heartbound$isTransformSceneActive()) {
               this.playingAttackMap.put(playerId, false);
               return PlayState.STOP;
            } else {
               AnimationController<?> controller = state.getController();
               if (player.isUsingItem() && player.getActiveItem().getItem() instanceof BowItem) {
                  this.playingAttackMap.put(playerId, false);
                  return state.setAndContinue(RawAnimation.begin().thenLoop(this.animPath("bowcharge")));
               } else if (player.handSwinging && !playingAttack) {
                  this.playingAttackMap.put(playerId, true);
                  controller.forceAnimationReset();
                  int attackNum = ThreadLocalRandom.current().nextInt(3);
                  return state.setAndContinue(RawAnimation.begin().then(this.animPath("attack" + attackNum), LoopType.PLAY_ONCE));
               } else {
                  if (playingAttack) {
                     if (!controller.hasAnimationFinished() && controller.getAnimationState() != State.STOPPED) {
                        return PlayState.CONTINUE;
                     }

                     this.playingAttackMap.put(playerId, false);
                  }

                  return state.setAndContinue(RawAnimation.begin().thenLoop(this.animPath(this.getMovementAnimName(player))));
               }
            }
         } else {
            this.playingAttackMap.put(playerId, false);
            return state.setAndContinue(RawAnimation.begin().then(this.animPath("downed"), LoopType.PLAY_ONCE));
         }
      }
   }

   private String getMovementAnimName(PlayerEntity player) {
      if (player.hasVehicle()) {
         return "ride";
      } else if (!player.isOnGround() && !player.isTouchingWater() && !player.isSwimming()) {
         return "fly";
      } else if (player.limbAnimator.getSpeed() > 0.01F) {
         if (player.isSprinting()) {
            return "run";
         } else {
            return this.isWalkingBackward(player) ? "backwards_walk" : "walk";
         }
      } else {
         return "idle";
      }
   }

   private PlayState handleFace(AnimationState<TransformedPlayerAnimatable> state) {
      return state.setAndContinue(RawAnimation.begin().then(this.animPath("blink"), LoopType.LOOP));
   }

   private PlayState handleStrip(AnimationState<TransformedPlayerAnimatable> state) {
      PlayerEntity player = this.currentEntity;
      if (player == null) {
         return PlayState.STOP;
      } else if (!player.isDead() && !(player.getHealth() <= 0.0F)) {
         AnimationController<?> controller = state.getController();
         if (this.isLocalPlayer(player)) {
            if (this.stripRequested) {
               this.stripRequested = false;
               this.stripAnimPlaying = true;
               this.stripKeyframeFired = false;
               controller.forceAnimationReset();
               ClientPlayNetworking.send(new PlayerAnimLockC2SPacket(true));
               ClientPlayNetworking.send(new PlayerStripStartC2SPacket());
               return state.setAndContinue(RawAnimation.begin().then(this.animPath("strip"), LoopType.PLAY_ONCE));
            }

            if (this.stripAnimPlaying) {
               if (controller.getAnimationState() != State.STOPPED) {
                  return PlayState.CONTINUE;
               }

               this.stripAnimPlaying = false;
               ClientPlayNetworking.send(new PlayerAnimLockC2SPacket(false));
               if (!this.stripKeyframeFired) {
                  ClientPlayNetworking.send(new PlayerStripToggleC2SPacket());
               }
            }
         } else {
            TransformablePlayer tp = (TransformablePlayer)player;
            boolean currentlyStripping = tp.heartbound$isStripping();
            Boolean wasStripping = this.lastKnownStripped.get(player.getUuid());
            if (currentlyStripping && (wasStripping == null || !wasStripping)) {
               this.lastKnownStripped.put(player.getUuid(), true);
               controller.forceAnimationReset();
               return state.setAndContinue(RawAnimation.begin().then(this.animPath("strip"), LoopType.PLAY_ONCE));
            }

            if (!currentlyStripping) {
               this.lastKnownStripped.put(player.getUuid(), false);
            }

            if (controller.getAnimationState() == State.RUNNING || controller.getAnimationState() == State.TRANSITIONING) {
               return PlayState.CONTINUE;
            }
         }

         return PlayState.STOP;
      } else {
         if (this.stripAnimPlaying && this.isLocalPlayer(player)) {
            ClientPlayNetworking.send(new PlayerAnimLockC2SPacket(false));
         }

         this.stripAnimPlaying = false;
         this.stripRequested = false;
         return PlayState.CONTINUE;
      }
   }

   private void handleSoundKeyframe(SoundKeyframeEvent<TransformedPlayerAnimatable> event) {
      String key = event.getKeyframeData().getSound().toLowerCase();

      for (SoundEvent sound : SceneKeyframeEventRegistry.getSound(this.currentGirlId, key)) {
         MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(sound, 1.0F, 1.0F));
      }
   }

   private void handleStripSoundKeyframe(SoundKeyframeEvent<TransformedPlayerAnimatable> event) {
      this.handleSoundKeyframe(event);
      if ("becomenude".equalsIgnoreCase(event.getKeyframeData().getSound()) && this.currentEntity != null && this.isLocalPlayer(this.currentEntity)) {
         this.stripKeyframeFired = true;
         ClientPlayNetworking.send(new PlayerStripToggleC2SPacket());
      }
   }

   private PlayState handleScene(AnimationState<TransformedPlayerAnimatable> state) {
      PlayerEntity player = this.currentEntity;
      if (player == null) {
         return PlayState.STOP;
      } else {
         TransformablePlayer tp = (TransformablePlayer)player;
         ScenePhase phase = tp.heartbound$getTransformScenePhase();
         UUID playerId = player.getUuid();
         if (phase == ScenePhase.NONE) {
            this.lastSceneAnimMap.remove(playerId);
            return PlayState.STOP;
         } else {
            Scene scene = tp.heartbound$getTransformScene();
            AnimationController<?> controller = state.getController();
            String lastAnim = this.lastSceneAnimMap.getOrDefault(playerId, "");
            if ((controller.hasAnimationFinished() || controller.getAnimationState() == State.PAUSED) && !lastAnim.isEmpty()) {
               if (this.isLocalPlayer(player)) {
                  ClientPlayNetworking.send(new TransformAnimationFinishC2SPacket());
               }

               this.lastSceneAnimMap.put(playerId, "");
            }
            return switch (phase) {
               case LAYING_DOWN -> {
                  String laying = scene.bedIdle().isEmpty() ? "null" : scene.layOnBed();
                  yield this.setSceneAnimIfChanged(state, laying, LoopType.HOLD_ON_LAST_FRAME, playerId);
               }
               case BED_IDLE -> {
                  String bedIdle = scene.bedIdle().isEmpty() ? "null" : scene.bedIdle();
                  yield this.setSceneAnimIfChanged(state, bedIdle, LoopType.LOOP, playerId);
               }
               case INTRO -> {
                  List<String> intros = scene.introAnim();
                  if (intros.isEmpty()) {
                     yield PlayState.CONTINUE;
                  } else {
                     String current = intros.get(Math.min(tp.heartbound$getTransformIntroIndex(), intros.size() - 1));
                     yield this.setSceneAnimIfChanged(state, current, LoopType.HOLD_ON_LAST_FRAME, playerId);
                  }
               }
               case HAVING_SEX -> {
                  boolean thrusting = tp.heartbound$isTransformThrusting();
                  List<String> anims = thrusting ? scene.fastAnim() : scene.slowAnim();
                  yield anims.isEmpty() ? PlayState.CONTINUE : state.setAndContinue(RawAnimation.begin().then(this.animPath(anims.getFirst()), LoopType.LOOP));
               }
               case CUM -> this.setSceneAnimIfChanged(state, scene.cumAnim(), LoopType.HOLD_ON_LAST_FRAME, playerId);
               case STATIONARY_INTRO -> {
                  List<String> sequence = scene.stationaryIntroAnim();
                  if (sequence.isEmpty()) {
                     yield PlayState.CONTINUE;
                  } else {
                     String current = sequence.get(Math.min(tp.heartbound$getTransformStationaryIndex(), sequence.size() - 1));
                     yield this.setSceneAnimIfChanged(state, current, LoopType.HOLD_ON_LAST_FRAME, playerId);
                  }
               }
               case STATIONARY -> {
                  String loopAnim = scene.stationaryLoopAnim();
                  yield loopAnim != null && !loopAnim.isEmpty()
                     ? this.setSceneAnimIfChanged(state, loopAnim, LoopType.HOLD_ON_LAST_FRAME, playerId)
                     : PlayState.STOP;
               }
               default -> PlayState.CONTINUE;
            };
         }
      }
   }

   private PlayState setSceneAnimIfChanged(AnimationState<?> state, String anim, LoopType loop, UUID playerId) {
      if (anim != null && !anim.isEmpty()) {
         String lastAnim = this.lastSceneAnimMap.getOrDefault(playerId, "");
         if (!anim.equals(lastAnim)) {
            state.resetCurrentAnimation();
            this.lastSceneAnimMap.put(playerId, anim);
            return state.setAndContinue(RawAnimation.begin().then(this.animPath(anim), loop));
         } else {
            return PlayState.CONTINUE;
         }
      } else {
         return PlayState.CONTINUE;
      }
   }

   private void handleSceneSoundKeyframe(SoundKeyframeEvent<TransformedPlayerAnimatable> event) {
      this.handleSoundKeyframe(event);
      String key = event.getKeyframeData().getSound().toLowerCase();
      if (key.contains("thrust")) {
         PlayerEntity player = this.currentEntity;
         if (player != null && this.isLocalPlayer(player)) {
            ClientPlayNetworking.send(new TransformThrustKeyframeC2SPacket());
         }
      }
   }

   private String animPath(String name) {
      return "animation." + this.currentGirlId + "." + name;
   }

   private boolean isWalkingBackward(PlayerEntity player) {
      double dx = player.getX() - player.prevX;
      double dz = player.getZ() - player.prevZ;
      Vec3d look = player.getRotationVector();
      return dx * look.x + dz * look.z < -0.01;
   }

   @Override
   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.cache;
   }

   public EntityType<?> getReplacingEntityType() {
      return EntityType.PLAYER;
   }
}
