package com.cuddly.heartbound.mixins.transformation;

import com.cuddly.heartbound.Heartbound;
import com.cuddly.heartbound.networking.S2C.PlayCumHudAnimationS2CPacket;
import com.cuddly.heartbound.registries.HeartboundTrackedDataRegistry;
import com.cuddly.heartbound.transformation.TransformablePlayer;
import com.cuddly.heartbound.util.Utils;
import com.cuddly.heartbound.util.variables.Scene;
import com.cuddly.heartbound.util.variables.ScenePhase;
import com.cuddly.heartbound.util.variables.SceneType;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Player.class})
public abstract class PlayerTransformationMixin implements TransformablePlayer {
   @Unique
   private static final EntityDataAccessor<String> HEARTBOUND_TRANSFORM_GIRL_ID = SynchedEntityData.defineId(Player.class, EntityDataSerializers.STRING);
   @Unique
   private static final EntityDataAccessor<Boolean> HEARTBOUND_TRANSFORM_STRIPPED = SynchedEntityData.defineId(Player.class, EntityDataSerializers.BOOLEAN);
   @Unique
   private static final EntityDataAccessor<Boolean> HEARTBOUND_TRANSFORM_STRIPPING = SynchedEntityData.defineId(Player.class, EntityDataSerializers.BOOLEAN);
   @Unique
   private static final EntityDataAccessor<Boolean> HEARTBOUND_ANIM_LOCKED = SynchedEntityData.defineId(Player.class, EntityDataSerializers.BOOLEAN);
   @Unique
   private static final EntityDataAccessor<Scene> HEARTBOUND_TRANSFORM_SCENE = SynchedEntityData.defineId(Player.class, HeartboundTrackedDataRegistry.SCENE);
   @Unique
   private static final EntityDataAccessor<ScenePhase> HEARTBOUND_TRANSFORM_SCENE_PHASE = SynchedEntityData.defineId(
      Player.class, HeartboundTrackedDataRegistry.SCENE_PHASE
   );
   @Unique
   private static final EntityDataAccessor<Float> HEARTBOUND_TRANSFORM_SCENE_PROGRESS = SynchedEntityData.defineId(Player.class, EntityDataSerializers.FLOAT);
   @Unique
   private static final EntityDataAccessor<Float> HEARTBOUND_TRANSFORM_CUM_THRESHOLD = SynchedEntityData.defineId(Player.class, EntityDataSerializers.FLOAT);
   @Unique
   private static final EntityDataAccessor<Boolean> HEARTBOUND_TRANSFORM_THRUSTING = SynchedEntityData.defineId(Player.class, EntityDataSerializers.BOOLEAN);
   @Unique
   private static final EntityDataAccessor<Integer> HEARTBOUND_TRANSFORM_INTRO_INDEX = SynchedEntityData.defineId(Player.class, EntityDataSerializers.INT);
   @Unique
   private static final EntityDataAccessor<Integer> HEARTBOUND_TRANSFORM_STATIONARY_INDEX = SynchedEntityData.defineId(
      Player.class, EntityDataSerializers.INT
   );
   @Unique
   private static final EntityDataAccessor<Integer> HEARTBOUND_TRANSFORM_STATIONARY_LOOP = SynchedEntityData.defineId(
      Player.class, EntityDataSerializers.INT
   );
   @Unique
   private static final EntityDataAccessor<Integer> HEARTBOUND_TRANSFORM_STATIONARY_LOOP_THRESHOLD = SynchedEntityData.defineId(
      Player.class, EntityDataSerializers.INT
   );
   @Unique
   private static final EntityDataAccessor<Optional<UUID>> HEARTBOUND_TRANSFORM_SCENE_PARTNER = SynchedEntityData.defineId(
      Player.class, HeartboundTrackedDataRegistry.OPTIONAL_UUID
   );
   @Unique
   private static final EntityDataAccessor<Boolean> HEARTBOUND_WAITING_FOR_BED = SynchedEntityData.defineId(Player.class, EntityDataSerializers.BOOLEAN);
   @Unique
   private static final EntityDataAccessor<Boolean> HEARTBOUND_WAITING_FOR_TARGET = SynchedEntityData.defineId(Player.class, EntityDataSerializers.BOOLEAN);
   @Unique
   private static final String NBT_KEY = "heartbound_transformation";
   @Unique
   private static final String NBT_STRIPPED_KEY = "heartbound_stripped";
   @Unique
   private static final float PROGRESS_SPEED = 0.1F;
   @Unique
   private int heartbound$animLockTicks = 0;
   @Unique
   private BlockPos heartbound$bedPos = null;
   @Unique
   private String heartbound$lastSceneAnim = "";
   @Unique
   private Vec3 heartbound$passengerBonePos = Vec3.ZERO;
   @Unique
   private float heartbound$lockedYaw = Float.NaN;
   @Unique
   private String heartbound$lastGirlId = "";

