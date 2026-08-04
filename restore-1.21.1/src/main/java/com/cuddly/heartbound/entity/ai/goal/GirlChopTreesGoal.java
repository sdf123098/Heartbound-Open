package com.cuddly.heartbound.entity.ai.goal;

import com.cuddly.heartbound.entity.base.tamable.TameableGirlEntity;
import com.cuddly.heartbound.networking.S2C.PlayAttackAnimationS2CPacket;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.PillarBlock;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.Goal.Control;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.world.World;

public class GirlChopTreesGoal extends Goal {
   private final TameableGirlEntity girl;
   private final World world;
   private final double speed;
   private BlockPos targetTreePos;
   private int choppingTicks;
   private static final int CHOPPING_DURATION = 20;
   private int searchCooldown;
   private static final int SEARCH_COOLDOWN_DURATION = 40;
   private static final int SEARCH_RANGE = 15;
   private Set<BlockPos> treeBlocks;
   private boolean isChopping;

   public GirlChopTreesGoal(TameableGirlEntity girl, double speed) {
      this.girl = girl;
      this.world = girl.getWorld();
      this.speed = speed;
      this.treeBlocks = new HashSet<>();
      this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
   }

   @Override
   public boolean canStart() {
      if (!this.girl.isRoaming() && this.girl.isChopping()) {
         this.girl.setChopping(false);
         return false;
      } else if (!this.girl.isTamed()
         || !this.girl.isChopping()
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
         this.targetTreePos = this.findNearbyTreeToChop();
         if (this.targetTreePos != null) {
            this.searchCooldown = 40;
            return true;
         } else {
            this.searchCooldown = 40;
            return false;
         }
      }
   }

   @Override
   public boolean shouldContinue() {
      if (this.targetTreePos == null) {
         return false;
      } else if (!this.girl.isRoaming()) {
         if (this.girl.isChopping()) {
            this.girl.setChopping(false);
         }

         return false;
      } else {
         return this.girl.isChopping()
               && this.girl.isRoaming()
               && !this.girl.isFollowing()
               && !this.girl.isSitting()
               && !this.girl.isHavingSex()
               && !this.girl.hasVehicle()
            ? this.isChopping
               || !this.girl.getNavigation().isIdle()
               || this.girl
                     .squaredDistanceTo((double)this.targetTreePos.getX() + 0.5, (double)this.targetTreePos.getY(), (double)this.targetTreePos.getZ() + 0.5)
                  > 6.25
            : false;
      }
   }

   @Override
   public void start() {
      this.choppingTicks = 0;
      this.isChopping = false;
      this.treeBlocks.clear();
      if (this.targetTreePos != null) {
         this.girl
            .getNavigation()
            .startMovingTo((double)this.targetTreePos.getX() + 0.5, (double)this.targetTreePos.getY(), (double)this.targetTreePos.getZ() + 0.5, this.speed);
      }
   }

   @Override
   public void tick() {
      if (this.targetTreePos != null) {
         this.girl.getLookControl().lookAt((double)this.targetTreePos.getX() + 0.5, (double)this.targetTreePos.getY(), (double)this.targetTreePos.getZ() + 0.5);
         double distance = this.girl
            .squaredDistanceTo((double)this.targetTreePos.getX() + 0.5, (double)this.targetTreePos.getY(), (double)this.targetTreePos.getZ() + 0.5);
         if (this.isChopping) {
            this.choppingTicks++;
            if (this.choppingTicks >= 20) {
               this.chopTree();
               this.isChopping = false;
               this.choppingTicks = 0;
               this.targetTreePos = this.findNearbyTreeToChop();
               if (this.targetTreePos != null) {
                  this.girl
                     .getNavigation()
                     .startMovingTo(
                        (double)this.targetTreePos.getX() + 0.5, (double)this.targetTreePos.getY(), (double)this.targetTreePos.getZ() + 0.5, this.speed
                     );
               }
            }
         } else if (distance <= 6.25) {
            this.girl.getNavigation().stop();
            this.isChopping = true;
            this.choppingTicks = 0;
         } else {
            BlockPos girlPos = this.girl.getBlockPos();
            if (this.isPathBlockedByLeaves(girlPos, this.targetTreePos)) {
               this.breakBlockingLeaves(girlPos, this.targetTreePos);
            } else {
               this.girl
                  .getNavigation()
                  .startMovingTo(
                     (double)this.targetTreePos.getX() + 0.5, (double)this.targetTreePos.getY(), (double)this.targetTreePos.getZ() + 0.5, this.speed
                  );
            }
         }
      }
   }

