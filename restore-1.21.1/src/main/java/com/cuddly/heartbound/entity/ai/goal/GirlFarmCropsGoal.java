package com.cuddly.heartbound.entity.ai.goal;

import com.cuddly.heartbound.entity.base.tamable.TameableGirlEntity;
import com.cuddly.heartbound.networking.S2C.PlayAttackAnimationS2CPacket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CocoaBlock;
import net.minecraft.block.CropBlock;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.Goal.Control;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.world.World;

public class GirlFarmCropsGoal extends Goal {
   private final TameableGirlEntity girl;
   private final World world;
   private final double speed;
   private BlockPos targetPos;
   private int farmingTicks;
   private int collectingTicks;
   private GirlFarmCropsGoal.FarmingState state = GirlFarmCropsGoal.FarmingState.MOVING_TO_CROP;
   private int searchCooldown;
   private int movingTicks;
   private static final int FARMING_DURATION = 20;
   private static final int COLLECTING_DURATION = 15;
   private static final int SEARCH_COOLDOWN_DURATION = 40;
   private static final int SEARCH_RANGE = 10;
   private static final double COLLECT_RANGE = 2.5;
   private static final int MOVING_TIMEOUT = 100;

   public GirlFarmCropsGoal(TameableGirlEntity girl, double speed) {
      this.girl = girl;
      this.world = girl.getWorld();
      this.speed = speed;
      this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
   }

   @Override
   public boolean canStart() {
      if (!this.girl.isTamed()
         || !this.girl.isRoaming()
         || this.girl.isFollowing()
         || this.girl.isSitting()
         || this.girl.hasVehicle()
         || this.girl.isHavingSex()) {
         return false;
      } else if (this.searchCooldown > 0) {
         this.searchCooldown--;
         return false;
      } else {
         this.targetPos = this.findNearbyFarmableCrop();
         if (this.targetPos != null) {
            this.state = GirlFarmCropsGoal.FarmingState.MOVING_TO_CROP;
            return true;
         } else {
            this.searchCooldown = 40;
            return false;
         }
      }
   }

   @Override
   public boolean shouldContinue() {
      if (!this.girl.isRoaming() || this.girl.isFollowing() || this.girl.isSitting() || this.girl.hasVehicle() || this.girl.isHavingSex()) {
         return false;
      } else if (this.state == GirlFarmCropsGoal.FarmingState.COLLECTING) {
         return true;
      } else if (this.targetPos == null) {
         return false;
      } else {
         BlockState blockState = this.world.getBlockState(this.targetPos);
         return this.isFarmableBlock(blockState, this.targetPos);
      }
   }

   @Override
   public void start() {
      this.farmingTicks = 0;
      this.collectingTicks = 0;
      this.movingTicks = 0;
      this.state = GirlFarmCropsGoal.FarmingState.MOVING_TO_CROP;
      if (this.targetPos != null) {
         this.girl
            .getNavigation()
            .startMovingTo((double)this.targetPos.getX() + 0.5, (double)this.targetPos.getY(), (double)this.targetPos.getZ() + 0.5, this.speed);
      }
   }

   @Override
   public void tick() {
      if (this.targetPos != null || this.state == GirlFarmCropsGoal.FarmingState.COLLECTING) {
         switch (this.state) {
            case MOVING_TO_CROP:
               this.tickMoving();
               break;
            case HARVESTING:
               this.tickHarvesting();
               break;
            case COLLECTING:
               this.tickCollecting();
         }
      }
   }

   private void tickMoving() {
      this.girl.getLookControl().lookAt((double)this.targetPos.getX() + 0.5, (double)this.targetPos.getY(), (double)this.targetPos.getZ() + 0.5);
      this.movingTicks++;
      double distance = this.girl.squaredDistanceTo((double)this.targetPos.getX() + 0.5, (double)this.targetPos.getY(), (double)this.targetPos.getZ() + 0.5);
      if (distance <= 2.25) {
         this.girl.getNavigation().stop();
         this.state = GirlFarmCropsGoal.FarmingState.HARVESTING;
         this.farmingTicks = 0;
         this.movingTicks = 0;
      } else if (this.movingTicks >= 100) {
         this.targetPos = null;
         this.movingTicks = 0;
      } else if (this.girl.getNavigation().isIdle()) {
         this.girl
            .getNavigation()
            .startMovingTo((double)this.targetPos.getX() + 0.5, (double)this.targetPos.getY(), (double)this.targetPos.getZ() + 0.5, this.speed);
      }
   }

   private void tickHarvesting() {
      this.girl.getLookControl().lookAt((double)this.targetPos.getX() + 0.5, (double)this.targetPos.getY(), (double)this.targetPos.getZ() + 0.5);
      this.farmingTicks++;
      if (this.farmingTicks >= 20) {
         this.harvestCrop(this.targetPos);
         this.state = GirlFarmCropsGoal.FarmingState.COLLECTING;
         this.collectingTicks = 0;
      }
   }

