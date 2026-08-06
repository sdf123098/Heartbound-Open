package com.cuddly.heartbound.entity.base;

import com.cuddly.heartbound.advancement.criterion.HeartboundCriteria;
import com.cuddly.heartbound.entity.ai.pathing.GirlNavigation;
import com.cuddly.heartbound.registries.HeartboundTrackedDataRegistry;
import com.cuddly.heartbound.util.HeartboundLangUtils;
import com.cuddly.heartbound.util.inventory.GirlInventory;
import com.cuddly.heartbound.util.variables.Scene;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public abstract class GirlEntity extends PathfinderMob implements RangedAttackMob {
   private static final EntityDataAccessor<Boolean> WAITING_AT_BED = SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> IS_TEMPORARY = SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> CREATED_CLONE = SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> LOCKED_STATE = SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> WAITING_FOR_PLAYER = SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> FROZEN_STATE = SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> STRIPPED = SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> FOLLOWING = SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> ROAMING = SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> CHOPPING = SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> MINING = SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> IN_SCENE = SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> OVERRIDE_LOOP = SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> OVERRIDE_HOLD = SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> OVERRIDE_ANIM_PLAYING = SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> PLAYER_MODEL_SLIM = SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> HAVING_SEX = SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> SITTING = SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> WALKING_BACKWARD = SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<String> OVERRIDE_ANIM = SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.STRING);
   private static final EntityDataAccessor<String> SCENE_ANIM = SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.STRING);
   private static final EntityDataAccessor<Integer> BREAST_SIZE = SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> RELATIONSHIP_LEVEL = SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> MAX_RELATIONSHIP_LEVEL = SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<BlockPos> BASE_POS = SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.BLOCK_POS);
   private static final EntityDataAccessor<Vec3> PASSENGER_BONE_POSITION = SynchedEntityData.defineId(GirlEntity.class, HeartboundTrackedDataRegistry.VEC3D);
   private static final EntityDataAccessor<Vec3> BREAST_OFFSET = SynchedEntityData.defineId(GirlEntity.class, HeartboundTrackedDataRegistry.VEC3D);
   private static final EntityDataAccessor<ItemStack> CONSUMING_STACK = SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.ITEM_STACK);
   public static final Random RANDOM = new Random();
   public Map<String, Boolean> boneVisibility = new HashMap<>();
   public Map<String, Integer> boneColorOverrides = new HashMap<>();
   public Map<String, Identifier> boneTextureOverrides = new HashMap<>();
   public Map<String, Identifier> boneTextureOverridesLayer2 = new HashMap<>();
   public Map<String, Identifier> boneTextureOverridesLayer3 = new HashMap<>();
   public Map<String, Vec3> boneSizeOverrides = new HashMap<>();
   public Map<String, Vec3> bonePositionOffset = new HashMap<>();
   public Map<String, Vec2> boneUVOffsets = new HashMap<>();
   public final Map<EquipmentSlot, Boolean> armorVisibility = new EnumMap<>(EquipmentSlot.class);
   public Vec3 previousVelocity = Vec3.ZERO;
   private static final int SPRINTING_FLAG_INDEX = 3;
   private static final int MAX_TICKS_NO_HIT = 400;
   private static final Identifier SPRINTING_SPEED_MODIFIER_ID = Identifier.fromNamespaceAndPath("heartbound", "sprinting_speed_modifier");
   private static final AttributeModifier SPRINTING_SPEED_BOOST = new AttributeModifier(
      SPRINTING_SPEED_MODIFIER_ID, 0.6499999999999999, Operation.ADD_MULTIPLIED_TOTAL
   );
   private int ticksSinceLastHit;
   public float previousYaw = 0.0F;
   public float passengerYOffset = -1.0F;
   public boolean currentLoopState = false;
   public boolean currentHoldState = false;
   private boolean guiOpenSate = false;
   public String currentAnimState = "idle";
   public final GirlInventory inventory = GirlInventory.ofSize();
   private LivingEntity attackTarget;
   private Player lookAtTarget;
   private int autoEatCooldown = 0;
   private static final int AUTO_EAT_INTERVAL = 40;
   private int goldenAppleCooldown = 0;
   private int enchantedAppleCooldown = 0;
   private static final int GOLDEN_APPLE_COOLDOWN = 100;
   private static final int ENCHANTED_APPLE_COOLDOWN = 100;
   private final Set<BlockPos> openedFenceGates = new HashSet<>();
   private int fenceGateCheckCooldown = 0;

   protected GirlEntity(EntityType<? extends GirlEntity> entityType, Level world) {
      super(entityType, world);
      if (this.getNavigation() instanceof GroundPathNavigation mobNav) {
         mobNav.setCanOpenDoors(true);
      }
   }

   @Override
   protected PathNavigation createNavigation(Level world) {
      return new GirlNavigation(this, world);
   }

   @Override
   protected void defineSynchedData(Builder builder) {
      super.defineSynchedData(builder);
      builder.define(WAITING_AT_BED, false);
      builder.define(IS_TEMPORARY, false);
      builder.define(CREATED_CLONE, false);
      builder.define(LOCKED_STATE, false);
      builder.define(FROZEN_STATE, false);
      builder.define(WAITING_FOR_PLAYER, false);
      builder.define(STRIPPED, false);
      builder.define(FOLLOWING, false);
      builder.define(ROAMING, false);
      builder.define(CHOPPING, false);
      builder.define(MINING, false);
      builder.define(IN_SCENE, false);
      builder.define(OVERRIDE_LOOP, false);
      builder.define(OVERRIDE_HOLD, false);
      builder.define(OVERRIDE_ANIM_PLAYING, false);
      builder.define(PLAYER_MODEL_SLIM, false);
      builder.define(HAVING_SEX, false);
      builder.define(SITTING, false);
      builder.define(WALKING_BACKWARD, false);
      builder.define(RELATIONSHIP_LEVEL, 0);
      builder.define(MAX_RELATIONSHIP_LEVEL, 4);
      builder.define(BREAST_SIZE, 100);
      builder.define(BREAST_OFFSET, Vec3.ZERO);
      builder.define(PASSENGER_BONE_POSITION, Vec3.ZERO);
      builder.define(BASE_POS, this.blockPosition());
      builder.define(OVERRIDE_ANIM, "");
      builder.define(SCENE_ANIM, "");
      builder.define(CONSUMING_STACK, Items.COOKED_BEEF.getDefaultInstance());
   }

   public void setFollowing(boolean follow) {
      this.entityData.set(FOLLOWING, follow);
   }

   public boolean isFollowing() {
      return this.entityData.get(FOLLOWING);
   }

   public void setRoaming(boolean roaming) {
      this.entityData.set(ROAMING, roaming);
   }

   public boolean isRoaming() {
      return this.entityData.get(ROAMING);
   }

   public void setChopping(boolean chopping) {
      this.entityData.set(CHOPPING, chopping);
   }

   public boolean isChopping() {
      return this.entityData.get(CHOPPING);
   }

   public void setMining(boolean mining) {
      this.entityData.set(MINING, mining);
   }

   public boolean isMining() {
      return this.entityData.get(MINING);
   }

   public void setStripped(boolean stripped) {
      this.entityData.set(STRIPPED, stripped);
   }

   public boolean isStripped() {
      return this.entityData.get(STRIPPED);
   }

   public void setFreeze(boolean locked) {
      this.entityData.set(FROZEN_STATE, locked);
   }

   public boolean isFrozenInPlace() {
      return this.entityData.get(FROZEN_STATE);
   }

   public void setMovementLockedState(boolean locked) {
      this.entityData.set(LOCKED_STATE, locked);
   }

   public boolean isMovementLocked() {
      return this.entityData.get(LOCKED_STATE);
   }

   public void setGUIOpenState(boolean state, @Nullable Player lookAt) {
      this.guiOpenSate = state;
      this.lookAtTarget = lookAt;
   }

   public void setGUIOpenState(boolean state) {
      this.setGUIOpenState(state, null);
   }

   public boolean isGUIOpen() {
      return this.guiOpenSate;
   }

   public void setSceneState(boolean inScene) {
      this.entityData.set(IN_SCENE, inScene);
   }

   public boolean isSceneActive() {
      return this.entityData.get(IN_SCENE);
   }

   public void setOverrideAnim(String anim) {
      this.entityData.set(OVERRIDE_ANIM, anim);
   }

   public String getOverrideAnim() {
      return this.entityData.get(OVERRIDE_ANIM);
   }

   public void setOverrideLoop(boolean loop) {
      this.entityData.set(OVERRIDE_LOOP, loop);
   }

   public boolean getOverrideLoopState() {
      return this.entityData.get(OVERRIDE_LOOP);
   }

   public void setOverrideHold(boolean hold) {
      this.entityData.set(OVERRIDE_HOLD, hold);
   }

   public boolean getOverrideHoldState() {
      return this.entityData.get(OVERRIDE_HOLD);
   }

   public boolean isWaitingAtBed() {
      return this.entityData.get(WAITING_AT_BED);
   }

   public void setWaitingAtBedState(boolean state) {
      this.entityData.set(WAITING_AT_BED, state);
   }

   public boolean isTemporary() {
      return this.entityData.get(IS_TEMPORARY);
   }

   public void setTemporaryState(boolean state) {
      this.entityData.set(IS_TEMPORARY, state);
   }

   public boolean createdClone() {
      return this.entityData.get(CREATED_CLONE);
   }

   public void setCreatedCloneState(boolean state) {
      this.entityData.set(CREATED_CLONE, state);
   }

   public boolean isWaitingForPlayer() {
      return this.entityData.get(WAITING_FOR_PLAYER);
   }

   public void setWaitingForPlayerState(boolean state) {
      this.entityData.set(WAITING_FOR_PLAYER, state);
   }

   public void setIsPlayerModelSlim(boolean isSlim) {
      this.entityData.set(PLAYER_MODEL_SLIM, isSlim);
   }

   public boolean isPlayerModelSlim() {
      return this.entityData.get(PLAYER_MODEL_SLIM);
   }

   public void setHavingSex(boolean state) {
      this.entityData.set(HAVING_SEX, state);
   }

   public boolean isHavingSex() {
      return this.entityData.get(HAVING_SEX);
   }

   public int getCurrentRelationshipLevel() {
      return this.entityData.get(RELATIONSHIP_LEVEL);
   }

   public void setCurrentRelationshipLevel(int value) {
      this.entityData.set(RELATIONSHIP_LEVEL, value);
   }

   public void setPassengerBonePosition(Vec3 position) {
      this.entityData.set(PASSENGER_BONE_POSITION, position);
   }

   public Vec3 getPassengerBonePosition() {
      return this.entityData.get(PASSENGER_BONE_POSITION);
   }

   public void setBasePos(BlockPos block) {
      this.entityData.set(BASE_POS, block);
   }

   public BlockPos getBasePos() {
      return this.entityData.get(BASE_POS);
   }

   public void setBreastSize(int value) {
      this.entityData.set(BREAST_SIZE, value);
   }

   public int getBreastSize() {
      return this.entityData.get(BREAST_SIZE);
   }

   public void setBreastOffset(Vec3 value) {
      this.entityData.set(BREAST_OFFSET, value);
   }

   public Vec3 getBreastOffset() {
      return this.entityData.get(BREAST_OFFSET);
   }

   public GirlInventory getInventory() {
      return this.inventory;
   }

   public Item isAttractedTo() {
      return Items.DANDELION;
   }

   public boolean useUpRelationShipLevels() {
      return false;
   }

   public String getGirlID() {
      return "null";
   }

   public String getGirlDisplayName() {
      return HeartboundLangUtils.getStringFromKey("entity.heartbound." + this.getGirlID());
   }

   public int getBreastMinSize() {
      return 25;
   }

   public int getBreastMaxSize() {
      return 150;
   }

   public int getSizeGUI() {
      return 20;
   }

   public float getYAxisGUI() {
      return 0.0625F;
   }

   public List<Scene> getScenes() {
      return new ArrayList<>();
   }

   public float getWeaponBoneXRotation() {
      return 150.0F;
   }

   public boolean hasStripAnim() {
      return true;
   }

   public boolean hasBackwardsWalkAnim() {
      return true;
   }

   public boolean isAerialEntity() {
      return false;
   }

   public void setWalkingBackward(boolean backward) {
      this.entityData.set(WALKING_BACKWARD, backward);
   }

   public boolean isWalkingBackward() {
      return this.entityData.get(WALKING_BACKWARD);
   }

   public int maxRelationshipLevel() {
      if (!this.level().isClientSide()) {
         try {
            List<Scene> options = this.getScenes();
            int value = options != null && !options.isEmpty() ? options.stream().map(Scene::requiredRelationshipLevel).max(Integer::compareTo).orElse(4) : 4;
            this.entityData.set(MAX_RELATIONSHIP_LEVEL, value);
         } catch (Exception var3) {
            this.entityData.set(MAX_RELATIONSHIP_LEVEL, 4);
         }
      }

      return this.entityData.get(MAX_RELATIONSHIP_LEVEL);
   }

   protected Map<EquipmentSlot, List<String>> getArmorBones() {
      Map<EquipmentSlot, List<String>> armor = new HashMap<>();
      armor.put(EquipmentSlot.HEAD, new ArrayList<>(List.of("armorHelmet")));
      armor.put(EquipmentSlot.CHEST, new ArrayList<>(List.of("armorBoobs", "armorChest", "armorShoulderL", "armorShoulderR")));
      armor.put(
         EquipmentSlot.LEGS,
         new ArrayList<>(List.of("armorHip", "armorPantsLowL", "armorPantsUpL", "armorPantsLowR", "armorPantsUpR", "armorBootyL", "armorBootyR"))
      );
      armor.put(EquipmentSlot.FEET, new ArrayList<>(List.of("armorShoesL", "armorShoesR")));
      return armor;
   }

   public boolean isFoodItem(ItemStack stack) {
      if (stack == null || stack.isEmpty()) {
         return false;
      } else if (stack.is(Items.ROTTEN_FLESH)) {
         return false;
      } else if (stack.is(Items.POISONOUS_POTATO) || stack.is(Items.SPIDER_EYE) || stack.is(Items.CHORUS_FRUIT) || stack.is(Items.POPPED_CHORUS_FRUIT)) {
         return false;
      } else if (stack.is(Items.BEEF) || stack.is(Items.PORKCHOP) || stack.is(Items.CHICKEN) || stack.is(Items.MUTTON) || stack.is(Items.RABBIT)) {
         return false;
      } else if (stack.is(Items.COD) || stack.is(Items.SALMON) || stack.is(Items.TROPICAL_FISH) || stack.is(Items.PUFFERFISH)) {
         return false;
      } else if (stack.is(ItemTags.WOLF_FOOD)) {
         return true;
      } else {
         FoodProperties foodComponent = stack.get(DataComponents.FOOD);
         return foodComponent != null;
      }
   }

   @Override
   public boolean shouldShowName() {
      this.setCustomName(Component.nullToEmpty(this.getGirlDisplayName()));
      this.setCustomNameVisible(true);
      return true;
   }

   public void setBasePosHere() {
      this.setBasePos(this.blockPosition());
   }

   public void teleportToBase() {
      this.teleportToBase(true);
   }

   public void teleportToBase(boolean shouldSit) {
      if (shouldSit) {
         this.setSitting(true);
      }

      BlockPos base = this.getBasePos();
      this.teleportTo((double)base.getX() + 0.5, (double)base.getY(), (double)base.getZ() + 0.5);
      this.getNavigation().stop();
   }

   @Override
   public ItemStack getItemBySlot(EquipmentSlot slot) {
      return this.inventory.getEquipmentStack(slot);
   }

   @Override
   public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
      this.inventory.setEquipmentStack(slot, stack);
   }

   @Override
   public void addAdditionalSaveData(ValueOutput nbt) {
      super.addAdditionalSaveData(nbt);
      ContainerHelper.saveAllItems(nbt, this.inventory.getItems());
      nbt.putBoolean("SitSate", this.isSitting());
      nbt.putBoolean("StripState", this.isStripped());
      nbt.putBoolean("FollowState", this.isFollowing());
      nbt.putBoolean("RoamingState", this.isRoaming());
      nbt.putBoolean("ChoppingState", this.isChopping());
      nbt.putBoolean("MiningState", this.isMining());
      nbt.putInt("RelationshipLevel", this.getCurrentRelationshipLevel());
      nbt.putInt("BreastSize", this.getBreastSize());
      nbt.store("BreastOffset", Vec3.CODEC, this.getBreastOffset());
      nbt.store("BasePos", BlockPos.CODEC, this.getBasePos());
      nbt.putBoolean("Sitting", this.isSitting());
   }

   @Override
   public void readAdditionalSaveData(ValueInput nbt) {
      super.readAdditionalSaveData(nbt);
      Level w = this.level();
      if (w != null) {
         ContainerHelper.loadAllItems(nbt, this.inventory.getItems());
      }

      boolean sitting = nbt.getBooleanOr("Sitting", false);
      this.setSitting(sitting);
      boolean following = nbt.getBooleanOr("FollowState", false);
      this.setFollowing(following);
      boolean roaming = nbt.getBooleanOr("RoamingState", false);
      this.setRoaming(roaming);
      boolean chopping = nbt.getBooleanOr("ChoppingState", false);
      this.setChopping(chopping);
      boolean mining = nbt.getBooleanOr("MiningState", false);
      this.setMining(mining);
      boolean stripped = nbt.getBooleanOr("StripState", false);
      this.setStripped(stripped);
      int relationship = nbt.getIntOr("RelationshipLevel", 0);
      this.setCurrentRelationshipLevel(relationship);
      this.setBasePos(nbt.read("BasePos", BlockPos.CODEC).orElse(new BlockPos(0, 0, 0)));
      this.setBreastOffset(nbt.read("BreastOffset", Vec3.CODEC).orElse(Vec3.ZERO));
      this.setBreastSize(nbt.getIntOr("BreastSize", 0));
   }

   public boolean canAttackWithOwner(LivingEntity target, LivingEntity owner) {
      return true;
   }

   public boolean isSitting() {
      return this.entityData.get(SITTING);
   }

   public void setSitting(boolean sitting) {
      this.setTarget(null);
      this.entityData.set(SITTING, sitting);
      if (sitting) {
         try {
            this.setRoaming(false);
         } catch (Throwable var3) {
         }
      }
   }

   @Override
   public void die(DamageSource damageSource) {
      if (damageSource.getEntity() instanceof ServerPlayer serverPlayer) {
         HeartboundCriteria.KILL_GIRL.trigger(serverPlayer);
      }

      this.ejectPassengers();
      super.die(damageSource);
   }

   @Override
   public void tick() {
      super.tick();
      this.previousYaw = this.getYRot();
      this.previousVelocity = this.getDeltaMovement();
      this.setMovementLockedState(this.isFrozenInPlace() || this.isWaitingAtBed() || this.isSceneActive() || this.isWaitingForPlayer());
      if (!this.level().isClientSide()) {
         this.handleFenceGates();
         if (this.autoEatCooldown > 0) {
            this.autoEatCooldown--;
         }

         if (this.goldenAppleCooldown > 0) {
            this.goldenAppleCooldown--;
         }

         if (this.enchantedAppleCooldown > 0) {
            this.enchantedAppleCooldown--;
         }

         if (this.autoEatCooldown <= 0) {
            this.tryAutoEat();
            this.autoEatCooldown = 40 + RANDOM.nextInt(20);
         }
      }
   }

   private void handleFenceGates() {
      this.openedFenceGates.removeIf(gatePos -> {
         if (this.distanceToSqr((double)gatePos.getX() + 0.5, (double)gatePos.getY(), (double)gatePos.getZ() + 0.5) > 9.0) {
            BlockState statex = this.level().getBlockState(gatePos);
            if (statex.getBlock() instanceof FenceGateBlock && statex.getValue(FenceGateBlock.OPEN)) {
               this.level().setBlock(gatePos, statex.setValue(FenceGateBlock.OPEN, Boolean.valueOf(false)), 10);
               this.level().playSound(null, gatePos, SoundEvents.FENCE_GATE_CLOSE, SoundSource.BLOCKS, 1.0F, 1.0F);
            }

            return true;
         } else {
            return false;
         }
      });
      boolean colliding = this.horizontalCollision;
      if (!colliding) {
         if (this.fenceGateCheckCooldown > 0) {
            this.fenceGateCheckCooldown--;
            return;
         }

         if (this.getNavigation().isDone()) {
            return;
         }

         this.fenceGateCheckCooldown = 5;
      }

      BlockPos entityPos = this.blockPosition();
      boolean openedAny = false;

      for (int dx = -1; dx <= 1; dx++) {
         for (int dz = -1; dz <= 1; dz++) {
            for (int dy = 0; dy <= 1; dy++) {
               BlockPos pos = entityPos.offset(dx, dy, dz);
               BlockState state = this.level().getBlockState(pos);
               if (state.getBlock() instanceof FenceGateBlock && !state.getValue(FenceGateBlock.OPEN)) {
                  this.level().setBlock(pos, state.setValue(FenceGateBlock.OPEN, Boolean.valueOf(true)), 10);
                  this.level().playSound(null, pos, SoundEvents.FENCE_GATE_OPEN, SoundSource.BLOCKS, 1.0F, 1.0F);
                  this.openedFenceGates.add(pos.immutable());
                  openedAny = true;
               }
            }
         }
      }

      if (openedAny && colliding) {
         this.getNavigation().recomputePath();
      }
   }

   @Override
   public void setSprinting(boolean sprinting) {
      this.setSharedFlag(3, sprinting);
      AttributeInstance entityAttributeInstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
      entityAttributeInstance.removeModifier(SPRINTING_SPEED_BOOST.id());
      if (sprinting) {
         entityAttributeInstance.addTransientModifier(SPRINTING_SPEED_BOOST);
      }
   }

   protected void eat(Player player, InteractionHand hand, ItemStack stack) {
      ItemStack particleStack = stack.copy();
      particleStack.setCount(1);
      if (!player.isCreative()) {
         stack.shrink(1);
      }

      this.makeSound(SoundEvents.GENERIC_EAT.value());
      this.entityData.set(CONSUMING_STACK, particleStack);
      this.level().broadcastEntityEvent(this, (byte)74);
      ItemStackTemplate remainderTemplate = stack.getItem().getCraftingRemainder();
      if (remainderTemplate.item().value() != Items.AIR) {
         ItemStack remainder = remainderTemplate.create();
         if (!player.getInventory().add(remainder)) {
            player.drop(remainder, false);
         }
      }
   }

   public Vec3 getPassengerPos() {
      boolean isZero = this.getPassengerBonePosition().closerThan(Vec3.ZERO, 0.1);
      return !isZero && this.isHavingSex()
         ? this.position().add(this.getPassengerBonePosition()).add(0.0, (double)this.passengerYOffset, 0.0)
         : this.position().add(0.0, 1.0, 0.0);
   }

   @Override
   public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
      return this.getPassengerPos();
   }

   @Override
   public Vec3 getPassengerRidingPosition(Entity passenger) {
      return this.getPassengerPos();
   }

   @Override
   public boolean removeWhenFarAway(double distanceSquared) {
      return false;
   }

   @Override
   protected void dropEquipment(ServerLevel serverWorld) {
      super.dropEquipment(serverWorld);
      if (this.level() instanceof ServerLevel serverLevel && !this.isRuleEnabled(serverLevel, GameRules.KEEP_INVENTORY)) {
         for (ItemStack stack : this.getInventory().getItems()) {
            if (!stack.isEmpty()) {
               this.spawnAtLocation(serverLevel, stack);
            }
         }

         this.getInventory().clearContent();
      }
   }

   public boolean isRuleEnabled(ServerLevel world, GameRule<Boolean> rule) {
      return world.getGameRules().get(rule);
   }

   @Override
   public void push(Entity entity) {
      if (!this.isMovementLocked()) {
         super.push(entity);
      }
   }

   @Override
   public void knockback(double strength, double x, double z) {
      if (!this.isMovementLocked()) {
         super.knockback(strength, x, z);
      } else {
         this.setDeltaMovement(Vec3.ZERO);
      }
   }

   @Override
   public boolean isPushable() {
      return !this.isMovementLocked();
   }

   @Override
   public void push(double deltaX, double deltaY, double deltaZ) {
      if (!this.isMovementLocked()) {
         super.push(deltaX, deltaY, deltaZ);
      }
   }

   @Override
   public void stopInPlace() {
      super.stopInPlace();
      this.setDeltaMovement(0.0, this.getDeltaMovement().y > 0.0 ? 0.0 : this.getDeltaMovement().y, 0.0);
      this.setJumping(false);
      this.yBodyRot = this.getVisualRotationYInDegrees();
      MoveControl control = this.getMoveControl();
      if (control != null) {
         control.setWantedPosition(this.getX(), this.getY(), this.getZ(), 0.0);
      }
   }

   public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder createDefaultAttributes() {
      return Mob.createMobAttributes()
         .add(Attributes.MAX_HEALTH, 20.0)
         .add(Attributes.MOVEMENT_SPEED, 0.2)
         .add(Attributes.FOLLOW_RANGE, 100.0)
         .add(Attributes.TEMPT_RANGE, 10.0)
         .add(Attributes.ATTACK_DAMAGE, 2.0);
   }

   @Override
   public void performRangedAttack(LivingEntity target, float pullProgress) {
      ItemStack itemStack = this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, Items.BOW));
      ItemStack itemStack2 = this.getProjectile(itemStack);
      AbstractArrow arrow = this.createArrowProjectile(itemStack2, pullProgress, itemStack);
      float arrowSpeed = 1.6F;
      double arrowStartY = arrow.getY();
      Vec3 targetVel = target.getDeltaMovement();
      double dx = target.getX() - this.getX();
      double dz = target.getZ() - this.getZ();
      double travelTime = Math.sqrt(dx * dx + dz * dz) / (double)arrowSpeed;
      double aimX = dx + targetVel.x * travelTime;
      double aimZ = dz + targetVel.z * travelTime;
      double targetAbsY = target.getY() + (double)target.getBbHeight() / 2.0 + targetVel.y * travelTime;
      double aimY = targetAbsY - arrowStartY;
      double aimHDist = Math.sqrt(aimX * aimX + aimZ * aimZ);
      double dy = aimY;

      for (int i = 0; i < 5; i++) {
         double mag = Math.sqrt(aimX * aimX + dy * dy + aimZ * aimZ);
         double vx = aimX / mag * (double)arrowSpeed;
         double vy = dy / mag * (double)arrowSpeed;
         double vz = aimZ / mag * (double)arrowSpeed;
         double simY = arrowStartY;
         double simH = 0.0;

         for (int t = 0; t < 200 && simH < aimHDist; t++) {
            simY += vy;
            simH += Math.sqrt(vx * vx + vz * vz);
            vx *= 0.99;
            vy *= 0.99;
            vz *= 0.99;
            vy -= 0.05;
         }

         dy += targetAbsY - simY;
      }

      arrow.shoot(aimX, dy, aimZ, arrowSpeed, 0.0F);
      if (this.level() instanceof ServerLevel serverWorld) {
         serverWorld.addFreshEntity(arrow);
      }

      this.playSound(SoundEvents.ARROW_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
   }

   protected AbstractArrow createArrowProjectile(ItemStack arrow, float damageModifier, @Nullable ItemStack shotFrom) {
      if (arrow == null || arrow.isEmpty()) {
         arrow = Items.ARROW.getDefaultInstance();
      }

      Arrow arrowEntity = new Arrow(this.level(), this, arrow.copy(), shotFrom);
      arrowEntity.setBaseDamage(2.0 * (double)damageModifier);
      return arrowEntity;
   }

   @Override
   public void handleEntityEvent(byte status) {
      if (status == 71) {
         this.spawnParticles(ParticleTypes.HEART);
      } else if (status == 70) {
         this.spawnParticles(ParticleTypes.SMOKE);
      } else if (status == 73) {
         this.spawnParticles(ParticleTypes.HAPPY_VILLAGER);
      } else if (status == 72) {
         this.spawnParticles(ParticleTypes.ANGRY_VILLAGER);
      } else if (status == 74) {
         this.spawnItemParticlesCustom(this.entityData.get(CONSUMING_STACK), 16);
      } else {
         super.handleEntityEvent(status);
      }
   }

   protected void spawnItemParticlesCustom(ItemStack stack, int count) {
      if (stack != null && !stack.isEmpty()) {
         for (int i = 0; i < count; i++) {
            Vec3 velocity = new Vec3(((double)this.random.nextFloat() - 0.5) * 0.1, Math.random() * 0.1 + 0.1, 0.0)
               .xRot(-this.getXRot() * (float) (Math.PI / 180.0))
               .yRot(-this.getYRot() * (float) (Math.PI / 180.0));
            this.level()
               .addParticle(
                  new ItemParticleOption(ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(stack)),
                  this.getX() + this.getLookAngle().x / 2.0,
                  this.getY() + (double)this.getEyeHeight(this.getPose()) - 0.2,
                  this.getZ() + this.getLookAngle().z / 2.0,
                  velocity.x,
                  velocity.y + 0.05,
                  velocity.z
               );
         }
      }
   }

   protected void tryAutoEat() {
      if (!this.level().isClientSide()) {
         if (this.isAlive() && !this.isRemoved()) {
            float hp = this.getHealth();
            float max = this.getMaxHealth();
            if (!(hp >= max)) {
               boolean allowAny = hp <= max * 0.8F;
               boolean allowGolden = hp < max * 0.5F && this.goldenAppleCooldown <= 0;
               boolean allowEnchanted = hp < max * 0.3F && this.enchantedAppleCooldown <= 0;
               int useIndex = this.findAutoEatIndex(allowEnchanted, allowGolden, allowAny);
               if (useIndex != -1) {
                  this.consumeAutoEatIndex(useIndex);
               }
            }
         }
      }
   }

   private int findAutoEatIndex(boolean allowEnchanted, boolean allowGolden, boolean allowAny) {
      int enchantedIdx = -1;
      int goldenIdx = -1;
      int anyIdx = -1;

      for (int i = 5; i <= 28; i++) {
         ItemStack s = this.inventory.getItem(i);
         if (s != null && !s.isEmpty() && !s.is(Items.ROTTEN_FLESH) && !s.is(Items.PUFFERFISH)) {
            if (enchantedIdx == -1 && s.is(Items.ENCHANTED_GOLDEN_APPLE)) {
               enchantedIdx = i;
            } else if (goldenIdx == -1 && s.is(Items.GOLDEN_APPLE)) {
               goldenIdx = i;
            } else if (anyIdx == -1 && this.isFoodItem(s) && !s.is(Items.GOLDEN_APPLE) && !s.is(Items.ENCHANTED_GOLDEN_APPLE)) {
               anyIdx = i;
            }
         }
      }

      if (allowEnchanted && enchantedIdx != -1) {
         return enchantedIdx;
      } else if (allowGolden && goldenIdx != -1) {
         return goldenIdx;
      } else {
         return allowAny && anyIdx != -1 ? anyIdx : -1;
      }
   }

   private void consumeAutoEatIndex(int useIndex) {
      ItemStack used = this.inventory.removeItem(useIndex, 1);
      if (used != null && !used.isEmpty()) {
         ItemStack particleStack = used.copy();
         particleStack.setCount(1);
         ItemStack remainder = used.getItem().finishUsingItem(used, this.level(), this);
         if (used.is(Items.GOLDEN_APPLE)) {
            this.goldenAppleCooldown = 100;
         } else if (used.is(Items.ENCHANTED_GOLDEN_APPLE)) {
            this.enchantedAppleCooldown = 100;
         }

         this.insertOrDropRemainder(remainder);
         float healAmount = this.computeHealAmount(particleStack);
         if (healAmount > 0.0F) {
            this.heal(healAmount);
         }

         this.makeSound(SoundEvents.GENERIC_EAT.value());
         this.entityData.set(CONSUMING_STACK, particleStack);
         this.level().broadcastEntityEvent(this, (byte)74);
      }
   }

   private void insertOrDropRemainder(ItemStack remainder) {
      if (remainder != null && !remainder.isEmpty()) {
         boolean inserted = false;

         for (int i = 5; i <= 28; i++) {
            ItemStack slot = this.inventory.getItem(i);
            if (slot.isEmpty()) {
               this.inventory.setItem(i, remainder);
               inserted = true;
               break;
            }
         }

         if (!inserted) {
            this.spawnAtLocation((ServerLevel)this.level(), remainder);
         }
      }
   }

   private float computeHealAmount(ItemStack particleStack) {
      FoodProperties food = particleStack.get(DataComponents.FOOD);
      if (food != null) {
         return 2.0F * (float)food.nutrition();
      } else if (particleStack.is(Items.GOLDEN_APPLE)) {
         return 8.0F;
      } else {
         return particleStack.is(Items.ENCHANTED_GOLDEN_APPLE) ? 10.0F : 0.0F;
      }
   }

   protected void spawnParticles(ParticleOptions parameters) {
      for (int i = 0; i < 5; i++) {
         double d = this.random.nextGaussian() * 0.02;
         double e = this.random.nextGaussian() * 0.02;
         double f = this.random.nextGaussian() * 0.02;
         this.level().addParticle(parameters, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), d, e, f);
      }
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (this.attackTarget != null) {
         this.ticksSinceLastHit++;
         if (this.ticksSinceLastHit >= 400) {
            this.setTarget(null);
            this.attackTarget = null;
            this.ticksSinceLastHit = 0;
         }
      }

      if (this.isGUIOpen()) {
         this.navigation.stop();
         if (this.lookAtTarget != null) {
            this.getLookControl().setLookAt(this.lookAtTarget, (float)(this.getMaxHeadYRot() + 20), (float)this.getMaxHeadXRot());
         }
      }
   }

   @Override
   public void setTarget(@Nullable LivingEntity target) {
      super.setTarget(target);
      if (target != null) {
         this.attackTarget = target;
         this.ticksSinceLastHit = 0;
      } else {
         this.attackTarget = null;
         this.ticksSinceLastHit = 0;
      }
   }

   @Override
   public boolean doHurtTarget(ServerLevel serverLevel, Entity target) {
      boolean success = super.doHurtTarget(serverLevel, target);
      if (success && target == this.attackTarget) {
         this.ticksSinceLastHit = 0;
      }

      return success;
   }

   public GirlEntity createTempClone() {
      if (this.level().isClientSide()) {
         return null;
      } else {
         GirlEntity clone = (GirlEntity)this.getType().create(this.level(), EntitySpawnReason.MOB_SUMMONED);
         clone.setTemporaryState(true);
         clone.setPos(this.getX(), 800.0, this.getZ());
         clone.setInvisible(true);
         clone.setInvulnerable(true);
         clone.setNoGravity(true);
         this.onTempCloneCreation(clone);
         this.level().addFreshEntity(clone);
         this.setCreatedCloneState(true);
         return clone;
      }
   }

   public void onTempCloneCreation(GirlEntity clone) {
      clone.setStripped(this.isStripped());
   }

   @Override
   public float getVoicePitch() {
      return 1.0F;
   }

   @Override
   public boolean isPersistenceRequired() {
      return true;
   }
}