   @Override
   public void stop() {
      this.targetTreePos = null;
      this.choppingTicks = 0;
      this.isChopping = false;
      this.treeBlocks.clear();
      this.girl.getNavigation().stop();
   }

   private BlockPos findNearbyTreeToChop() {
      List<BlockPos> accessibleLogs = this.findAccessibleLogBlocks();
      if (accessibleLogs.isEmpty()) {
         return null;
      } else {
         BlockPos targetLog = this.findBestTargetLog(accessibleLogs);
         if (targetLog == null) {
            return null;
         } else {
            BlockPos targetPos = this.findBestPositionAtHeight(targetLog);
            return targetPos != null ? targetPos : targetLog;
         }
      }
   }

   private List<BlockPos> findAccessibleLogBlocks() {
      BlockPos girlPos = this.girl.getBlockPos();
      Mutable mutablePos = new Mutable();
      List<BlockPos> accessibleLogs = new ArrayList<>();

      for (int x = -15; x <= 15; x++) {
         for (int y = -2; y <= 10; y++) {
            for (int z = -15; z <= 15; z++) {
               mutablePos.set(girlPos.getX() + x, girlPos.getY() + y, girlPos.getZ() + z);
               if (this.isValidTreeLog(mutablePos)) {
                  accessibleLogs.add(mutablePos.toImmutable());
               }
            }
         }
      }

      return accessibleLogs;
   }

   private boolean isValidTreeLog(BlockPos pos) {
      BlockState state = this.world.getBlockState(pos);
      return this.isLogBlock(state) && this.isPartOfTree(pos) && this.girl.getNavigation().findPathTo(pos, 1) != null;
   }

   private BlockPos findBestTargetLog(List<BlockPos> accessibleLogs) {
      BlockPos lowestGroundLog = null;

      for (BlockPos log : accessibleLogs) {
         BlockPos belowPos = log.down();
         BlockState belowState = this.world.getBlockState(belowPos);
         if (this.isGroundBlock(belowState) && (lowestGroundLog == null || log.getY() < lowestGroundLog.getY())) {
            lowestGroundLog = log;
         }
      }

      return lowestGroundLog;
   }

   private BlockPos findBestPositionAtHeight(BlockPos targetLog) {
      int targetY = targetLog.getY();
      BlockPos girlPos = this.girl.getBlockPos();

      for (int attempt = 0; attempt < 20; attempt++) {
         double angle = (double)attempt / 20.0 * 2.0 * Math.PI;
         double distance = 0.5 + (double)(attempt % 4) * 0.5;
         int x = targetLog.getX() + (int)(Math.cos(angle) * distance);
         int z = targetLog.getZ() + (int)(Math.sin(angle) * distance);
         BlockPos testPos = new BlockPos(x, targetY, z);
         if (this.girl.getNavigation().findPathTo(testPos, 1) != null) {
            if (!this.isPathBlockedByLeaves(girlPos, testPos)) {
               return testPos;
            }

            this.breakBlockingLeaves(girlPos, testPos);
            if (!this.isPathBlockedByLeaves(girlPos, testPos)) {
               return testPos;
            }
         }
      }

      return null;
   }

