package com.cuddly.heartbound.entity.ai.goal;

import com.cuddly.heartbound.entity.base.tamable.TameableGirlEntity;
import java.util.Collections;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
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

public class GirlRoamAtBaseGoal extends Goal {
   private final TameableGirlEntity girl;
   private final double speed;
   private final Level world;
   private static final int ROAMING_RANGE = 15;
   private static final int WANDER_INTERVAL = 100;
   private int wanderCooldown;

   public GirlRoamAtBaseGoal(TameableGirlEntity girl, double speed) {
      this.girl = girl;
      this.world = girl.level();
      this.speed = speed;
      this.setFlags(EnumSet.of(Flag.MOVE));
   }

   @Override
   public boolean canUse() {
      if (!this.girl.isTamed()
         || !this.girl.isRoaming()
         || this.girl.isSitting()
         || this.girl.isFollowing()
         || this.girl.isHavingSex()
         || this.girl.isPassenger()
         || this.girl.isChopping()) {
         return false;
      } else if (this.wanderCooldown > 0) {
         this.wanderCooldown--;
         return false;
      } else if (this.hasNearbyCropsToHarvest()) {
         return false;
      } else {
         BlockPos basePos = this.girl.getBasePos();
         double distanceFromBase = this.girl.distanceToSqr((double)basePos.getX() + 0.5, (double)basePos.getY(), (double)basePos.getZ() + 0.5);
         if (distanceFromBase > 225.0) {
            this.girl.getNavigation().moveTo((double)basePos.getX() + 0.5, (double)basePos.getY(), (double)basePos.getZ() + 0.5, this.speed);
            this.wanderCooldown = 20;
            return false;
         } else {
            return true;
         }
      }
   }

   @Override
   public boolean canContinueToUse() {
      return this.girl.isRoaming()
            && !this.girl.isFollowing()
            && !this.girl.isSitting()
            && !this.girl.isHavingSex()
            && !this.girl.isPassenger()
            && !this.girl.isChopping()
         ? !this.girl.getNavigation().isDone()
         : false;
   }

   @Override
   public void start() {
      BlockPos basePos = this.girl.getBasePos();
      BlockPos targetPos = this.getRandomPosNearBase(basePos);
      if (targetPos != null) {
         this.girl.getNavigation().moveTo((double)targetPos.getX() + 0.5, (double)targetPos.getY(), (double)targetPos.getZ() + 0.5, this.speed);
      }

      this.wanderCooldown = 100;
   }

   @Override
   public void stop() {
      this.wanderCooldown = 100;
   }

   private BlockPos getRandomPosNearBase(BlockPos basePos) {
      for (int attempt = 0; attempt < 10; attempt++) {
         int xOffset = this.girl.getRandom().nextInt(31) - 15;
         int zOffset = this.girl.getRandom().nextInt(31) - 15;
         BlockPos targetPos = basePos.offset(xOffset, 0, zOffset);
         BlockPos groundPos = this.findGroundLevel(targetPos);
         if (groundPos != null && Math.abs(groundPos.getY() - basePos.getY()) <= 1 && this.girl.getNavigation().createPath(groundPos, 1) != null) {
            return groundPos;
         }
      }

      return null;
   }

   private BlockPos findGroundLevel(BlockPos pos) {
      for (int y = 3; y >= -3; y--) {
         BlockPos check = pos.offset(0, y, 0);
         if (!this.world.getBlockState(check.below()).isAir() && this.world.getBlockState(check).isAir() && this.world.getBlockState(check.above()).isAir()) {
            return check;
         }
      }

      return null;
   }

   private boolean hasNearbyCropsToHarvest() {
      BlockPos girlPos = this.girl.blockPosition();
      MutableBlockPos mutablePos = new MutableBlockPos();

      for (int x = -10; x <= 10; x++) {
         for (int y = -2; y <= 2; y++) {
            for (int z = -10; z <= 10; z++) {
               mutablePos.set(girlPos.getX() + x, girlPos.getY() + y, girlPos.getZ() + z);
               BlockState state = this.world.getBlockState(mutablePos);
               if (this.isFarmableBlock(state, mutablePos) && this.isFullyGrown(state)) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private boolean isFarmableBlock(BlockState state, BlockPos pos) {
      Block block = state.getBlock();
      if (block instanceof CropBlock || block instanceof NetherWartBlock || block instanceof SweetBerryBushBlock || block instanceof CocoaBlock) {
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
      } else {
         IntegerProperty ageProperty = findAgeProperty(state);
         if (ageProperty != null) {
            int currentAge = state.getValue(ageProperty);
            int maxAge = Collections.max(ageProperty.getPossibleValues());
            return currentAge >= maxAge;
         } else {
            return false;
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
}
