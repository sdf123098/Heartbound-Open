package com.cuddly.heartbound.entity.ai.goal;

import com.cuddly.heartbound.entity.base.tamable.TameableGirlEntity;
import com.cuddly.heartbound.networking.S2C.PlayAttackAnimationS2CPacket;
import java.util.ArrayList;
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
import net.minecraft.world.level.block.state.BlockState;

public class GirlMineOresGoal extends Goal {
   private final TameableGirlEntity girl;
   private final Level world;
   private final double speed;
   private BlockPos targetOrePos;
   private int miningTicks;
   private static final int MINING_DURATION = 30;
   private int miningDurationThreshold = 30;
   private int searchCooldown;
   private static final int SEARCH_COOLDOWN_DURATION = 40;
   private static final int SEARCH_RANGE = 12;
   private boolean isMining;

   public GirlMineOresGoal(TameableGirlEntity girl, double speed) {
      this.girl = girl;
      this.world = girl.level();
      this.speed = speed;
      this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
   }

   @Override
   public boolean canUse() {
      if (!this.girl.isTamed()
         || !this.girl.isMining()
         || !this.girl.isFollowing()
         || this.girl.isSitting()
         || this.girl.isPassenger()
         || this.girl.isHavingSex()) {
         return false;
      } else if (this.searchCooldown > 0) {
         this.searchCooldown--;
         return false;
      } else {
         this.targetOrePos = this.findNearbyExposedOre();
         if (this.targetOrePos != null) {
            this.searchCooldown = 40;
            return true;
         } else {
            this.searchCooldown = 40;
            return false;
         }
      }
   }

   @Override
   public boolean canContinueToUse() {
      if (this.targetOrePos == null) {
         return false;
      } else {
         return this.girl.isMining() && this.girl.isFollowing() && !this.girl.isSitting() && !this.girl.isPassenger() && !this.girl.isHavingSex()
            ? this.isMining
               || !this.girl.getNavigation().isDone()
               || this.girl.distanceToSqr((double)this.targetOrePos.getX() + 0.5, (double)this.targetOrePos.getY(), (double)this.targetOrePos.getZ() + 0.5)
                  > 6.25
            : false;
      }
   }

   @Override
   public void start() {
      this.miningTicks = 0;
      this.isMining = false;
      if (this.targetOrePos != null) {
         this.girl
            .getNavigation()
            .moveTo((double)this.targetOrePos.getX() + 0.5, (double)this.targetOrePos.getY(), (double)this.targetOrePos.getZ() + 0.5, this.speed);
      }
   }

   @Override
   public void tick() {
      if (this.targetOrePos != null) {
         this.girl.getLookControl().setLookAt((double)this.targetOrePos.getX() + 0.5, (double)this.targetOrePos.getY(), (double)this.targetOrePos.getZ() + 0.5);
         double distance = this.girl
            .distanceToSqr((double)this.targetOrePos.getX() + 0.5, (double)this.targetOrePos.getY(), (double)this.targetOrePos.getZ() + 0.5);
         if (this.isMining) {
            this.miningTicks++;
            if (this.miningTicks >= this.miningDurationThreshold) {
               this.mineOre();
               this.isMining = false;
               this.miningTicks = 0;
               this.targetOrePos = this.findNearbyExposedOre();
               if (this.targetOrePos != null) {
                  this.girl
                     .getNavigation()
                     .moveTo(
                        (double)this.targetOrePos.getX() + 0.5, (double)this.targetOrePos.getY(), (double)this.targetOrePos.getZ() + 0.5, this.speed
                     );
               }
            }
         } else if (distance <= 6.25) {
            this.girl.getNavigation().stop();
            this.miningDurationThreshold = this.computeMiningDurationForPos(this.targetOrePos, this.girl.getMainHandItem());
            this.isMining = true;
            this.miningTicks = 0;
         } else if (this.girl.getNavigation().isDone()) {
            this.girl
               .getNavigation()
               .moveTo((double)this.targetOrePos.getX() + 0.5, (double)this.targetOrePos.getY(), (double)this.targetOrePos.getZ() + 0.5, this.speed);
         }
      }
   }

   @Override
   public void stop() {
      this.targetOrePos = null;
      this.miningTicks = 0;
      this.isMining = false;
      this.girl.getNavigation().stop();
   }

