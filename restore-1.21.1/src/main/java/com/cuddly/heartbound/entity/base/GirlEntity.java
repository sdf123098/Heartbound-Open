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
import net.minecraft.block.BlockState;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.data.DataTracker.Builder;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.GameRules.BooleanRule;
import net.minecraft.world.GameRules.Key;
import org.jetbrains.annotations.Nullable;

public abstract class GirlEntity extends PathAwareEntity implements RangedAttackMob {
   private static final TrackedData<Boolean> WAITING_AT_BED = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   private static final TrackedData<Boolean> IS_TEMPORARY = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   private static final TrackedData<Boolean> CREATED_CLONE = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   private static final TrackedData<Boolean> LOCKED_STATE = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   private static final TrackedData<Boolean> WAITING_FOR_PLAYER = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   private static final TrackedData<Boolean> FROZEN_STATE = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   private static final TrackedData<Boolean> STRIPPED = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   private static final TrackedData<Boolean> FOLLOWING = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   private static final TrackedData<Boolean> ROAMING = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   private static final TrackedData<Boolean> CHOPPING = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   private static final TrackedData<Boolean> MINING = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   private static final TrackedData<Boolean> IN_SCENE = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   private static final TrackedData<Boolean> OVERRIDE_LOOP = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   private static final TrackedData<Boolean> OVERRIDE_HOLD = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   private static final TrackedData<Boolean> OVERRIDE_ANIM_PLAYING = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   private static final TrackedData<Boolean> PLAYER_MODEL_SLIM = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   private static final TrackedData<Boolean> HAVING_SEX = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   private static final TrackedData<Boolean> SITTING = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   private static final TrackedData<Boolean> WALKING_BACKWARD = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   private static final TrackedData<String> OVERRIDE_ANIM = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.STRING);
   private static final TrackedData<String> SCENE_ANIM = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.STRING);
   private static final TrackedData<Integer> BREAST_SIZE = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.INTEGER);
   private static final TrackedData<Integer> RELATIONSHIP_LEVEL = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.INTEGER);
   private static final TrackedData<Integer> MAX_RELATIONSHIP_LEVEL = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.INTEGER);
   private static final TrackedData<BlockPos> BASE_POS = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.BLOCK_POS);
   private static final TrackedData<Vec3d> PASSENGER_BONE_POSITION = DataTracker.registerData(GirlEntity.class, HeartboundTrackedDataRegistry.VEC3D);
   private static final TrackedData<Vec3d> BREAST_OFFSET = DataTracker.registerData(GirlEntity.class, HeartboundTrackedDataRegistry.VEC3D);
   private static final TrackedData<ItemStack> CONSUMING_STACK = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
   public static final Random RANDOM = new Random();
   public Map<String, Boolean> boneVisibility = new HashMap<>();
   public Map<String, Integer> boneColorOverrides = new HashMap<>();
   public Map<String, Identifier> boneTextureOverrides = new HashMap<>();
   public Map<String, Identifier> boneTextureOverridesLayer2 = new HashMap<>();
   public Map<String, Identifier> boneTextureOverridesLayer3 = new HashMap<>();
   public Map<String, Vec3d> boneSizeOverrides = new HashMap<>();
   public Map<String, Vec3d> bonePositionOffset = new HashMap<>();
   public Map<String, Vec2f> boneUVOffsets = new HashMap<>();
   public final Map<EquipmentSlot, Boolean> armorVisibility = new EnumMap<>(EquipmentSlot.class);
   public Vec3d previousVelocity = Vec3d.ZERO;
   private static final int SPRINTING_FLAG_INDEX = 3;
   private static final int MAX_TICKS_NO_HIT = 400;
   private static final Identifier SPRINTING_SPEED_MODIFIER_ID = Identifier.of("heartbound", "sprinting_speed_modifier");
   private static final EntityAttributeModifier SPRINTING_SPEED_BOOST = new EntityAttributeModifier(
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
   private PlayerEntity lookAtTarget;
   private int autoEatCooldown = 0;
   private static final int AUTO_EAT_INTERVAL = 40;
   private int goldenAppleCooldown = 0;
   private int enchantedAppleCooldown = 0;
   private static final int GOLDEN_APPLE_COOLDOWN = 100;
   private static final int ENCHANTED_APPLE_COOLDOWN = 100;
   private final Set<BlockPos> openedFenceGates = new HashSet<>();
   private int fenceGateCheckCooldown = 0;

   protected GirlEntity(EntityType<? extends GirlEntity> entityType, World world) {
      super(entityType, world);
      if (this.getNavigation() instanceof MobNavigation mobNav) {
         mobNav.setCanPathThroughDoors(true);
      }
   }

   @Override
   protected EntityNavigation createNavigation(World world) {
      return new GirlNavigation(this, world);
   }

   @Override
   protected void initDataTracker(Builder builder) {
      super.initDataTracker(builder);
      builder.add(WAITING_AT_BED, false);
      builder.add(IS_TEMPORARY, false);
      builder.add(CREATED_CLONE, false);
      builder.add(LOCKED_STATE, false);
      builder.add(FROZEN_STATE, false);
      builder.add(WAITING_FOR_PLAYER, false);
      builder.add(STRIPPED, false);
      builder.add(FOLLOWING, false);
      builder.add(ROAMING, false);
      builder.add(CHOPPING, false);
      builder.add(MINING, false);
      builder.add(IN_SCENE, false);
      builder.add(OVERRIDE_LOOP, false);
      builder.add(OVERRIDE_HOLD, false);
      builder.add(OVERRIDE_ANIM_PLAYING, false);
      builder.add(PLAYER_MODEL_SLIM, false);
      builder.add(HAVING_SEX, false);
      builder.add(SITTING, false);
      builder.add(WALKING_BACKWARD, false);
      builder.add(RELATIONSHIP_LEVEL, 0);
      builder.add(MAX_RELATIONSHIP_LEVEL, 4);
      builder.add(BREAST_SIZE, 100);
      builder.add(BREAST_OFFSET, Vec3d.ZERO);
      builder.add(PASSENGER_BONE_POSITION, Vec3d.ZERO);
      builder.add(BASE_POS, this.getBlockPos());
      builder.add(OVERRIDE_ANIM, "");
      builder.add(SCENE_ANIM, "");
      builder.add(CONSUMING_STACK, Items.COOKED_BEEF.getDefaultStack());
   }

   public void setFollowing(boolean follow) {
      this.dataTracker.set(FOLLOWING, follow);
   }

   public boolean isFollowing() {
      return this.dataTracker.get(FOLLOWING);
   }

   public void setRoaming(boolean roaming) {
      this.dataTracker.set(ROAMING, roaming);
   }

   public boolean isRoaming() {
      return this.dataTracker.get(ROAMING);
   }

   public void setChopping(boolean chopping) {
      this.dataTracker.set(CHOPPING, chopping);
   }

   public boolean isChopping() {
      return this.dataTracker.get(CHOPPING);
   }

   public void setMining(boolean mining) {
      this.dataTracker.set(MINING, mining);
   }

   public boolean isMining() {
      return this.dataTracker.get(MINING);
   }

   public void setStripped(boolean stripped) {
      this.dataTracker.set(STRIPPED, stripped);
   }

   public boolean isStripped() {
      return this.dataTracker.get(STRIPPED);
   }

   public void setFreeze(boolean locked) {
      this.dataTracker.set(FROZEN_STATE, locked);
   }

   public boolean isFrozenInPlace() {
      return this.dataTracker.get(FROZEN_STATE);
   }

   public void setMovementLockedState(boolean locked) {
      this.dataTracker.set(LOCKED_STATE, locked);
   }

   public boolean isMovementLocked() {
      return this.dataTracker.get(LOCKED_STATE);
   }

   public void setGUIOpenState(boolean state, @Nullable PlayerEntity lookAt) {
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
      this.dataTracker.set(IN_SCENE, inScene);
   }

   public boolean isSceneActive() {
      return this.dataTracker.get(IN_SCENE);
   }

   public void setOverrideAnim(String anim) {
      this.dataTracker.set(OVERRIDE_ANIM, anim);
   }

   public String getOverrideAnim() {
      return this.dataTracker.get(OVERRIDE_ANIM);
   }

   public void setOverrideLoop(boolean loop) {
      this.dataTracker.set(OVERRIDE_LOOP, loop);
   }

   public boolean getOverrideLoopState() {
      return this.dataTracker.get(OVERRIDE_LOOP);
   }

   public void setOverrideHold(boolean hold) {
      this.dataTracker.set(OVERRIDE_HOLD, hold);
   }

   public boolean getOverrideHoldState() {
      return this.dataTracker.get(OVERRIDE_HOLD);
   }

   public boolean isWaitingAtBed() {
      return this.dataTracker.get(WAITING_AT_BED);
   }

   public void setWaitingAtBedState(boolean state) {
      this.dataTracker.set(WAITING_AT_BED, state);
   }

   public boolean isTemporary() {
      return this.dataTracker.get(IS_TEMPORARY);
   }

   public void setTemporaryState(boolean state) {
      this.dataTracker.set(IS_TEMPORARY, state);
   }

   public boolean createdClone() {
      return this.dataTracker.get(CREATED_CLONE);
   }

   public void setCreatedCloneState(boolean state) {
      this.dataTracker.set(CREATED_CLONE, state);
   }

   public boolean isWaitingForPlayer() {
      return this.dataTracker.get(WAITING_FOR_PLAYER);
   }

   public void setWaitingForPlayerState(boolean state) {
      this.dataTracker.set(WAITING_FOR_PLAYER, state);
   }

   public void setIsPlayerModelSlim(boolean isSlim) {
      this.dataTracker.set(PLAYER_MODEL_SLIM, isSlim);
   }

   public boolean isPlayerModelSlim() {
      return this.dataTracker.get(PLAYER_MODEL_SLIM);
   }

   public void setHavingSex(boolean state) {
      this.dataTracker.set(HAVING_SEX, state);
   }

   public boolean isHavingSex() {
      return this.dataTracker.get(HAVING_SEX);
   }

   public int getCurrentRelationshipLevel() {
      return this.dataTracker.get(RELATIONSHIP_LEVEL);
   }

   public void setCurrentRelationshipLevel(int value) {
      this.dataTracker.set(RELATIONSHIP_LEVEL, value);
   }

   public void setPassengerBonePosition(Vec3d position) {
      this.dataTracker.set(PASSENGER_BONE_POSITION, position);
   }

   public Vec3d getPassengerBonePosition() {
      return this.dataTracker.get(PASSENGER_BONE_POSITION);
   }

   public void setBasePos(BlockPos block) {
      this.dataTracker.set(BASE_POS, block);
   }

   public BlockPos getBasePos() {
      return this.dataTracker.get(BASE_POS);
   }

   public void setBreastSize(int value) {
      this.dataTracker.set(BREAST_SIZE, value);
   }

   public int getBreastSize() {
      return this.dataTracker.get(BREAST_SIZE);
   }

   public void setBreastOffset(Vec3d value) {
      this.dataTracker.set(BREAST_OFFSET, value);
   }

   public Vec3d getBreastOffset() {
      return this.dataTracker.get(BREAST_OFFSET);
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
      this.dataTracker.set(WALKING_BACKWARD, backward);
   }

   public boolean isWalkingBackward() {
      return this.dataTracker.get(WALKING_BACKWARD);
   }

   public int maxRelationshipLevel() {
      if (!this.getWorld().isClient()) {
         try {
            List<Scene> options = this.getScenes();
            int value = options != null && !options.isEmpty() ? options.stream().map(Scene::requiredRelationshipLevel).max(Integer::compareTo).orElse(4) : 4;
            this.dataTracker.set(MAX_RELATIONSHIP_LEVEL, value);
         } catch (Exception var3) {
            this.dataTracker.set(MAX_RELATIONSHIP_LEVEL, 4);
         }
      }

      return this.dataTracker.get(MAX_RELATIONSHIP_LEVEL);
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
      } else if (stack.isOf(Items.ROTTEN_FLESH)) {
         return false;
      } else if (stack.isOf(Items.POISONOUS_POTATO) || stack.isOf(Items.SPIDER_EYE) || stack.isOf(Items.CHORUS_FRUIT) || stack.isOf(Items.POPPED_CHORUS_FRUIT)) {
         return false;
      } else if (stack.isOf(Items.BEEF) || stack.isOf(Items.PORKCHOP) || stack.isOf(Items.CHICKEN) || stack.isOf(Items.MUTTON) || stack.isOf(Items.RABBIT)) {
         return false;
      } else if (stack.isOf(Items.COD) || stack.isOf(Items.SALMON) || stack.isOf(Items.TROPICAL_FISH) || stack.isOf(Items.PUFFERFISH)) {
         return false;
      } else if (stack.isIn(ItemTags.WOLF_FOOD)) {
         return true;
      } else {
         FoodComponent foodComponent = stack.get(DataComponentTypes.FOOD);
         return foodComponent != null;
      }
   }

   @Override
   public boolean shouldRenderName() {
      this.setCustomName(Text.of(this.getGirlDisplayName()));
      this.setCustomNameVisible(true);
      return true;
   }

   public void setBasePosHere() {
      this.setBasePos(this.getBlockPos());
   }

   public void teleportToBase() {
      this.teleportToBase(true);
   }

   public void teleportToBase(boolean shouldSit) {
      if (shouldSit) {
         this.setSitting(true);
      }

      BlockPos base = this.getBasePos();
      this.requestTeleport((double)base.getX() + 0.5, (double)base.getY(), (double)base.getZ() + 0.5);
      this.getNavigation().stop();
   }

   @Override
   public ItemStack getEquippedStack(EquipmentSlot slot) {
      return this.inventory.getEquipmentStack(slot);
   }

   @Override
   public void equipStack(EquipmentSlot slot, ItemStack stack) {
      this.inventory.setEquipmentStack(slot, stack);
   }

   @Override
   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      WrapperLookup registryLookup = this.getWorld().getRegistryManager();
      Inventories.writeNbt(nbt, this.inventory.getItems(), registryLookup);
      nbt.putBoolean("SitSate", this.isSitting());
      nbt.putBoolean("StripState", this.isStripped());
      nbt.putBoolean("FollowState", this.isFollowing());
      nbt.putBoolean("RoamingState", this.isRoaming());
      nbt.putBoolean("ChoppingState", this.isChopping());
      nbt.putBoolean("MiningState", this.isMining());
      nbt.putInt("RelationshipLevel", this.getCurrentRelationshipLevel());
      nbt.putInt("BreastSize", this.getBreastSize());
      Vec3d.CODEC.encodeStart(NbtOps.INSTANCE, this.getBreastOffset()).result().ifPresent(element -> nbt.put("BreastOffset", element));
      BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, this.getBasePos()).result().ifPresent(element -> nbt.put("BasePos", element));
      nbt.putBoolean("Sitting", this.isSitting());
   }

   @Override
   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      World w = this.getWorld();
      if (w != null) {
         WrapperLookup registryLookup = w.getRegistryManager();
         Inventories.readNbt(nbt, this.inventory.getItems(), registryLookup);
      }

      boolean sitting = nbt.getBoolean("Sitting");
      this.setSitting(sitting);
      boolean following = nbt.getBoolean("FollowState");
      this.setFollowing(following);
      boolean roaming = nbt.getBoolean("RoamingState");
      this.setRoaming(roaming);
      boolean chopping = nbt.getBoolean("ChoppingState");
      this.setChopping(chopping);
      boolean mining = nbt.getBoolean("MiningState");
      this.setMining(mining);
      boolean stripped = nbt.getBoolean("StripState");
      this.setStripped(stripped);
      int relationship = nbt.getInt("RelationshipLevel");
      this.setCurrentRelationshipLevel(relationship);
      this.setBasePos(BlockPos.CODEC.parse(NbtOps.INSTANCE, nbt.get("BasePos")).result().orElse(new BlockPos(0, 0, 0)));
      this.setBreastOffset(Vec3d.CODEC.parse(NbtOps.INSTANCE, nbt.get("BreastOffset")).result().orElse(Vec3d.ZERO));
      this.setBreastSize(nbt.getInt("BreastSize"));
   }

   public boolean canAttackWithOwner(LivingEntity target, LivingEntity owner) {
      return true;
   }

   public boolean isSitting() {
      return this.dataTracker.get(SITTING);
   }

   public void setSitting(boolean sitting) {
      this.setTarget(null);
      this.dataTracker.set(SITTING, sitting);
      if (sitting) {
         try {
            this.setRoaming(false);
         } catch (Throwable var3) {
         }
      }
   }

   @Override
   public void onDeath(DamageSource damageSource) {
      if (damageSource.getAttacker() instanceof ServerPlayerEntity serverPlayer) {
         HeartboundCriteria.KILL_GIRL.trigger(serverPlayer);
      }

      this.removeAllPassengers();
      super.onDeath(damageSource);
   }

   @Override
   public void tick() {
      super.tick();
      this.previousYaw = this.getYaw();
      this.previousVelocity = this.getVelocity();
      this.setMovementLockedState(this.isFrozenInPlace() || this.isWaitingAtBed() || this.isSceneActive() || this.isWaitingForPlayer());
      if (!this.getWorld().isClient) {
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
         if (this.squaredDistanceTo((double)gatePos.getX() + 0.5, (double)gatePos.getY(), (double)gatePos.getZ() + 0.5) > 9.0) {
            BlockState statex = this.getWorld().getBlockState(gatePos);
            if (statex.getBlock() instanceof FenceGateBlock && statex.get(FenceGateBlock.OPEN)) {
               this.getWorld().setBlockState(gatePos, statex.with(FenceGateBlock.OPEN, Boolean.valueOf(false)), 10);
               this.getWorld().playSound(null, gatePos, SoundEvents.BLOCK_FENCE_GATE_CLOSE, SoundCategory.BLOCKS, 1.0F, 1.0F);
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

         if (this.getNavigation().isIdle()) {
            return;
         }

         this.fenceGateCheckCooldown = 5;
      }

      BlockPos entityPos = this.getBlockPos();
      boolean openedAny = false;

      for (int dx = -1; dx <= 1; dx++) {
         for (int dz = -1; dz <= 1; dz++) {
            for (int dy = 0; dy <= 1; dy++) {
               BlockPos pos = entityPos.add(dx, dy, dz);
               BlockState state = this.getWorld().getBlockState(pos);
               if (state.getBlock() instanceof FenceGateBlock && !state.get(FenceGateBlock.OPEN)) {
                  this.getWorld().setBlockState(pos, state.with(FenceGateBlock.OPEN, Boolean.valueOf(true)), 10);
                  this.getWorld().playSound(null, pos, SoundEvents.BLOCK_FENCE_GATE_OPEN, SoundCategory.BLOCKS, 1.0F, 1.0F);
                  this.openedFenceGates.add(pos.toImmutable());
                  openedAny = true;
               }
            }
         }
      }

      if (openedAny && colliding) {
         this.getNavigation().recalculatePath();
      }
   }

   @Override
   public void setSprinting(boolean sprinting) {
      this.setFlag(3, sprinting);
      EntityAttributeInstance entityAttributeInstance = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
      entityAttributeInstance.removeModifier(SPRINTING_SPEED_BOOST.id());
      if (sprinting) {
         entityAttributeInstance.addTemporaryModifier(SPRINTING_SPEED_BOOST);
      }
   }

   protected void eat(PlayerEntity player, Hand hand, ItemStack stack) {
      ItemStack particleStack = stack.copy();
      particleStack.setCount(1);
      if (!player.isCreative()) {
         stack.decrement(1);
      }

      this.playSound(SoundEvents.ENTITY_GENERIC_EAT);
      this.dataTracker.set(CONSUMING_STACK, particleStack);
      this.getWorld().sendEntityStatus(this, (byte)74);
      if (stack.getItem().hasRecipeRemainder()) {
         ItemStack remainder = new ItemStack(stack.getItem().getRecipeRemainder());
         if (!player.getInventory().insertStack(remainder)) {
            player.dropItem(remainder, false);
         }
      }
   }

   public Vec3d getPassengerPos() {
      boolean isZero = this.getPassengerBonePosition().isInRange(Vec3d.ZERO, 0.1);
      return !isZero && this.isHavingSex()
         ? this.getPos().add(this.getPassengerBonePosition()).add(0.0, (double)this.passengerYOffset, 0.0)
         : this.getPos().add(0.0, 1.0, 0.0);
   }

   @Override
   public Vec3d updatePassengerForDismount(LivingEntity passenger) {
      return this.getPassengerPos();
   }

   @Override
   public Vec3d getPassengerRidingPos(Entity passenger) {
      return this.getPassengerPos();
   }

   @Override
   public boolean canImmediatelyDespawn(double distanceSquared) {
      return false;
   }

   @Override
   protected void dropInventory() {
      super.dropInventory();
      if (this.getWorld() instanceof ServerWorld serverWorld && !this.isRuleEnabled(serverWorld, GameRules.KEEP_INVENTORY)) {
         for (ItemStack stack : this.getInventory().getItems()) {
            if (!stack.isEmpty()) {
               this.dropStack(stack);
            }
         }

         this.getInventory().clear();
      }
   }

   public boolean isRuleEnabled(ServerWorld world, Key<BooleanRule> rule) {
      return world.getGameRules().getBoolean(rule);
   }

   @Override
   public void pushAwayFrom(Entity entity) {
      if (!this.isMovementLocked()) {
         super.pushAwayFrom(entity);
      }
   }

   @Override
   public void takeKnockback(double strength, double x, double z) {
      if (!this.isMovementLocked()) {
         super.takeKnockback(strength, x, z);
      } else {
         this.setVelocity(Vec3d.ZERO);
      }
   }

   @Override
   public boolean isPushable() {
      return !this.isMovementLocked();
   }

   @Override
   public void addVelocity(double deltaX, double deltaY, double deltaZ) {
      if (!this.isMovementLocked()) {
         super.addVelocity(deltaX, deltaY, deltaZ);
      }
   }

   @Override
   public void stopMovement() {
      super.stopMovement();
      this.setVelocity(0.0, this.getVelocity().y > 0.0 ? 0.0 : this.getVelocity().y, 0.0);
      this.setJumping(false);
      this.bodyYaw = this.getBodyYaw();
      MoveControl control = this.getMoveControl();
      if (control != null) {
         control.moveTo(this.getX(), this.getY(), this.getZ(), 0.0);
      }
   }

   public static net.minecraft.entity.attribute.DefaultAttributeContainer.Builder createDefaultAttributes() {
      return MobEntity.createMobAttributes()
         .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
         .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2)
         .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 100.0)
         .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0);
   }

   @Override
   public void shootAt(LivingEntity target, float pullProgress) {
      ItemStack itemStack = this.getStackInHand(ProjectileUtil.getHandPossiblyHolding(this, Items.BOW));
      ItemStack itemStack2 = this.getProjectileType(itemStack);
      PersistentProjectileEntity arrow = this.createArrowProjectile(itemStack2, pullProgress, itemStack);
      float arrowSpeed = 1.6F;
      double arrowStartY = arrow.getY();
      Vec3d targetVel = target.getVelocity();
      double dx = target.getX() - this.getX();
      double dz = target.getZ() - this.getZ();
      double travelTime = Math.sqrt(dx * dx + dz * dz) / (double)arrowSpeed;
      double aimX = dx + targetVel.x * travelTime;
      double aimZ = dz + targetVel.z * travelTime;
      double targetAbsY = target.getY() + (double)target.getHeight() / 2.0 + targetVel.y * travelTime;
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

      arrow.setVelocity(aimX, dy, aimZ, arrowSpeed, 0.0F);
      if (this.getWorld() instanceof ServerWorld serverWorld) {
         serverWorld.spawnEntity(arrow);
      }

      this.playSound(SoundEvents.ENTITY_ARROW_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
   }

   protected PersistentProjectileEntity createArrowProjectile(ItemStack arrow, float damageModifier, @Nullable ItemStack shotFrom) {
      if (arrow == null || arrow.isEmpty()) {
         arrow = Items.ARROW.getDefaultStack();
      }

      ArrowEntity arrowEntity = new ArrowEntity(this.getWorld(), this, arrow.copy(), shotFrom);
      arrowEntity.setDamage(arrowEntity.getDamage() * (double)damageModifier);
      return arrowEntity;
   }

   @Override
   public void handleStatus(byte status) {
      if (status == 71) {
         this.spawnParticles(ParticleTypes.HEART);
      } else if (status == 70) {
         this.spawnParticles(ParticleTypes.SMOKE);
      } else if (status == 73) {
         this.spawnParticles(ParticleTypes.HAPPY_VILLAGER);
      } else if (status == 72) {
         this.spawnParticles(ParticleTypes.ANGRY_VILLAGER);
      } else if (status == 74) {
         this.spawnItemParticlesCustom(this.dataTracker.get(CONSUMING_STACK), 16);
      } else {
         super.handleStatus(status);
      }
   }

   protected void spawnItemParticlesCustom(ItemStack stack, int count) {
      if (stack != null && !stack.isEmpty()) {
         for (int i = 0; i < count; i++) {
            Vec3d velocity = new Vec3d(((double)this.random.nextFloat() - 0.5) * 0.1, Math.random() * 0.1 + 0.1, 0.0)
               .rotateX(-this.getPitch() * (float) (Math.PI / 180.0))
               .rotateY(-this.getYaw() * (float) (Math.PI / 180.0));
            this.getWorld()
               .addParticle(
                  new ItemStackParticleEffect(ParticleTypes.ITEM, stack),
                  this.getX() + this.getRotationVector().x / 2.0,
                  this.getY() + (double)this.getEyeHeight(this.getPose()) - 0.2,
                  this.getZ() + this.getRotationVector().z / 2.0,
                  velocity.x,
                  velocity.y + 0.05,
                  velocity.z
               );
         }
      }
   }

   protected void tryAutoEat() {
      if (!this.getWorld().isClient) {
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
         ItemStack s = this.inventory.getStack(i);
         if (s != null && !s.isEmpty() && !s.isOf(Items.ROTTEN_FLESH) && !s.isOf(Items.PUFFERFISH)) {
            if (enchantedIdx == -1 && s.isOf(Items.ENCHANTED_GOLDEN_APPLE)) {
               enchantedIdx = i;
            } else if (goldenIdx == -1 && s.isOf(Items.GOLDEN_APPLE)) {
               goldenIdx = i;
            } else if (anyIdx == -1 && this.isFoodItem(s) && !s.isOf(Items.GOLDEN_APPLE) && !s.isOf(Items.ENCHANTED_GOLDEN_APPLE)) {
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
      ItemStack used = this.inventory.removeStack(useIndex, 1);
      if (used != null && !used.isEmpty()) {
         ItemStack particleStack = used.copy();
         particleStack.setCount(1);
         ItemStack remainder = used.getItem().finishUsing(used, this.getWorld(), this);
         if (used.isOf(Items.GOLDEN_APPLE)) {
            this.goldenAppleCooldown = 100;
         } else if (used.isOf(Items.ENCHANTED_GOLDEN_APPLE)) {
            this.enchantedAppleCooldown = 100;
         }

         this.insertOrDropRemainder(remainder);
         float healAmount = this.computeHealAmount(particleStack);
         if (healAmount > 0.0F) {
            this.heal(healAmount);
         }

         this.playSound(SoundEvents.ENTITY_GENERIC_EAT);
         this.dataTracker.set(CONSUMING_STACK, particleStack);
         this.getWorld().sendEntityStatus(this, (byte)74);
      }
   }

   private void insertOrDropRemainder(ItemStack remainder) {
      if (remainder != null && !remainder.isEmpty()) {
         boolean inserted = false;

         for (int i = 5; i <= 28; i++) {
            ItemStack slot = this.inventory.getStack(i);
            if (slot.isEmpty()) {
               this.inventory.setStack(i, remainder);
               inserted = true;
               break;
            }
         }

         if (!inserted) {
            this.dropStack(remainder);
         }
      }
   }

   private float computeHealAmount(ItemStack particleStack) {
      FoodComponent food = particleStack.get(DataComponentTypes.FOOD);
      if (food != null) {
         return 2.0F * (float)food.nutrition();
      } else if (particleStack.isOf(Items.GOLDEN_APPLE)) {
         return 8.0F;
      } else {
         return particleStack.isOf(Items.ENCHANTED_GOLDEN_APPLE) ? 10.0F : 0.0F;
      }
   }

   protected void spawnParticles(ParticleEffect parameters) {
      for (int i = 0; i < 5; i++) {
         double d = this.random.nextGaussian() * 0.02;
         double e = this.random.nextGaussian() * 0.02;
         double f = this.random.nextGaussian() * 0.02;
         this.getWorld().addParticle(parameters, this.getParticleX(1.0), this.getRandomBodyY() + 0.5, this.getParticleZ(1.0), d, e, f);
      }
   }

   @Override
   public void tickMovement() {
      super.tickMovement();
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
            this.getLookControl().lookAt(this.lookAtTarget, (float)(this.getMaxHeadRotation() + 20), (float)this.getMaxLookPitchChange());
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
   public boolean tryAttack(Entity target) {
      boolean success = super.tryAttack(target);
      if (success && target == this.attackTarget) {
         this.ticksSinceLastHit = 0;
      }

      return success;
   }

   public GirlEntity createTempClone() {
      if (this.getWorld().isClient()) {
         return null;
      } else {
         GirlEntity clone = (GirlEntity)this.getType().create(this.getWorld());
         clone.setTemporaryState(true);
         clone.setPosition(this.getX(), 800.0, this.getZ());
         clone.setInvisible(true);
         clone.setInvulnerable(true);
         clone.setNoGravity(true);
         this.onTempCloneCreation(clone);
         this.getWorld().spawnEntity(clone);
         this.setCreatedCloneState(true);
         return clone;
      }
   }

   public void onTempCloneCreation(GirlEntity clone) {
      clone.setStripped(this.isStripped());
   }

   @Override
   public float getSoundPitch() {
      return 1.0F;
   }

   @Override
   public boolean isPersistent() {
      return true;
   }
}
