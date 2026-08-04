package com.cuddly.heartbound.util;

import com.cuddly.heartbound.Heartbound;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Direction.Type;
import net.minecraft.world.World;
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

   public static BlockPos getBlockPosFromVec3d(Vec3d pos) {
      return new BlockPos((int)pos.getX(), (int)pos.getY(), (int)pos.getZ());
   }

   public static BlockPos findNearbyDoor(World world, BlockPos origin, Direction facing) {
      BlockPos check = getBlockBehind(origin, facing);
      BlockPos result = null;
      BlockPos below = check.down();
      if (world.getBlockState(below).isIn(BlockTags.DOORS)) {
         result = below;
      }

      for (Direction dir : Type.HORIZONTAL) {
         BlockPos side = check.offset(dir);
         BlockState state = world.getBlockState(side);
         if (state.isIn(BlockTags.DOORS)) {
            result = side;
         }

         BlockPos sideBelow = side.down();
         if (world.getBlockState(sideBelow).isIn(BlockTags.DOORS)) {
            result = sideBelow;
         }

         BlockPos sideAbove = side.up();
         if (world.getBlockState(sideAbove).isIn(BlockTags.DOORS)) {
            result = sideAbove;
         }
      }

      return result != null && world.getBlockState(result).get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.UPPER)
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

   public static Utils.BlockInfo findNearbyBed(World world, BlockPos center, int radius) {
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
            if (state.isIn(BlockTags.BEDS) && state.get(Properties.BED_PART) == BedPart.FOOT && !Heartbound.usedBeds.containsValue(pos)) {
               Direction facing = state.contains(Properties.HORIZONTAL_FACING) ? state.get(Properties.HORIZONTAL_FACING) : Direction.NORTH;
               return new Utils.BlockInfo(pos.toImmutable(), state, facing);
            }

            for (Direction dir : Type.HORIZONTAL) {
               BlockPos next = pos.offset(dir);
               if (!visited.contains(next) && center.getManhattanDistance(next) <= radius) {
                  visited.add(next);
                  queue.add(next);
               }
            }
         }

         return null;
      }
   }

   public static boolean checkForBlockAt(World world, BlockPos blockPos, @Nullable Block block, @Nullable TagKey<Block> blockTag) {
      BlockState state = world.getBlockState(blockPos);
      return isBlockOrTag(state, block, blockTag);
   }

   private static boolean isBlockOrTag(BlockState state, @Nullable Block block, @Nullable TagKey<Block> tag) {
      return block != null && state.isOf(block) ? true : tag != null && state.isIn(tag);
   }

   public static float Round(float d, int decimalPlace) {
      return BigDecimal.valueOf((double)d).setScale(decimalPlace, RoundingMode.HALF_DOWN).floatValue();
   }

   public static int withFullAlpha(int color) {
      return (color & 0xFF000000) != 0 ? color : 0xFF000000 | color;
   }

   public static String getReadableItemName(Item tameItem) {
      Identifier id = Registries.ITEM.getId(tameItem);
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

   public static String getPlayerName(PlayerEntity player) {
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
