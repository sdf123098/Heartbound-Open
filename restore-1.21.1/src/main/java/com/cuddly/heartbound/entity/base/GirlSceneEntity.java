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
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.data.DataTracker.Builder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.animation.Animation.LoopType;
import software.bernie.geckolib.animation.AnimationController.State;
import software.bernie.geckolib.animation.keyframe.event.SoundKeyframeEvent;
import software.bernie.geckolib.animation.keyframe.event.data.SoundKeyframeData;

public abstract class GirlSceneEntity extends GirlEntity implements GeoEntity {
   public final Queue<String> animationEventQueueClient = new LinkedList<>();
   public final Queue<String> animationEventQueueServer = new LinkedList<>();
   private static final TrackedData<Scene> CURRENT_SCENE = DataTracker.registerData(GirlSceneEntity.class, HeartboundTrackedDataRegistry.SCENE);
   private static final TrackedData<ScenePhase> CURRENT_SCENE_PHASE = DataTracker.registerData(GirlSceneEntity.class, HeartboundTrackedDataRegistry.SCENE_PHASE);
   private static final TrackedData<String> CURRENT_SEX_ANIM = DataTracker.registerData(GirlSceneEntity.class, TrackedDataHandlerRegistry.STRING);
   public static final TrackedData<Float> SCENE_PROGRESS = DataTracker.registerData(GirlSceneEntity.class, TrackedDataHandlerRegistry.FLOAT);
   public static final TrackedData<Float> CUM_THRESHOLD = DataTracker.registerData(GirlSceneEntity.class, TrackedDataHandlerRegistry.FLOAT);
   public static final TrackedData<Integer> STATIONARY_LOOP = DataTracker.registerData(GirlSceneEntity.class, TrackedDataHandlerRegistry.INTEGER);
   public static final TrackedData<Integer> STATIONARY_LOOP_THRESHOLD = DataTracker.registerData(GirlSceneEntity.class, TrackedDataHandlerRegistry.INTEGER);
   private static final TrackedData<Boolean> THRUSTING = DataTracker.registerData(GirlSceneEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   private static final TrackedData<Integer> INTRO_INDEX = DataTracker.registerData(GirlSceneEntity.class, TrackedDataHandlerRegistry.INTEGER);
   private static final TrackedData<Integer> STATIONARY_INDEX = DataTracker.registerData(GirlSceneEntity.class, TrackedDataHandlerRegistry.INTEGER);
   public static final TrackedData<Optional<UUID>> CURRENT_SCENE_PLAYER = DataTracker.registerData(
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

   protected GirlSceneEntity(EntityType<? extends GirlSceneEntity> entityType, World world) {
      super(entityType, world);
   }

   @Override
   protected void initDataTracker(Builder builder) {
      super.initDataTracker(builder);
      builder.add(CURRENT_SCENE, Scene.EMPTY);
      builder.add(CURRENT_SCENE_PHASE, ScenePhase.NONE);
      builder.add(CURRENT_SEX_ANIM, "");
      builder.add(SCENE_PROGRESS, 0.0F);
      builder.add(CUM_THRESHOLD, 5.0F);
      builder.add(STATIONARY_LOOP, 0);
      builder.add(STATIONARY_LOOP_THRESHOLD, 0);
      builder.add(THRUSTING, false);
      builder.add(INTRO_INDEX, 0);
      builder.add(STATIONARY_INDEX, 0);
      builder.add(CURRENT_SCENE_PLAYER, Optional.empty());
   }

   public void setCurrentScene(Scene scene) {
      this.dataTracker.set(CURRENT_SCENE, scene);
   }

   public Scene getCurrentScene() {
      return this.dataTracker.get(CURRENT_SCENE);
   }

   public void setCurrentScenePhase(ScenePhase phase) {
      this.dataTracker.set(CURRENT_SCENE_PHASE, phase);
      this.sentMessageKeys.clear();
   }

   public ScenePhase getCurrentScenePhase() {
      return this.dataTracker.get(CURRENT_SCENE_PHASE);
   }

   public void setCurrentSexAnim(String anim) {
      this.dataTracker.set(CURRENT_SEX_ANIM, anim);
   }

   public String getCurrentSexAnim() {
      return this.dataTracker.get(CURRENT_SEX_ANIM);
   }

   public Queue<String> getAnimationKeyFrameEvent() {
      return this.getWorld().isClient() ? this.animationEventQueueClient : this.animationEventQueueServer;
   }

   public void setThrusting(boolean thrust) {
      this.dataTracker.set(THRUSTING, thrust);
   }

   public boolean isThrusting() {
      return this.dataTracker.get(THRUSTING);
   }

   public void setIntroIndex(int num) {
      this.dataTracker.set(INTRO_INDEX, num);
   }

   public int getIntroIndex() {
      return this.dataTracker.get(INTRO_INDEX);
   }

   public void setStationaryIndex(int num) {
      this.dataTracker.set(STATIONARY_INDEX, num);
   }

   public int getStationaryIndex() {
      return this.dataTracker.get(STATIONARY_INDEX);
   }

   public void setSceneProgress(float progress) {
      this.dataTracker.set(SCENE_PROGRESS, progress);
   }

   public float getSceneProgress() {
      return this.dataTracker.get(SCENE_PROGRESS);
   }

   public void setStationaryLoop(int progress) {
      this.dataTracker.set(STATIONARY_LOOP, progress);
   }

   public int getStationaryLoop() {
      return this.dataTracker.get(STATIONARY_LOOP);
   }

   public void setStationaryLoopThreshold(int progress) {
      this.dataTracker.set(STATIONARY_LOOP_THRESHOLD, progress);
   }

   public int getStationaryLoopThreshold() {
      return this.dataTracker.get(STATIONARY_LOOP_THRESHOLD);
   }

   public void setCumThreshold(float threshold) {
      this.dataTracker.set(CUM_THRESHOLD, threshold);
   }

   public float getCumThreshold() {
      return this.dataTracker.get(CUM_THRESHOLD);
   }

   public void setBoneVisibility(List<String> bones, boolean visible) {
      if (this.getWorld().isClient) {
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
   public PlayerEntity getScenePlayer() {
      return (PlayerEntity)(this.getWorld().isClient() ? this.getScenePlayerClient() : this.getScenePlayerServer());
   }

   @Nullable
   public ServerPlayerEntity getScenePlayerServer() {
      Optional<UUID> opt = this.getDataTracker().get(CURRENT_SCENE_PLAYER);
      if (opt.isEmpty()) {
         return null;
      } else {
         return this.getWorld() instanceof ServerWorld serverWorld ? serverWorld.getServer().getPlayerManager().getPlayer(opt.get()) : null;
      }
   }

   @Environment(EnvType.CLIENT)
   @Nullable
   public PlayerEntity getScenePlayerClient() {
      Optional<UUID> opt = this.getDataTracker().get(CURRENT_SCENE_PLAYER);
      if (opt.isEmpty()) {
         return null;
      } else if (this.getWorld() instanceof ClientWorld clientWorld) {
         UUID var4 = opt.get();
         return clientWorld.getPlayers().stream().filter(p -> p.getUuid().equals(var4)).findFirst().orElse(null);
      } else {
         return null;
      }
   }

   public void setScenePlayer(@Nullable PlayerEntity player) {
      if (player == null) {
         this.getDataTracker().set(CURRENT_SCENE_PLAYER, Optional.empty());
      } else {
         this.getDataTracker().set(CURRENT_SCENE_PLAYER, Optional.of(player.getUuid()));
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
         this.boneUVOffsets.put(boneName, new Vec2f(uOffset, vOffset));
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
      this.setBonePos(bone, new Vec3d((double)x, (double)y, (double)z));
   }

   public void setBonePos(String bone, Vec3d pos) {
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

      this.boneSizeOverrides.put(bone, new Vec3d((double)x, (double)y, (double)z));
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

   public void startScene(PlayerEntity player, Scene option) {
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
                  Utils.BlockInfo bedInfo = Utils.findNearbyBed(this.getWorld(), this.getBlockPos(), 15);
                  if (bedInfo == null) {
                     this.messageAsEntity(false, HeartboundLangUtils.getStringFromKey("msg.heartbound.noBedFound"));
                  } else {
                     Heartbound.usedBeds.put(this.getUuid(), bedInfo.pos());
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

   public void startRidingScene(PlayerEntity player) {
      SceneType type = this.getCurrentScene().sceneType();
      if (!type.equals(SceneType.STATIONARY_INTRO) && !type.equals(SceneType.STATIONARY)) {
         player.setInvisible(true);
         this.getScenePlayer().sendMessage(Text.of("msg.heartbound.canGoInToFreeCam"), true);
         this.setSceneProgress(0.0F);
         this.setCumThreshold(this.getCurrentScene().cumThreshold());
         this.setThrusting(false);
         this.targetBedPos = null;
         this.getScenePlayer().startRiding(this, false);
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
         if (this.getWorld().isClient()) {
            ClientPlayNetworking.send(new StopSceneOnServerC2SPacket(this.getId()));
         } else {
            Heartbound.usedBeds.remove(this.getUuid());
            if (this.getScenePlayer() != null) {
               Heartbound.activeScenes.remove(this.getScenePlayer().getUuid());
            }

            this.setIntroIndex(0);
            this.setStationaryIndex(0);
            this.setSceneProgress(0.0F);
            this.setPassengerBonePosition(Vec3d.ZERO);
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
      if (this.hasPassengers()) {
         this.removeAllPassengers();
      }

      if (this.getScenePlayer() != null) {
         this.getScenePlayer().setInvisible(false);
      }
   }

   public void playPhase(ScenePhase phase) {
      if (this.getWorld().isClient()) {
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
         if (!this.getWorld().isClient() && this.getFirstPassenger() instanceof ServerPlayerEntity rider) {
            ServerPlayNetworking.send(rider, new PlayCumHudAnimationS2CPacket());
         }
      }
   }

   private PlayState setSceneAnimIfChanged(AnimationState<?> state, String anim, LoopType loop) {
      if (anim == null || anim.isEmpty()) {
         return null;
      } else if (!anim.equals(this.lastSceneAnim)) {
         state.resetCurrentAnimation();
         this.lastSceneAnim = anim;
         return state.setAndContinue(RawAnimation.begin().then(this.getAnimationPath(anim), loop));
      } else {
         return PlayState.CONTINUE;
      }
   }

   private void onSceneActive() {
      this.applySkinToBone(this.getScenePlayer());
      if (!this.getWorld().isClient()) {
         if (this.bedPos != null && this.isBedScene() && !Utils.checkForBlockAt(this.getWorld(), this.bedPos, null, BlockTags.BEDS)) {
            this.stopScene();
         }
      }
   }

   public void modelLogic() {
      if (this.getWorld().isClient()) {
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
      if (this.getWorld().isClient()) {
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
               MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(sound, 1.0F, 1.0F));
            }
         }
      }
   }

   public void handleAnimationEventServer(String key) {
      if (!this.getWorld().isClient()) {
         this.animationEventQueueServer.add(key.toLowerCase());
         this.handleSceneSpeed(key);
         this.handleSceneFootstepSounds(key);
      }
   }

   private void handleSceneFootstepSounds(String key) {
      BlockPos posBelow = this.getBlockPos().down();
      BlockState state = this.getWorld().getBlockState(posBelow);
      BlockSoundGroup soundGroup = state.getSoundGroup();
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
      if (!this.getWorld().isClient()) {
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
      if (!this.hasPassengers() && this.isSceneActive() && isStopPhase) {
         this.stopScene();
      }

      if (!this.getWorld().isClient()) {
         this.animationEventQueueServer.clear();
      } else {
         this.animationEventQueueClient.clear();
      }
   }

   public void triggerSwing() {
      this.swinging = true;
      this.lastSwing = this.getWorld().getTime();
   }

   @Override
   public void registerControllers(ControllerRegistrar controllerRegistrar) {
      controllerRegistrar.add(new AnimationController<>(this, "girl_animations", 4, this::handleAnimations).setSoundKeyframeHandler(this::handleSoundKeyframe));
      controllerRegistrar.add(new AnimationController<>(this, "girl_attack", 4, this::handleAttackAnimations));
      controllerRegistrar.add(new AnimationController<>(this, "girl_face", 4, this::handleFacialAnimations));
   }

   @Environment(EnvType.CLIENT)
   private void handleSoundKeyframe(SoundKeyframeEvent<GirlSceneEntity> event) {
      if (this.getWorld().isClient()) {
         SoundKeyframeData data = event.getKeyframeData();
         String key = data.getSound().toLowerCase();
         this.getAnimationKeyFrameEvent().add(key);
         this.handleAnimationEventClient(key);
         ClientPlayNetworking.send(new SoundEventSyncC2SPacket(this.getId(), key));
      }
   }

   private PlayState handleFacialAnimations(AnimationState<GirlSceneEntity> state) {
      AnimationController<?> controller = state.getController();
      return state.setAndContinue(RawAnimation.begin().then(this.getAnimationPath("blink"), LoopType.LOOP));
   }

   private PlayState handleAttackAnimations(AnimationState<GirlSceneEntity> state) {
      AnimationController<?> controller = state.getController();
      if (this.swinging && this.lastSwing + 7L <= this.getWorld().getTime()) {
         this.swinging = false;
      }

      if (this.swinging && controller.getAnimationState() == State.STOPPED) {
         controller.forceAnimationReset();
         return state.setAndContinue(RawAnimation.begin().then(this.getAnimationPath("attack" + RANDOM.nextInt(0, 3)), LoopType.PLAY_ONCE));
      } else {
         return controller.getAnimationState() == State.STOPPED ? PlayState.STOP : PlayState.CONTINUE;
      }
   }

   @Override
   public boolean tryAttack(Entity target) {
      boolean hit = super.tryAttack(target);
      if (hit && this.getWorld() instanceof ServerWorld serverWorld) {
         for (ServerPlayerEntity player : serverWorld.getPlayers()) {
            ServerPlayNetworking.send(player, new PlayAttackAnimationS2CPacket(this.getId()));
         }
      }

      return hit;
   }

   private PlayState handleAnimations(AnimationState<GirlSceneEntity> state) {
      if (this.isSceneActive() && this.getOverrideAnim().isEmpty()) {
         AnimationController<?> controller = state.getController();
         Scene options = this.getCurrentScene();
         if ((controller.hasAnimationFinished() || controller.getAnimationState() == State.PAUSED) && !this.lastSceneAnim.isEmpty()) {
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

         AnimationController<?> controller = state.getController();
         String overrideAnim = this.getOverrideAnim();
         boolean overrideLoop = this.getOverrideLoopState();
         boolean overrideHold = this.getOverrideHoldState();
         if (overrideAnim != null && !overrideAnim.isEmpty()) {
            this.currentAnimState = overrideAnim;
            this.currentLoopState = overrideLoop;
            this.currentHoldState = overrideHold;
            if (!overrideLoop && (controller.getAnimationState() == State.STOPPED || controller.getAnimationState() == State.PAUSED)) {
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
      if (!this.getWorld().isClient()) {
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
      Vec3d velocity = this.getVelocity();
      double horizontalSpeedSq = velocity.x * velocity.x + velocity.z * velocity.z;
      return horizontalSpeedSq > 1.0E-4;
   }

   private String getDefaultAnimation(AnimationState<?> state) {
      String base;
      if (this.isSitting()) {
         base = "sit";
      } else if (this.hasVehicle()) {
         base = "ride";
      } else if (this.isAerialEntity()) {
         if (this.isEntityMoving() && this.isSprinting()) {
            base = "fly_fast";
         } else if (this.isEntityMoving()) {
            base = "fly";
         } else {
            base = "idle";
         }
      } else if (!this.isOnGround()) {
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
      if (!this.getWorld().isClient) {
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

   public void applySkinToBone(PlayerEntity player) {
      if (this.getWorld().isClient()) {
         try {
            Class<?> helper = Class.forName("com.cuddly.heartbound.client.GirlSceneClientHelper");
            Method m = helper.getMethod("applySkinToBone", GirlSceneEntity.class, PlayerEntity.class);
            m.invoke(null, this, player);
         } catch (Throwable var4) {
         }
      }
   }

   private void updateClothingAndArmor() {
      if (!this.getWorld().isClient()) {
         boolean stripped = this.isStripped();

         for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (this.isGirlArmorSlot(slot)) {
               boolean hasArmor = !this.inventory.getEquipmentStack(slot).isEmpty();
               this.armorVisibility.put(slot, hasArmor & !stripped);
            }
         }

         List<Boolean> armorList = Arrays.stream(EquipmentSlot.values()).map(s -> this.armorVisibility.getOrDefault(s, false)).toList();
         ClothingArmorVisibilityS2CPacket packet = new ClothingArmorVisibilityS2CPacket(this.getId(), armorList);

         for (ServerPlayerEntity player : Objects.requireNonNull(this.getServer()).getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(player, packet);
         }
      }
   }

   public void applyClothingAndArmor() {
      if (this.getWorld().isClient()) {
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
         DyedColorComponent dyed = stack.get(DataComponentTypes.DYED_COLOR);
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
      } else if (this instanceof TameableGirlEntity tameable && tameable.getOwner() instanceof PlayerEntity owner) {
         HeartboundMessages.PlayerSpecificMessage(owner, finalMessage);
      }
   }

   public void messageAsEntityTranslatable(String translationKey) {
      Text message = Text.literal("<" + this.getGirlDisplayName() + "> ").append(Text.translatable(translationKey));
      if (this.getScenePlayer() != null) {
         HeartboundMessages.PlayerSpecificMessage(this.getScenePlayer(), message);
      } else if (this instanceof TameableGirlEntity tameable && tameable.getOwner() instanceof PlayerEntity owner) {
         HeartboundMessages.PlayerSpecificMessage(owner, message);
      }
   }

   public void messageAsPlayer(String message) {
      if (this.getScenePlayer() != null) {
         GameProfile profile = this.getScenePlayer().getGameProfile();
         String finalMessage = "<" + profile.getName() + "> " + message;
         HeartboundMessages.PlayerSpecificMessage(this.getScenePlayer(), finalMessage);
      }
   }

   public void messageAsPlayerTranslatable(String translationKey) {
      if (this.getScenePlayer() != null) {
         GameProfile profile = this.getScenePlayer().getGameProfile();
         Text message = Text.literal("<" + profile.getName() + "> ").append(Text.translatable(translationKey));
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
   public boolean damage(DamageSource source, float amount) {
      return this.isTemporary() ? false : super.damage(source, amount);
   }

   @Override
   protected void initGoals() {
      super.initGoals();
      this.goalSelector.add(-4, new StationaryContactGoal(this));
      this.goalSelector.add(-3, new MoveToPlayerGoal(this, 1.25));
      this.goalSelector.add(-2, new BedGoal(this, 1.25));
      this.goalSelector.add(-1, new StripGoal(this));
      this.goalSelector.add(0, new StopMovementGoal(this));
      this.goalSelector.add(1, new GirlSitGoal(this));
   }

   public boolean isCurrentScenePlayer(PlayerEntity player) {
      return this.getScenePlayer() == null ? false : this.getScenePlayer().getUuid().equals(player.getUuid());
   }

   @Override
   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
   }

   @Override
   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
   }
}