   @Inject(
      method = {"defineSynchedData(Lnet/minecraft/network/syncher/SynchedEntityData$Builder;)V"},
      at = {@At("TAIL")}
   )
   private void heartbound$initDataTracker(Builder builder, CallbackInfo ci) {
      builder.define(HEARTBOUND_TRANSFORM_GIRL_ID, "");
      builder.define(HEARTBOUND_TRANSFORM_STRIPPED, false);
      builder.define(HEARTBOUND_TRANSFORM_STRIPPING, false);
      builder.define(HEARTBOUND_ANIM_LOCKED, false);
      builder.define(HEARTBOUND_TRANSFORM_SCENE, Scene.EMPTY);
      builder.define(HEARTBOUND_TRANSFORM_SCENE_PHASE, ScenePhase.NONE);
      builder.define(HEARTBOUND_TRANSFORM_SCENE_PROGRESS, 0.0F);
      builder.define(HEARTBOUND_TRANSFORM_CUM_THRESHOLD, 5.0F);
      builder.define(HEARTBOUND_TRANSFORM_THRUSTING, false);
      builder.define(HEARTBOUND_TRANSFORM_INTRO_INDEX, 0);
      builder.define(HEARTBOUND_TRANSFORM_STATIONARY_INDEX, 0);
      builder.define(HEARTBOUND_TRANSFORM_STATIONARY_LOOP, 0);
      builder.define(HEARTBOUND_TRANSFORM_STATIONARY_LOOP_THRESHOLD, 0);
      builder.define(HEARTBOUND_TRANSFORM_SCENE_PARTNER, Optional.empty());
      builder.define(HEARTBOUND_WAITING_FOR_BED, false);
      builder.define(HEARTBOUND_WAITING_FOR_TARGET, false);
   }

   @Override
   public String heartbound$getTransformGirlId() {
      return ((Player)(Object)this).getEntityData().get(HEARTBOUND_TRANSFORM_GIRL_ID);
   }

   @Override
   public void heartbound$setTransformGirlId(String girlId) {
      ((Player)(Object)this).getEntityData().set(HEARTBOUND_TRANSFORM_GIRL_ID, girlId != null ? girlId : "");
      this.heartbound$setStripped(false);
      ((Player)(Object)this).refreshDimensions();
      Heartbound.LOGGER.info("[PlayerTransformationMixin] {} setTransformGirlId -> '{}' (stripped reset, dimensions refreshed)", ((Player)(Object)this).getScoreboardName(), girlId);
   }

   @Override
   public boolean heartbound$isStripped() {
      return ((Player)(Object)this).getEntityData().get(HEARTBOUND_TRANSFORM_STRIPPED);
   }

   @Override
   public void heartbound$setStripped(boolean stripped) {
      ((Player)(Object)this).getEntityData().set(HEARTBOUND_TRANSFORM_STRIPPED, stripped);
   }

   @Override
   public boolean heartbound$isStripping() {
      return ((Player)(Object)this).getEntityData().get(HEARTBOUND_TRANSFORM_STRIPPING);
   }

   @Override
   public void heartbound$setStripping(boolean stripping) {
      ((Player)(Object)this).getEntityData().set(HEARTBOUND_TRANSFORM_STRIPPING, stripping);
   }

   @Override
   public boolean heartbound$isAnimLocked() {
      return ((Player)(Object)this).getEntityData().get(HEARTBOUND_ANIM_LOCKED);
   }

   @Override
   public void heartbound$setAnimLocked(boolean locked) {
      ((Player)(Object)this).getEntityData().set(HEARTBOUND_ANIM_LOCKED, locked);
      if (!locked) {
         this.heartbound$animLockTicks = 0;
      }
   }