   private void tickCollecting() {
      this.collectingTicks++;
      if (this.collectingTicks >= 15) {
         this.collectNearbyItems();
         if (this.isBackpackFull()) {
            this.targetPos = null;
            this.state = GirlFarmCropsGoal.FarmingState.MOVING_TO_CROP;
         } else {
            this.targetPos = this.findNearbyFarmableCrop();
            if (this.targetPos != null) {
               this.state = GirlFarmCropsGoal.FarmingState.MOVING_TO_CROP;
               this.farmingTicks = 0;
               this.collectingTicks = 0;
               this.girl
                  .getNavigation()
                  .startMovingTo((double)this.targetPos.getX() + 0.5, (double)this.targetPos.getY(), (double)this.targetPos.getZ() + 0.5, this.speed);
            } else {
               this.state = GirlFarmCropsGoal.FarmingState.MOVING_TO_CROP;
            }
         }
      }
   }

   @Override
   public void stop() {
      this.targetPos = null;
      this.farmingTicks = 0;
      this.collectingTicks = 0;
      this.state = GirlFarmCropsGoal.FarmingState.MOVING_TO_CROP;
      this.girl.getNavigation().stop();
      this.searchCooldown = 40;
   }

   private void collectNearbyItems() {
      Box searchBox = this.girl.getBoundingBox().expand(2.5);

      for (ItemEntity itemEntity : this.world.getEntitiesByClass(ItemEntity.class, searchBox, item -> item.isAlive() && item.getItemAge() >= 10)) {
         ItemStack stack = itemEntity.getStack().copy();
         boolean added = this.addToInventory(stack);
         if (added || stack.isEmpty()) {
            itemEntity.discard();
            this.world
               .playSound(
                  null,
                  this.girl.getX(),
                  this.girl.getY(),
                  this.girl.getZ(),
                  SoundEvents.ENTITY_ITEM_PICKUP,
                  this.girl.getSoundCategory(),
                  0.2F,
                  (this.girl.getRandom().nextFloat() - this.girl.getRandom().nextFloat()) * 1.4F + 2.0F
               );
         }
      }
   }

