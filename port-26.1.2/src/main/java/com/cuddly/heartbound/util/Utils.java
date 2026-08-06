package com.cuddly.heartbound.util;

import com.cuddly.heartbound.Heartbound;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class Utils {
   public static Boolean isStringInQueue(Queue<String> queue, String text) {
      for (String event : queue) {
         if (event.contains(text)) {
            return true;
         }
      }

      return false;
   }

   public static BlockPos getBlockPosFromVec3d(Vec3 pos) {
      return new BlockPos((int)pos.x(), (int)pos.y(), (int)pos.z());
   }

   public static BlockPos findNearbyDoor(Level world, BlockPos origin, Direction facing) {
      BlockPos check = getBlockBehind(origin, facing);
      BlockPos result = null;
      BlockPos below = check.below();
      if (world.getBlockState(below).is(BlockTags.DOORS)) {
         result = below;
      }

      for (Direction dir : Plane.HORIZONTAL) {
         BlockPos side = check.relative(dir);
         BlockState state = world.getBlockState(side);
         if (state.is(BlockTags.DOORS)) {
            result = side;
         }

         BlockPos sideBelow = side.below();
         if (world.getBlockState(sideBelow).is(BlockTags.DOORS)) {
            result = sideBelow;
         }

         BlockPos sideAbove = side.above();
         if (world.getBlockState(sideAbove).is(BlockTags.DOORS)) {
            result = sideAbove;
         }
      }

      return result != null && world.getBlockState(result).getValue(BlockStateProperties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.UPPER)
         ? new BlockPos(result.getX(), result.getY() - 1, result.getZ())
         : result;
   }

   public static BlockPos getBlockBehind(BlockPos origin, Direction dir) {
      if (dir.equals(Direction.SOUTH)) {
         return new BlockPos(origin.getX(), origin.getY(), origin.getZ() - 1);
      } else if (dir.equals(Direction.EAST)) {
         return new BlockPos(origin.getX() - 1, origin.getY(), origin.getZ());
      } else {
         return dir.equals(Direction.WEST)
            ? new BlockPos(origin.getX() + 1, origin.getY(), origin.getZ())
            : new BlockPos(origin.getX(), origin.getY(), origin.getZ() + 1);
      }
   }

   public static Utils.BlockInfo findNearbyBed(Level world, BlockPos center, int radius) {
      if (radius <= 0) {
         return null;
      } else {
         Set<BlockPos> visited = new HashSet<>();
         Queue<BlockPos> queue = new ArrayDeque<>();
         queue.add(center);
         visited.add(center);

         while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();
            BlockState state = world.getBlockState(pos);
            if (state.is(BlockTags.BEDS) && state.getValue(BlockStateProperties.BED_PART) == BedPart.FOOT && !Heartbound.usedBeds.containsValue(pos)) {
               Direction facing = state.hasProperty(BlockStateProperties.HORIZONTAL_FACING) ? state.getValue(BlockStateProperties.HORIZONTAL_FACING) : Direction.NORTH;
               return new Utils.BlockInfo(pos.immutable(), state, facing);
            }

            for (Direction dir : Plane.HORIZONTAL) {
               BlockPos next = pos.relative(dir);
               if (!visited.contains(next) && center.distManhattan(next) <= radius) {
                  visited.add(next);
                  queue.add(next);
               }
            }
         }

         return null;
      }
   }

   public static boolean checkForBlockAt(Level world, BlockPos blockPos, @Nullable Block block, @Nullable TagKey<Block> blockTag) {
      BlockState state = world.getBlockState(blockPos);
      return isBlockOrTag(state, block, blockTag);
   }

   private static boolean isBlockOrTag(BlockState state, @Nullable Block block, @Nullable TagKey<Block> tag) {
      return block != null && state.is(block) ? true : tag != null && state.is(tag);
   }

   public static float Round(float d, int decimalPlace) {
      return BigDecimal.valueOf((double)d).setScale(decimalPlace, RoundingMode.HALF_DOWN).floatValue();
   }

   public static int withFullAlpha(int color) {
      return (color & 0xFF000000) != 0 ? color : 0xFF000000 | color;
   }

   public static String getReadableItemName(Item tameItem) {
      Identifier id = BuiltInRegistries.ITEM.getKey(tameItem);
      if (id != null) {
         String path = id.getPath();
         return getFormattedByUnderscore(path);
      } else {
         return "Unknown Item";
      }
   }

   public static String getFormattedByUnderscore(String input) {
      String[] words = input.split("_");
      StringBuilder formatted = new StringBuilder();

      for (String word : words) {
         if (!word.isEmpty()) {
            formatted.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
         }
      }

      return formatted.toString().trim();
   }

   public static String getFirstLetterCapitalized(String input) {
      return input != null && !input.isEmpty() ? input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase() : input;
   }

   public static String getPlayerName(Player player) {
      if (player == null) {
         return "Unknown";
      } else if (player.getName() == null) {
         return "Unknown";
      } else {
         String name = player.getName().getString();
         return name == null ? "Unknown" : name.replace("literal{", "").replace("}", "");
      }
   }

   public static record BlockInfo(BlockPos pos, BlockState state, Direction facing) {
   }
}