   @Override
   public void heartbound$lockAnimForTicks(int ticks) {
      this.heartbound$setAnimLocked(true);
      this.heartbound$animLockTicks = ticks;
   }

   @Override
   public Scene heartbound$getTransformScene() {
      return ((Player)(Object)this).getEntityData().get(HEARTBOUND_TRANSFORM_SCENE);
   }

   @Override
   public void heartbound$setTransformScene(Scene scene) {
      ((Player)(Object)this).getEntityData().set(HEARTBOUND_TRANSFORM_SCENE, scene);
   }

   @Override
   public ScenePhase heartbound$getTransformScenePhase() {
      return ((Player)(Object)this).getEntityData().get(HEARTBOUND_TRANSFORM_SCENE_PHASE);
   }

   @Override
   public void heartbound$setTransformScenePhase(ScenePhase phase) {
      ((Player)(Object)this).getEntityData().set(HEARTBOUND_TRANSFORM_SCENE_PHASE, phase);
   }

   @Override
   public float heartbound$getTransformSceneProgress() {
      return ((Player)(Object)this).getEntityData().get(HEARTBOUND_TRANSFORM_SCENE_PROGRESS);
   }

   @Override
   public void heartbound$setTransformSceneProgress(float progress) {
      ((Player)(Object)this).getEntityData().set(HEARTBOUND_TRANSFORM_SCENE_PROGRESS, progress);
   }

   @Override
   public float heartbound$getTransformCumThreshold() {
      return ((Player)(Object)this).getEntityData().get(HEARTBOUND_TRANSFORM_CUM_THRESHOLD);
   }

   @Override
   public void heartbound$setTransformCumThreshold(float threshold) {
      ((Player)(Object)this).getEntityData().set(HEARTBOUND_TRANSFORM_CUM_THRESHOLD, threshold);
   }

   @Override
   public boolean heartbound$isTransformThrusting() {
      return ((Player)(Object)this).getEntityData().get(HEARTBOUND_TRANSFORM_THRUSTING);
   }

   @Override
   public void heartbound$setTransformThrusting(boolean thrusting) {
      ((Player)(Object)this).getEntityData().set(HEARTBOUND_TRANSFORM_THRUSTING, thrusting);
   }

   @Override
   public int heartbound$getTransformIntroIndex() {
      return ((Player)(Object)this).getEntityData().get(HEARTBOUND_TRANSFORM_INTRO_INDEX);
   }

   @Override
   public void heartbound$setTransformIntroIndex(int index) {
      ((Player)(Object)this).getEntityData().set(HEARTBOUND_TRANSFORM_INTRO_INDEX, index);
   }

   @Override
   public int heartbound$getTransformStationaryIndex() {
      return ((Player)(Object)this).getEntityData().get(HEARTBOUND_TRANSFORM_STATIONARY_INDEX);
   }

   @Override
   public void heartbound$setTransformStationaryIndex(int index) {
      ((Player)(Object)this).getEntityData().set(HEARTBOUND_TRANSFORM_STATIONARY_INDEX, index);
   }

   @Override
   public int heartbound$getTransformStationaryLoop() {
      return ((Player)(Object)this).getEntityData().get(HEARTBOUND_TRANSFORM_STATIONARY_LOOP);
   }

   @Override
   public void heartbound$setTransformStationaryLoop(int loop) {
      ((Player)(Object)this).getEntityData().set(HEARTBOUND_TRANSFORM_STATIONARY_LOOP, loop);
   }

   @Override
   public int heartbound$getTransformStationaryLoopThreshold() {
      return ((Player)(Object)this).getEntityData().get(HEARTBOUND_TRANSFORM_STATIONARY_LOOP_THRESHOLD);
   }

   @Override
   public void heartbound$setTransformStationaryLoopThreshold(int threshold) {
      ((Player)(Object)this).getEntityData().set(HEARTBOUND_TRANSFORM_STATIONARY_LOOP_THRESHOLD, threshold);
   }

   @Override
   public Optional<UUID> heartbound$getTransformScenePartner() {
      return ((Player)(Object)this).getEntityData().get(HEARTBOUND_TRANSFORM_SCENE_PARTNER);
   }

   @Override
   public void heartbound$setTransformScenePartner(@Nullable Player player) {
      if (player == null) {
         ((Player)(Object)this).getEntityData().set(HEARTBOUND_TRANSFORM_SCENE_PARTNER, Optional.empty());
      } else {
         ((Player)(Object)this).getEntityData().set(HEARTBOUND_TRANSFORM_SCENE_PARTNER, Optional.of(player.getUUID()));
      }
   }

