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
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.data.DataTracker.Builder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class TameableGirlEntity extends GirlSceneEntity implements Tameable {
   protected static final TrackedData<Byte> TAMEABLE_FLAGS = DataTracker.registerData(TameableGirlEntity.class, TrackedDataHandlerRegistry.BYTE);
   protected static final TrackedData<Optional<UUID>> OWNER_UUID = DataTracker.registerData(TameableGirlEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);

   public List<String> giftRepliesLike() {
      return List.of("msg.heartbound.gift.like.1", "msg.heartbound.gift.like.2");
   }

   public List<String> giftRepliesLove() {
      return List.of("msg.heartbound.gift.love.1", "msg.heartbound.gift.love.2");
   }

   protected TameableGirlEntity(EntityType<? extends GirlSceneEntity> entityType, World world) {
      super(entityType, world);
   }

   @Override
   protected void initDataTracker(Builder builder) {
      super.initDataTracker(builder);
      builder.add(TAMEABLE_FLAGS, (byte)0);
      builder.add(OWNER_UUID, Optional.empty());
   }

   @Override
   public ActionResult interactMob(PlayerEntity player, Hand hand) {
      ItemStack itemStack = player.getStackInHand(hand);
      Item itemInHand = itemStack.getItem();
      if (!this.getWorld().isClient() && this.getOverrideAnim().isEmpty() && hand.equals(Hand.MAIN_HAND)) {
         if (this.isFoodItem(itemStack) && this.getHealth() < this.getMaxHealth()) {
            this.getNavigation().findPathTo(player, 20);
            this.eat(player, hand, itemStack);
            FoodComponent foodComponent = itemStack.get(DataComponentTypes.FOOD);
            float f = foodComponent != null ? (float)foodComponent.nutrition() : 1.0F;
            this.heal(2.0F * f);
            return ActionResult.CONSUME;
         } else if (itemStack.isOf(HeartboundItems.LOVE_BALL) && this.isTamed() && this.isOwner(player)) {
            if (itemStack.contains(HeartboundDataComponentTypes.STORED_ENTITY)) {
               player.sendMessage(Text.translatable("item.heartbound.love_ball.already_contains").formatted(Formatting.RED), true);
               return ActionResult.FAIL;
            } else {
               NbtCompound entityNbt = new NbtCompound();
               this.saveNbt(entityNbt);
               String entityTypeId = EntityType.getId(this.getType()).toString();
               entityNbt.putString("id", entityTypeId);
               entityNbt.putString("OwnerName", player.getName().getString());
               itemStack.set(HeartboundDataComponentTypes.STORED_ENTITY, entityNbt);
               this.discard();
               player.sendMessage(Text.translatable("item.heartbound.love_ball.stored", this.getGirlDisplayName()).formatted(Formatting.GREEN), true);
               if (player instanceof ServerPlayerEntity serverPlayer) {
                  HeartboundCriteria.CAPTURE_LOVE_BALL.trigger(serverPlayer);
               }

               return ActionResult.SUCCESS;
            }
         } else if (itemStack.isOf(HeartboundItems.MASTER_RING)) {
            if (this.isTamed() && !this.isOwner(player)) {
               player.sendMessage(Text.translatable("item.heartbound.master_ring.has_owner").formatted(Formatting.RED), true);
               return ActionResult.FAIL;
            } else if (itemStack.contains(HeartboundDataComponentTypes.STORED_ENTITY)) {
               player.sendMessage(Text.translatable("item.heartbound.master_ring.already_contains").formatted(Formatting.RED), true);
               return ActionResult.FAIL;
            } else {
               NbtCompound entityNbt = new NbtCompound();
               this.saveNbt(entityNbt);
               String entityTypeId = EntityType.getId(this.getType()).toString();
               entityNbt.putString("id", entityTypeId);
               if (this.isTamed()) {
                  entityNbt.putString("OwnerName", player.getName().getString());
               }

               itemStack.set(HeartboundDataComponentTypes.STORED_ENTITY, entityNbt);
               this.discard();
               player.sendMessage(Text.translatable("item.heartbound.master_ring.stored", this.getGirlDisplayName()).formatted(Formatting.GREEN), true);
               if (player instanceof ServerPlayerEntity serverPlayer) {
                  HeartboundCriteria.CAPTURE_MASTER_RING.trigger(serverPlayer);
               }

               return ActionResult.SUCCESS;
            }
         } else if (itemStack.isOf(Items.POTION)) {
            return ActionResult.FAIL;
         } else {
            return this.isTamed() ? this.interactTamed(player, itemStack, itemInHand) : this.interactNotTamed(player, itemStack, itemInHand);
         }
      } else {
         return ActionResult.PASS;
      }
   }

   public ActionResult interactTamed(PlayerEntity player, ItemStack itemStack, Item itemInHand) {
      if (this.isOwner(player)) {
         if (itemInHand.equals(this.isAttractedTo()) && this.getCurrentRelationshipLevel() < this.maxRelationshipLevel()) {
            itemStack.decrementUnlessCreative(1, player);
            player.sendMessage(Text.translatable("msg.heartbound.sheLikedTheGift"), true);
            String replyKey = this.getCurrentRelationshipLevel() < 4
               ? this.giftRepliesLike().get(RANDOM.nextInt(this.giftRepliesLike().size()))
               : this.giftRepliesLove().get(RANDOM.nextInt(this.giftRepliesLove().size()));
            this.messageAsEntityTranslatable(replyKey);
            this.setCurrentRelationshipLevel(this.getCurrentRelationshipLevel() + 1);
            if (this.getCurrentRelationshipLevel() >= this.maxRelationshipLevel() && player instanceof ServerPlayerEntity serverPlayer) {
               HeartboundCriteria.MAX_RELATIONSHIP.trigger(serverPlayer);
            }

            this.getWorld().sendEntityStatus(this, (byte)73);
            this.playSound(HeartboundSoundEventRegistry.SoundGroup.GIGGLE.getSound(this.getGirlID()));
            return ActionResult.SUCCESS;
         } else if (!this.isSceneActive()) {
            if (player.isSneaking()) {
               this.setSitting(!this.isSitting());
               this.jumping = false;
               this.navigation.stop();
               return ActionResult.SUCCESS;
            } else {
               player.openHandledScreen(new GirlInventoryScreenHandlerFactory(this));
               this.setGUIOpenState(true, player);
               return ActionResult.SUCCESS;
            }
         } else {
            return ActionResult.FAIL;
         }
      } else {
         player.sendMessage(Text.translatable("msg.heartbound.alreadyInRelationship"), true);
         return ActionResult.FAIL;
      }
   }

   public ActionResult interactNotTamed(PlayerEntity player, ItemStack itemStack, Item itemInHand) {
      if (itemInHand.equals(this.isAttractedTo())) {
         itemStack.decrementUnlessCreative(1, player);
         this.tryTame(player);
         return ActionResult.SUCCESS;
      } else {
         player.sendMessage(Text.translatable("msg.heartbound.sheIgnoresYou", this.isAttractedTo().getName()), true);
         return ActionResult.FAIL;
      }
   }

   private void tryTame(PlayerEntity player) {
      if (this.random.nextInt(3) == 0) {
         this.playSound(HeartboundSoundEventRegistry.SoundGroup.HAPPOH.getSound(this.getGirlID()));
         this.setTamedBy(player);
         this.navigation.stop();
         this.setTarget(null);
         this.getWorld().sendEntityStatus(this, (byte)71);
         player.sendMessage(Text.translatable("msg.heartbound.askedOutYes", this.getGirlDisplayName()), true);
         this.setBasePosHere();
      } else {
         this.getWorld().sendEntityStatus(this, (byte)70);
      }
   }

   public void breakUp(PlayerEntity player) {
      if (!this.getWorld().isClient()) {
         this.setTamed(false, true);
         this.setOwner((LivingEntity)null);
         TamedGirlManager.get((ServerWorld)this.getWorld()).removeGirl(this.getUuid());
         this.setSitting(false);
         this.setStripped(false);
         this.setFollowing(false);
         this.dropInventory();
         this.setCurrentRelationshipLevel(0);
         if (player instanceof ServerPlayerEntity serverPlayer) {
            HeartboundCriteria.BREAK_UP.trigger(serverPlayer);
         }

         if (!this.isTamed() && !this.isOwner(player)) {
            player.sendMessage(Text.translatable("msg.heartbound.brokeUp", this.getGirlDisplayName()).formatted(Formatting.RED), true);
         }
      }
   }

   public void breakUpParticles(PlayerEntity player) {
      this.breakUp(player);
      this.getWorld().sendEntityStatus(this, (byte)72);
      this.playSound(HeartboundSoundEventRegistry.SoundGroup.SADOH.getSound(this.getGirlID()));
   }

   @Override
   public boolean damage(DamageSource source, float amount) {
      if (this.isInvulnerableTo(source)) {
         return false;
      } else {
         String damageType = source.getName();
         if (!damageType.equals("outOfWorld") && !damageType.equals("genericKill")) {
            if (this.isTamed()
               && this.getHealth() - amount <= 0.0F & (!damageType.equals("outOfWorld") && !damageType.equals("genericKill") && !this.isMovementLocked())) {
               this.setHealth(this.getMaxHealth());
               this.resetAnimationState();
               if (this.getOwner() instanceof PlayerEntity owner) {
                  owner.sendMessage(
                     Text.translatable(
                        "msg.heartbound.diedRespawned", this.getGirlDisplayName(), this.getBasePos().getX(), this.getBasePos().getY(), this.getBasePos().getZ()
                     ),
                     false
                  );
               }

               this.dropInventory();
               this.teleportToBase(!this.isRoaming());
               return false;
            } else if (!(this.isMovementLocked() & !damageType.equals("outOfWorld")) && !damageType.equals("genericKill")) {
               return super.damage(source, amount);
            } else {
               if (!this.hasPassengers()) {
                  ((PlayerEntity)this.getOwner()).sendMessage(Text.of(this.getGirlDisplayName() + " is busy at the moment"), true);
               }

               return false;
            }
         } else {
            return super.damage(source, amount);
         }
      }
   }

   @Override
   public void onDeath(DamageSource damageSource) {
      if (this.getWorld() instanceof ServerWorld serverWorld
         && serverWorld.getGameRules().getBoolean(GameRules.SHOW_DEATH_MESSAGES)
         && this.getOwner() instanceof ServerPlayerEntity serverPlayerEntity) {
         serverPlayerEntity.sendMessage(this.getDamageTracker().getDeathMessage());
      }

      super.onDeath(damageSource);
   }

   @Override
   public void tick() {
      if (!this.getWorld().isClient()) {
         ServerWorld world = (ServerWorld)this.getWorld();
         if (this.isTamed()) {
            TamedGirlManager.get(world).registerGirl(this);
         } else if (TamedGirlManager.get(world).containsGirl(this.getUuid())) {
            TamedGirlManager.get(world).removeGirl(this.getUuid());
         }

         byte b = this.dataTracker.get(TAMEABLE_FLAGS);
         if (this.isSitting()) {
            this.dataTracker.set(TAMEABLE_FLAGS, (byte)(b | 1));
         } else {
            this.dataTracker.set(TAMEABLE_FLAGS, (byte)(b & -2));
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
      return (this.dataTracker.get(TAMEABLE_FLAGS) & 4) != 0;
   }

   public void setTamed(boolean tamed, boolean updateAttributes) {
      byte b = this.dataTracker.get(TAMEABLE_FLAGS);
      if (tamed) {
         this.dataTracker.set(TAMEABLE_FLAGS, (byte)(b | 4));
      } else {
         this.dataTracker.set(TAMEABLE_FLAGS, (byte)(b & -5));
      }

      if (updateAttributes) {
         this.updateAttributesForTamed();
      }
   }

   protected void updateAttributesForTamed() {
   }

   @Nullable
   @Override
   public UUID getOwnerUuid() {
      return this.dataTracker.get(OWNER_UUID).orElse(null);
   }

   @Nullable
   @Override
   public LivingEntity getOwner() {
      UUID uuid = this.getOwnerUuid();
      if (uuid == null) {
         return null;
      } else {
         World world = this.getWorld();
         if (world instanceof ServerWorld serverWorld) {
            Entity entity = serverWorld.getEntity(uuid);
            if (entity instanceof LivingEntity) {
               return (LivingEntity)entity;
            }
         }

         return world.getPlayerByUuid(uuid);
      }
   }

   public void setOwner(@Nullable LivingEntity owner) {
      if (owner != null) {
         this.dataTracker.set(OWNER_UUID, Optional.of(owner.getUuid()));
      } else {
         this.dataTracker.set(OWNER_UUID, Optional.empty());
      }
   }

   public void setOwnerUuid(@Nullable UUID ownerUuid) {
      this.dataTracker.set(OWNER_UUID, Optional.ofNullable(ownerUuid));
   }

   public void setTamedBy(PlayerEntity player) {
      this.setTamed(true, true);
      this.setOwner(player);
      if (player instanceof ServerPlayerEntity serverPlayerEntity) {
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
   public boolean canTarget(LivingEntity target) {
      return !this.isOwner(target) && super.canTarget(target);
   }

   public boolean isOwner(LivingEntity entity) {
      return entity == this.getOwner();
   }

   @Nullable
   @Override
   public Team getScoreboardTeam() {
      Team team = super.getScoreboardTeam();
      if (team != null) {
         return team;
      } else {
         if (this.isTamed()) {
            LivingEntity livingEntity = this.getOwner();
            if (livingEntity != null) {
               return livingEntity.getScoreboardTeam();
            }
         }

         return null;
      }
   }

   @Override
   public boolean isTeammate(Entity other) {
      if (this.isTamed()) {
         LivingEntity livingEntity = this.getOwner();
         if (other == livingEntity) {
            return true;
         }

         if (livingEntity != null) {
            return this.isTeamPlayer(other.getScoreboardTeam());
         }
      }

      return super.isTeammate(other);
   }

   public void tryTeleportToOwner() {
      LivingEntity livingEntity = this.getOwner();
      if (livingEntity != null) {
         this.tryTeleportNear(livingEntity.getBlockPos());
      }
   }

   public boolean shouldTryTeleportToOwner() {
      LivingEntity livingEntity = this.getOwner();
      return livingEntity != null && this.squaredDistanceTo(this.getOwner()) >= 144.0;
   }

   private void tryTeleportNear(BlockPos pos) {
      for (int i = 0; i < 10; i++) {
         int j = this.random.nextBetween(-3, 3);
         int k = this.random.nextBetween(-3, 3);
         if (Math.abs(j) >= 2 || Math.abs(k) >= 2) {
            int l = this.random.nextBetween(-1, 1);
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
         this.refreshPositionAndAngles((double)x + 0.5, (double)y, (double)z + 0.5, this.getYaw(), this.getPitch());
         this.navigation.stop();
         return true;
      }
   }

   private boolean canTeleportTo(BlockPos pos) {
      PathNodeType pathNodeType = LandPathNodeMaker.getLandNodeType(this, pos);
      if (pathNodeType != PathNodeType.WALKABLE) {
         return false;
      } else {
         BlockState blockState = this.getWorld().getBlockState(pos.down());
         if (!this.canTeleportOntoLeaves() && blockState.getBlock() instanceof LeavesBlock) {
            return false;
         } else {
            BlockPos blockPos = pos.subtract(this.getBlockPos());
            return this.getWorld().isSpaceEmpty(this, this.getBoundingBox().offset(blockPos));
         }
      }
   }

   public final boolean cannotFollowOwner() {
      return this.isSitting() || this.hasVehicle() || this.mightBeLeashed() || this.getOwner() != null && this.getOwner().isSpectator();
   }

   protected boolean canTeleportOntoLeaves() {
      return false;
   }

   @Override
   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      UUID ownerUuid = this.getOwnerUuid();
      if (ownerUuid != null) {
         nbt.putUuid("Owner", ownerUuid);
      }
   }

   @Override
   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      UUID ownerUuid = null;
      if (nbt.containsUuid("Owner")) {
         ownerUuid = nbt.getUuid("Owner");
      }

      if (ownerUuid != null) {
         try {
            this.dataTracker.set(OWNER_UUID, Optional.of(ownerUuid));
            this.setTamed(true, false);
         } catch (Throwable var4) {
            this.setTamed(false, true);
         }
      } else {
         this.dataTracker.set(OWNER_UUID, Optional.empty());
         this.setTamed(false, true);
      }
   }

   public class TameableGirlEscapeDangerGoal extends EscapeDangerGoal {
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
