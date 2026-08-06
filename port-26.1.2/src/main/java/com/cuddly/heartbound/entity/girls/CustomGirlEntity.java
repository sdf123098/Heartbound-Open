package com.cuddly.heartbound.entity.girls;

import com.cuddly.heartbound.entity.base.GirlEntity;
import com.cuddly.heartbound.entity.base.tamable.BaseGirlEntityAI;
import com.cuddly.heartbound.util.json.CustomGirlLoader;
import com.cuddly.heartbound.util.variables.CustomGirlProfile;
import com.cuddly.heartbound.util.variables.Scene;
import java.util.List;
import java.util.Objects;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.Level;

public class CustomGirlEntity extends BaseGirlEntityAI {
   private CustomGirlProfile profile = CustomGirlProfile.DEFAULT;
   private float lastHitboxHeight = 1.0F;
   private static final EntityDataAccessor<String> GIRL_ID = SynchedEntityData.defineId(CustomGirlEntity.class, EntityDataSerializers.STRING);
   private static final EntityDataAccessor<String> GIRL_NAME = SynchedEntityData.defineId(CustomGirlEntity.class, EntityDataSerializers.STRING);
   private static final EntityDataAccessor<Float> HITBOX_HEIGHT = SynchedEntityData.defineId(CustomGirlEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Boolean> IS_PROFILE_PERMANENT = SynchedEntityData.defineId(CustomGirlEntity.class, EntityDataSerializers.BOOLEAN);

   public CustomGirlEntity(EntityType<? extends BaseGirlEntityAI> type, Level world) {
      super(type, world);
   }

   @Override
   protected void defineSynchedData(Builder builder) {
      super.defineSynchedData(builder);
      builder.define(GIRL_ID, "default");
      builder.define(GIRL_NAME, "Default Girl");
      builder.define(HITBOX_HEIGHT, 1.95F);
      builder.define(IS_PROFILE_PERMANENT, false);
   }

   public void setProfile(CustomGirlProfile profile, boolean isPermanent) {
      if (profile == null) {
         profile = CustomGirlProfile.DEFAULT;
      }

      this.profile = profile;
      Objects.requireNonNull(this.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(profile.maxHealth());
      Objects.requireNonNull(this.getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(profile.movementSpeed());
      Objects.requireNonNull(this.getAttribute(Attributes.ATTACK_DAMAGE)).setBaseValue(profile.attackDamage());
      this.setHealth((float)profile.maxHealth());
      this.entityData.set(GIRL_ID, profile.id());
      this.entityData.set(GIRL_NAME, profile.name());
      this.entityData.set(HITBOX_HEIGHT, profile.hitboxHeight());
      this.refreshDimensions();
      this.entityData.set(IS_PROFILE_PERMANENT, isPermanent);
   }

   public CustomGirlProfile getProfile() {
      return this.profile != null ? this.profile : CustomGirlProfile.DEFAULT;
   }

   @Override
   public String getGirlID() {
      return this.entityData.get(GIRL_ID);
   }

   @Override
   public String getGirlDisplayName() {
      return this.entityData.get(GIRL_NAME);
   }

   @Override
   public Item isAttractedTo() {
      return this.getProfile().tameItem();
   }

   @Override
   public List<Scene> getScenes() {
      return this.getProfile().scenes();
   }

   @Override
   public int getSizeGUI() {
      return this.getProfile().guiSize();
   }

   @Override
   public float getYAxisGUI() {
      return this.getProfile().guiYOffset();
   }

   private float getHitBoxHeight() {
      return this.entityData.get(HITBOX_HEIGHT);
   }

   @Override
   public float getWeaponBoneXRotation() {
      return this.getProfile().weaponBoneRotation();
   }

   @Override
   protected EntityDimensions getDefaultDimensions(Pose pose) {
      return EntityDimensions.scalable(0.5F, this.getHitBoxHeight());
   }

   @Override
   public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
      super.onSyncedDataUpdated(data);
      if (data.equals(HITBOX_HEIGHT)) {
         this.refreshDimensions();
      }
   }

   @Override
   public void tick() {
      if (!this.level().isClientSide()) {
         this.entityData.set(GIRL_ID, this.getProfile().id());
         this.entityData.set(GIRL_NAME, this.getProfile().name());
         this.maxRelationshipLevel();
         float currentHeight = this.getProfile().hitboxHeight();
         if (currentHeight != this.lastHitboxHeight) {
            this.lastHitboxHeight = currentHeight;
            this.entityData.set(HITBOX_HEIGHT, currentHeight);
            this.refreshDimensions();
         }
      }

      super.tick();
   }

   @Override
   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      if (!this.level().isClientSide() && player.isShiftKeyDown()) {
         ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
         CustomGirlProfile profile = CustomGirlLoader.checkItem(itemStack.getItem());
         if (profile != null) {
            if (!this.isTamed()) {
               return this.trySwitchingProfiles(player);
            }

            if (this.isOwner(player)) {
               return this.trySwitchingProfiles(player);
            }
         }
      }

      return super.mobInteract(player, hand);
   }

   private InteractionResult trySwitchingProfiles(Player player) {
      if (this.entityData.get(IS_PROFILE_PERMANENT)) {
         return InteractionResult.FAIL;
      } else {
         ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
         Item itemInHand = itemStack.getItem();
         CustomGirlProfile profile = CustomGirlLoader.checkItem(itemInHand);
         if (profile != null) {
            if (this.getProfile().equals(profile)) {
               return InteractionResult.FAIL;
            } else {
               this.setProfile(profile, false);
               player.sendOverlayMessage(Component.literal("§dSwitched girl profile → §b" + profile.id()));
               return InteractionResult.SUCCESS;
            }
         } else {
            return InteractionResult.FAIL;
         }
      }
   }

   @Override
   public void addAdditionalSaveData(ValueOutput nbt) {
      super.addAdditionalSaveData(nbt);
      nbt.putString("GirlProfileID", this.profile.id());
      nbt.putBoolean("IsPermanent", this.entityData.get(IS_PROFILE_PERMANENT));
   }

   @Override
   public void readAdditionalSaveData(ValueInput nbt) {
      super.readAdditionalSaveData(nbt);
      String id = nbt.getString("GirlProfileID").orElse("default_girl");
      CustomGirlProfile p = CustomGirlLoader.LOADED_PROFILES.get(id);
      this.profile = p != null ? p : CustomGirlProfile.DEFAULT;
      this.entityData.set(GIRL_ID, this.profile.id());
      this.entityData.set(GIRL_NAME, this.profile.name());
      this.entityData.set(HITBOX_HEIGHT, this.profile.hitboxHeight());
      this.refreshDimensions();
      boolean isPer = nbt.getBooleanOr("IsPermanent", false);
      this.entityData.set(IS_PROFILE_PERMANENT, isPer);
   }

   @Override
   public void onTempCloneCreation(GirlEntity clone) {
      super.onTempCloneCreation(clone);
      CustomGirlEntity girl = (CustomGirlEntity)clone;
      girl.setProfile(this.getProfile(), false);
   }
}