   @Override
   public boolean heartbound$isWaitingForBed() {
      return ((Player)(Object)this).getEntityData().get(HEARTBOUND_WAITING_FOR_BED);
   }

   @Override
   public void heartbound$setWaitingForBed(boolean waiting) {
      ((Player)(Object)this).getEntityData().set(HEARTBOUND_WAITING_FOR_BED, waiting);
   }

   @Override
   public boolean heartbound$isWaitingForTarget() {
      return ((Player)(Object)this).getEntityData().get(HEARTBOUND_WAITING_FOR_TARGET);
   }

   @Override
   public void heartbound$setWaitingForTarget(boolean waiting) {
      ((Player)(Object)this).getEntityData().set(HEARTBOUND_WAITING_FOR_TARGET, waiting);
   }

   @Override
   public Vec3 heartbound$getPassengerBonePosition() {
      return this.heartbound$passengerBonePos;
   }

   @Override
   public void heartbound$setPassengerBonePosition(Vec3 position) {
      this.heartbound$passengerBonePos = position;
   }

   @Inject(
      method = {"tick()V"},
      at = {@At("TAIL")}
   )
   private void heartbound$tickAnimLock(CallbackInfo ci) {
      Player self = (Player)(Object)this;
      String currentGirlId = this.heartbound$getTransformGirlId();
      if (!currentGirlId.equals(this.heartbound$lastGirlId)) {
         this.heartbound$lastGirlId = currentGirlId;
         self.refreshDimensions();
      }

      if (this.heartbound$isTransformSceneActive()) {
         if (Float.isNaN(this.heartbound$lockedYaw)) {
            this.heartbound$lockedYaw = self.getYRot();
         }

         self.yBodyRotO = this.heartbound$lockedYaw;
         self.setYBodyRot(this.heartbound$lockedYaw);
         self.setSprinting(false);
      } else {
         this.heartbound$lockedYaw = Float.NaN;
      }

      if (!self.level().isClientSide()) {
         if (this.heartbound$animLockTicks > 0) {
            this.heartbound$animLockTicks--;
            if (this.heartbound$animLockTicks <= 0) {
               this.heartbound$setAnimLocked(false);
            }
         }

         if (this.heartbound$isTransformed()) {
            this.heartbound$tickTransformScene();
         }
      }
   }

   @Unique
   private void heartbound$tickTransformScene() {
      Player self = (Player)(Object)this;
      if (this.heartbound$isWaitingForBed()) {
         this.heartbound$tickWaitingForBed();
      } else if (this.heartbound$isWaitingForTarget()) {
         this.heartbound$tickWaitingForTarget();
      } else {
         if (!Float.isNaN(this.heartbound$lockedYaw)) {
            self.yBodyRotO = this.heartbound$lockedYaw;
            self.setYBodyRot(this.heartbound$lockedYaw);
         }

         ScenePhase phase = this.heartbound$getTransformScenePhase();
         if (phase != ScenePhase.NONE) {
            Scene scene = this.heartbound$getTransformScene();
            SceneType type = scene.sceneType();
            if (this.heartbound$bedPos != null
               && type == SceneType.ON_BED
               && !Utils.checkForBlockAt(self.level(), this.heartbound$bedPos, null, BlockTags.BEDS)) {
               this.heartbound$stopTransformScene();
            } else {
               if (phase == ScenePhase.BED_IDLE && !self.isVehicle()) {
                  this.heartbound$tickWaitingForContact();
               }
               boolean isActivePhase = switch (phase) {
                  case INTRO, HAVING_SEX, CUM -> true;
                  default -> false;
               };
               if (isActivePhase && !self.isVehicle()) {
                  this.heartbound$stopTransformScene();
               }
            }
         }
      }
   }

