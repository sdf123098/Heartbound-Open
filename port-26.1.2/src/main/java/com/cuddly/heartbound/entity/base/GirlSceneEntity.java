package com.cuddly.heartbound.entity.base;

import com.cuddly.heartbound.Heartbound;
import com.cuddly.heartbound.config.ModConfig;
import com.cuddly.heartbound.entity.ai.goal.BedGoal;
import com.cuddly.heartbound.entity.ai.goal.GirlSitGoal;
import com.cuddly.heartbound.entity.ai.goal.MoveToPlayerGoal;
import com.cuddly.heartbound.entity.ai.goal.StationaryContactGoal;
import com.cuddly.heartbound.entity.ai.goal.StopMovementGoal;
import com.cuddly.heartbound.entity.ai.goal.StripGoal;
import com.cuddly.heartbound.entity.base.tamable.TameableGirlEntity;
import com.cuddly.heartbound.networking.C2S.AnimationFinishC2SPacket;
import com.cuddly.heartbound.networking.C2S.AnimationSyncC2SPacket;
import com.cuddly.heartbound.networking.C2S.ScenePhaseSyncC2SPacket;
import com.cuddly.heartbound.networking.C2S.SoundEventSyncC2SPacket;
import com.cuddly.heartbound.networking.C2S.StopSceneOnServerC2SPacket;
import com.cuddly.heartbound.networking.S2C.ClothingArmorVisibilityS2CPacket;
import com.cuddly.heartbound.networking.S2C.PlayAttackAnimationS2CPacket;
import com.cuddly.heartbound.networking.S2C.PlayCumHudAnimationS2CPacket;
import com.cuddly.heartbound.registries.HeartboundTrackedDataRegistry;
import com.cuddly.heartbound.registries.SceneKeyframeEventRegistry;
import com.cuddly.heartbound.util.HeartboundLangUtils;
import com.cuddly.heartbound.util.HeartboundMessages;
import com.cuddly.heartbound.util.Utils;
import com.cuddly.heartbound.util.variables.Scene;
import com.cuddly.heartbound.util.variables.ScenePhase;
import com.cuddly.heartbound.util.variables.SceneType;
import com.mojang.authlib.GameProfile;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager.ControllerRegistrar;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.LoopType;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.animation.state.KeyFrameEvent;
import com.geckolib.cache.animation.keyframeevent.SoundKeyframeData;