   private boolean isPathBlockedByLeaves(BlockPos start, BlockPos end) {
      int minX = Math.min(start.getX(), end.getX()) - 1;
      int maxX = Math.max(start.getX(), end.getX()) + 1;
      int minY = Math.min(start.getY(), end.getY()) - 1;
      int maxY = Math.max(start.getY(), end.getY()) + 2;
      int minZ = Math.min(start.getZ(), end.getZ()) - 1;
      int maxZ = Math.max(start.getZ(), end.getZ()) + 1;

      for (int x = minX; x <= maxX; x++) {
         for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
               BlockPos checkPos = new BlockPos(x, y, z);
               if (this.isBlockingLeaf(checkPos, start)) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private boolean isBlockingLeaf(BlockPos leafPos, BlockPos start) {
      BlockState state = this.world.getBlockState(leafPos);
      if (!(state.getBlock() instanceof LeavesBlock)) {
         return false;
      } else {
         double horizontalDistance = Math.sqrt(Math.pow((double)(leafPos.getX() - start.getX()), 2.0) + Math.pow((double)(leafPos.getZ() - start.getZ()), 2.0));
         if (!(horizontalDistance <= 2.0)) {
            return false;
         } else {
            int girlHeight = start.getY() + 1;
            return leafPos.getY() >= start.getY() - 1 && leafPos.getY() <= girlHeight;
         }
      }
   }

   private void breakBlockingLeaves(BlockPos start, BlockPos end) {
      if (this.world instanceof ServerWorld serverWorld) {
         List<BlockPos> leavesToBreak = this.findBlockingLeaves(start, end);
         leavesToBreak.sort((pos1, pos2) -> {
            double dist1 = start.getSquaredDistance(pos1);
            double dist2 = start.getSquaredDistance(pos2);
            return Double.compare(dist1, dist2);
         });
         int leavesBroken = 0;

         for (BlockPos leafPos : leavesToBreak) {
            if (leavesBroken >= 10) {
               break;
            }

            this.breakLeafBlock(leafPos, serverWorld);
            leavesBroken++;
         }
      }
   }

   private List<BlockPos> findBlockingLeaves(BlockPos start, BlockPos end) {
      List<BlockPos> leavesToBreak = new ArrayList<>();
      int minX = Math.min(start.getX(), end.getX()) - 1;
      int maxX = Math.max(start.getX(), end.getX()) + 1;
      int minY = Math.min(start.getY(), end.getY()) - 1;
      int maxY = Math.max(start.getY(), end.getY()) + 1;
      int minZ = Math.min(start.getZ(), end.getZ()) - 1;
      int maxZ = Math.max(start.getZ(), end.getZ()) + 1;

      for (int x = minX; x <= maxX; x++) {
         for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
               BlockPos checkPos = new BlockPos(x, y, z);
               if (this.isBlockingLeaf(checkPos, start)) {
                  leavesToBreak.add(checkPos);
               }
            }
         }
      }

      return leavesToBreak;
   }

   private void breakLeafBlock(BlockPos leafPos, ServerWorld serverWorld) {
      BlockState state = this.world.getBlockState(leafPos);
      if (state.getBlock() instanceof LeavesBlock) {
         for (ItemStack drop : Block.getDroppedStacks(state, serverWorld, leafPos, null, this.girl, this.girl.getMainHandStack())) {
            ItemEntity itemEntity = new ItemEntity(this.world, (double)leafPos.getX() + 0.5, (double)leafPos.getY() + 0.5, (double)leafPos.getZ() + 0.5, drop);
            itemEntity.setVelocity(0.0, 0.05, 0.0);
            itemEntity.setPickupDelay(10);
            this.world.spawnEntity(itemEntity);
         }

         this.world.breakBlock(leafPos, false);
         this.world.playSound(null, leafPos, SoundEvents.BLOCK_GRASS_BREAK, SoundCategory.BLOCKS, 1.0F, 1.0F);
      }
   }

   private boolean isLogBlock(BlockState state) {
      Block block = state.getBlock();
      return block instanceof PillarBlock && (block.getTranslationKey().contains("log") || block.getTranslationKey().contains("stem"));
   }

   private boolean isGroundBlock(BlockState state) {
      Block block = state.getBlock();
      return block == Blocks.GRASS_BLOCK
         || block == Blocks.DIRT
         || block == Blocks.COARSE_DIRT
         || block == Blocks.PODZOL
         || block == Blocks.MYCELIUM
         || block == Blocks.FARMLAND;
   }

