package com.cuddly.heartbound.mixins.transformation;

import com.cuddly.heartbound.Heartbound;
import com.cuddly.heartbound.networking.S2C.PlayCumHudAnimationS2CPacket;
import com.cuddly.heartbound.registries.HeartboundTrackedDataRegistry;
import com.cuddly.heartbound.transformation.GirlTransformationInfo;
import com.cuddly.heartbound.transformation.TransformablePlayer;
import com.cuddly.heartbound.util.Utils;
import com.cuddly.heartbound.util.variables.Scene;
import com.cuddly.heartbound.util.variables.ScenePhase;
import com.cuddly.heartbound.util.variables.SceneType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.data.DataTracker.Builder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({PlayerEntity.class})
public abstract class PlayerTransformationMixin implements TransformablePlayer {
   @Unique
   private static final TrackedData<String> HEARTBOUND_TRANSFORM_GIRL_ID = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.STRING);
   @Unique
   private static final TrackedData<Boolean> HEARTBOUND_TRANSFORM_STRIPPED = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   @Unique
   private static final TrackedData<Boolean> HEARTBOUND_TRANSFORM_STRIPPING = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   @Unique
   private static final TrackedData<Boolean> HEARTBOUND_ANIM_LOCKED = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   @Unique
   private static final TrackedData<Scene> HEARTBOUND_TRANSFORM_SCENE = DataTracker.registerData(PlayerEntity.class, HeartboundTrackedDataRegistry.SCENE);
   @Unique
   private static final TrackedData<ScenePhase> HEARTBOUND_TRANSFORM_SCENE_PHASE = DataTracker.registerData(
      PlayerEntity.class, HeartboundTrackedDataRegistry.SCENE_PHASE
   );
   @Unique
   private static final TrackedData<Float> HEARTBOUND_TRANSFORM_SCENE_PROGRESS = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.FLOAT);
   @Unique
   private static final TrackedData<Float> HEARTBOUND_TRANSFORM_CUM_THRESHOLD = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.FLOAT);
   @Unique
   private static final TrackedData<Boolean> HEARTBOUND_TRANSFORM_THRUSTING = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   @Unique
   private static final TrackedData<Integer> HEARTBOUND_TRANSFORM_INTRO_INDEX = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.INTEGER);
   @Unique
   private static final TrackedData<Integer> HEARTBOUND_TRANSFORM_STATIONARY_INDEX = DataTracker.registerData(
      PlayerEntity.class, TrackedDataHandlerRegistry.INTEGER
   );
   @Unique
   private static final TrackedData<Integer> HEARTBOUND_TRANSFORM_STATIONARY_LOOP = DataTracker.registerData(
      PlayerEntity.class, TrackedDataHandlerRegistry.INTEGER
   );
   @Unique
   private static final TrackedData<Integer> HEARTBOUND_TRANSFORM_STATIONARY_LOOP_THRESHOLD = DataTracker.registerData(
      PlayerEntity.class, TrackedDataHandlerRegistry.INTEGER
   );
   @Unique
   private static final TrackedData<Optional<UUID>> HEARTBOUND_TRANSFORM_SCENE_PARTNER = DataTracker.registerData(
      PlayerEntity.class, HeartboundTrackedDataRegistry.OPTIONAL_UUID
   );
   @Unique
   private static final TrackedData<Boolean> HEARTBOUND_WAITING_FOR_BED = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   @Unique
   private static final TrackedData<Boolean> HEARTBOUND_WAITING_FOR_TARGET = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
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
   private Vec3d heartbound$passengerBonePos = Vec3d.ZERO;
   @Unique
   private float heartbound$lockedYaw = Float.NaN;
   @Unique
   private String heartbound$lastGirlId = "";

   @Inject(
      method = {"initDataTracker(Lnet/minecraft/entity/data/DataTracker$Builder;)V"},
      at = {@At("TAIL")}
   )
   private void heartbound$initDataTracker(Builder builder, CallbackInfo ci) {
      builder.add(HEARTBOUND_TRANSFORM_GIRL_ID, "");
      builder.add(HEARTBOUND_TRANSFORM_STRIPPED, false);
      builder.add(HEARTBOUND_TRANSFORM_STRIPPING, false);
      builder.add(HEARTBOUND_ANIM_LOCKED, false);
      builder.add(HEARTBOUND_TRANSFORM_SCENE, Scene.EMPTY);
      builder.add(HEARTBOUND_TRANSFORM_SCENE_PHASE, ScenePhase.NONE);
      builder.add(HEARTBOUND_TRANSFORM_SCENE_PROGRESS, 0.0F);
      builder.add(HEARTBOUND_TRANSFORM_CUM_THRESHOLD, 5.0F);
      builder.add(HEARTBOUND_TRANSFORM_THRUSTING, false);
      builder.add(HEARTBOUND_TRANSFORM_INTRO_INDEX, 0);
      builder.add(HEARTBOUND_TRANSFORM_STATIONARY_INDEX, 0);
      builder.add(HEARTBOUND_TRANSFORM_STATIONARY_LOOP, 0);
      builder.add(HEARTBOUND_TRANSFORM_STATIONARY_LOOP_THRESHOLD, 0);
      builder.add(HEARTBOUND_TRANSFORM_SCENE_PARTNER, Optional.empty());
      builder.add(HEARTBOUND_WAITING_FOR_BED, false);
      builder.add(HEARTBOUND_WAITING_FOR_TARGET, false);
   }

   @Override
   public String heartbound$getTransformGirlId() {
      return ((PlayerEntity)(Object)this).getDataTracker().get(HEARTBOUND_TRANSFORM_GIRL_ID);
   }

   @Override
   public void heartbound$setTransformGirlId(String girlId) {
      ((PlayerEntity)(Object)this).getDataTracker().set(HEARTBOUND_TRANSFORM_GIRL_ID, girlId != null ? girlId : "");
      this.heartbound$setStripped(false);
      ((PlayerEntity)(Object)this).calculateDimensions();
   }

   @Override
   public boolean heartbound$isStripped() {
      return ((PlayerEntity)(Object)this).getDataTracker().get(HEARTBOUND_TRANSFORM_STRIPPED);
   }

   @Override
   public void heartbound$setStripped(boolean stripped) {
      ((PlayerEntity)(Object)this).getDataTracker().set(HEARTBOUND_TRANSFORM_STRIPPED, stripped);
   }

   @Override
   public boolean heartbound$isStripping() {
      return ((PlayerEntity)(Object)this).getDataTracker().get(HEARTBOUND_TRANSFORM_STRIPPING);
   }

   @Override
   public void heartbound$setStripping(boolean stripping) {
      ((PlayerEntity)(Object)this).getDataTracker().set(HEARTBOUND_TRANSFORM_STRIPPING, stripping);
   }

   @Override
   public boolean heartbound$isAnimLocked() {
      return ((PlayerEntity)(Object)this).getDataTracker().get(HEARTBOUND_ANIM_LOCKED);
   }

   @Override
   public void heartbound$setAnimLocked(boolean locked) {
      ((PlayerEntity)(Object)this).getDataTracker().set(HEARTBOUND_ANIM_LOCKED, locked);
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
      return ((PlayerEntity)(Object)this).getDataTracker().get(HEARTBOUND_TRANSFORM_SCENE);
   }

   @Override
   public void heartbound$setTransformScene(Scene scene) {
      ((PlayerEntity)(Object)this).getDataTracker().set(HEARTBOUND_TRANSFORM_SCENE, scene);
   }

   @Override
   public ScenePhase heartbound$getTransformScenePhase() {
      return ((PlayerEntity)(Object)this).getDataTracker().get(HEARTBOUND_TRANSFORM_SCENE_PHASE);
   }

   @Override
   public void heartbound$setTransformScenePhase(ScenePhase phase) {
      ((PlayerEntity)(Object)this).getDataTracker().set(HEARTBOUND_TRANSFORM_SCENE_PHASE, phase);
   }

   @Override
   public float heartbound$getTransformSceneProgress() {
      return ((PlayerEntity)(Object)this).getDataTracker().get(HEARTBOUND_TRANSFORM_SCENE_PROGRESS);
   }

   @Override
   public void heartbound$setTransformSceneProgress(float progress) {
      ((PlayerEntity)(Object)this).getDataTracker().set(HEARTBOUND_TRANSFORM_SCENE_PROGRESS, progress);
   }

   @Override
   public float heartbound$getTransformCumThreshold() {
      return ((PlayerEntity)(Object)this).getDataTracker().get(HEARTBOUND_TRANSFORM_CUM_THRESHOLD);
   }

   @Override
   public void heartbound$setTransformCumThreshold(float threshold) {
      ((PlayerEntity)(Object)this).getDataTracker().set(HEARTBOUND_TRANSFORM_CUM_THRESHOLD, threshold);
   }

   @Override
   public boolean heartbound$isTransformThrusting() {
      return ((PlayerEntity)(Object)this).getDataTracker().get(HEARTBOUND_TRANSFORM_THRUSTING);
   }

   @Override
   public void heartbound$setTransformThrusting(boolean thrusting) {
      ((PlayerEntity)(Object)this).getDataTracker().set(HEARTBOUND_TRANSFORM_THRUSTING, thrusting);
   }

   @Override
   public int heartbound$getTransformIntroIndex() {
      return ((PlayerEntity)(Object)this).getDataTracker().get(HEARTBOUND_TRANSFORM_INTRO_INDEX);
   }

   @Override
   public void heartbound$setTransformIntroIndex(int index) {
      ((PlayerEntity)(Object)this).getDataTracker().set(HEARTBOUND_TRANSFORM_INTRO_INDEX, index);
   }

   @Override
   public int heartbound$getTransformStationaryIndex() {
      return ((PlayerEntity)(Object)this).getDataTracker().get(HEARTBOUND_TRANSFORM_STATIONARY_INDEX);
   }

   @Override
   public void heartbound$setTransformStationaryIndex(int index) {
      ((PlayerEntity)(Object)this).getDataTracker().set(HEARTBOUND_TRANSFORM_STATIONARY_INDEX, index);
   }

   @Override
   public int heartbound$getTransformStationaryLoop() {
      return ((PlayerEntity)(Object)this).getDataTracker().get(HEARTBOUND_TRANSFORM_STATIONARY_LOOP);
   }

   @Override
   public void heartbound$setTransformStationaryLoop(int loop) {
      ((PlayerEntity)(Object)this).getDataTracker().set(HEARTBOUND_TRANSFORM_STATIONARY_LOOP, loop);
   }

   @Override
   public int heartbound$getTransformStationaryLoopThreshold() {
      return ((PlayerEntity)(Object)this).getDataTracker().get(HEARTBOUND_TRANSFORM_STATIONARY_LOOP_THRESHOLD);
   }

   @Override
   public void heartbound$setTransformStationaryLoopThreshold(int threshold) {
      ((PlayerEntity)(Object)this).getDataTracker().set(HEARTBOUND_TRANSFORM_STATIONARY_LOOP_THRESHOLD, threshold);
   }

   @Override
   public Optional<UUID> heartbound$getTransformScenePartner() {
      return ((PlayerEntity)(Object)this).getDataTracker().get(HEARTBOUND_TRANSFORM_SCENE_PARTNER);
   }

   @Override
   public void heartbound$setTransformScenePartner(@Nullable PlayerEntity player) {
      if (player == null) {
         ((PlayerEntity)(Object)this).getDataTracker().set(HEARTBOUND_TRANSFORM_SCENE_PARTNER, Optional.empty());
      } else {
         ((PlayerEntity)(Object)this).getDataTracker().set(HEARTBOUND_TRANSFORM_SCENE_PARTNER, Optional.of(player.getUuid()));
      }
   }

   @Override
   public boolean heartbound$isWaitingForBed() {
      return ((PlayerEntity)(Object)this).getDataTracker().get(HEARTBOUND_WAITING_FOR_BED);
   }

   @Override
   public void heartbound$setWaitingForBed(boolean waiting) {
      ((PlayerEntity)(Object)this).getDataTracker().set(HEARTBOUND_WAITING_FOR_BED, waiting);
   }

   @Override
   public boolean heartbound$isWaitingForTarget() {
      return ((PlayerEntity)(Object)this).getDataTracker().get(HEARTBOUND_WAITING_FOR_TARGET);
   }

   @Override
   public void heartbound$setWaitingForTarget(boolean waiting) {
      ((PlayerEntity)(Object)this).getDataTracker().set(HEARTBOUND_WAITING_FOR_TARGET, waiting);
   }

   @Override
   public Vec3d heartbound$getPassengerBonePosition() {
      return this.heartbound$passengerBonePos;
   }

   @Override
   public void heartbound$setPassengerBonePosition(Vec3d position) {
      this.heartbound$passengerBonePos = position;
   }

   @Inject(
      method = {"tick()V"},
      at = {@At("TAIL")}
   )
   private void heartbound$tickAnimLock(CallbackInfo ci) {
      PlayerEntity self = (PlayerEntity)(Object)this;
      String currentGirlId = this.heartbound$getTransformGirlId();
      if (!currentGirlId.equals(this.heartbound$lastGirlId)) {
         this.heartbound$lastGirlId = currentGirlId;
         self.calculateDimensions();
      }

      if (this.heartbound$isTransformSceneActive()) {
         if (Float.isNaN(this.heartbound$lockedYaw)) {
            this.heartbound$lockedYaw = self.getYaw();
         }

         self.prevBodyYaw = this.heartbound$lockedYaw;
         self.setBodyYaw(this.heartbound$lockedYaw);
         self.setSprinting(false);
      } else {
         this.heartbound$lockedYaw = Float.NaN;
      }

      if (!self.getWorld().isClient) {
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
      PlayerEntity self = (PlayerEntity)(Object)this;
      if (this.heartbound$isWaitingForBed()) {
         this.heartbound$tickWaitingForBed();
      } else if (this.heartbound$isWaitingForTarget()) {
         this.heartbound$tickWaitingForTarget();
      } else {
         if (!Float.isNaN(this.heartbound$lockedYaw)) {
            self.prevBodyYaw = this.heartbound$lockedYaw;
            self.setBodyYaw(this.heartbound$lockedYaw);
         }

         ScenePhase phase = this.heartbound$getTransformScenePhase();
         if (phase != ScenePhase.NONE) {
            Scene scene = this.heartbound$getTransformScene();
            SceneType type = scene.sceneType();
            if (this.heartbound$bedPos != null
               && type == SceneType.ON_BED
               && !Utils.checkForBlockAt(self.getWorld(), this.heartbound$bedPos, null, BlockTags.BEDS)) {
               this.heartbound$stopTransformScene();
            } else {
               if (phase == ScenePhase.BED_IDLE && !self.hasPassengers()) {
                  this.heartbound$tickWaitingForContact();
               }
               boolean isActivePhase = switch (phase) {
                  case INTRO, HAVING_SEX, CUM -> true;
                  default -> false;
               };
               if (isActivePhase && !self.hasPassengers()) {
                  this.heartbound$stopTransformScene();
               }
            }
         }
      }
   }

   @Unique
   private Vec3d heartbound$calculateBedSnapPos(BlockPos bedPos, Direction facing) {
      float offset = this.heartbound$getTransformScene().bedAlignmentOffset();

      return switch (facing) {
         case NORTH -> new Vec3d((double)bedPos.getX() + 0.5, (double)bedPos.getY(), (double)bedPos.getZ() + 1.5 - (double)offset);
         case EAST -> new Vec3d((double)bedPos.getX() - 0.5 + (double)offset, (double)bedPos.getY(), (double)bedPos.getZ() + 0.5);
         case SOUTH -> new Vec3d((double)bedPos.getX() + 0.5, (double)bedPos.getY(), (double)bedPos.getZ() - 0.5 + (double)offset);
         case WEST -> new Vec3d((double)bedPos.getX() + 1.5 - (double)offset, (double)bedPos.getY(), (double)bedPos.getZ() + 0.5);
         default -> bedPos.toCenterPos();
      };
   }

   @Unique
   private void heartbound$tickWaitingForTarget() {
      PlayerEntity self = (PlayerEntity)(Object)this;
      if (self.getWorld() instanceof ServerWorld serverWorld) {
         for (ServerPlayerEntity other : serverWorld.getPlayers()) {
            if (other != self && !Heartbound.activeScenes.containsKey(other.getUuid())) {
               if (other instanceof TransformablePlayer) {
                  TransformablePlayer tp = (TransformablePlayer)other;
                  if (tp.heartbound$isTransformSceneActive()) {
                     continue;
                  }
               }

               if (self.squaredDistanceTo(other) <= 6.25) {
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
      PlayerEntity self = (PlayerEntity)(Object)this;
      if (this.heartbound$bedPos == null) {
         this.heartbound$stopTransformScene();
      } else if (!Utils.checkForBlockAt(self.getWorld(), this.heartbound$bedPos, null, BlockTags.BEDS)) {
         this.heartbound$stopTransformScene();
      } else {
         double distSq = self.getBlockPos().getSquaredDistance(this.heartbound$bedPos);
         if (distSq <= 2.25) {
            BlockState state = self.getWorld().getBlockState(this.heartbound$bedPos);
            Direction bedFacing = Direction.NORTH;
            if (state.contains(Properties.HORIZONTAL_FACING)) {
               bedFacing = state.get(Properties.HORIZONTAL_FACING);
            }

            Vec3d snapPos = this.heartbound$calculateBedSnapPos(this.heartbound$bedPos, bedFacing);
            float playerLookYaw = self.getYaw();
            float yaw = bedFacing.asRotation();
            self.refreshPositionAndAngles(snapPos, yaw, self.getPitch());
            self.setHeadYaw(yaw);
            self.prevBodyYaw = playerLookYaw;
            self.setBodyYaw(playerLookYaw);
            this.heartbound$lockedYaw = playerLookYaw;
            this.heartbound$setWaitingForBed(false);
            this.heartbound$setAnimLocked(true);
            this.heartbound$setTransformScenePhase(ScenePhase.LAYING_DOWN);
         }
      }
   }

   @Unique
   private void heartbound$tickWaitingForContact() {
      PlayerEntity self = (PlayerEntity)(Object)this;
      if (self.getWorld() instanceof ServerWorld serverWorld) {
         for (ServerPlayerEntity other : serverWorld.getPlayers()) {
            if (other != self && !Heartbound.activeScenes.containsKey(other.getUuid())) {
               if (other instanceof TransformablePlayer) {
                  TransformablePlayer tp = (TransformablePlayer)other;
                  if (tp.heartbound$isTransformSceneActive()) {
                     continue;
                  }
               }

               if (self.squaredDistanceTo(other) <= 2.25) {
                  this.heartbound$startRidingScene(other);
                  return;
               }
            }
         }
      }
   }

   @Override
   public void heartbound$startTransformScene(Scene scene) {
      PlayerEntity self = (PlayerEntity)(Object)this;
      if (!this.heartbound$isTransformSceneActive()) {
         if (this.heartbound$isTransformed()) {
            if (!this.heartbound$isStripped() && scene.needsToStrip()) {
               self.sendMessage(Text.translatable("msg.heartbound.needsToStrip"), true);
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
                  Utils.BlockInfo bedInfo = Utils.findNearbyBed(self.getWorld(), self.getBlockPos(), 15);
                  if (bedInfo == null) {
                     self.sendMessage(Text.translatable("msg.heartbound.noBedFound"), false);
                     this.heartbound$setTransformScene(Scene.EMPTY);
                  } else {
                     this.heartbound$bedPos = bedInfo.pos();
                     Heartbound.usedBeds.put(self.getUuid(), bedInfo.pos());
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
   private void heartbound$startRidingScene(PlayerEntity otherPlayer) {
      PlayerEntity self = (PlayerEntity)(Object)this;
      otherPlayer.setInvisible(true);
      this.heartbound$setTransformSceneProgress(0.0F);
      this.heartbound$setTransformCumThreshold(this.heartbound$getTransformScene().cumThreshold());
      this.heartbound$setTransformThrusting(false);
      this.heartbound$setTransformIntroIndex(0);
      this.heartbound$lastSceneAnim = "";
      this.heartbound$setTransformScenePartner(otherPlayer);
      otherPlayer.startRiding(self, true);
      this.heartbound$setTransformScenePhase(ScenePhase.INTRO);
      this.heartbound$setAnimLocked(true);
      Heartbound.activeScenes.put(otherPlayer.getUuid(), self.getUuid());
   }

   @Override
   public void heartbound$stopTransformScene() {
      PlayerEntity self = (PlayerEntity)(Object)this;
      if (this.heartbound$isTransformSceneActive() || this.heartbound$isWaitingForBed() || this.heartbound$isWaitingForTarget()) {
         Heartbound.usedBeds.remove(self.getUuid());
         this.heartbound$bedPos = null;
         Optional<UUID> partnerUuid = this.heartbound$getTransformScenePartner();
         if (partnerUuid.isPresent()) {
            Heartbound.activeScenes.remove(partnerUuid.get());
            if (self.getWorld() instanceof ServerWorld serverWorld) {
               PlayerEntity partner = serverWorld.getPlayerByUuid(partnerUuid.get());
               if (partner != null) {
                  partner.setInvisible(false);
               }
            }
         }

         if (self.hasPassengers()) {
            self.removeAllPassengers();
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
      PlayerEntity self = (PlayerEntity)(Object)this;
      if (!self.getWorld().isClient()) {
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
         PlayerEntity self = (PlayerEntity)(Object)this;
         if (!self.getWorld().isClient()) {
            if (self instanceof ServerPlayerEntity serverSelf) {
               ServerPlayNetworking.send(serverSelf, new PlayCumHudAnimationS2CPacket());
            }

            if (self.getFirstPassenger() instanceof ServerPlayerEntity rider) {
               ServerPlayNetworking.send(rider, new PlayCumHudAnimationS2CPacket());
            }
         }
      }
   }

   @ModifyVariable(
      method = {"travel(Lnet/minecraft/util/math/Vec3d;)V"},
      at = @At("HEAD"),
      argsOnly = true
   )
   private Vec3d heartbound$freezeMovement(Vec3d movementInput) {
      PlayerEntity self = (PlayerEntity)(Object)this;
      if (this.heartbound$isAnimLocked()) {
         self.setVelocity(0.0, self.getVelocity().y, 0.0);
         return Vec3d.ZERO;
      } else {
         return movementInput;
      }
   }

   @Inject(
      method = {"jump()V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void heartbound$blockJump(CallbackInfo ci) {
      if (this.heartbound$isTransformSceneActive()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"onDeath(Lnet/minecraft/entity/damage/DamageSource;)V"},
      at = {@At("TAIL")}
   )
   private void heartbound$onDeath(DamageSource source, CallbackInfo ci) {
      this.heartbound$setStripped(false);
      this.heartbound$setStripping(false);
      this.heartbound$stopTransformScene();
   }

   @Inject(
      method = {"writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V"},
      at = {@At("TAIL")}
   )
   private void heartbound$writeNbt(NbtCompound nbt, CallbackInfo ci) {
      String girlId = this.heartbound$getTransformGirlId();
      if (girlId != null && !girlId.isEmpty()) {
         nbt.putString("heartbound_transformation", girlId);
         nbt.putBoolean("heartbound_stripped", this.heartbound$isStripped());
      }
   }

   @Inject(
      method = {"readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V"},
      at = {@At("TAIL")}
   )
   private void heartbound$readNbt(NbtCompound nbt, CallbackInfo ci) {
      if (nbt.contains("heartbound_transformation")) {
         this.heartbound$setTransformGirlId(nbt.getString("heartbound_transformation"));
      }

      if (nbt.contains("heartbound_stripped")) {
         this.heartbound$setStripped(nbt.getBoolean("heartbound_stripped"));
      }
   }

   @Inject(
      method = {"getBaseDimensions(Lnet/minecraft/entity/EntityPose;)Lnet/minecraft/entity/EntityDimensions;"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void heartbound$getBaseDimensions(EntityPose pose, CallbackInfoReturnable<EntityDimensions> cir) {
      String girlId = this.heartbound$getTransformGirlId();
      if (girlId != null && !girlId.isEmpty()) {
         GirlTransformationInfo info = GirlTransformationInfo.REGISTRY.get(girlId);
         if (info != null) {
            cir.setReturnValue(EntityDimensions.changing(info.width(), info.height()).withEyeHeight(info.eyeHeight()));
            return;
         }
      }

      EntityDimensions original = (EntityDimensions)cir.getReturnValue();
      if (original.width() > 0.5F) {
         cir.setReturnValue(EntityDimensions.changing(0.5F, original.height()).withEyeHeight(original.eyeHeight()));
      }
   }
}