public abstract class GirlSceneEntity extends GirlEntity implements GeoEntity {
   public final Queue<String> animationEventQueueClient = new LinkedList<>();
   public final Queue<String> animationEventQueueServer = new LinkedList<>();
   private static final EntityDataAccessor<Scene> CURRENT_SCENE = SynchedEntityData.defineId(GirlSceneEntity.class, HeartboundTrackedDataRegistry.SCENE);
   private static final EntityDataAccessor<ScenePhase> CURRENT_SCENE_PHASE = SynchedEntityData.defineId(GirlSceneEntity.class, HeartboundTrackedDataRegistry.SCENE_PHASE);
   private static final EntityDataAccessor<String> CURRENT_SEX_ANIM = SynchedEntityData.defineId(GirlSceneEntity.class, EntityDataSerializers.STRING);
   public static final EntityDataAccessor<Float> SCENE_PROGRESS = SynchedEntityData.defineId(GirlSceneEntity.class, EntityDataSerializers.FLOAT);
   public static final EntityDataAccessor<Float> CUM_THRESHOLD = SynchedEntityData.defineId(GirlSceneEntity.class, EntityDataSerializers.FLOAT);
   public static final EntityDataAccessor<Integer> STATIONARY_LOOP = SynchedEntityData.defineId(GirlSceneEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Integer> STATIONARY_LOOP_THRESHOLD = SynchedEntityData.defineId(GirlSceneEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Boolean> THRUSTING = SynchedEntityData.defineId(GirlSceneEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Integer> INTRO_INDEX = SynchedEntityData.defineId(GirlSceneEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> STATIONARY_INDEX = SynchedEntityData.defineId(GirlSceneEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Optional<UUID>> CURRENT_SCENE_PLAYER = SynchedEntityData.defineId(
      GirlSceneEntity.class, HeartboundTrackedDataRegistry.OPTIONAL_UUID
   );
   private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
   public BlockPos targetBedPos;
   public Scene stripOptions = Scene.EMPTY;
   private boolean requestStrip = false;
   private boolean stripCancelled = false;
   private boolean requestMoveToBed = false;
   private boolean requestMoveToPlayer;
   private boolean requestWaitForPlayer;
   private String lastSceneAnim = "";
   public String passengerBoneName = "boyCam";
   BlockPos bedPos;
   private static final float PROGRESS_SPEED = 0.1F;
   private boolean swinging = false;
   private long lastSwing = 0L;
   private String lastSoundKeyframe = "";
   private long lastSoundKeyframeTime = 0L;
   private final Set<String> sentMessageKeys = new HashSet<>();

   @Override
   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.cache;
   }

   protected GirlSceneEntity(EntityType<? extends GirlSceneEntity> entityType, Level world) {
      super(entityType, world);
   }

   @Override
   protected void defineSynchedData(Builder builder) {
      super.defineSynchedData(builder);
      builder.define(CURRENT_SCENE, Scene.EMPTY);
      builder.define(CURRENT_SCENE_PHASE, ScenePhase.NONE);
      builder.define(CURRENT_SEX_ANIM, "");
      builder.define(SCENE_PROGRESS, 0.0F);
      builder.define(CUM_THRESHOLD, 5.0F);
      builder.define(STATIONARY_LOOP, 0);
      builder.define(STATIONARY_LOOP_THRESHOLD, 0);
      builder.define(THRUSTING, false);
      builder.define(INTRO_INDEX, 0);
      builder.define(STATIONARY_INDEX, 0);
      builder.define(CURRENT_SCENE_PLAYER, Optional.empty());
   }

   public void setCurrentScene(Scene scene) {
      this.entityData.set(CURRENT_SCENE, scene);
   }

   public Scene getCurrentScene() {
      return this.entityData.get(CURRENT_SCENE);
   }

   public void setCurrentScenePhase(ScenePhase phase) {
      this.entityData.set(CURRENT_SCENE_PHASE, phase);
      this.sentMessageKeys.clear();
   }

   public ScenePhase getCurrentScenePhase() {
      return this.entityData.get(CURRENT_SCENE_PHASE);
   }

   public void setCurrentSexAnim(String anim) {
      this.entityData.set(CURRENT_SEX_ANIM, anim);
   }

   public String getCurrentSexAnim() {
      return this.entityData.get(CURRENT_SEX_ANIM);
   }

   public Queue<String> getAnimationKeyFrameEvent() {
      return this.level().isClientSide() ? this.animationEventQueueClient : this.animationEventQueueServer;
   }

   public void setThrusting(boolean thrust) {
      this.entityData.set(THRUSTING, thrust);
   }

   public boolean isThrusting() {
      return this.entityData.get(THRUSTING);
   }

   public void setIntroIndex(int num) {
      this.entityData.set(INTRO_INDEX, num);
   }

   public int getIntroIndex() {
      return this.entityData.get(INTRO_INDEX);
   }

   public void setStationaryIndex(int num) {
      this.entityData.set(STATIONARY_INDEX, num);
   }

   public int getStationaryIndex() {
      return this.entityData.get(STATIONARY_INDEX);
   }

   public void setSceneProgress(float progress) {
      this.entityData.set(SCENE_PROGRESS, progress);
   }

   public float getSceneProgress() {
      return this.entityData.get(SCENE_PROGRESS);
   }

   public void setStationaryLoop(int progress) {
      this.entityData.set(STATIONARY_LOOP, progress);
   }

   public int getStationaryLoop() {
      return this.entityData.get(STATIONARY_LOOP);
   }

   public void setStationaryLoopThreshold(int progress) {
      this.entityData.set(STATIONARY_LOOP_THRESHOLD, progress);
   }

   public int getStationaryLoopThreshold() {
      return this.entityData.get(STATIONARY_LOOP_THRESHOLD);
   }

   public void setCumThreshold(float threshold) {
      this.entityData.set(CUM_THRESHOLD, threshold);
   }

   public float getCumThreshold() {
      return this.entityData.get(CUM_THRESHOLD);
   }

   public void setBoneVisibility(List<String> bones, boolean visible) {
      if (this.level().isClientSide()) {
         if (this.boneVisibility == null) {
            this.boneVisibility = new HashMap<>();
         }

         for (String boneName : bones) {
            this.boneVisibility.put(boneName, visible);
         }
      }
   }

   public void setBoneVisibility(String bone, boolean visible) {
      this.setBoneVisibility(List.of(bone), visible);
   }

   @Nullable
   public Player getScenePlayer() {
      return (Player)(this.level().isClientSide() ? this.getScenePlayerClient() : this.getScenePlayerServer());
   }

   @Nullable
   public ServerPlayer getScenePlayerServer() {
      Optional<UUID> opt = this.getEntityData().get(CURRENT_SCENE_PLAYER);
      if (opt.isEmpty()) {
         return null;
      } else {
         return this.level() instanceof ServerLevel serverWorld ? serverWorld.getServer().getPlayerList().getPlayer(opt.get()) : null;
      }
   }

   @Environment(EnvType.CLIENT)
   @Nullable
   public Player getScenePlayerClient() {
      Optional<UUID> opt = this.getEntityData().get(CURRENT_SCENE_PLAYER);
      if (opt.isEmpty()) {
         return null;
      } else if (this.level() instanceof ClientLevel clientWorld) {
         UUID var4 = opt.get();
         return clientWorld.players().stream().filter(p -> p.getUUID().equals(var4)).findFirst().orElse(null);
      } else {
         return null;
      }
   }

   public void setScenePlayer(@Nullable Player player) {
      if (player == null) {
         this.getEntityData().set(CURRENT_SCENE_PLAYER, Optional.empty());
      } else {
         this.getEntityData().set(CURRENT_SCENE_PLAYER, Optional.of(player.getUUID()));
      }
   }

   public void overrideBoneTexture(String boneName, Identifier texture) {
      this.overrideBoneTexture(List.of(boneName), texture);
   }

   public void overrideBoneTexture(List<String> bones, Identifier texture) {
      if (this.boneTextureOverrides == null) {
         this.boneTextureOverrides = new HashMap<>();
      }

      for (String boneName : bones) {
         this.boneTextureOverrides.put(boneName, texture);
      }
   }

   public void overrideBoneTextureLayer2(String bones, Identifier texture) {
      if (this.boneTextureOverridesLayer2 == null) {
         this.boneTextureOverridesLayer2 = new HashMap<>();
      }

      this.boneTextureOverridesLayer2.put(bones, texture);
   }

   public void overrideBoneTextureLayer3(String bones, Identifier texture) {
      if (this.boneTextureOverridesLayer3 == null) {
         this.boneTextureOverridesLayer3 = new HashMap<>();
      }

      this.boneTextureOverridesLayer3.put(bones, texture);
   }

   public void overrideBoneUV(List<String> bones, float uOffset, float vOffset) {
      if (this.boneUVOffsets == null) {
         this.boneUVOffsets = new HashMap<>();
      }

      for (String boneName : bones) {
         this.boneUVOffsets.put(boneName, new Vec2(uOffset, vOffset));
      }
   }

   public void overrideBoneColor(List<String> bones, Integer hex) {
      if (this.boneColorOverrides == null) {
         this.boneColorOverrides = new HashMap<>();
      }

      for (String bone : bones) {
         this.boneColorOverrides.put(bone, Utils.withFullAlpha(hex));
      }
   }

   public void setBonePos(String bone, float x, float y, float z) {
      this.setBonePos(bone, new Vec3((double)x, (double)y, (double)z));
   }

   public void setBonePos(String bone, Vec3 pos) {
      if (this.bonePositionOffset == null) {
         this.bonePositionOffset = new HashMap<>();
      }

      this.bonePositionOffset.put(bone, pos);
   }

   public void setBoneSize(String bone, float x, float y, float z, float min, float max) {
      if (this.boneSizeOverrides == null) {
         this.boneSizeOverrides = new HashMap<>();
      }

      if (min != 0.0F && max != 0.0F) {
         x = Math.clamp(x, min, max);
         y = Math.clamp(y, min, max);
         z = Math.clamp(z, min, max);
      }

      this.boneSizeOverrides.put(bone, new Vec3((double)x, (double)y, (double)z));
   }

   public void setBoneSize(String bone, int size, int min, int max) {
      float finalSize = (float)size / 100.0F;
      if (min == 0 && max == 0) {
         this.setBoneSize(bone, finalSize, finalSize, finalSize, 0.0F, 0.0F);
      } else {
         float finalMin = (float)min / 100.0F;
         float finalMax = (float)max / 100.0F;
         this.setBoneSize(bone, finalSize, finalSize, finalSize, finalMin, finalMax);
      }
   }

   public void setBoneSize(String bone, int size) {
      this.setBoneSize(bone, size, 0, 0);
   }

   public void setBoneSize(String bone, int x, int y, int z, int min, int max) {
      float finalX = (float)x / 100.0F;
      float finalY = (float)y / 100.0F;
      float finalZ = (float)z / 100.0F;
      float finalMin = (float)min / 100.0F;
      float finalMax = (float)max / 100.0F;
      this.setBoneSize(bone, finalX, finalY, finalZ, finalMin, finalMax);
   }

   public void startScene(Player player, Scene option) {
      if (!this.isSceneActive()) {
         this.setScenePlayer(player);
         if (this.getScenePlayer() != null) {
            if (this.isSitting()) {
               this.setSitting(false);
            }

            this.setCurrentScene(option);
            if (!this.isStripped() && option.needsToStrip()) {
               this.requestStrip(option);
            } else {
               if (this.useUpRelationShipLevels()) {
                  this.setCurrentRelationshipLevel(this.getCurrentRelationshipLevel() - option.requiredRelationshipLevel());
               }

               if (option.sceneType().equals(SceneType.ON_BED)) {
                  Utils.BlockInfo bedInfo = Utils.findNearbyBed(this.level(), this.blockPosition(), 15);
                  if (bedInfo == null) {
                     this.messageAsEntity(false, HeartboundLangUtils.getStringFromKey("msg.heartbound.noBedFound"));
                  } else {
                     Heartbound.usedBeds.put(this.getUUID(), bedInfo.pos());
                     this.targetBedPos = bedInfo.pos();
                     this.bedPos = bedInfo.pos();
                     this.requestMoveToBed();
                  }
               } else if (option.sceneType().equals(SceneType.ON_PLAYER)) {
                  this.requestMoveToPlayer();
               } else if (option.sceneType().equals(SceneType.STATIONARY_CONTACT)) {
                  this.requestWaitForPlayer();
               } else if (option.sceneType().equals(SceneType.STATIONARY_INTRO)) {
                  this.startStationaryIntro(option);
               } else {
                  this.startStationaryLoop(option);
               }
            }
         }
      }
   }

   public void startRidingScene(Player player) {
      SceneType type = this.getCurrentScene().sceneType();
      if (!type.equals(SceneType.STATIONARY_INTRO) && !type.equals(SceneType.STATIONARY)) {
         player.setInvisible(true);
         this.getScenePlayer().sendOverlayMessage(Component.nullToEmpty("msg.heartbound.canGoInToFreeCam"));
         this.setSceneProgress(0.0F);
         this.setCumThreshold(this.getCurrentScene().cumThreshold());
         this.setThrusting(false);
         this.targetBedPos = null;
         this.getScenePlayer().startRiding(this, false, false);
         this.setIntroIndex(0);
         this.lastSceneAnim = "";
         this.playPhase(ScenePhase.INTRO);
         this.setSceneState(true);
      }
   }

   private void startStationaryIntro(Scene option) {
      this.setSceneState(true);
      this.setSceneProgress(0.0F);
      this.setCurrentScenePhase(ScenePhase.STATIONARY_INTRO);
      this.lastSceneAnim = "";
      this.setStationaryIndex(0);
      this.setStationaryLoop(0);
      this.setStationaryLoopThreshold(option.amountOfLoops());
   }

   private void startStationaryLoop(Scene option) {
      this.setSceneState(true);
      this.setSceneProgress(0.0F);
      this.setCurrentScenePhase(ScenePhase.STATIONARY);
      this.lastSceneAnim = "";
      this.setStationaryLoop(0);
      this.setStationaryLoopThreshold(option.amountOfLoops());
   }

   public void stopScene() {
      if (this.isSceneActive()) {
         this.lastSceneAnim = "";
         if (this.level().isClientSide()) {
            ClientPlayNetworking.send(new StopSceneOnServerC2SPacket(this.getId()));
         } else {
            Heartbound.usedBeds.remove(this.getUUID());
            if (this.getScenePlayer() != null) {
               Heartbound.activeScenes.remove(this.getScenePlayer().getUUID());
            }

            this.setIntroIndex(0);
            this.setStationaryIndex(0);
            this.setSceneProgress(0.0F);
            this.setPassengerBonePosition(Vec3.ZERO);
            this.setHavingSex(false);
            this.setSceneState(false);
            this.onSceneStop();
            this.setCurrentScenePhase(ScenePhase.NONE);
            this.getNavigation().stop();
            if (this.getScenePlayer() != null) {
               this.setScenePlayer(null);
            }
         }
      }
   }

   private void onSceneStop() {
      if (this.isVehicle()) {
         this.ejectPassengers();
      }

      if (this.getScenePlayer() != null) {
         this.getScenePlayer().setInvisible(false);
      }
   }

   public void playPhase(ScenePhase phase) {
      if (this.level().isClientSide()) {
         ClientPlayNetworking.send(new ScenePhaseSyncC2SPacket(this.getId(), phase));
      } else {
         this.setCurrentScenePhase(phase);
         this.lastSceneAnim = "";
         if (phase != ScenePhase.INTRO) {
            this.setIntroIndex(0);
         }
      }
   }

   public void tryTriggerCum() {
      if (this.isSceneActive() && this.getSceneProgress() >= this.getCumThreshold() && this.getCurrentScenePhase() != ScenePhase.CUM) {
         this.playPhase(ScenePhase.CUM);
         if (!this.level().isClientSide() && this.getFirstPassenger() instanceof ServerPlayer rider) {
            ServerPlayNetworking.send(rider, new PlayCumHudAnimationS2CPacket());
         }
      }
   }

   private PlayState setSceneAnimIfChanged(AnimationTest<?> state, String anim, LoopType loop) {
      if (anim == null || anim.isEmpty()) {
         return null;
      } else if (!anim.equals(this.lastSceneAnim)) {
         state.controller().reset();
         this.lastSceneAnim = anim;
         return state.setAndContinue(RawAnimation.begin().then(this.getAnimationPath(anim), loop));
      } else {
         return PlayState.CONTINUE;
      }
   }

   private void onSceneActive() {
      this.applySkinToBone(this.getScenePlayer());
      if (!this.level().isClientSide()) {
         if (this.bedPos != null && this.isBedScene() && !Utils.checkForBlockAt(this.level(), this.bedPos, null, BlockTags.BEDS)) {
            this.stopScene();
         }
      }
   }

   public void modelLogic() {
      if (this.level().isClientSide()) {
         this.modelLogicClient();
      }
   }

   @Environment(EnvType.CLIENT)
   private void modelLogicClient() {
      boolean isActivePhase = switch (this.getCurrentScenePhase()) {
         case NONE, BED_IDLE, LAYING_DOWN -> false;
         default -> true;
      };
      if (!this.getCurrentScene().hidePlayer()) {
         this.overrideBoneColor(List.of("nut"), ModConfig.INSTANCE.player.penisHeadColor);
         this.overrideBoneColor(List.of("shaft", "ballL", "ballR"), ModConfig.INSTANCE.player.penisShaftColor);
         this.setBoneVisibility(List.of("RightLeg", "LeftLeg", "Torso2"), isActivePhase);
         List<String> slim = List.of("rightArmAlex", "rightLowerArmAlex", "leftLowerArmAlex", "leftArmAlex");
         List<String> wide = List.of("rightArmSteve", "rightLowerArmSteve", "leftLowerArmSteve", "leftArmSteve");
         this.setBoneVisibility(slim, this.isPlayerModelSlim() && isActivePhase);
         this.setBoneVisibility(wide, !this.isPlayerModelSlim() && isActivePhase);
      }

      this.setBoneVisibility(List.of("nose"), true);
      this.setBoneSize("belly", 100);
   }

   @Environment(EnvType.CLIENT)
   public void handleAnimationEventClient(String key) {
      if (this.level().isClientSide()) {
         key = key.toLowerCase();
         long currentTime = System.currentTimeMillis();
         if (!key.equals(this.lastSoundKeyframe) || currentTime - this.lastSoundKeyframeTime >= 200L) {
            this.lastSoundKeyframe = key;
            this.lastSoundKeyframeTime = currentTime;
            this.animationEventQueueClient.add(key);
            if (!this.sentMessageKeys.contains(key)) {
               List<String> girlMsgs = SceneKeyframeEventRegistry.getMessage(this.getGirlID(), key);
               List<String> playerMsgs = SceneKeyframeEventRegistry.getPlayerMessage(key);
               if (!girlMsgs.isEmpty() || !playerMsgs.isEmpty()) {
                  this.sentMessageKeys.add(key);
               }

               for (String msg : girlMsgs) {
                  this.messageAsEntityTranslatable(msg);
               }

               for (String msg : playerMsgs) {
                  this.messageAsPlayerTranslatable(msg);
               }
            }

            for (SoundEvent sound : SceneKeyframeEventRegistry.getSound(this.getGirlID(), key)) {
               Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, 1.0F, 1.0F));
            }
         }
      }
   }

   public void handleAnimationEventServer(String key) {
      if (!this.level().isClientSide()) {
         this.animationEventQueueServer.add(key.toLowerCase());
         this.handleSceneSpeed(key);
         this.handleSceneFootstepSounds(key);
      }
   }

   private void handleSceneFootstepSounds(String key) {
      BlockPos posBelow = this.blockPosition().below();
      BlockState state = this.level().getBlockState(posBelow);
      SoundType soundGroup = state.getSoundType();
      SoundEvent stepSound = soundGroup.getStepSound();
      if (key.equals("paizuri_startStep".toLowerCase())) {
         this.playSound(stepSound, 1.0F, 1.0F);
      }
   }

   private void handleSceneSpeed(String key) {
      if (this.getCurrentScenePhase().equals(ScenePhase.HAVING_SEX)) {
         if (key.contains("thrust")) {
            this.setSceneProgress(this.getSceneProgress() + 0.1F);
         }

         this.setSceneProgress(Math.clamp(this.getSceneProgress(), 0.0F, this.getCumThreshold()));
      }
   }

   @Override
   public void tick() {
      super.tick();
      this.updateClothingAndArmor();
      this.modelLogic();
      if (!this.level().isClientSide()) {
         this.setSceneState(this.getCurrentScenePhase() != ScenePhase.NONE);

         boolean InSexPhases = switch (this.getCurrentScenePhase()) {
            case BED_IDLE, LAYING_DOWN, DIALOG -> false;
            default -> true;
         };
         this.setHavingSex(this.isSceneActive() && InSexPhases);
      }

      if (this.isSceneActive()) {
         this.onSceneActive();
      }
      boolean isStopPhase = switch (this.getCurrentScenePhase()) {
         case BED_IDLE, LAYING_DOWN, DIALOG, STATIONARY, STATIONARY_INTRO -> false;
         default -> true;
      };
      if (!this.isVehicle() && this.isSceneActive() && isStopPhase) {
         this.stopScene();
      }

      if (!this.level().isClientSide()) {
         this.animationEventQueueServer.clear();
      } else {
         this.animationEventQueueClient.clear();
      }
   }

   public void triggerSwing() {
      this.swinging = true;
      this.lastSwing = this.level().getGameTime();
   }

   @Override
   public void registerControllers(ControllerRegistrar controllerRegistrar) {
      controllerRegistrar.add(new AnimationController<>("girl_animations", 4, this::handleAnimations).setSoundKeyframeHandler(this::handleSoundKeyframe));
      controllerRegistrar.add(new AnimationController<>("girl_attack", 4, this::handleAttackAnimations));
      controllerRegistrar.add(new AnimationController<>("girl_face", 4, this::handleFacialAnimations));
   }

   @Environment(EnvType.CLIENT)
   private void handleSoundKeyframe(KeyFrameEvent<GirlSceneEntity, SoundKeyframeData> event) {
      if (this.level().isClientSide()) {
         String key = event.keyframeData().getSound().toLowerCase();
         this.getAnimationKeyFrameEvent().add(key);
         this.handleAnimationEventClient(key);
         ClientPlayNetworking.send(new SoundEventSyncC2SPacket(this.getId(), key));
      }
   }

   private PlayState handleFacialAnimations(AnimationTest<GirlSceneEntity> state) {
      return state.setAndContinue(RawAnimation.begin().then(this.getAnimationPath("blink"), LoopType.LOOP));
   }

   private PlayState handleAttackAnimations(AnimationTest<GirlSceneEntity> state) {
      AnimationController<?> controller = state.controller();
      if (this.swinging && this.lastSwing + 7L <= this.level().getGameTime()) {
         this.swinging = false;
      }

      if (this.swinging && controller.getPlayState() == PlayState.STOP) {
         controller.reset();
         return state.setAndContinue(RawAnimation.begin().then(this.getAnimationPath("attack" + RANDOM.nextInt(3)), LoopType.PLAY_ONCE));
      } else {
         return controller.getPlayState() == PlayState.STOP ? PlayState.STOP : PlayState.CONTINUE;
      }
   }

   @Override
   public boolean doHurtTarget(ServerLevel serverLevel, Entity target) {
      boolean hit = super.doHurtTarget(serverLevel, target);
      if (hit && this.level() instanceof ServerLevel serverWorld) {
         for (ServerPlayer player : serverWorld.players()) {
            ServerPlayNetworking.send(player, new PlayAttackAnimationS2CPacket(this.getId()));
         }
      }

      return hit;
   }

   private PlayState handleAnimations(AnimationTest<GirlSceneEntity> state) {
      if (this.isSceneActive() && this.getOverrideAnim().isEmpty()) {
         AnimationController<?> controller = state.controller();
         Scene options = this.getCurrentScene();
         if ((controller.hasAnimationFinished() || controller.getPlayState() == PlayState.PAUSE) && !this.lastSceneAnim.isEmpty()) {
            ClientPlayNetworking.send(new AnimationFinishC2SPacket(this.getId()));
            this.lastSceneAnim = "";
         }

         switch (this.getCurrentScenePhase()) {
            case BED_IDLE:
               String bedIdle = options.bedIdle().isEmpty() ? "null" : options.bedIdle();
               return this.setSceneAnimIfChanged(state, bedIdle, LoopType.LOOP);
            case LAYING_DOWN:
               String laying = options.bedIdle().isEmpty() ? "null" : options.layOnBed();
               return this.setSceneAnimIfChanged(state, laying, LoopType.HOLD_ON_LAST_FRAME);
            case DIALOG:
            default:
               return PlayState.STOP;
            case STATIONARY:
               String loopAnim = options.stationaryLoopAnim();
               int loopsNeeded = this.getStationaryLoopThreshold();
               if (loopAnim != null && !loopAnim.isEmpty()) {
                  if (this.getStationaryLoop() >= loopsNeeded) {
                     this.stopScene();
                     return PlayState.STOP;
                  }

                  return this.setSceneAnimIfChanged(state, loopAnim, LoopType.HOLD_ON_LAST_FRAME);
               }

               this.stopScene();
               return PlayState.STOP;
            case STATIONARY_INTRO:
               List<String> sequence = options.stationaryIntroAnim();
               if (sequence.isEmpty()) {
                  this.playPhase(ScenePhase.STATIONARY);
                  return PlayState.CONTINUE;
               }

               String current = sequence.get(Math.min(this.getStationaryIndex(), sequence.size() - 1));
               return this.setSceneAnimIfChanged(state, current, LoopType.HOLD_ON_LAST_FRAME);
            case INTRO:
               List<String> intros = options.introAnim();
               if (intros.isEmpty()) {
                  this.playPhase(ScenePhase.HAVING_SEX);
                  return PlayState.CONTINUE;
               }

               String currentIntro = intros.get(Math.min(this.getIntroIndex(), intros.size() - 1));
               return this.setSceneAnimIfChanged(state, currentIntro, LoopType.HOLD_ON_LAST_FRAME);
            case HAVING_SEX:
               boolean thrustKeyDown = this.isThrusting();
               if (this.getCurrentSexAnim().isBlank()) {
                  this.setCurrentSexAnim(this.getRandomFromList(options.slowAnim()));
               }

               if (options.useKeyFrameEvents()) {
                  Queue<String> key = this.getAnimationKeyFrameEvent();
                  if (Utils.isStringInQueue(key, "switch") && thrustKeyDown) {
                     this.setCurrentSexAnim(this.getRandomFromList(options.fastAnim()));
                  }

                  if (Utils.isStringInQueue(key, "reset") && thrustKeyDown) {
                     return state.setAndContinue(RawAnimation.begin().then(this.getAnimationPath(this.getRandomFromList(options.fastAnim())), LoopType.LOOP));
                  }

                  if (Utils.isStringInQueue(key, "reset") && !thrustKeyDown) {
                     this.setCurrentSexAnim(this.getRandomFromList(options.slowAnim()));
                  }

                  return this.setSceneAnimIfChanged(state, this.getCurrentSexAnim(), LoopType.LOOP);
               }

               List<String> anims = thrustKeyDown ? options.fastAnim() : options.slowAnim();
               return state.setAndContinue(RawAnimation.begin().then(this.getAnimationPath(anims.getFirst()), LoopType.LOOP));
            case CUM:
               this.setCurrentSexAnim("");
               return this.setSceneAnimIfChanged(state, options.cumAnim(), LoopType.HOLD_ON_LAST_FRAME);
         }
      } else {
         if (!this.lastSceneAnim.isEmpty()) {
            this.lastSceneAnim = "";
         }

         AnimationController<?> controller = state.controller();
         String overrideAnim = this.getOverrideAnim();
         boolean overrideLoop = this.getOverrideLoopState();
         boolean overrideHold = this.getOverrideHoldState();
         if (overrideAnim != null && !overrideAnim.isEmpty()) {
            this.currentAnimState = overrideAnim;
            this.currentLoopState = overrideLoop;
            this.currentHoldState = overrideHold;
            if (!overrideLoop && (controller.getPlayState() == PlayState.STOP || controller.getPlayState() == PlayState.PAUSE)) {
               ClientPlayNetworking.send(new AnimationSyncC2SPacket(this.getId(), "", false, false));
            }
         } else {
            this.currentAnimState = !this.isTemporary() ? this.getDefaultAnimation(state) : "idle";
            this.currentLoopState = true;
         }

         LoopType loopType;
         if (this.currentLoopState) {
            loopType = LoopType.LOOP;
         } else if (this.currentHoldState) {
            loopType = LoopType.HOLD_ON_LAST_FRAME;
         } else {
            loopType = LoopType.PLAY_ONCE;
         }

         return state.setAndContinue(RawAnimation.begin().then(this.getAnimationPath(this.currentAnimState), loopType));
      }
   }

   public void animationFinished() {
      if (!this.level().isClientSide()) {
         if (this.isSceneActive()) {
            switch (this.getCurrentScenePhase()) {
               case LAYING_DOWN:
                  this.playPhase(ScenePhase.BED_IDLE);
               case DIALOG:
               case HAVING_SEX:
               default:
                  break;
               case STATIONARY:
                  int current = this.getStationaryLoop();
                  int needed = this.getStationaryLoopThreshold();
                  if (current < needed) {
                     this.setStationaryLoop(current + 1);
                  } else {
                     this.stopScene();
                  }
                  break;
               case STATIONARY_INTRO:
                  Scene options = this.getCurrentScene();
                  List<String> sequence = options.stationaryIntroAnim();
                  if (this.getStationaryIndex() < sequence.size() - 1) {
                     this.setStationaryIndex(this.getStationaryIndex() + 1);
                  } else {
                     this.playPhase(ScenePhase.STATIONARY);
                  }
                  break;
               case INTRO:
                  List<String> intros = this.getCurrentScene().introAnim();
                  if (this.getIntroIndex() < intros.size() - 1) {
                     this.setIntroIndex(this.getIntroIndex() + 1);
                  } else {
                     this.playPhase(ScenePhase.HAVING_SEX);
                  }
                  break;
               case CUM:
                  this.stopScene();
            }
         }
      }
   }

   private String getRandomFromList(List<String> list) {
      return list.size() == 1 ? list.getFirst() : list.get(RANDOM.nextInt(list.size()));
   }

   private boolean isEntityMoving() {
      Vec3 velocity = this.getDeltaMovement();
      double horizontalSpeedSq = velocity.x * velocity.x + velocity.z * velocity.z;
      return horizontalSpeedSq > 1.0E-4;
   }

   private String getDefaultAnimation(AnimationTest<?> state) {
      String base;
      if (this.isSitting()) {
         base = "sit";
      } else if (this.isPassenger()) {
         base = "ride";
      } else if (this.isAerialEntity()) {
         if (this.isEntityMoving() && this.isSprinting()) {
            base = "fly_fast";
         } else if (this.isEntityMoving()) {
            base = "fly";
         } else {
            base = "idle";
         }
      } else if (!this.onGround()) {
         base = "fly";
      } else if (this.isEntityMoving() && !this.isSprinting()) {
         base = this.isWalkingBackward() && this.hasBackwardsWalkAnim() ? "backwards_walk" : "walk";
      } else if (this.isEntityMoving() && this.isSprinting()) {
         base = "run";
      } else {
         base = "idle";
      }

      return this.mapDefaultAnimation(base);
   }

   protected String mapDefaultAnimation(String animation) {
      return animation;
   }

   public void playAnimation(String animationName, boolean loop, boolean holdOnLastFrame) {
      if (!this.level().isClientSide()) {
         this.setOverrideAnim(animationName != null ? animationName : "");
         this.setOverrideLoop(loop);
         this.setOverrideHold(holdOnLastFrame);
      } else {
         ClientPlayNetworking.send(new AnimationSyncC2SPacket(this.getId(), animationName != null ? animationName : "", loop, holdOnLastFrame));
      }
   }

   private String getAnimationPath(String animation) {
      return "animation." + this.getGirlID() + "." + animation;
   }

   private boolean isGirlArmorSlot(EquipmentSlot slot) {
      return slot == EquipmentSlot.HEAD || slot == EquipmentSlot.CHEST || slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET;
   }

   public void applySkinToBone(Player player) {
      if (this.level().isClientSide()) {
         try {
            Class<?> helper = Class.forName("com.cuddly.heartbound.client.GirlSceneClientHelper");
            Method m = helper.getMethod("applySkinToBone", GirlSceneEntity.class, Player.class);
            m.invoke(null, this, player);
         } catch (Throwable var4) {
         }
      }
   }

   private void updateClothingAndArmor() {
      if (!this.level().isClientSide()) {
         boolean stripped = this.isStripped();

         for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (this.isGirlArmorSlot(slot)) {
               boolean hasArmor = !this.inventory.getEquipmentStack(slot).isEmpty();
               this.armorVisibility.put(slot, hasArmor & !stripped);
            }
         }

         List<Boolean> armorList = Arrays.stream(EquipmentSlot.values()).map(s -> this.armorVisibility.getOrDefault(s, false)).toList();
         ClothingArmorVisibilityS2CPacket packet = new ClothingArmorVisibilityS2CPacket(this.getId(), armorList);

         for (ServerPlayer player : Objects.requireNonNull(this.level().getServer()).getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(player, packet);
         }
      }
   }

   public void applyClothingAndArmor() {
      if (this.level().isClientSide()) {
         for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (this.isGirlArmorSlot(slot)) {
               List<String> armorBones = this.getArmorBones().get(slot);
               if (armorBones != null) {
                  this.setBoneVisibility(armorBones, this.armorVisibility.getOrDefault(slot, false));
                  if (slot == EquipmentSlot.LEGS) {
                     boolean legsCovered = this.armorVisibility.getOrDefault(slot, false);
                     this.setBoneVisibility("vagina", !legsCovered);
                  }
               }

               this.displayArmor(slot);
            }
         }
      }
   }

