package com.cuddly.heartbound.entity.ai.goal;

import com.cuddly.heartbound.entity.base.tamable.TameableGirlEntity;
import com.cuddly.heartbound.networking.S2C.PlayAttackAnimationS2CPacket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;

public class GirlFarmCropsGoal extends Goal {
   private final TameableGirlEntity girl;
   private final Level world;
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
      this.world = girl.level();
      this.speed = speed;
      this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
   }

   @Override
   public boolean canUse() {
      if (!this.girl.isTamed()
         || !this.girl.isRoaming()
         || this.girl.isFollowing()
         || this.girl.isSitting()
         || this.girl.isPassenger()
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
   public boolean canContinueToUse() {
      if (!this.girl.isRoaming() || this.girl.isFollowing() || this.girl.isSitting() || this.girl.isPassenger() || this.girl.isHavingSex()) {
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
            .moveTo((double)this.targetPos.getX() + 0.5, (double)this.targetPos.getY(), (double)this.targetPos.getZ() + 0.5, this.speed);
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
      this.girl.getLookControl().setLookAt((double)this.targetPos.getX() + 0.5, (double)this.targetPos.getY(), (double)this.targetPos.getZ() + 0.5);
      this.movingTicks++;
      double distance = this.girl.distanceToSqr((double)this.targetPos.getX() + 0.5, (double)this.targetPos.getY(), (double)this.targetPos.getZ() + 0.5);
      if (distance <= 2.25) {
         this.girl.getNavigation().stop();
         this.state = GirlFarmCropsGoal.FarmingState.HARVESTING;
         this.farmingTicks = 0;
         this.movingTicks = 0;
      } else if (this.movingTicks >= 100) {
         this.targetPos = null;
         this.movingTicks = 0;
      } else if (this.girl.getNavigation().isDone()) {
         this.girl
            .getNavigation()
            .moveTo((double)this.targetPos.getX() + 0.5, (double)this.targetPos.getY(), (double)this.targetPos.getZ() + 0.5, this.speed);
      }
   }

   private void tickHarvesting() {
      this.girl.getLookControl().setLookAt((double)this.targetPos.getX() + 0.5, (double)this.targetPos.getY(), (double)this.targetPos.getZ() + 0.5);
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
                  .moveTo((double)this.targetPos.getX() + 0.5, (double)this.targetPos.getY(), (double)this.targetPos.getZ() + 0.5, this.speed);
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
      AABB searchBox = this.girl.getBoundingBox().inflate(2.5);

      for (ItemEntity itemEntity : this.world.getEntitiesOfClass(ItemEntity.class, searchBox, item -> item.isAlive() && item.getAge() >= 10)) {
         ItemStack stack = itemEntity.getItem().copy();
         boolean added = this.addToInventory(stack);
         if (added || stack.isEmpty()) {
            itemEntity.discard();
            this.world
               .playSound(
                  null,
                  this.girl.getX(),
                  this.girl.getY(),
                  this.girl.getZ(),
                  SoundEvents.ITEM_PICKUP,
                  this.girl.getSoundSource(),
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
            ItemStack slotStack = this.girl.inventory.getItem(i);
            if (!slotStack.isEmpty() && ItemStack.isSameItemSameComponents(slotStack, stack)) {
               int maxCount = Math.min(stack.getMaxStackSize(), this.girl.inventory.getMaxStackSize(stack));
               int available = maxCount - slotStack.getCount();
               if (available > 0) {
                  int transfer = Math.min(available, stack.getCount());
                  slotStack.grow(transfer);
                  stack.shrink(transfer);
                  this.girl.inventory.setChanged();
                  if (stack.isEmpty()) {
                     return true;
                  }
               }
            }
         }

         for (int ix = 5; ix <= 28; ix++) {
            ItemStack slotStack = this.girl.inventory.getItem(ix);
            if (slotStack.isEmpty()) {
               this.girl.inventory.setItem(ix, stack.copy());
               stack.setCount(0);
               return true;
            }
         }

         return false;
      }
   }

   private boolean isBackpackFull() {
      for (int i = 5; i <= 28; i++) {
         if (this.girl.inventory.getItem(i).isEmpty()) {
            return false;
         }
      }

      return true;
   }

   private BlockPos findNearbyFarmableCrop() {
      BlockPos girlPos = this.girl.blockPosition();
      MutableBlockPos mutablePos = new MutableBlockPos();
      List<BlockPos> candidates = new ArrayList<>();

      for (int x = -10; x <= 10; x++) {
         for (int y = -2; y <= 2; y++) {
            for (int z = -10; z <= 10; z++) {
               mutablePos.set(girlPos.getX() + x, girlPos.getY() + y, girlPos.getZ() + z);
               BlockState blockState = this.world.getBlockState(mutablePos);
               if (this.isFarmableBlock(blockState, mutablePos) && this.isFullyGrown(blockState)) {
                  candidates.add(mutablePos.immutable());
               }
            }
         }
      }

      candidates.sort(Comparator.comparingDouble(girlPos::distSqr));

      for (BlockPos pos : candidates) {
         if (this.girl.getNavigation().createPath(pos, 1) != null) {
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
         Block below = this.world.getBlockState(pos.below()).getBlock();
         return below instanceof FarmlandBlock || below == Blocks.SOUL_SAND;
      }
   }

   private boolean isFullyGrown(BlockState state) {
      Block block = state.getBlock();
      if (block instanceof CropBlock cropBlock) {
         return cropBlock.isMaxAge(state);
      } else if (block instanceof NetherWartBlock) {
         return state.getValue(NetherWartBlock.AGE) >= 3;
      } else if (block instanceof SweetBerryBushBlock) {
         return state.getValue(SweetBerryBushBlock.AGE) >= 3;
      } else if (block instanceof CocoaBlock) {
         return state.getValue(CocoaBlock.AGE) >= 2;
      } else if (block != Blocks.MELON && block != Blocks.PUMPKIN) {
         IntegerProperty ageProperty = findAgeProperty(state);
         if (ageProperty != null) {
            int currentAge = state.getValue(ageProperty);
            int maxAge = Collections.max(ageProperty.getPossibleValues());
            return currentAge >= maxAge;
         } else {
            return false;
         }
      } else {
         return true;
      }
   }

   private void harvestCrop(BlockPos pos) {
      if (this.world instanceof ServerLevel serverWorld) {
         BlockState state = this.world.getBlockState(pos);
         Block block = state.getBlock();
         this.girl.triggerSwing();

         for (ServerPlayer player : serverWorld.players()) {
            ServerPlayNetworking.send(player, new PlayAttackAnimationS2CPacket(this.girl.getId()));
         }

         this.world.playSound(null, pos, SoundEvents.CROP_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
         if (block instanceof CropBlock cropBlock) {
            for (ItemStack drop : Block.getDrops(state, serverWorld, pos, null, this.girl, this.girl.getMainHandItem())) {
               ItemEntity itemEntity = new ItemEntity(this.world, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, drop);
               itemEntity.setDeltaMovement(0.0, 0.05, 0.0);
               itemEntity.setPickUpDelay(10);
               this.world.addFreshEntity(itemEntity);
            }

            this.world.setBlock(pos, cropBlock.defaultBlockState(), 3);
         } else if (block instanceof NetherWartBlock) {
            for (ItemStack drop : Block.getDrops(state, serverWorld, pos, null, this.girl, this.girl.getMainHandItem())) {
               ItemEntity itemEntity = new ItemEntity(this.world, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, drop);
               itemEntity.setPickUpDelay(10);
               this.world.addFreshEntity(itemEntity);
            }

            this.world.setBlock(pos, Blocks.NETHER_WART.defaultBlockState(), 3);
         } else if (block instanceof SweetBerryBushBlock) {
            ItemStack berries = new ItemStack(Items.SWEET_BERRIES, 2 + this.world.getRandom().nextInt(2));
            ItemEntity itemEntity = new ItemEntity(this.world, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, berries);
            itemEntity.setPickUpDelay(10);
            this.world.addFreshEntity(itemEntity);
            this.world.setBlock(pos, state.setValue(SweetBerryBushBlock.AGE, Integer.valueOf(1)), 3);
         } else if (block instanceof CocoaBlock) {
            for (ItemStack drop : Block.getDrops(state, serverWorld, pos, null, this.girl, this.girl.getMainHandItem())) {
               ItemEntity itemEntity = new ItemEntity(this.world, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, drop);
               itemEntity.setPickUpDelay(10);
               this.world.addFreshEntity(itemEntity);
            }

            this.world.setBlock(pos, block.defaultBlockState().setValue(CocoaBlock.FACING, state.getValue(CocoaBlock.FACING)), 3);
         } else if (block != Blocks.MELON && block != Blocks.PUMPKIN && block != Blocks.CARVED_PUMPKIN) {
            IntegerProperty ageProperty = findAgeProperty(state);
            if (ageProperty != null) {
               for (ItemStack drop : Block.getDrops(state, serverWorld, pos, null, this.girl, this.girl.getMainHandItem())) {
                  ItemEntity itemEntity = new ItemEntity(this.world, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, drop);
                  itemEntity.setPickUpDelay(10);
                  this.world.addFreshEntity(itemEntity);
               }

               int minAge = Collections.min(ageProperty.getPossibleValues());
               this.world.setBlock(pos, state.setValue(ageProperty, Integer.valueOf(minAge)), 3);
            }
         } else {
            for (ItemStack drop : Block.getDrops(state, serverWorld, pos, null, this.girl, this.girl.getMainHandItem())) {
               ItemEntity itemEntity = new ItemEntity(this.world, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, drop);
               itemEntity.setPickUpDelay(10);
               this.world.addFreshEntity(itemEntity);
            }

            this.world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
         }
      }
   }

   private static IntegerProperty findAgeProperty(BlockState state) {
      for (Property<?> property : state.getProperties()) {
         if (property instanceof IntegerProperty intProperty && property.getName().equals("age")) {
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