   @Unique
   private Vec3 heartbound$calculateBedSnapPos(BlockPos bedPos, Direction facing) {
      float offset = this.heartbound$getTransformScene().bedAlignmentOffset();

      return switch (facing) {
         case NORTH -> new Vec3((double)bedPos.getX() + 0.5, (double)bedPos.getY(), (double)bedPos.getZ() + 1.5 - (double)offset);
         case EAST -> new Vec3((double)bedPos.getX() - 0.5 + (double)offset, (double)bedPos.getY(), (double)bedPos.getZ() + 0.5);
         case SOUTH -> new Vec3((double)bedPos.getX() + 0.5, (double)bedPos.getY(), (double)bedPos.getZ() - 0.5 + (double)offset);
         case WEST -> new Vec3((double)bedPos.getX() + 1.5 - (double)offset, (double)bedPos.getY(), (double)bedPos.getZ() + 0.5);
         default -> bedPos.getCenter();
      };
   }

   @Unique
   private void heartbound$tickWaitingForTarget() {
      Player self = (Player)(Object)this;
      if (self.level() instanceof ServerLevel serverWorld) {
         for (ServerPlayer other : serverWorld.players()) {
            if (other != self && !Heartbound.activeScenes.containsKey(other.getUUID())) {
               if (other instanceof TransformablePlayer) {
                  TransformablePlayer tp = (TransformablePlayer)other;
                  if (tp.heartbound$isTransformSceneActive()) {
                     continue;
                  }
               }

               if (self.distanceToSqr(other) <= 6.25) {
                  this.heartbound$setWaitingForTarget(false);
                  this.heartbound$startRidingScene(other);
                  return;
               }
            }
         }
      }
   }