   private void displayArmor(EquipmentSlot slot) {
      if (!this.inventory.getEquipmentStack(slot).isEmpty()) {
         float u = 0.0F;
         float offset = 0.017578125F;
         ItemStack item = this.inventory.getEquipmentStack(slot);
         String armorType = item.toString().toLowerCase();
         if (armorType.contains("diamond")) {
            u = offset;
         }

         if (armorType.contains("gold")) {
            u = offset * 2.0F;
         }

         if (armorType.contains("iron")) {
            u = offset * 3.0F;
         }

         if (armorType.contains("copper")) {
            u = offset * 4.0F;
         }

         if (armorType.contains("chain")) {
            u = offset * 5.0F;
         }

         if (armorType.contains("leather")) {
            u = offset * 6.0F;
            this.overrideBoneColor(this.getArmorBones().get(slot), this.getDyedArmorColor(this.inventory.getEquipmentStack(slot)));
         }

         if (armorType.contains("turtle")) {
            u = offset * 7.0F;
         }

         this.overrideBoneUV(this.getArmorBones().get(slot), u, 0.0F);
      }
   }

   private int getDyedArmorColor(ItemStack stack) {
      if (stack.isEmpty()) {
         return 16777215;
      } else {
         DyedItemColor dyed = stack.get(DataComponents.DYED_COLOR);
         return dyed != null ? dyed.rgb() : 10511680;
      }
   }

