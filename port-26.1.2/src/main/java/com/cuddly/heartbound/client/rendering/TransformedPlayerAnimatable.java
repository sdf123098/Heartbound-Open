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
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.phys.Vec3;
import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.GeoReplacedEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animatable.manager.AnimatableManager.ControllerRegistrar;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.LoopType;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.animation.state.KeyFrameEvent;
import com.geckolib.cache.animation.keyframeevent.SoundKeyframeData;
import com.geckolib.util.GeckoLibUtil;

public class TransformedPlayerAnimatable implements GeoReplacedEntity {
   public static final TransformedPlayerAnimatable INSTANCE = new TransformedPlayerAnimatable();
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
   private String currentGirlId = "";
   private Player currentEntity;
   private boolean stripRequested = false;
   private boolean stripAnimPlaying = false;
   private boolean stripKeyframeFired = false;
   private final Map<UUID, String> lastSceneAnimMap = new HashMap<>();
   private final Map<UUID, Boolean> lastKnownStripped = new HashMap<>();
   private final Map<UUID, Boolean> playingAttackMap = new HashMap<>();

   private TransformedPlayerAnimatable() {
   }

   private boolean isLocalPlayer(Player player) {
      Minecraft client = Minecraft.getInstance();
      return client.player != null && client.player == player;
   }

   public void setCurrentGirlId(String girlId) {
      this.currentGirlId = girlId;
   }

   public void setCurrentEntity(Player entity) {
      this.currentEntity = entity;
   }

   public Player getCurrentEntity() {
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

      for (AnimationController<?> controller : manager.getAnimationControllers().values()) {
         controller.reset();
      }
   }

