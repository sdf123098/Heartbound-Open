package com.cuddly.heartbound.entity.base.tamable;

import com.cuddly.heartbound.advancement.criterion.HeartboundCriteria;
import com.cuddly.heartbound.component.HeartboundDataComponentTypes;
import com.cuddly.heartbound.entity.base.GirlSceneEntity;
import com.cuddly.heartbound.item.HeartboundItems;
import com.cuddly.heartbound.registries.HeartboundSoundEventRegistry;
import com.cuddly.heartbound.screen.GirlInventoryScreenHandlerFactory;
import com.cuddly.heartbound.util.managers.TamedGirlManager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.Nullable;

public abstract class TameableGirlEntity extends GirlSceneEntity implements OwnableEntity {
   protected static final EntityDataAccessor<Byte> TAMEABLE_FLAGS = SynchedEntityData.defineId(TameableGirlEntity.class, EntityDataSerializers.BYTE);
   protected static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData.defineId(TameableGirlEntity.class, EntityDataSerializer.forValueType(ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC)));

   public List<String> giftRepliesLike() {
      return List.of("msg.heartbound.gift.like.1", "msg.heartbound.gift.like.2");
   }

   public List<String> giftRepliesLove() {
      return List.of("msg.heartbound.gift.love.1", "msg.heartbound.gift.love.2");
   }

   protected TameableGirlEntity(EntityType<? extends GirlSceneEntity> entityType, Level world) {
      super(entityType, world);
   }

   @Override
   protected void defineSynchedData(Builder builder) {
      super.defineSynchedData(builder);
      builder.define(TAMEABLE_FLAGS, (byte)0);
      builder.define(OWNER_UUID, Optional.empty());
   }

   @Override
   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      ItemStack itemStack = player.getItemInHand(hand);
      Item itemInHand = itemStack.getItem();
      if (!this.level().isClientSide() && this.getOverrideAnim().isEmpty() && hand.equals(InteractionHand.MAIN_HAND)) {
         if (this.isFoodItem(itemStack) && this.getHealth() < this.getMaxHealth()) {
            this.getNavigation().createPath(player, 20);
            this.eat(player, hand, itemStack);
            FoodProperties foodComponent = itemStack.get(DataComponents.FOOD);
            float f = foodComponent != null ? (float)foodComponent.nutrition() : 1.0F;
            this.heal(2.0F * f);
            return InteractionResult.CONSUME;
         } else if (itemStack.is(HeartboundItems.LOVE_BALL) && this.isTamed() && this.isOwner(player)) {
            if (itemStack.has(HeartboundDataComponentTypes.STORED_ENTITY)) {
               player.sendOverlayMessage(Component.translatable("item.heartbound.love_ball.already_contains").withStyle(ChatFormatting.RED));
               return InteractionResult.FAIL;
            } else {
               TagValueOutput saveOutput = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
               this.save(saveOutput);
               CompoundTag entityNbt = saveOutput.buildResult();
               String entityTypeId = EntityType.getKey(this.getType()).toString();
               entityNbt.putString("id", entityTypeId);
               entityNbt.putString("OwnerName", player.getName().getString());
               itemStack.set(HeartboundDataComponentTypes.STORED_ENTITY, entityNbt);
               this.discard();
               player.sendOverlayMessage(Component.translatable("item.heartbound.love_ball.stored", this.getGirlDisplayName()).withStyle(ChatFormatting.GREEN));
               if (player instanceof ServerPlayer serverPlayer) {
                  HeartboundCriteria.CAPTURE_LOVE_BALL.trigger(serverPlayer);
               }

               return InteractionResult.SUCCESS;
            }
         } else if (itemStack.is(HeartboundItems.MASTER_RING)) {
            if (this.isTamed() && !this.isOwner(player)) {
               player.sendOverlayMessage(Component.translatable("item.heartbound.master_ring.has_owner").withStyle(ChatFormatting.RED));
               return InteractionResult.FAIL;
            } else if (itemStack.has(HeartboundDataComponentTypes.STORED_ENTITY)) {
               player.sendOverlayMessage(Component.translatable("item.heartbound.master_ring.already_contains").withStyle(ChatFormatting.RED));
               return InteractionResult.FAIL;
            } else {
               TagValueOutput saveOutput = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
               this.save(saveOutput);
               CompoundTag entityNbt = saveOutput.buildResult();
               String entityTypeId = EntityType.getKey(this.getType()).toString();
               entityNbt.putString("id", entityTypeId);
               if (this.isTamed()) {
                  entityNbt.putString("OwnerName", player.getName().getString());
               }

               itemStack.set(HeartboundDataComponentTypes.STORED_ENTITY, entityNbt);
               this.discard();
               player.sendOverlayMessage(Component.translatable("item.heartbound.master_ring.stored", this.getGirlDisplayName()).withStyle(ChatFormatting.GREEN));
               if (player instanceof ServerPlayer serverPlayer) {
                  HeartboundCriteria.CAPTURE_MASTER_RING.trigger(serverPlayer);
               }

               return InteractionResult.SUCCESS;
            }
         } else if (itemStack.is(Items.POTION)) {
            return InteractionResult.FAIL;
         } else {
            return this.isTamed() ? this.interactTamed(player, itemStack, itemInHand) : this.interactNotTamed(player, itemStack, itemInHand);
         }
      } else {
         return InteractionResult.PASS;
      }
   }

   public InteractionResult interactTamed(Player player, ItemStack itemStack, Item itemInHand) {
      if (this.isOwner(player)) {
         if (itemInHand.equals(this.isAttractedTo()) && this.getCurrentRelationshipLevel() < this.maxRelationshipLevel()) {
            itemStack.consume(1, player);
            player.sendOverlayMessage(Component.translatable("msg.heartbound.sheLikedTheGift"));
            String replyKey = this.getCurrentRelationshipLevel() < 4
               ? this.giftRepliesLike().get(RANDOM.nextInt(this.giftRepliesLike().size()))
               : this.giftRepliesLove().get(RANDOM.nextInt(this.giftRepliesLove().size()));
            this.messageAsEntityTranslatable(replyKey);
            this.setCurrentRelationshipLevel(this.getCurrentRelationshipLevel() + 1);
            if (this.getCurrentRelationshipLevel() >= this.maxRelationshipLevel() && player instanceof ServerPlayer serverPlayer) {
               HeartboundCriteria.MAX_RELATIONSHIP.trigger(serverPlayer);
            }

            this.level().broadcastEntityEvent(this, (byte)73);
            this.makeSound(HeartboundSoundEventRegistry.SoundGroup.GIGGLE.getSound(this.getGirlID()));
            return InteractionResult.SUCCESS;
         } else if (!this.isSceneActive()) {
            if (player.isShiftKeyDown()) {
               this.setSitting(!this.isSitting());
               this.jumping = false;
               this.navigation.stop();
               return InteractionResult.SUCCESS;
            } else {
               player.openMenu(new GirlInventoryScreenHandlerFactory(this));
               this.setGUIOpenState(true, player);
               return InteractionResult.SUCCESS;
            }
         } else {
            return InteractionResult.FAIL;
         }
      } else {
         player.sendOverlayMessage(Component.translatable("msg.heartbound.alreadyInRelationship"));
         return InteractionResult.FAIL;
      }
   }

   public InteractionResult interactNotTamed(Player player, ItemStack itemStack, Item itemInHand) {
      if (itemInHand.equals(this.isAttractedTo())) {
         itemStack.consume(1, player);
         this.tryTame(player);
         return InteractionResult.SUCCESS;
      } else {
          player.sendOverlayMessage(Component.translatable("msg.heartbound.sheIgnoresYou", this.isAttractedTo().getName(ItemStack.EMPTY)));
         return InteractionResult.FAIL;
      }
   }

   private void tryTame(Player player) {
      if (this.random.nextInt(3) == 0) {
         this.makeSound(HeartboundSoundEventRegistry.SoundGroup.HAPPOH.getSound(this.getGirlID()));
         this.setTamedBy(player);
         this.navigation.stop();
         this.setTarget(null);
         this.level().broadcastEntityEvent(this, (byte)71);
         player.sendOverlayMessage(Component.translatable("msg.heartbound.askedOutYes", this.getGirlDisplayName()));
         this.setBasePosHere();
      } else {
         this.level().broadcastEntityEvent(this, (byte)70);
      }
   }

   public void breakUp(Player player) {
      if (!this.level().isClientSide()) {
         this.setTamed(false, true);
         this.setOwner((LivingEntity)null);
         TamedGirlManager.get((ServerLevel)this.level()).removeGirl(this.getUUID());
         this.setSitting(false);
         this.setStripped(false);
         this.setFollowing(false);
         this.dropEquipment((ServerLevel)this.level());
         this.setCurrentRelationshipLevel(0);
         if (player instanceof ServerPlayer serverPlayer) {
            HeartboundCriteria.BREAK_UP.trigger(serverPlayer);
         }

         if (!this.isTamed() && !this.isOwner(player)) {
            player.sendOverlayMessage(Component.translatable("msg.heartbound.brokeUp", this.getGirlDisplayName()).withStyle(ChatFormatting.RED));
         }
      }
   }

   public void breakUpParticles(Player player) {
      this.breakUp(player);
      this.level().broadcastEntityEvent(this, (byte)72);
      this.makeSound(HeartboundSoundEventRegistry.SoundGroup.SADOH.getSound(this.getGirlID()));
   }

   @Override
   public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount) {
      if (this.isInvulnerableTo(serverLevel, source)) {
         return false;
      } else {
         String damageType = source.getMsgId();
         if (!damageType.equals("outOfWorld") && !damageType.equals("genericKill")) {
            if (this.isTamed()
               && this.getHealth() - amount <= 0.0F & (!damageType.equals("outOfWorld") && !damageType.equals("genericKill") && !this.isMovementLocked())) {
               this.setHealth(this.getMaxHealth());
               this.resetAnimationState();
               if (this.getOwner() instanceof Player owner) {
                  owner.sendSystemMessage(
                     Component.translatable(
                        "msg.heartbound.diedRespawned", this.getGirlDisplayName(), this.getBasePos().getX(), this.getBasePos().getY(), this.getBasePos().getZ()
                     )
                  );
               }

               this.dropEquipment((ServerLevel)this.level());
               this.teleportToBase(!this.isRoaming());
               return false;
            } else if (!(this.isMovementLocked() & !damageType.equals("outOfWorld")) && !damageType.equals("genericKill")) {
               return super.hurtServer(serverLevel, source, amount);
            } else {
               if (!this.isVehicle()) {
                  ((Player)this.getOwner()).sendOverlayMessage(Component.nullToEmpty(this.getGirlDisplayName() + " is busy at the moment"));
               }

               return false;
            }
         } else {
            return super.hurtServer(serverLevel, source, amount);
         }
      }
   }

   @Override
   public void die(DamageSource damageSource) {
      if (this.level() instanceof ServerLevel serverWorld
         && serverWorld.getGameRules().get(GameRules.SHOW_DEATH_MESSAGES)
         && this.getOwner() instanceof ServerPlayer serverPlayerEntity) {
         serverPlayerEntity.sendSystemMessage(this.getCombatTracker().getDeathMessage());
      }

      super.die(damageSource);
   }

   @Override
   public void tick() {
      if (!this.level().isClientSide()) {
         ServerLevel world = (ServerLevel)this.level();
         if (this.isTamed()) {
            TamedGirlManager.get(world).registerGirl(this);
         } else if (TamedGirlManager.get(world).containsGirl(this.getUUID())) {
            TamedGirlManager.get(world).removeGirl(this.getUUID());
         }

         byte b = this.entityData.get(TAMEABLE_FLAGS);
         if (this.isSitting()) {
            this.entityData.set(TAMEABLE_FLAGS, (byte)(b | 1));
         } else {
            this.entityData.set(TAMEABLE_FLAGS, (byte)(b & -2));
         }
      }

      super.tick();
   }

   @Override
   public void modelLogic() {
      super.modelLogic();
      this.setBoneSize("boobs", this.getBreastSize(), this.getBreastMinSize(), this.getBreastMaxSize());
      this.setBonePos("boobs", this.getBreastOffset());
   }

   @Override
   public boolean canBeLeashed() {
      return true;
   }

   public boolean isTamed() {
      return (this.entityData.get(TAMEABLE_FLAGS) & 4) != 0;
   }

   public void setTamed(boolean tamed, boolean updateAttributes) {
      byte b = this.entityData.get(TAMEABLE_FLAGS);
      if (tamed) {
         this.entityData.set(TAMEABLE_FLAGS, (byte)(b | 4));
      } else {
         this.entityData.set(TAMEABLE_FLAGS, (byte)(b & -5));
      }

      if (updateAttributes) {
         this.updateAttributesForTamed();
      }
   }

   protected void updateAttributesForTamed() {
   }

   @Nullable
   public UUID getOwnerUUID() {
      return this.entityData.get(OWNER_UUID).orElse(null);
   }

   @Override
   public EntityReference<LivingEntity> getOwnerReference() {
      return this.entityData.get(OWNER_UUID).map(uuid -> EntityReference.<LivingEntity>of(uuid)).orElse(EntityReference.<LivingEntity>of(Util.NIL_UUID));
   }

   @Nullable
   @Override
   public LivingEntity getOwner() {
      UUID uuid = this.getOwnerUUID();
      if (uuid == null) {
         return null;
      } else {
         Level world = this.level();
         if (world instanceof ServerLevel serverWorld) {
            Entity entity = serverWorld.getEntity(uuid);
            if (entity instanceof LivingEntity) {
               return (LivingEntity)entity;
            }
         }

         return world.getPlayerByUUID(uuid);
      }
   }

   public void setOwner(@Nullable LivingEntity owner) {
      if (owner != null) {
         this.entityData.set(OWNER_UUID, Optional.of(owner.getUUID()));
      } else {
         this.entityData.set(OWNER_UUID, Optional.empty());
      }
   }

   public void setOwnerUuid(@Nullable UUID ownerUuid) {
      this.entityData.set(OWNER_UUID, Optional.ofNullable(ownerUuid));
   }

   public void setTamedBy(Player player) {
      this.setTamed(true, true);
      this.setOwner(player);
      if (player instanceof ServerPlayer serverPlayerEntity) {
         HeartboundCriteria.TAME_GIRL.trigger(serverPlayerEntity, this);
      }
   }

   @Override
   public void setTarget(@Nullable LivingEntity target) {
      if (target == null || !this.isOwner(target)) {
         super.setTarget(target);
      }
   }

   @Override
   public boolean canAttack(LivingEntity target) {
      return !this.isOwner(target) && super.canAttack(target);
   }

   public boolean isOwner(LivingEntity entity) {
      return entity == this.getOwner();
   }

   @Nullable
   @Override
   public PlayerTeam getTeam() {
      PlayerTeam team = super.getTeam();
      if (team != null) {
         return team;
      } else {
         if (this.isTamed()) {
            LivingEntity livingEntity = this.getOwner();
            if (livingEntity != null) {
               return livingEntity.getTeam();
            }
         }

         return null;
      }
   }

   public void tryTeleportToOwner() {
      LivingEntity livingEntity = this.getOwner();
      if (livingEntity != null) {
         this.tryTeleportNear(livingEntity.blockPosition());
      }
   }

   public boolean shouldTryTeleportToOwner() {
      LivingEntity livingEntity = this.getOwner();
      return livingEntity != null && this.distanceToSqr(this.getOwner()) >= 144.0;
   }

   private void tryTeleportNear(BlockPos pos) {
      for (int i = 0; i < 10; i++) {
         int j = this.random.nextIntBetweenInclusive(-3, 3);
         int k = this.random.nextIntBetweenInclusive(-3, 3);
         if (Math.abs(j) >= 2 || Math.abs(k) >= 2) {
            int l = this.random.nextIntBetweenInclusive(-1, 1);
            if (this.tryTeleportTo(pos.getX() + j, pos.getY() + l, pos.getZ() + k)) {
               return;
            }
         }
      }
   }

   private boolean tryTeleportTo(int x, int y, int z) {
      if (!this.canTeleportTo(new BlockPos(x, y, z))) {
         return false;
      } else {
         this.setPos((double)x + 0.5, (double)y, (double)z + 0.5);
         this.setYRot(this.getYRot());
         this.setXRot(this.getXRot());
         this.navigation.stop();
         return true;
      }
   }

   private boolean canTeleportTo(BlockPos pos) {
      PathType pathNodeType = WalkNodeEvaluator.getPathTypeStatic(this, pos);
      if (pathNodeType != PathType.WALKABLE) {
         return false;
      } else {
         BlockState blockState = this.level().getBlockState(pos.below());
         if (!this.canTeleportOntoLeaves() && blockState.getBlock() instanceof LeavesBlock) {
            return false;
         } else {
            BlockPos blockPos = pos.subtract(this.blockPosition());
            return this.level().noCollision(this, this.getBoundingBox().move(blockPos));
         }
      }
   }

   public final boolean cannotFollowOwner() {
      return this.isSitting() || this.isPassenger() || this.mayBeLeashed() || this.getOwner() != null && this.getOwner().isSpectator();
   }

   protected boolean canTeleportOntoLeaves() {
      return false;
   }

   @Override
   public void addAdditionalSaveData(ValueOutput nbt) {
      super.addAdditionalSaveData(nbt);
      UUID ownerUuid = this.getOwnerUUID();
      if (ownerUuid != null) {
         nbt.putIntArray(
            "Owner",
            new int[] {(int)(ownerUuid.getMostSignificantBits() >> 32), (int)ownerUuid.getMostSignificantBits(), (int)(ownerUuid.getLeastSignificantBits() >> 32), (int)ownerUuid.getLeastSignificantBits()}
         );
      }
   }

   @Override
   public void readAdditionalSaveData(ValueInput nbt) {
      super.readAdditionalSaveData(nbt);
      UUID ownerUuid = null;
      int[] ownerArr = nbt.getIntArray("Owner").orElse(null);
      if (ownerArr != null && ownerArr.length == 4) {
         ownerUuid = new UUID((long)ownerArr[0] << 32 | ownerArr[1] & 0xFFFFFFFFL, (long)ownerArr[2] << 32 | ownerArr[3] & 0xFFFFFFFFL);
      }

      if (ownerUuid != null) {
         try {
            this.entityData.set(OWNER_UUID, Optional.of(ownerUuid));
            this.setTamed(true, false);
         } catch (Throwable var4) {
            this.setTamed(false, true);
         }
      } else {
         this.entityData.set(OWNER_UUID, Optional.empty());
         this.setTamed(false, true);
      }
   }

   public class TameableGirlEscapeDangerGoal extends PanicGoal {
      public TameableGirlEscapeDangerGoal(final double speed, final TagKey<DamageType> dangerousDamageTypes) {
         super(TameableGirlEntity.this, speed, dangerousDamageTypes);
      }

      public TameableGirlEscapeDangerGoal(final double speed) {
         super(TameableGirlEntity.this, speed);
      }

      @Override
      public void tick() {
         if (!TameableGirlEntity.this.cannotFollowOwner() && TameableGirlEntity.this.shouldTryTeleportToOwner()) {
            TameableGirlEntity.this.tryTeleportToOwner();
         }

         super.tick();
      }
   }
}