   public float getBedOffset() {
      return this.getCurrentScene().bedAlignmentOffset();
   }

   public boolean isBedScene() {
      return this.getCurrentScene().sceneType().equals(SceneType.ON_BED);
   }

   public void messageAsEntity(String message) {
      this.messageAsEntity(false, message);
   }

   public void messageAsEntity(boolean sendFromServer, String message) {
      String finalMessage = "<" + this.getGirlDisplayName() + "> " + message;
      if (this.getScenePlayer() != null) {
         HeartboundMessages.PlayerSpecificMessage(this.getScenePlayer(), finalMessage);
      } else if (this instanceof TameableGirlEntity tameable && tameable.getOwner() instanceof Player owner) {
         HeartboundMessages.PlayerSpecificMessage(owner, finalMessage);
      }
   }

   public void messageAsEntityTranslatable(String translationKey) {
      Component message = Component.literal("<" + this.getGirlDisplayName() + "> ").append(Component.translatable(translationKey));
      if (this.getScenePlayer() != null) {
         HeartboundMessages.PlayerSpecificMessage(this.getScenePlayer(), message);
      } else if (this instanceof TameableGirlEntity tameable && tameable.getOwner() instanceof Player owner) {
         HeartboundMessages.PlayerSpecificMessage(owner, message);
      }
   }

