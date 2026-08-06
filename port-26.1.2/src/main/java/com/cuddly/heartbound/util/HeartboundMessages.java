package com.cuddly.heartbound.util;

import java.util.Objects;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class HeartboundMessages {
   public static void GlobleMessage(Level world, Component message) {
      if (!world.isClientSide()) {
         Objects.requireNonNull(world.getServer()).getPlayerList().broadcastSystemMessage(message, false);
      }
   }

   public static void GlobleMessage(Level world, String message) {
      GlobleMessage(world, Component.literal(message));
   }

   public static void PlayerSpecificMessage(Player playerEntity, String messageContent) {
      Component message = Component.literal(messageContent);
      playerEntity.sendSystemMessage(message);
   }

   public static void PlayerSpecificMessage(Player playerEntity, Component message) {
      playerEntity.sendSystemMessage(message);
   }
}
