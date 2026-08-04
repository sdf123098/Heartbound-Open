package com.cuddly.heartbound.util;

import java.util.Objects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class HeartboundMessages {
   public static void GlobleMessage(World world, Text message) {
      if (!world.isClient()) {
         Objects.requireNonNull(world.getServer()).getPlayerManager().broadcast(message, false);
      }
   }

   public static void GlobleMessage(World world, String message) {
      GlobleMessage(world, Text.literal(message));
   }

   public static void PlayerSpecificMessage(PlayerEntity playerEntity, String messageContent) {
      Text message = Text.literal(messageContent);
      playerEntity.sendMessage(message, false);
   }

   public static void PlayerSpecificMessage(PlayerEntity playerEntity, Text message) {
      playerEntity.sendMessage(message, false);
   }
}