   @Override
   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(new AnimationController<>("transform_movement", 4, this::handleMovement).setSoundKeyframeHandler(this::handleSoundKeyframe));
      controllers.add(new AnimationController<>("transform_attack", 4, this::handleAttack).setSoundKeyframeHandler(this::handleSoundKeyframe));
      controllers.add(new AnimationController<>("transform_face", 4, this::handleFace).setSoundKeyframeHandler(this::handleSoundKeyframe));
      controllers.add(new AnimationController<>("transform_strip", 4, this::handleStrip).setSoundKeyframeHandler(this::handleStripSoundKeyframe));
      controllers.add(new AnimationController<>("transform_scene", 4, this::handleScene).setSoundKeyframeHandler(this::handleSceneSoundKeyframe));
   }

   private PlayState handleMovement(AnimationTest<TransformedPlayerAnimatable> state) {
      Player player = this.currentEntity;
      if (player == null) {
         return state.setAndContinue(RawAnimation.begin().thenLoop(this.animPath("idle")));
      } else {
         TransformablePlayer tp = (TransformablePlayer)player;
         if (tp.heartbound$isTransformSceneActive()) {
            return PlayState.STOP;
         } else {
            return !player.isDeadOrDying() && !(player.getHealth() <= 0.0F)
               ? state.setAndContinue(RawAnimation.begin().thenLoop(this.animPath(this.getMovementAnimName(player))))
               : state.setAndContinue(RawAnimation.begin().then(this.animPath("downed"), LoopType.PLAY_ONCE));
         }
      }
   }

   private PlayState handleAttack(AnimationTest<TransformedPlayerAnimatable> state) {
      Player player = this.currentEntity;
      if (player == null) {
         return PlayState.STOP;
      } else {
         UUID playerId = player.getUUID();
         boolean playingAttack = this.playingAttackMap.getOrDefault(playerId, false);
         if (!player.isDeadOrDying() && !(player.getHealth() <= 0.0F)) {
            TransformablePlayer tp = (TransformablePlayer)player;
            if (tp.heartbound$isTransformSceneActive()) {
               this.playingAttackMap.put(playerId, false);
               return PlayState.STOP;
            } else {
               AnimationController<?> controller = state.controller();
               if (player.isUsingItem() && player.getUseItem().getItem() instanceof BowItem) {
                  this.playingAttackMap.put(playerId, false);
                  return state.setAndContinue(RawAnimation.begin().thenLoop(this.animPath("bowcharge")));
               } else if (player.swinging && !playingAttack) {
                  this.playingAttackMap.put(playerId, true);
                  controller.reset();
                  int attackNum = ThreadLocalRandom.current().nextInt(3);
                  return state.setAndContinue(RawAnimation.begin().then(this.animPath("attack" + attackNum), LoopType.PLAY_ONCE));
               } else {
                  if (playingAttack) {
                     if (!controller.hasAnimationFinished()) {
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

   private String getMovementAnimName(Player player) {
      if (player.isPassenger()) {
         return "ride";
      } else if (!player.onGround() && !player.isInWater() && !player.isSwimming()) {
         return "fly";
      } else if (player.walkAnimation.speed() > 0.01F) {
         if (player.isSprinting()) {
            return "run";
         } else {
            return this.isWalkingBackward(player) ? "backwards_walk" : "walk";
         }
      } else {
         return "idle";
      }
   }

   private PlayState handleFace(AnimationTest<TransformedPlayerAnimatable> state) {
      return state.setAndContinue(RawAnimation.begin().then(this.animPath("blink"), LoopType.LOOP));
   }

   private PlayState handleStrip(AnimationTest<TransformedPlayerAnimatable> state) {
      Player player = this.currentEntity;
      if (player == null) {
         return PlayState.STOP;
      } else if (!player.isDeadOrDying() && !(player.getHealth() <= 0.0F)) {
         AnimationController<?> controller = state.controller();
         if (this.isLocalPlayer(player)) {
            if (this.stripRequested) {
               this.stripRequested = false;
               this.stripAnimPlaying = true;
               this.stripKeyframeFired = false;
               controller.reset();
               ClientPlayNetworking.send(new PlayerAnimLockC2SPacket(true));
               ClientPlayNetworking.send(new PlayerStripStartC2SPacket());
               return state.setAndContinue(RawAnimation.begin().then(this.animPath("strip"), LoopType.PLAY_ONCE));
            }

            if (this.stripAnimPlaying) {
               if (!controller.hasAnimationFinished()) {
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
            Boolean wasStripping = this.lastKnownStripped.get(player.getUUID());
            if (currentlyStripping && (wasStripping == null || !wasStripping)) {
               this.lastKnownStripped.put(player.getUUID(), true);
               controller.reset();
               return state.setAndContinue(RawAnimation.begin().then(this.animPath("strip"), LoopType.PLAY_ONCE));
            }

            if (!currentlyStripping) {
               this.lastKnownStripped.put(player.getUUID(), false);
            }

            if (controller.isAnimatingBones() && !controller.hasAnimationFinished()) {
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

   private void handleSoundKeyframe(KeyFrameEvent<TransformedPlayerAnimatable, SoundKeyframeData> event) {
      String key = event.keyframeData().getSound().toLowerCase();

      for (SoundEvent sound : SceneKeyframeEventRegistry.getSound(this.currentGirlId, key)) {
         Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, 1.0F, 1.0F));
      }
   }

   private void handleStripSoundKeyframe(KeyFrameEvent<TransformedPlayerAnimatable, SoundKeyframeData> event) {
      this.handleSoundKeyframe(event);
      if ("becomenude".equalsIgnoreCase(event.keyframeData().getSound()) && this.currentEntity != null && this.isLocalPlayer(this.currentEntity)) {
         this.stripKeyframeFired = true;
         ClientPlayNetworking.send(new PlayerStripToggleC2SPacket());
      }
   }

   private PlayState handleScene(AnimationTest<TransformedPlayerAnimatable> state) {
      Player player = this.currentEntity;
      if (player == null) {
         return PlayState.STOP;
      } else {
         TransformablePlayer tp = (TransformablePlayer)player;
         ScenePhase phase = tp.heartbound$getTransformScenePhase();
         UUID playerId = player.getUUID();
         if (phase == ScenePhase.NONE) {
            this.lastSceneAnimMap.remove(playerId);
            return PlayState.STOP;
         } else {
            Scene scene = tp.heartbound$getTransformScene();
            AnimationController<?> controller = state.controller();
            String lastAnim = this.lastSceneAnimMap.getOrDefault(playerId, "");
            if (controller.hasAnimationFinished() && !lastAnim.isEmpty()) {
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

   private PlayState setSceneAnimIfChanged(AnimationTest<?> state, String anim, LoopType loop, UUID playerId) {
      if (anim != null && !anim.isEmpty()) {
         String lastAnim = this.lastSceneAnimMap.getOrDefault(playerId, "");
         if (!anim.equals(lastAnim)) {
            state.controller().reset();
            this.lastSceneAnimMap.put(playerId, anim);
            return state.setAndContinue(RawAnimation.begin().then(this.animPath(anim), loop));
         } else {
            return PlayState.CONTINUE;
         }
      } else {
         return PlayState.CONTINUE;
      }
   }

   private void handleSceneSoundKeyframe(KeyFrameEvent<TransformedPlayerAnimatable, SoundKeyframeData> event) {
      this.handleSoundKeyframe(event);
      String key = event.keyframeData().getSound().toLowerCase();
      if (key.contains("thrust")) {
         Player player = this.currentEntity;
         if (player != null && this.isLocalPlayer(player)) {
            ClientPlayNetworking.send(new TransformThrustKeyframeC2SPacket());
         }
      }
   }

   private String animPath(String name) {
      return "animation." + this.currentGirlId + "." + name;
   }

   private boolean isWalkingBackward(Player player) {
      double dx = player.getX() - player.xo;
      double dz = player.getZ() - player.zo;
      Vec3 look = player.getLookAngle();
      return dx * look.x + dz * look.z < -0.01;
   }

   @Override
   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.cache;
   }
}