   public void messageAsPlayer(String message) {
      if (this.getScenePlayer() != null) {
         GameProfile profile = this.getScenePlayer().getGameProfile();
         String finalMessage = "<" + profile.name() + "> " + message;
         HeartboundMessages.PlayerSpecificMessage(this.getScenePlayer(), finalMessage);
      }
   }

   public void messageAsPlayerTranslatable(String translationKey) {
      if (this.getScenePlayer() != null) {
         GameProfile profile = this.getScenePlayer().getGameProfile();
         Component message = Component.literal("<" + profile.name() + "> ").append(Component.translatable(translationKey));
         HeartboundMessages.PlayerSpecificMessage(this.getScenePlayer(), message);
      }
   }

   public void requestMoveToBed() {
      this.requestMoveToBed = true;
   }

   public boolean shouldMoveToBed() {
      if (this.requestMoveToBed) {
         this.requestMoveToBed = false;
         return true;
      } else {
         return false;
      }
   }

   public void requestMoveToPlayer() {
      this.requestMoveToPlayer = true;
   }

   public boolean shouldMoveToPlayer() {
      if (this.requestMoveToPlayer) {
         this.requestMoveToPlayer = false;
         return true;
      } else {
         return false;
      }
   }

   public void requestWaitForPlayer() {
      this.requestWaitForPlayer = true;
   }

