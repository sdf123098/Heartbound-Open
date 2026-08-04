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
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.Brain.Profile;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.ai.goal.LongDoorInteractGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.data.DataTracker.Builder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.world.World;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour;
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
   private static final TrackedData<Boolean> SHOULD_TICK_BRAIN = DataTracker.registerData(BaseGirlEntityAI.class, TrackedDataHandlerRegistry.BOOLEAN);

   protected BaseGirlEntityAI(EntityType<? extends BaseGirlEntityAI> entityType, World world) {
      super(entityType, world);
   }

   @Override
   protected void initDataTracker(Builder builder) {
      super.initDataTracker(builder);
      builder.add(SHOULD_TICK_BRAIN, false);
   }

   @Override
   protected void initGoals() {
      super.initGoals();
      this.goalSelector.add(0, new GirlSitGoal(this));
      if (!this.dataTracker.get(SHOULD_TICK_BRAIN)) {
         this.goalSelector.add(1, new SwimGoal(this));
         if (this.supportsDoorInteraction()) {
            this.goalSelector.add(2, new LongDoorInteractGoal(this, true));
         }

         this.goalSelector.add(3, new TameableGirlEntity.TameableGirlEscapeDangerGoal(1.5, DamageTypeTags.PANIC_ENVIRONMENTAL_CAUSES));
         this.goalSelector.add(4, new GirlAttackSwitchGoal(this, 1.0, 5.0F, 6.0F, 11.0F));
         this.goalSelector.add(5, new ConditionalGoal(new GirlFollowOwnerGoal(this, 1.0, 10.0F, 2.0F), this::isFollowing));
         this.goalSelector.add(6, new TemptGoal(this, 1.0, Ingredient.ofItems(this.isAttractedTo()), false));
         this.goalSelector.add(7, new GirlStayNearBaseGoal(this, 1.0, 2.0F, 15.0F, 150.0F));
         this.goalSelector.add(8, new GirlFarmCropsGoal(this, 1.0));
         this.goalSelector.add(9, new GirlPickupCropsGoal(this, 1.2));
         this.goalSelector.add(10, new GirlPickupMobDropsGoal(this, 1.2));
         this.goalSelector.add(11, new GirlDepositItemsGoal(this, 1.0));
         this.goalSelector.add(12, new GirlRoamAtBaseGoal(this, 1.0));
         this.goalSelector.add(13, new GirlMineOresGoal(this, 1.0));
         this.goalSelector.add(14, new GirlChopTreesGoal(this, 1.0));
         this.goalSelector.add(15, new WanderAroundGoal(this, 1.0));
         this.goalSelector.add(16, new ConditionalGoal(new LookAtEntityGoal(this, PlayerEntity.class, 6.0F), () -> !this.isMovementLocked()));
         this.goalSelector.add(17, new ConditionalGoal(new LookAroundGoal(this), () -> !this.isMovementLocked()));
         this.targetSelector.add(1, new ConditionalGoal(new GirlTrackOwnerAttackerGoal(this), this::isFollowing));
         this.targetSelector.add(2, new ConditionalGoal(new GirlAttackWithOwnerGoal(this, BaseGirlEntityAI.class), this::isFollowing));
         this.targetSelector.add(3, new GirlDefendBaseGoal(this));
         this.targetSelector.add(4, new RevengeGoal(this, PlayerEntity.class, GirlEntity.class));
      }
   }

   @Override
   protected Profile<?> createBrainProfile() {
      return new SmartBrainProvider(this);
   }

   protected boolean supportsDoorInteraction() {
      return true;
   }

   @Override
   protected void mobTick() {
      if (this.dataTracker.get(SHOULD_TICK_BRAIN)) {
         this.tickBrain(this);
      }
   }

   @Override
   public List<? extends ExtendedSensor<? extends BaseGirlEntityAI>> getSensors() {
      return List.of(new NearbyLivingEntitySensor().setPredicate((target, entity) -> target instanceof PlayerEntity), new HurtBySensor());
   }

   @Override
   public BrainActivityGroup<? extends BaseGirlEntityAI> getCoreTasks() {
      return BrainActivityGroup.coreTasks(new MultiTickTask[]{new LookAtTarget(), new MoveToWalkTarget()});
   }

   @Override
   public BrainActivityGroup<? extends BaseGirlEntityAI> getIdleTasks() {
      return BrainActivityGroup.idleTasks(
         new MultiTickTask[]{
            new FirstApplicableBehaviour(new SetPlayerLookTarget(), new SetRandomLookTarget()),
            new OneRandomBehaviour(new SetRandomWalkTarget(), new Idle().runFor(entity -> entity.getRandom().nextBetween(30, 60)))
         }
      );
   }

   @Override
   public BrainActivityGroup<? extends BaseGirlEntityAI> getFightTasks() {
      return BrainActivityGroup.empty();
   }

   @Override
   public void tick() {
      this.dataTracker
         .set(SHOULD_TICK_BRAIN, !this.isMovementLocked() && !this.isSitting() && this.targetBedPos == null && !this.isFollowing() && !this.isRoaming());
      super.tick();
   }
}
