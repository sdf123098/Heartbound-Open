package com.cuddly.heartbound.entity.base.tamable;

import com.cuddly.heartbound.entity.ai.goal.ConditionalGoal;
import com.cuddly.heartbound.entity.ai.goal.GirlAttackSwitchGoal;
import com.cuddly.heartbound.entity.ai.goal.GirlAttackWithOwnerGoal;
import com.cuddly.heartbound.entity.ai.goal.GirlChopTreesGoal;
import com.cuddly.heartbound.entity.ai.goal.GirlDefendBaseGoal;
import com.cuddly.heartbound.entity.ai.goal.GirlDepositItemsGoal;
import com.cuddly.heartbound.entity.ai.goal.GirlFarmCropsGoal;
import com.cuddly.heartbound.entity.ai.goal.GirlFollowOwnerGoal;
import com.cuddly.heartbound.entity.ai.goal.GirlMineOresGoal;
import com.cuddly.heartbound.entity.ai.goal.GirlPickupCropsGoal;
import com.cuddly.heartbound.entity.ai.goal.GirlPickupMobDropsGoal;
import com.cuddly.heartbound.entity.ai.goal.GirlRoamAtBaseGoal;
import com.cuddly.heartbound.entity.ai.goal.GirlSitGoal;
import com.cuddly.heartbound.entity.ai.goal.GirlStayNearBaseGoal;
import com.cuddly.heartbound.entity.ai.goal.GirlTrackOwnerAttackerGoal;
import com.cuddly.heartbound.entity.base.GirlEntity;
import java.util.List;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain.Provider;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.behaviour.base.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.base.OneRandomBehaviour;
import net.tslat.smartbrainlib.api.internal.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Idle;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetPlayerLookTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetRandomLookTarget;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.HurtBySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;

public abstract class BaseGirlEntityAI extends TameableGirlEntity implements SmartBrainOwner<BaseGirlEntityAI> {
   private static final EntityDataAccessor<Boolean> SHOULD_TICK_BRAIN = SynchedEntityData.defineId(BaseGirlEntityAI.class, EntityDataSerializers.BOOLEAN);

   protected BaseGirlEntityAI(EntityType<? extends BaseGirlEntityAI> entityType, Level world) {
      super(entityType, world);
   }

   @Override
   protected void defineSynchedData(Builder builder) {
      super.defineSynchedData(builder);
      builder.define(SHOULD_TICK_BRAIN, false);
   }

   @Override
   protected void registerGoals() {
      super.registerGoals();
      this.goalSelector.addGoal(0, new GirlSitGoal(this));
      if (!this.entityData.get(SHOULD_TICK_BRAIN)) {
         this.goalSelector.addGoal(1, new FloatGoal(this));
         if (this.supportsDoorInteraction()) {
            this.goalSelector.addGoal(2, new OpenDoorGoal(this, true));
         }

         this.goalSelector.addGoal(3, new TameableGirlEntity.TameableGirlEscapeDangerGoal(1.5, DamageTypeTags.PANIC_ENVIRONMENTAL_CAUSES));
         this.goalSelector.addGoal(4, new GirlAttackSwitchGoal(this, 1.0, 5.0F, 6.0F, 11.0F));
         this.goalSelector.addGoal(5, new ConditionalGoal(new GirlFollowOwnerGoal(this, 1.0, 10.0F, 2.0F), this::isFollowing));
         this.goalSelector.addGoal(6, new TemptGoal(this, 1.0, Ingredient.of(this.isAttractedTo()), false));
         this.goalSelector.addGoal(7, new GirlStayNearBaseGoal(this, 1.0, 2.0F, 15.0F, 150.0F));
         this.goalSelector.addGoal(8, new GirlFarmCropsGoal(this, 1.0));
         this.goalSelector.addGoal(9, new GirlPickupCropsGoal(this, 1.2));
         this.goalSelector.addGoal(10, new GirlPickupMobDropsGoal(this, 1.2));
         this.goalSelector.addGoal(11, new GirlDepositItemsGoal(this, 1.0));
         this.goalSelector.addGoal(12, new GirlRoamAtBaseGoal(this, 1.0));
         this.goalSelector.addGoal(13, new GirlMineOresGoal(this, 1.0));
         this.goalSelector.addGoal(14, new GirlChopTreesGoal(this, 1.0));
         this.goalSelector.addGoal(15, new RandomStrollGoal(this, 1.0));
         this.goalSelector.addGoal(16, new ConditionalGoal(new LookAtPlayerGoal(this, Player.class, 6.0F), () -> !this.isMovementLocked()));
         this.goalSelector.addGoal(17, new ConditionalGoal(new RandomLookAroundGoal(this), () -> !this.isMovementLocked()));
         this.targetSelector.addGoal(1, new ConditionalGoal(new GirlTrackOwnerAttackerGoal(this), this::isFollowing));
         this.targetSelector.addGoal(2, new ConditionalGoal(new GirlAttackWithOwnerGoal(this, BaseGirlEntityAI.class), this::isFollowing));
         this.targetSelector.addGoal(3, new GirlDefendBaseGoal(this));
         this.targetSelector.addGoal(4, new HurtByTargetGoal(this, Player.class, GirlEntity.class));
      }
   }

   protected boolean supportsDoorInteraction() {
      return true;
   }

   @Override
   protected void customServerAiStep(ServerLevel serverLevel) {
      if (this.entityData.get(SHOULD_TICK_BRAIN)) {
         super.customServerAiStep(serverLevel);
      }
   }

   @Override
   public List<? extends ExtendedSensor<? extends BaseGirlEntityAI>> getSensors(BaseGirlEntityAI entity) {
      return List.of(new NearbyLivingEntitySensor().setPredicate((target, e) -> target instanceof Player), new HurtBySensor());
   }

   @Override
   public List<? extends BehaviorControl<?>> getAlwaysRunningBehaviours(BaseGirlEntityAI entity) {
      return List.<BehaviorControl<?>>of(new LookAtTarget<BaseGirlEntityAI>(), new MoveToWalkTarget<BaseGirlEntityAI>());
   }

   @Override
   public List<? extends BehaviorControl<?>> getIdleBehaviours(BaseGirlEntityAI entity) {
      return List.of(
         new FirstApplicableBehaviour<BaseGirlEntityAI>(new SetPlayerLookTarget<BaseGirlEntityAI>(), new SetRandomLookTarget<BaseGirlEntityAI>()),
         new OneRandomBehaviour<BaseGirlEntityAI>(new SetRandomWalkTarget<BaseGirlEntityAI>(), new Idle<BaseGirlEntityAI>().runFor(e -> e.getRandom().nextIntBetweenInclusive(30, 60)))
      );
   }

   @Override
   public void tick() {
      this.entityData
         .set(SHOULD_TICK_BRAIN, !this.isMovementLocked() && !this.isSitting() && this.targetBedPos == null && !this.isFollowing() && !this.isRoaming());
      super.tick();
   }
}
