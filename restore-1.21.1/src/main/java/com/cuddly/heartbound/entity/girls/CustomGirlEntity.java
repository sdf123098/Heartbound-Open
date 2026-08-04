package com.cuddly.heartbound.entity.girls;

import com.cuddly.heartbound.entity.base.GirlEntity;
import com.cuddly.heartbound.entity.base.tamable.BaseGirlEntityAI;
import com.cuddly.heartbound.util.json.CustomGirlLoader;
import com.cuddly.heartbound.util.variables.CustomGirlProfile;
import com.cuddly.heartbound.util.variables.Scene;
import java.util.List;
import java.util.Objects;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.data.DataTracker.Builder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class CustomGirlEntity extends BaseGirlEntityAI {
   private CustomGirlProfile profile = CustomGirlProfile.DEFAULT;
   private float lastHitboxHeight = 1.0F;
   private static final TrackedData<String> GIRL_ID = DataTracker.registerData(CustomGirlEntity.class, TrackedDataHandlerRegistry.STRING);
   private static final TrackedData<String> GIRL_NAME = DataTracker.registerData(CustomGirlEntity.class, TrackedDataHandlerRegistry.STRING);
   private static final TrackedData<Float> HITBOX_HEIGHT = DataTracker.registerData(CustomGirlEntity.class, TrackedDataHandlerRegistry.FLOAT);
   private static final TrackedData<Boolean> IS_PROFILE_PERMANENT = DataTracker.registerData(CustomGirlEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

   public CustomGirlEntity(EntityType<? extends BaseGirlEntityAI> type, World world) {
      super(type, world);
   }

   @Override
   protected void initDataTracker(Builder builder) {
      super.initDataTracker(builder);
      builder.add(GIRL_ID, "default");
      builder.add(GIRL_NAME, "Default Girl");
      builder.add(HITBOX_HEIGHT, 1.95F);
      builder.add(IS_PROFILE_PERMANENT, false);
   }

   public void setProfile(CustomGirlProfile profile, boolean isPermanent) {
      if (profile == null) {
         profile = CustomGirlProfile.DEFAULT;
      }

      this.profile = profile;
      Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).setBaseValue(profile.maxHealth());
      Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)).setBaseValue(profile.movementSpeed());
      Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)).setBaseValue(profile.attackDamage());
      this.setHealth((float)profile.maxHealth());
      this.dataTracker.set(GIRL_ID, profile.id());
      this.dataTracker.set(GIRL_NAME, profile.name());
      this.dataTracker.set(HITBOX_HEIGHT, profile.hitboxHeight());
      this.calculateDimensions();
      this.dataTracker.set(IS_PROFILE_PERMANENT, isPermanent);
   }

   public CustomGirlProfile getProfile() {
      return this.profile != null ? this.profile : CustomGirlProfile.DEFAULT;
   }

   @Override
   public String getGirlID() {
      return this.dataTracker.get(GIRL_ID);
   }

   @Override
   public String getGirlDisplayName() {
      return this.dataTracker.get(GIRL_NAME);
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
      return this.dataTracker.get(HITBOX_HEIGHT);
   }

   @Override
   public float getWeaponBoneXRotation() {
      return this.getProfile().weaponBoneRotation();
   }

   @Override
   protected EntityDimensions getBaseDimensions(EntityPose pose) {
      return EntityDimensions.changing(0.5F, this.getHitBoxHeight());
   }

   @Override
   public void onTrackedDataSet(TrackedData<?> data) {
      super.onTrackedDataSet(data);
      if (data.equals(HITBOX_HEIGHT)) {
         this.calculateDimensions();
      }
   }

   @Override
   public void tick() {
      if (!this.getWorld().isClient()) {
         this.dataTracker.set(GIRL_ID, this.getProfile().id());
         this.dataTracker.set(GIRL_NAME, this.getProfile().name());
         this.maxRelationshipLevel();
         float currentHeight = this.getProfile().hitboxHeight();
         if (currentHeight != this.lastHitboxHeight) {
            this.lastHitboxHeight = currentHeight;
            this.dataTracker.set(HITBOX_HEIGHT, currentHeight);
            this.calculateDimensions();
         }
      }

      super.tick();
   }

   @Override
   public ActionResult interactMob(PlayerEntity player, Hand hand) {
      if (!this.getWorld().isClient() && player.isSneaking()) {
         ItemStack itemStack = player.getStackInHand(Hand.MAIN_HAND);
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

      return super.interactMob(player, hand);
   }

   private ActionResult trySwitchingProfiles(PlayerEntity player) {
      if (this.dataTracker.get(IS_PROFILE_PERMANENT)) {
         return ActionResult.FAIL;
      } else {
         ItemStack itemStack = player.getStackInHand(Hand.MAIN_HAND);
         Item itemInHand = itemStack.getItem();
         CustomGirlProfile profile = CustomGirlLoader.checkItem(itemInHand);
         if (profile != null) {
            if (this.getProfile().equals(profile)) {
               return ActionResult.FAIL;
            } else {
               this.setProfile(profile, false);
               player.sendMessage(Text.literal("§dSwitched girl profile → §b" + profile.id()), true);
               return ActionResult.SUCCESS;
            }
         } else {
            return ActionResult.FAIL;
         }
      }
   }

   @Override
   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putString("GirlProfileID", this.profile.id());
      nbt.putBoolean("IsPermanent", this.dataTracker.get(IS_PROFILE_PERMANENT));
   }

   @Override
   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      String id = nbt.contains("GirlProfileID") ? nbt.getString("GirlProfileID") : "default_girl";
      CustomGirlProfile p = CustomGirlLoader.LOADED_PROFILES.get(id);
      this.profile = p != null ? p : CustomGirlProfile.DEFAULT;
      this.dataTracker.set(GIRL_ID, this.profile.id());
      this.dataTracker.set(GIRL_NAME, this.profile.name());
      this.dataTracker.set(HITBOX_HEIGHT, this.profile.hitboxHeight());
      this.calculateDimensions();
      boolean isPer = nbt.getBoolean("IsPermanent");
      this.dataTracker.set(IS_PROFILE_PERMANENT, isPer);
   }

   @Override
   public void onTempCloneCreation(GirlEntity clone) {
      super.onTempCloneCreation(clone);
      CustomGirlEntity girl = (CustomGirlEntity)clone;
      girl.setProfile(this.getProfile(), false);
   }
}