   private boolean addToInventory(ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      } else {
         for (int i = 5; i <= 28; i++) {
            ItemStack slotStack = this.girl.inventory.getStack(i);
            if (!slotStack.isEmpty() && ItemStack.areItemsAndComponentsEqual(slotStack, stack)) {
               int maxCount = Math.min(stack.getMaxCount(), this.girl.inventory.getMaxCount(stack));
               int available = maxCount - slotStack.getCount();
               if (available > 0) {
                  int transfer = Math.min(available, stack.getCount());
                  slotStack.increment(transfer);
                  stack.decrement(transfer);
                  this.girl.inventory.markDirty();
                  if (stack.isEmpty()) {
                     return true;
                  }
               }
            }
         }

         for (int ix = 5; ix <= 28; ix++) {
            ItemStack slotStack = this.girl.inventory.getStack(ix);
            if (slotStack.isEmpty()) {
               this.girl.inventory.setStack(ix, stack.copy());
               stack.setCount(0);
               return true;
            }
         }

         return false;
      }
   }

   private boolean isBackpackFull() {
      for (int i = 5; i <= 28; i++) {
         if (this.girl.inventory.getStack(i).isEmpty()) {
            return false;
         }
      }

      return true;
   }

   private BlockPos findNearbyFarmableCrop() {
      BlockPos girlPos = this.girl.getBlockPos();
      Mutable mutablePos = new Mutable();
      List<BlockPos> candidates = new ArrayList<>();

      for (int x = -10; x <= 10; x++) {
         for (int y = -2; y <= 2; y++) {
            for (int z = -10; z <= 10; z++) {
               mutablePos.set(girlPos.getX() + x, girlPos.getY() + y, girlPos.getZ() + z);
               BlockState blockState = this.world.getBlockState(mutablePos);
               if (this.isFarmableBlock(blockState, mutablePos) && this.isFullyGrown(blockState)) {
                  candidates.add(mutablePos.toImmutable());
               }
            }
         }
      }

      candidates.sort(Comparator.comparingDouble(girlPos::getSquaredDistance));

      for (BlockPos pos : candidates) {
         if (this.girl.getNavigation().findPathTo(pos, 1) != null) {
            return pos;
         }
      }

      return null;
   }

   private boolean isFarmableBlock(BlockState state, BlockPos pos) {
      Block block = state.getBlock();
      if (block instanceof CropBlock
         || block instanceof NetherWartBlock
         || block instanceof SweetBerryBushBlock
         || block instanceof CocoaBlock
         || block == Blocks.MELON
         || block == Blocks.PUMPKIN
         || block == Blocks.CARVED_PUMPKIN) {
         return true;
      } else if (findAgeProperty(state) == null) {
         return false;
      } else {
         Block below = this.world.getBlockState(pos.down()).getBlock();
         return below instanceof FarmlandBlock || below == Blocks.SOUL_SAND;
      }
   }

   private boolean isFullyGrown(BlockState state) {
      Block block = state.getBlock();
      if (block instanceof CropBlock cropBlock) {
         return cropBlock.isMature(state);
      } else if (block instanceof NetherWartBlock) {
         return state.get(NetherWartBlock.AGE) >= 3;
      } else if (block instanceof SweetBerryBushBlock) {
         return state.get(SweetBerryBushBlock.AGE) >= 3;
      } else if (block instanceof CocoaBlock) {
         return state.get(CocoaBlock.AGE) >= 2;
      } else if (block != Blocks.MELON && block != Blocks.PUMPKIN) {
         IntProperty ageProperty = findAgeProperty(state);
         if (ageProperty != null) {
            int currentAge = state.get(ageProperty);
            int maxAge = Collections.max(ageProperty.getValues());
            return currentAge >= maxAge;
         } else {
            return false;
         }
      } else {
         return true;
      }
   }

   private void harvestCrop(BlockPos pos) {
      if (this.world instanceof ServerWorld serverWorld) {
         BlockState state = this.world.getBlockState(pos);
         Block block = state.getBlock();
         this.girl.triggerSwing();

         for (ServerPlayerEntity player : serverWorld.getPlayers()) {
            ServerPlayNetworking.send(player, new PlayAttackAnimationS2CPacket(this.girl.getId()));
         }

         this.world.playSound(null, pos, SoundEvents.BLOCK_CROP_BREAK, SoundCategory.BLOCKS, 1.0F, 1.0F);
         if (block instanceof CropBlock cropBlock) {
            for (ItemStack drop : Block.getDroppedStacks(state, serverWorld, pos, null, this.girl, this.girl.getMainHandStack())) {
               ItemEntity itemEntity = new ItemEntity(this.world, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, drop);
               itemEntity.setVelocity(0.0, 0.05, 0.0);
               itemEntity.setPickupDelay(10);
               this.world.spawnEntity(itemEntity);
            }

            this.world.setBlockState(pos, cropBlock.getDefaultState(), 3);
         } else if (block instanceof NetherWartBlock) {
            for (ItemStack drop : Block.getDroppedStacks(state, serverWorld, pos, null, this.girl, this.girl.getMainHandStack())) {
               ItemEntity itemEntity = new ItemEntity(this.world, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, drop);
               itemEntity.setPickupDelay(10);
               this.world.spawnEntity(itemEntity);
            }

            this.world.setBlockState(pos, Blocks.NETHER_WART.getDefaultState(), 3);
         } else if (block instanceof SweetBerryBushBlock) {
            ItemStack berries = new ItemStack(Items.SWEET_BERRIES, 2 + this.world.random.nextInt(2));
            ItemEntity itemEntity = new ItemEntity(this.world, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, berries);
            itemEntity.setPickupDelay(10);
            this.world.spawnEntity(itemEntity);
            this.world.setBlockState(pos, state.with(SweetBerryBushBlock.AGE, Integer.valueOf(1)), 3);
         } else if (block instanceof CocoaBlock) {
            for (ItemStack drop : Block.getDroppedStacks(state, serverWorld, pos, null, this.girl, this.girl.getMainHandStack())) {
               ItemEntity itemEntity = new ItemEntity(this.world, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, drop);
               itemEntity.setPickupDelay(10);
               this.world.spawnEntity(itemEntity);
            }

            this.world.setBlockState(pos, block.getDefaultState().with(CocoaBlock.FACING, state.get(CocoaBlock.FACING)), 3);
         } else if (block != Blocks.MELON && block != Blocks.PUMPKIN && block != Blocks.CARVED_PUMPKIN) {
            IntProperty ageProperty = findAgeProperty(state);
            if (ageProperty != null) {
               for (ItemStack drop : Block.getDroppedStacks(state, serverWorld, pos, null, this.girl, this.girl.getMainHandStack())) {
                  ItemEntity itemEntity = new ItemEntity(this.world, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, drop);
                  itemEntity.setPickupDelay(10);
                  this.world.spawnEntity(itemEntity);
               }

               int minAge = Collections.min(ageProperty.getValues());
               this.world.setBlockState(pos, state.with(ageProperty, Integer.valueOf(minAge)), 3);
            }
         } else {
            for (ItemStack drop : Block.getDroppedStacks(state, serverWorld, pos, null, this.girl, this.girl.getMainHandStack())) {
               ItemEntity itemEntity = new ItemEntity(this.world, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, drop);
               itemEntity.setPickupDelay(10);
               this.world.spawnEntity(itemEntity);
            }

            this.world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
         }
      }
   }

   private static IntProperty findAgeProperty(BlockState state) {
      for (Property<?> property : state.getProperties()) {
         if (property instanceof IntProperty intProperty && property.getName().equals("age")) {
            return intProperty;
         }
      }

      return null;
   }

   private static enum FarmingState {
      MOVING_TO_CROP,
      HARVESTING,
      COLLECTING;
   }
}
