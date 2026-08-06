package com.cuddly.heartbound.entity.girls;

import com.cuddly.heartbound.entity.base.GirlEntity;
import com.cuddly.heartbound.entity.base.tamable.BaseGirlEntityAI;
import com.cuddly.heartbound.networking.S2C.OpenKoboldCustomizeScreenS2CPacket;
import com.cuddly.heartbound.util.Colors;
import com.cuddly.heartbound.util.variables.Scene;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.Level;

public class KoboldEntity extends BaseGirlEntityAI {
   private static final EntityDataAccessor<Boolean> LEADER_STATE = SynchedEntityData.defineId(KoboldEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Integer> BODY_SIZE = SynchedEntityData.defineId(KoboldEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> KOBOLD_BREAST_SIZE = SynchedEntityData.defineId(KoboldEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> KOBOLD_HEALTH = SynchedEntityData.defineId(KoboldEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> PRIMARY_COLOR = SynchedEntityData.defineId(KoboldEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> SECONDARY_COLOR = SynchedEntityData.defineId(KoboldEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> IRIS_COLOR = SynchedEntityData.defineId(KoboldEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> TOP_HORN_TYPE = SynchedEntityData.defineId(KoboldEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> BOTTOM_HORN_TYPE = SynchedEntityData.defineId(KoboldEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Float> HITBOX_HEIGHT = SynchedEntityData.defineId(KoboldEntity.class, EntityDataSerializers.FLOAT);
   private static final float MIN_HITBOX_HEIGHT = 1.0F;
   private static final float MAX_HITBOX_HEIGHT = 1.75F;
   private static final int MIN_BODY_SIZE = 65;
   private static final int MAX_BODY_SIZE = 115;
   private static final int MIN_BREAST_SIZE = 60;
   private static final int MAX_BREAST_SIZE = 160;
   private static final int MIN_HEALTH = 4;
   private static final int MAX_HEALTH = 12;
   private float lastHitboxHeight = 1.0F;
   private final List<String> topHornType0 = List.of("hornUL0", "hornUR0");
   private final List<String> topHornType1 = List.of("hornUL1", "hornUR1");
   private final List<String> topHornType2 = List.of("hornUL2", "hornUR2");
   private final List<String> topHornType3 = List.of("hornUL3", "hornUR3");
   private final List<String> topHornType4 = List.of("hornUL4", "hornUR4");
   private final List<String> topHornType5 = List.of("hornUL5", "hornUR5");
   private final List<String> topHornType6 = List.of("hornUL6", "hornUR6");
   private final List<String> topHornType7 = List.of("hornUL7", "hornUR7");
   private final List<String> bottomHornType0 = List.of("hornDL0", "hornDR0");
   private final List<String> bottomHornType1 = List.of("hornDL1", "hornDR1");
   private final List<String> bottomHornType2 = List.of("hornDL2", "hornDR2");
   private boolean customizationApplied = false;

   @Override
   protected Map<EquipmentSlot, List<String>> getArmorBones() {
      Map<EquipmentSlot, List<String>> bones = super.getArmorBones();
      bones.put(
         EquipmentSlot.LEGS,
         List.of("armorHip", "armorPantsLowL", "armorPantsUpL", "armorPantsLowR", "armorPantsUpR", "armorBootyL", "armorBootyR", "armorKneeR", "armorKneeL")
      );
      return bones;
   }

   private List<String> primaryBones() {
      return List.of("armL", "armR", "torsoR", "torsoL", "neck", "hip", "head", "hornDL2", "hornDR2", "hornDL3M", "hornDR3M", "legL", "legR");
   }

   private List<String> secondaryBones() {
      return List.of(
         "frontNeck",
         "layer2",
         "layer",
         "vagina",
         "boobs",
         "innerCheekRL",
         "innerCheekLL",
         "down",
         "down2",
         "down3",
         "down4",
         "down5",
         "hornDL3S",
         "hornDR3S",
         "fuckhole"
      );
   }

   private List<String> irisBones() {
      return List.of("irisL", "irisR");
   }

   private List<String> ignoreBones() {
      List<String> bones = new ArrayList<>(List.of("hornUR", "hornUL", "hornDR", "hornDL", "mouth", "eyes", "dotL", "dotR", "tailpack", "crown", "tounge"));

      for (List<String> boneNames : this.getArmorBones().values()) {
         bones.addAll(boneNames);
      }

      return bones;
   }

   public KoboldEntity(EntityType<? extends BaseGirlEntityAI> entityType, Level world) {
      super(entityType, world);
      this.randomizeAppearance();
   }

   @Override
   protected void defineSynchedData(Builder builder) {
      super.defineSynchedData(builder);
      builder.define(LEADER_STATE, false);
      builder.define(BODY_SIZE, 100);
      builder.define(KOBOLD_BREAST_SIZE, 100);
      builder.define(KOBOLD_HEALTH, 6);
      builder.define(PRIMARY_COLOR, -13159);
      builder.define(SECONDARY_COLOR, -7883);
      builder.define(IRIS_COLOR, -7876885);
      builder.define(TOP_HORN_TYPE, 0);
      builder.define(BOTTOM_HORN_TYPE, 0);
      builder.define(HITBOX_HEIGHT, calculateHitboxHeight(100));
   }

   private static float calculateHitboxHeight(int bodySize) {
      int clampedSize = Math.clamp((long)bodySize, 65, 115);
      float normalizedSize = (float)(clampedSize - 65) / 50.0F;
      return Mth.lerp(normalizedSize, 1.0F, 1.75F);
   }

   public void randomizeAppearance() {
      int randomHealth = RANDOM.nextInt(4, 13);
      this.setKoboldHealth(randomHealth);
      int randomBodySize = RANDOM.nextInt(65, 116);
      this.setBodySize(randomBodySize);
      KoboldEntity.PatternPresets preset = KoboldEntity.PatternPresets.values()[RANDOM.nextInt(KoboldEntity.PatternPresets.values().length)];
      this.setColorPreset(preset);
      Integer irisColor = Colors.ALL_COLORS.get(RANDOM.nextInt(Colors.ALL_COLORS.size()));
      this.setIrisColor(irisColor);
      this.setTopHornType(RANDOM.nextInt(0, 8));
      this.setBottomHornType(RANDOM.nextInt(0, 3));
      this.setKoboldBreastSize(RANDOM.nextInt(60, 161));
   }

   public void setColorPreset(KoboldEntity.PatternPresets preset) {
      this.entityData.set(PRIMARY_COLOR, preset.primary);
      this.entityData.set(SECONDARY_COLOR, preset.secondary);
      this.customizationApplied = false;
   }

   public void setLeaderState(boolean state) {
      this.entityData.set(LEADER_STATE, state);
   }

   public void setKoboldHealth(int num) {
      int clampedSize = Math.clamp((long)num, 4, 12);
      this.entityData.set(KOBOLD_HEALTH, clampedSize);
      this.customizationApplied = false;
   }

   public void setKoboldBreastSize(int size) {
      int clampedSize = Math.clamp((long)size, 60, 160);
      this.entityData.set(KOBOLD_BREAST_SIZE, clampedSize);
      this.customizationApplied = false;
   }

   public void setBodySize(int size) {
      int clampedSize = Math.clamp((long)size, 65, 115);
      this.entityData.set(BODY_SIZE, clampedSize);
      if (!this.level().isClientSide()) {
         float newHeight = calculateHitboxHeight(clampedSize);
         this.entityData.set(HITBOX_HEIGHT, newHeight);
      }

      this.customizationApplied = false;
   }

   public void setPrimaryColor(int color) {
      this.entityData.set(PRIMARY_COLOR, color);
      this.customizationApplied = false;
   }

   public void setSecondaryColor(int color) {
      this.entityData.set(SECONDARY_COLOR, color);
      this.customizationApplied = false;
   }

   public void setIrisColor(int color) {
      this.entityData.set(IRIS_COLOR, color);
      this.customizationApplied = false;
   }

   public void setTopHornType(int type) {
      this.entityData.set(TOP_HORN_TYPE, Math.clamp((long)type, 0, 7));
      this.customizationApplied = false;
   }

   public void setBottomHornType(int type) {
      this.entityData.set(BOTTOM_HORN_TYPE, Math.clamp((long)type, 0, 2));
      this.customizationApplied = false;
   }

   public boolean getLeaderState() {
      return this.entityData.get(LEADER_STATE);
   }

   public int getKoboldHealth() {
      return this.entityData.get(KOBOLD_HEALTH);
   }

   public int getKoboldBreastSize() {
      return this.entityData.get(KOBOLD_BREAST_SIZE);
   }

   public int getBodySize() {
      return this.entityData.get(BODY_SIZE);
   }

   public int getPrimaryColor() {
      return this.entityData.get(PRIMARY_COLOR);
   }

   public int getSecondaryColor() {
      return this.entityData.get(SECONDARY_COLOR);
   }

   public int getIrisColor() {
      return this.entityData.get(IRIS_COLOR);
   }

   public int getTopHornType() {
      return this.entityData.get(TOP_HORN_TYPE);
   }

   public int getBottomHornType() {
      return this.entityData.get(BOTTOM_HORN_TYPE);
   }

   private float getHitBoxHeight() {
      return this.entityData.get(HITBOX_HEIGHT);
   }

   @Override
   protected EntityDimensions getDefaultDimensions(Pose pose) {
      return EntityDimensions.scalable(0.5F, this.getHitBoxHeight());
   }

   private void applyCustomizations() {
      if (this.level().isClientSide()) {
         this.setBoneVisibility(List.of("crown"), this.getLeaderState());
         this.overrideBoneColor(this.primaryBones(), Integer.valueOf(this.getPrimaryColor()));
         this.overrideBoneColor(this.secondaryBones(), Integer.valueOf(this.getSecondaryColor()));
         this.overrideBoneColor(this.irisBones(), Integer.valueOf(this.getIrisColor()));
         this.overrideBoneColor(this.ignoreBones(), Integer.valueOf(-1));
         this.setBoneVisibility(this.topHornType0, false);
         this.setBoneVisibility(this.topHornType1, false);
         this.setBoneVisibility(this.topHornType2, false);
         this.setBoneVisibility(this.topHornType3, false);
         this.setBoneVisibility(this.topHornType4, false);
         this.setBoneVisibility(this.topHornType5, false);
         this.setBoneVisibility(this.topHornType6, false);
         this.setBoneVisibility(this.topHornType7, false);
         this.setBoneVisibility(this.bottomHornType0, false);
         this.setBoneVisibility(this.bottomHornType1, false);
         this.setBoneVisibility(this.bottomHornType2, false);
         switch (this.getTopHornType()) {
            case 0:
               this.setBoneVisibility(this.topHornType0, true);
               break;
            case 1:
               this.setBoneVisibility(this.topHornType1, true);
               break;
            case 2:
               this.setBoneVisibility(this.topHornType2, true);
               break;
            case 3:
               this.setBoneVisibility(this.topHornType3, true);
               break;
            case 4:
               this.setBoneVisibility(this.topHornType4, true);
               break;
            case 5:
               this.setBoneVisibility(this.topHornType5, true);
               break;
            case 6:
               this.setBoneVisibility(this.topHornType6, true);
               break;
            case 7:
               this.setBoneVisibility(this.topHornType7, true);
         }

         switch (this.getBottomHornType()) {
            case 0:
               this.setBoneVisibility(this.bottomHornType0, true);
               break;
            case 1:
               this.setBoneVisibility(this.bottomHornType1, true);
               break;
            case 2:
               this.setBoneVisibility(this.bottomHornType2, true);
         }

         Objects.requireNonNull(this.getAttribute(Attributes.MAX_HEALTH)).setBaseValue((double)this.getKoboldHealth());
         this.customizationApplied = true;
      }
   }

   private static float calculateBreastZOffset(int breastSize) {
      int clampedSize = Math.clamp((long)breastSize, 60, 160);
      if (clampedSize <= 100) {
         float normalizedSize = (float)(clampedSize - 60) / 40.0F;
         return Mth.lerp(normalizedSize, -0.875F, 0.0F);
      } else {
         float normalizedSize = (float)(clampedSize - 100) / 60.0F;
         return Mth.lerp(normalizedSize, 0.0F, 1.0F);
      }
   }

   @Override
   public void addAdditionalSaveData(ValueOutput nbt) {
      super.addAdditionalSaveData(nbt);
      nbt.putBoolean("LeaderSate", this.getLeaderState());
      nbt.putInt("KoboldHealth", this.getKoboldHealth());
      nbt.putInt("KoboldBreastSize", this.getKoboldBreastSize());
      nbt.putInt("BodySize", this.getBodySize());
      nbt.putInt("PrimaryColor", this.getPrimaryColor());
      nbt.putInt("SecondaryColor", this.getSecondaryColor());
      nbt.putInt("IrisColor", this.getIrisColor());
      nbt.putInt("TopHornType", this.getTopHornType());
      nbt.putInt("BottomHornType", this.getBottomHornType());
   }

   @Override
   public void readAdditionalSaveData(ValueInput nbt) {
      super.readAdditionalSaveData(nbt);
      int bodySize = nbt.getIntOr("BodySize", 0);
      if (bodySize == 0) {
         bodySize = 100;
      }

      this.entityData.set(LEADER_STATE, nbt.getBooleanOr("LeaderSate", false));
      this.entityData.set(KOBOLD_HEALTH, nbt.getInt("KoboldHealth").orElse(6));
      this.entityData.set(KOBOLD_BREAST_SIZE, nbt.getInt("KoboldBreastSize").orElse(100));
      this.entityData.set(BODY_SIZE, bodySize);
      this.entityData.set(HITBOX_HEIGHT, calculateHitboxHeight(bodySize));
      this.entityData.set(PRIMARY_COLOR, nbt.getInt("PrimaryColor").orElse(-13159));
      this.entityData.set(SECONDARY_COLOR, nbt.getInt("SecondaryColor").orElse(-7883));
      this.entityData.set(IRIS_COLOR, nbt.getInt("IrisColor").orElse(-7876885));
      this.entityData.set(TOP_HORN_TYPE, nbt.getIntOr("TopHornType", 0));
      this.entityData.set(BOTTOM_HORN_TYPE, nbt.getIntOr("BottomHornType", 0));
      this.refreshDimensions();
      this.customizationApplied = false;
   }

   @Override
   public void tick() {
      if (!this.level().isClientSide()) {
         float currentHeight = calculateHitboxHeight(this.getBodySize());
         if (Math.abs(currentHeight - this.lastHitboxHeight) > 0.001F) {
            this.lastHitboxHeight = currentHeight;
            this.entityData.set(HITBOX_HEIGHT, currentHeight);
            this.refreshDimensions();
         }
      }

      if (this.level().isClientSide()) {
         if (!this.customizationApplied) {
            this.applyCustomizations();
         }

         this.setBoneSize("body", this.getBodySize());
         int breastSize = this.getKoboldBreastSize();
         this.setBoneSize("boobs", breastSize, 60, 160);
         float zOffset = calculateBreastZOffset(breastSize);
         this.setBonePos("boobs", 0.0F, 0.0F, zOffset);
      }

      super.tick();
   }

   @Override
   public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
      super.onSyncedDataUpdated(data);
      if (data.equals(PRIMARY_COLOR) || data.equals(SECONDARY_COLOR) || data.equals(IRIS_COLOR) || data.equals(TOP_HORN_TYPE) || data.equals(BOTTOM_HORN_TYPE)) {
         this.customizationApplied = false;
      }

      if (data.equals(HITBOX_HEIGHT)) {
         this.refreshDimensions();
      }
   }

   @Override
   public Item isAttractedTo() {
      return Items.RAW_IRON;
   }

   @Override
   public String getGirlID() {
      return "kobold";
   }

   @Override
   public int getSizeGUI() {
      return 29;
   }

   @Override
   public float getYAxisGUI() {
      return 0.0525F;
   }

   @Override
   public float getWeaponBoneXRotation() {
      return -100.0F;
   }

   @Override
   public boolean hasStripAnim() {
      return false;
   }

   @Override
   public List<Scene> getScenes() {
      return List.of(
         Scene.onPlayer(
            "scene.heartbound.blowJob",
            4,
            List.of("blowjob_intro"),
            List.of("blowjob_slow_R", "blowjob_slow_L"),
            List.of("blowjob_fast"),
            "blowjob_cum",
            2.5F,
            false,
            false
         ),
         Scene.onPlayer("scene.heartbound.anal", 6, List.of("anal_intro"), List.of("anal_slow"), List.of("anal_fast"), "anal_cum", 4.5F, true, true)
      );
   }

   public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder createAttributes() {
      return GirlEntity.createDefaultAttributes()
         .add(Attributes.MAX_HEALTH, 15.0)
         .add(Attributes.MOVEMENT_SPEED, 0.12)
         .add(Attributes.ATTACK_DAMAGE, 2.0);
   }

   @Override
   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
      if (stack.is(Items.STICK)) {
         this.setGUIOpenState(true, player);
         if (!this.level().isClientSide() && !this.createdClone()) {
            ServerPlayNetworking.send((ServerPlayer)player, new OpenKoboldCustomizeScreenS2CPacket(this.getId(), this.createTempClone().getId()));
         }
      }

      return super.mobInteract(player, hand);
   }

   @Override
   public void onTempCloneCreation(GirlEntity clone) {
      KoboldEntity previewEntity = (KoboldEntity)clone;
      previewEntity.setBodySize(this.getBodySize());
      previewEntity.setKoboldBreastSize(this.getKoboldBreastSize());
      previewEntity.setPrimaryColor(this.getPrimaryColor());
      previewEntity.setSecondaryColor(this.getSecondaryColor());
      previewEntity.setIrisColor(this.getIrisColor());
      previewEntity.setTopHornType(this.getTopHornType());
      previewEntity.setBottomHornType(this.getBottomHornType());
      previewEntity.setLeaderState(this.getLeaderState());
      previewEntity.setKoboldHealth(this.getKoboldHealth());
   }

   public static enum PatternPresets {
      PEACH_BANANA(-13159, -7883),
      BLUE_WHITE(-16776961, -1),
      RED_ORANGE(-65536, -23296),
      GREEN_LIME(-16711936, -13447886),
      PURPLE_PINK(-8388480, -16181),
      GRAY_DARK(-8355712, -12566464),
      CYAN_TEAL(-16711681, -16744320);

      public final int primary;
      public final int secondary;

      private PatternPresets(int primary, int secondary) {
         this.primary = primary;
         this.secondary = secondary;
      }
   }
}