   @Unique
   private void heartbound$tickWaitingForBed() {
      Player self = (Player)(Object)this;
      if (this.heartbound$bedPos == null) {
         this.heartbound$stopTransformScene();
      } else if (!Utils.checkForBlockAt(self.level(), this.heartbound$bedPos, null, BlockTags.BEDS)) {
         this.heartbound$stopTransformScene();
      } else {
         double distSq = self.blockPosition().distSqr(this.heartbound$bedPos);
         if (distSq <= 2.25) {
            BlockState state = self.level().getBlockState(this.heartbound$bedPos);
            Direction bedFacing = Direction.NORTH;
            if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
               bedFacing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            }

            Vec3 snapPos = this.heartbound$calculateBedSnapPos(this.heartbound$bedPos, bedFacing);
            float playerLookYaw = self.getYRot();
            float yaw = bedFacing.toYRot();
            self.teleportTo(snapPos.x, snapPos.y, snapPos.z);
            self.setYRot(yaw);
            self.setXRot(self.getXRot());
            self.setYHeadRot(yaw);
            self.yBodyRotO = playerLookYaw;
            self.setYBodyRot(playerLookYaw);
            this.heartbound$lockedYaw = playerLookYaw;
            this.heartbound$setWaitingForBed(false);
            this.heartbound$setAnimLocked(true);
            this.heartbound$setTransformScenePhase(ScenePhase.LAYING_DOWN);
         }
      }
   }

   @Unique
   private void heartbound$tickWaitingForContact() {
      Player self = (Player)(Object)this;
      if (self.level() instanceof ServerLevel serverWorld) {
         for (ServerPlayer other : serverWorld.players()) {
            if (other != self && !Heartbound.activeScenes.containsKey(other.getUUID())) {
               if (other instanceof TransformablePlayer) {
                  TransformablePlayer tp = (TransformablePlayer)other;
                  if (tp.heartbound$isTransformSceneActive()) {
                     continue;
                  }
               }

               if (self.distanceToSqr(other) <= 2.25) {
                  this.heartbound$startRidingScene(other);
                  return;
               }
            }
         }
      }
   }

   @Override
   public void heartbound$startTransformScene(Scene scene) {
      Player self = (Player)(Object)this;
      if (!this.heartbound$isTransformSceneActive()) {
         if (this.heartbound$isTransformed()) {
            if (!this.heartbound$isStripped() && scene.needsToStrip()) {
               self.sendOverlayMessage(Component.translatable("msg.heartbound.needsToStrip"));
            } else {
               this.heartbound$setTransformScene(scene);
               this.heartbound$setTransformSceneProgress(0.0F);
               this.heartbound$setTransformIntroIndex(0);
               this.heartbound$setTransformStationaryIndex(0);
               this.heartbound$setTransformStationaryLoop(0);
               this.heartbound$setTransformThrusting(false);
               this.heartbound$lastSceneAnim = "";
               SceneType type = scene.sceneType();
               if (type == SceneType.ON_BED) {
                  Utils.BlockInfo bedInfo = Utils.findNearbyBed(self.level(), self.blockPosition(), 15);
                  if (bedInfo == null) {
                     self.sendSystemMessage(Component.translatable("msg.heartbound.noBedFound"));
                     this.heartbound$setTransformScene(Scene.EMPTY);
                  } else {
                     this.heartbound$bedPos = bedInfo.pos();
                     Heartbound.usedBeds.put(self.getUUID(), bedInfo.pos());
                     this.heartbound$setWaitingForBed(true);
                  }
               } else if (type == SceneType.ON_PLAYER) {
                  this.heartbound$setWaitingForTarget(true);
                  this.heartbound$setAnimLocked(true);
               } else if (type == SceneType.STATIONARY_CONTACT) {
                  this.heartbound$setAnimLocked(true);
                  this.heartbound$setTransformScenePhase(ScenePhase.LAYING_DOWN);
               } else if (type == SceneType.STATIONARY_INTRO) {
                  this.heartbound$setAnimLocked(true);
                  this.heartbound$setTransformScenePhase(ScenePhase.STATIONARY_INTRO);
                  this.heartbound$setTransformStationaryLoopThreshold(scene.amountOfLoops());
               } else {
                  this.heartbound$setAnimLocked(true);
                  this.heartbound$setTransformScenePhase(ScenePhase.STATIONARY);
                  this.heartbound$setTransformStationaryLoopThreshold(scene.amountOfLoops());
               }
            }
         }
      }
   }

   @Unique
   private void heartbound$startRidingScene(Player otherPlayer) {
      Player self = (Player)(Object)this;
      otherPlayer.setInvisible(true);
      this.heartbound$setTransformSceneProgress(0.0F);
      this.heartbound$setTransformCumThreshold(this.heartbound$getTransformScene().cumThreshold());
      this.heartbound$setTransformThrusting(false);
      this.heartbound$setTransformIntroIndex(0);
      this.heartbound$lastSceneAnim = "";
      this.heartbound$setTransformScenePartner(otherPlayer);
      otherPlayer.startRiding(self);
      this.heartbound$setTransformScenePhase(ScenePhase.INTRO);
      this.heartbound$setAnimLocked(true);
      Heartbound.activeScenes.put(otherPlayer.getUUID(), self.getUUID());
   }

   @Override
   public void heartbound$stopTransformScene() {
      Player self = (Player)(Object)this;
      if (this.heartbound$isTransformSceneActive() || this.heartbound$isWaitingForBed() || this.heartbound$isWaitingForTarget()) {
         Heartbound.usedBeds.remove(self.getUUID());
         this.heartbound$bedPos = null;
         Optional<UUID> partnerUuid = this.heartbound$getTransformScenePartner();
         if (partnerUuid.isPresent()) {
            Heartbound.activeScenes.remove(partnerUuid.get());
            if (self.level() instanceof ServerLevel serverWorld) {
               Player partner = serverWorld.getPlayerByUUID(partnerUuid.get());
               if (partner != null) {
                  partner.setInvisible(false);
               }
            }
         }

         if (self.isVehicle()) {
            self.ejectPassengers();
         }

         this.heartbound$setTransformScene(Scene.EMPTY);
         this.heartbound$setTransformScenePhase(ScenePhase.NONE);
         this.heartbound$setTransformSceneProgress(0.0F);
         this.heartbound$setTransformCumThreshold(5.0F);
         this.heartbound$setTransformThrusting(false);
         this.heartbound$setTransformIntroIndex(0);
         this.heartbound$setTransformStationaryIndex(0);
         this.heartbound$setTransformStationaryLoop(0);
         this.heartbound$setTransformStationaryLoopThreshold(0);
         this.heartbound$setTransformScenePartner(null);
         this.heartbound$setWaitingForBed(false);
         this.heartbound$setWaitingForTarget(false);
         this.heartbound$setAnimLocked(false);
         this.heartbound$lockedYaw = Float.NaN;
         this.heartbound$lastSceneAnim = "";
      }
   }

   @Override
   public void heartbound$transformAnimationFinished() {
      Player self = (Player)(Object)this;
      if (!self.level().isClientSide()) {
         if (this.heartbound$isTransformSceneActive()) {
            Scene scene = this.heartbound$getTransformScene();
            switch (this.heartbound$getTransformScenePhase()) {
               case INTRO:
                  List<String> intros = scene.introAnim();
                  if (this.heartbound$getTransformIntroIndex() < intros.size() - 1) {
                     this.heartbound$setTransformIntroIndex(this.heartbound$getTransformIntroIndex() + 1);
                  } else {
                     this.heartbound$setTransformScenePhase(ScenePhase.HAVING_SEX);
                  }
               case HAVING_SEX:
               default:
                  break;
               case CUM:
                  this.heartbound$stopTransformScene();
                  break;
               case LAYING_DOWN:
                  this.heartbound$setTransformScenePhase(ScenePhase.BED_IDLE);
                  break;
               case STATIONARY_INTRO:
                  List<String> sequence = scene.stationaryIntroAnim();
                  if (this.heartbound$getTransformStationaryIndex() < sequence.size() - 1) {
                     this.heartbound$setTransformStationaryIndex(this.heartbound$getTransformStationaryIndex() + 1);
                  } else {
                     this.heartbound$setTransformScenePhase(ScenePhase.STATIONARY);
                  }
                  break;
               case STATIONARY:
                  int current = this.heartbound$getTransformStationaryLoop();
                  int needed = this.heartbound$getTransformStationaryLoopThreshold();
                  if (current < needed) {
                     this.heartbound$setTransformStationaryLoop(current + 1);
                  } else {
                     this.heartbound$stopTransformScene();
                  }
            }
         }
      }
   }

   @Override
   public void heartbound$handleTransformSceneThrust() {
      if (this.heartbound$getTransformScenePhase() == ScenePhase.HAVING_SEX) {
         float progress = this.heartbound$getTransformSceneProgress() + 0.1F;
         float threshold = this.heartbound$getTransformCumThreshold();
         this.heartbound$setTransformSceneProgress(Math.min(progress, threshold));
      }
   }

   @Override
   public void heartbound$tryTransformTriggerCum() {
      if (this.heartbound$isTransformSceneActive()
         && this.heartbound$getTransformSceneProgress() >= this.heartbound$getTransformCumThreshold()
         && this.heartbound$getTransformScenePhase() != ScenePhase.CUM) {
         this.heartbound$setTransformScenePhase(ScenePhase.CUM);
         Player self = (Player)(Object)this;
         if (!self.level().isClientSide()) {
            if (self instanceof ServerPlayer serverSelf) {
               ServerPlayNetworking.send(serverSelf, new PlayCumHudAnimationS2CPacket());
            }

            if (self.getFirstPassenger() instanceof ServerPlayer rider) {
               ServerPlayNetworking.send(rider, new PlayCumHudAnimationS2CPacket());
            }
         }
      }
   }

   @ModifyVariable(
      method = {"travel(Lnet/minecraft/world/phys/Vec3;)V"},
      at = @At("HEAD"),
      argsOnly = true
   )
   private Vec3 heartbound$freezeMovement(Vec3 movementInput) {
      Player self = (Player)(Object)this;
      if (this.heartbound$isAnimLocked()) {
         self.setDeltaMovement(0.0, self.getDeltaMovement().y, 0.0);
         return Vec3.ZERO;
      } else {
         return movementInput;
      }
   }

   @Inject(
      method = {"die(Lnet/minecraft/world/damagesource/DamageSource;)V"},
      at = {@At("TAIL")}
   )
   private void heartbound$onDeath(DamageSource source, CallbackInfo ci) {
      this.heartbound$setStripped(false);
      this.heartbound$setStripping(false);
      this.heartbound$stopTransformScene();
   }

   @Inject(
      method = {"addAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueOutput;)V"},
      at = {@At("TAIL")}
   )
   private void heartbound$writeNbt(ValueOutput nbt, CallbackInfo ci) {
      String girlId = this.heartbound$getTransformGirlId();
      if (girlId != null && !girlId.isEmpty()) {
         nbt.putString("heartbound_transformation", girlId);
         nbt.putBoolean("heartbound_stripped", this.heartbound$isStripped());
      }
   }

   @Inject(
      method = {"readAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueInput;)V"},
      at = {@At("TAIL")}
   )
   private void heartbound$readNbt(ValueInput nbt, CallbackInfo ci) {
      nbt.getString("heartbound_transformation").ifPresent(this::heartbound$setTransformGirlId);
      nbt.read("heartbound_stripped", Codec.BOOL).ifPresent(this::heartbound$setStripped);
   }
}