   private BlockPos findNearbyExposedOre() {
      BlockPos girlPos = this.girl.blockPosition();
      MutableBlockPos mutablePos = new MutableBlockPos();
      List<BlockPos> ores = new ArrayList<>();

      for (int x = -12; x <= 12; x++) {
         for (int y = -2; y <= 4; y++) {
            for (int z = -12; z <= 12; z++) {
               mutablePos.set(girlPos.getX() + x, girlPos.getY() + y, girlPos.getZ() + z);
               if (this.isExposedOre(mutablePos)) {
                  ores.add(mutablePos.immutable());
               }
            }
         }
      }

      if (ores.isEmpty()) {
         return null;
      } else {
         BlockPos closest = null;
         double bestDist = Double.MAX_VALUE;

         for (BlockPos pos : ores) {
            double dist = this.girl.distanceToSqr((double)pos.getX() + 0.5, (double)pos.getY(), (double)pos.getZ() + 0.5);
            if (dist < bestDist && this.girl.getNavigation().createPath(pos, 1) != null) {
               closest = pos;
               bestDist = dist;
            }
         }

         return closest;
      }
   }

   private boolean isExposedOre(BlockPos pos) {
      BlockState state = this.world.getBlockState(pos);
      Block block = state.getBlock();
      return !this.isOreBlock(block)
         ? false
         : this.world.isEmptyBlock(pos.above())
            || this.world.isEmptyBlock(pos.north())
            || this.world.isEmptyBlock(pos.south())
            || this.world.isEmptyBlock(pos.east())
            || this.world.isEmptyBlock(pos.west());
   }

   private boolean isOreBlock(Block block) {
      return block == Blocks.COAL_ORE
         || block == Blocks.IRON_ORE
         || block == Blocks.GOLD_ORE
         || block == Blocks.DIAMOND_ORE
         || block == Blocks.COPPER_ORE
         || block == Blocks.REDSTONE_ORE
         || block == Blocks.LAPIS_ORE
         || block == Blocks.EMERALD_ORE
         || block == Blocks.NETHER_QUARTZ_ORE
         || block == Blocks.DEEPSLATE_COAL_ORE
         || block == Blocks.DEEPSLATE_IRON_ORE
         || block == Blocks.DEEPSLATE_GOLD_ORE
         || block == Blocks.DEEPSLATE_DIAMOND_ORE
         || block == Blocks.DEEPSLATE_COPPER_ORE
         || block == Blocks.DEEPSLATE_REDSTONE_ORE
         || block == Blocks.DEEPSLATE_LAPIS_ORE
         || block == Blocks.DEEPSLATE_EMERALD_ORE;
   }

   private int computeMiningDurationForPos(BlockPos pos, ItemStack stack) {
      int base = 30;
      if (stack != null && !stack.isEmpty()) {
         if (stack.getItem() == Items.DIAMOND_PICKAXE || stack.getItem() == Items.NETHERITE_PICKAXE) {
            base = 20;
         }

         return base;
      } else {
         return base;
      }
   }

   private void mineOre() {
      if (this.world instanceof ServerLevel serverWorld) {
         if (this.targetOrePos != null) {
            BlockState state = this.world.getBlockState(this.targetOrePos);
            if (this.isOreBlock(state.getBlock())) {
               this.girl.triggerSwing();

               for (ServerPlayer player : serverWorld.players()) {
                  ServerPlayNetworking.send(player, new PlayAttackAnimationS2CPacket(this.girl.getId()));
               }

               for (ItemStack drop : Block.getDrops(state, serverWorld, this.targetOrePos, null, this.girl, this.girl.getMainHandItem())) {
                  ItemStack stack = drop.copy();
                  boolean added = this.addToInventory(stack);
                  if (!added && !stack.isEmpty()) {
                     ItemEntity itemEntity = new ItemEntity(
                        this.world,
                        (double)this.targetOrePos.getX() + 0.5,
                        (double)this.targetOrePos.getY() + 0.5,
                        (double)this.targetOrePos.getZ() + 0.5,
                        stack
                     );
                     itemEntity.setDeltaMovement(0.0, 0.05, 0.0);
                     itemEntity.setPickUpDelay(10);
                     this.world.addFreshEntity(itemEntity);
                  }
               }

               this.world.setBlock(this.targetOrePos, Blocks.AIR.defaultBlockState(), 3);
               this.world.playSound(null, this.targetOrePos, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
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
}