   private boolean isPartOfTree(BlockPos logPos) {
      return this.hasLeavesInExtendedRange(logPos) ? true : this.isConnectedToTreeWithLeaves(logPos);
   }

   private boolean hasLeavesInExtendedRange(BlockPos logPos) {
      for (int x = -3; x <= 3; x++) {
         for (int y = -3; y <= 8; y++) {
            for (int z = -3; z <= 3; z++) {
               BlockPos checkPos = logPos.add(x, y, z);
               BlockState state = this.world.getBlockState(checkPos);
               if (state.getBlock() instanceof LeavesBlock) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private boolean isConnectedToTreeWithLeaves(BlockPos logPos) {
      for (int dx = -1; dx <= 1; dx++) {
         for (int dy = -1; dy <= 1; dy++) {
            for (int dz = -1; dz <= 1; dz++) {
               if ((dx != 0 || dy != 0 || dz != 0) && this.isAdjacentLogWithLeaves(logPos, dx, dy, dz)) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private boolean isAdjacentLogWithLeaves(BlockPos logPos, int dx, int dy, int dz) {
      BlockPos adjacentPos = logPos.add(dx, dy, dz);
      BlockState adjacentState = this.world.getBlockState(adjacentPos);
      return this.isLogBlock(adjacentState) && this.hasLeavesNearby(adjacentPos);
   }

   private boolean hasLeavesNearby(BlockPos pos) {
      for (int x = -2; x <= 2; x++) {
         for (int y = -2; y <= 5; y++) {
            for (int z = -2; z <= 2; z++) {
               BlockPos checkPos = pos.add(x, y, z);
               BlockState state = this.world.getBlockState(checkPos);
               if (state.getBlock() instanceof LeavesBlock) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private void chopTree() {
      if (this.world instanceof ServerWorld serverWorld) {
         this.findTreeBlocks(this.targetTreePos);
         ArrayList<BlockPos> var11 = new ArrayList<>();

         for (BlockPos pos : this.treeBlocks) {
            if (this.isLogBlock(this.world.getBlockState(pos))) {
               var11.add(pos);
            }
         }

         Block saplingType = this.determineSaplingType();
         this.girl.triggerSwing();

         for (ServerPlayerEntity player : serverWorld.getPlayers()) {
            ServerPlayNetworking.send(player, new PlayAttackAnimationS2CPacket(this.girl.getId()));
         }

         for (BlockPos posx : var11) {
            BlockState state = this.world.getBlockState(posx);

            for (ItemStack drop : Block.getDroppedStacks(state, serverWorld, posx, null, this.girl, this.girl.getMainHandStack())) {
               ItemEntity itemEntity = new ItemEntity(this.world, (double)posx.getX() + 0.5, (double)posx.getY() + 0.5, (double)posx.getZ() + 0.5, drop);
               itemEntity.setVelocity(0.0, 0.05, 0.0);
               itemEntity.setPickupDelay(10);
               this.world.spawnEntity(itemEntity);
            }

            this.world.setBlockState(posx, Blocks.AIR.getDefaultState(), 3);
            this.world.playSound(null, posx, SoundEvents.BLOCK_WOOD_BREAK, SoundCategory.BLOCKS, 1.0F, 1.0F);
         }

         this.plantSapling(saplingType, var11);
      }
   }

   private void findTreeBlocks(BlockPos startPos) {
      this.treeBlocks.clear();
      Set<BlockPos> visited = new HashSet<>();
      Queue<BlockPos> queue = new LinkedList<>();
      queue.add(startPos);
      visited.add(startPos);

      while (!queue.isEmpty()) {
         BlockPos current = queue.poll();
         this.treeBlocks.add(current);
         this.checkAdjacentBlocks(current, visited, queue);
      }
   }

   private void checkAdjacentBlocks(BlockPos current, Set<BlockPos> visited, Queue<BlockPos> queue) {
      for (int dx = -1; dx <= 1; dx++) {
         for (int dy = -1; dy <= 1; dy++) {
            for (int dz = -1; dz <= 1; dz++) {
               if (dx != 0 || dy != 0 || dz != 0) {
                  this.processNeighborBlock(current.add(dx, dy, dz), visited, queue);
               }
            }
         }
      }
   }

   private void processNeighborBlock(BlockPos neighbor, Set<BlockPos> visited, Queue<BlockPos> queue) {
      if (!visited.contains(neighbor)) {
         visited.add(neighbor);
         BlockState state = this.world.getBlockState(neighbor);
         if (this.isLogBlock(state) || state.getBlock() instanceof LeavesBlock) {
            queue.add(neighbor);
         }
      }
   }

   private void plantSapling(Block saplingType, List<BlockPos> logPositions) {
      BlockPos basePos = this.findTreeBase(logPositions);
      if (this.canPlantSaplingAt(basePos)) {
         this.world.setBlockState(basePos, saplingType.getDefaultState(), 3);
      }
   }

   private BlockPos findTreeBase(List<BlockPos> logPositions) {
      BlockPos basePos = null;

      for (BlockPos pos : logPositions) {
         if (basePos == null || pos.getY() < basePos.getY()) {
            basePos = pos;
         }
      }

      return basePos;
   }

   private boolean canPlantSaplingAt(BlockPos pos) {
      return this.world.getBlockState(pos).isAir() && this.world.getBlockState(pos.down()).isSolid();
   }

   private Block determineSaplingType() {
      for (BlockPos pos : this.treeBlocks) {
         BlockState state = this.world.getBlockState(pos);
         if (this.isLogBlock(state)) {
            Block logBlock = state.getBlock();
            return this.getSaplingForLog(logBlock);
         }
      }

      return Blocks.OAK_SAPLING;
   }

   private Block getSaplingForLog(Block logBlock) {
      Map<Block, Block> logToSaplingMap = Map.ofEntries(
         Map.entry(Blocks.OAK_LOG, Blocks.OAK_SAPLING),
         Map.entry(Blocks.STRIPPED_OAK_LOG, Blocks.OAK_SAPLING),
         Map.entry(Blocks.SPRUCE_LOG, Blocks.SPRUCE_SAPLING),
         Map.entry(Blocks.STRIPPED_SPRUCE_LOG, Blocks.SPRUCE_SAPLING),
         Map.entry(Blocks.BIRCH_LOG, Blocks.BIRCH_SAPLING),
         Map.entry(Blocks.STRIPPED_BIRCH_LOG, Blocks.BIRCH_SAPLING),
         Map.entry(Blocks.JUNGLE_LOG, Blocks.JUNGLE_SAPLING),
         Map.entry(Blocks.STRIPPED_JUNGLE_LOG, Blocks.JUNGLE_SAPLING),
         Map.entry(Blocks.ACACIA_LOG, Blocks.ACACIA_SAPLING),
         Map.entry(Blocks.STRIPPED_ACACIA_LOG, Blocks.ACACIA_SAPLING),
         Map.entry(Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_SAPLING),
         Map.entry(Blocks.STRIPPED_DARK_OAK_LOG, Blocks.DARK_OAK_SAPLING),
         Map.entry(Blocks.MANGROVE_LOG, Blocks.MANGROVE_PROPAGULE),
         Map.entry(Blocks.STRIPPED_MANGROVE_LOG, Blocks.MANGROVE_PROPAGULE),
         Map.entry(Blocks.CHERRY_LOG, Blocks.CHERRY_SAPLING),
         Map.entry(Blocks.STRIPPED_CHERRY_LOG, Blocks.CHERRY_SAPLING),
         Map.entry(Blocks.CRIMSON_STEM, Blocks.CRIMSON_FUNGUS),
         Map.entry(Blocks.STRIPPED_CRIMSON_STEM, Blocks.CRIMSON_FUNGUS),
         Map.entry(Blocks.WARPED_STEM, Blocks.WARPED_FUNGUS),
         Map.entry(Blocks.STRIPPED_WARPED_STEM, Blocks.WARPED_FUNGUS)
      );
      return logToSaplingMap.getOrDefault(logBlock, Blocks.OAK_SAPLING);
   }
}