   public boolean shouldWaitForPlayer() {
      if (this.requestWaitForPlayer) {
         this.requestWaitForPlayer = false;
         return true;
      } else {
         return false;
      }
   }

   public void requestStrip() {
      this.requestStrip(null);
   }

   public void requestStrip(@Nullable Scene options) {
      this.requestStrip = true;
      if (options != null) {
         this.stripOptions = options;
      }
   }

   public boolean shouldStrip() {
      if (this.requestStrip) {
         this.requestStrip = false;
         return true;
      } else {
         return false;
      }
   }

   public void resetAnimationState() {
      this.requestStrip = false;
      this.stripCancelled = true;
      this.stripOptions = Scene.EMPTY;
      this.swinging = false;
      this.setOverrideAnim("");
      this.setFreeze(false);
   }

   public boolean isStripCancelled() {
      return this.stripCancelled;
   }

   public void clearStripCancelled() {
      this.stripCancelled = false;
   }

   @Override
   public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount) {
      return this.isTemporary() ? false : super.hurtServer(serverLevel, source, amount);
   }

   @Override
   protected void registerGoals() {
      super.registerGoals();
      this.goalSelector.addGoal(-4, new StationaryContactGoal(this));
      this.goalSelector.addGoal(-3, new MoveToPlayerGoal(this, 1.25));
      this.goalSelector.addGoal(-2, new BedGoal(this, 1.25));
      this.goalSelector.addGoal(-1, new StripGoal(this));
      this.goalSelector.addGoal(0, new StopMovementGoal(this));
      this.goalSelector.addGoal(1, new GirlSitGoal(this));
   }

   public boolean isCurrentScenePlayer(Player player) {
      return this.getScenePlayer() == null ? false : this.getScenePlayer().getUUID().equals(player.getUUID());
   }

   @Override
   public void addAdditionalSaveData(ValueOutput nbt) {
      super.addAdditionalSaveData(nbt);
   }

   @Override
   public void readAdditionalSaveData(ValueInput nbt) {
      super.readAdditionalSaveData(nbt);
   }
}
