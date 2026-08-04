package com.cuddly.heartbound.entity.ai.goal;

import com.cuddly.heartbound.entity.base.tamable.TameableGirlEntity;
import java.util.Collections;
import java.util.EnumSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CocoaBlock;
import net.minecraft.block.CropBlock;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.Goal.Control;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.world.World;

public class GirlRoamAtBaseGoal extends Goal {
   private final TameableGirlEntity girl;
   private final double speed;
   private final World world;
   private static final int ROAMING_RANGE = 15;
   private static final int WANDER_INTERVAL = 100;
   private int wanderCooldown;

   public GirlRoamAtBaseGoal(TameableGirlEntity girl, double speed) {
      this.girl = girl;
      this.world = girl.getWorld();
      this.speed = speed;
      this.setControls(EnumSet.of(Control.MOVE));
   }

   @Override
   public boolean canStart() {
      if (!this.girl.isTamed()
         || !this.girl.isRoaming()
         || this.girl.isSitting()
         || this.girl.isFollowing()
         || this.girl.isHavingSex()
         || this.girl.hasVehicle()
         || this.girl.isChopping()) {
         return false;
      } else if (this.wanderCooldown > 0) {
         this.wanderCooldown--;
         return false;
      } else if (this.hasNearbyCropsToHarvest()) {
         return false;
      } else {
         BlockPos basePos = this.girl.getBasePos();
         double distanceFromBase = this.girl.squaredDistanceTo((double)basePos.getX() + 0.5, (double)basePos.getY(), (double)basePos.getZ() + 0.5);
         if (distanceFromBase > 225.0) {
            this.girl.getNavigation().startMovingTo((double)basePos.getX() + 0.5, (double)basePos.getY(), (double)basePos.getZ() + 0.5, this.speed);
            this.wanderCooldown = 20;
            return false;
         } else {
            return true;
         }
      }
   }

   @Override
   public boolean shouldContinue() {
      return this.girl.isRoaming()
            && !this.girl.isFollowing()
            && !this.girl.isSitting()
            && !this.girl.isHavingSex()
            && !this.girl.hasVehicle()
            && !this.girl.isChopping()
         ? !this.girl.getNavigation().isIdle()
         : false;
   }

   @Override
   public void start() {
      BlockPos basePos = this.girl.getBasePos();
      BlockPos targetPos = this.getRandomPosNearBase(basePos);
      if (targetPos != null) {
         this.girl.getNavigation().startMovingTo((double)targetPos.getX() + 0.5, (double)targetPos.getY(), (double)targetPos.getZ() + 0.5, this.speed);
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
         BlockPos targetPos = basePos.add(xOffset, 0, zOffset);
         BlockPos groundPos = this.findGroundLevel(targetPos);
         if (groundPos != null && Math.abs(groundPos.getY() - basePos.getY()) <= 1 && this.girl.getNavigation().findPathTo(groundPos, 1) != null) {
            return groundPos;
         }
      }

      return null;
   }

   private BlockPos findGroundLevel(BlockPos pos) {
      for (int y = 3; y >= -3; y--) {
         BlockPos check = pos.add(0, y, 0);
         if (!this.world.getBlockState(check.down()).isAir() && this.world.getBlockState(check).isAir() && this.world.getBlockState(check.up()).isAir()) {
            return check;
         }
      }

      return null;
   }

   private boolean hasNearbyCropsToHarvest() {
      BlockPos girlPos = this.girl.getBlockPos();
      Mutable mutablePos = new Mutable();

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
      } else {
         IntProperty ageProperty = findAgeProperty(state);
         if (ageProperty != null) {
            int currentAge = state.get(ageProperty);
            int maxAge = Collections.max(ageProperty.getValues());
            return currentAge >= maxAge;
         } else {
            return false;
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
}
