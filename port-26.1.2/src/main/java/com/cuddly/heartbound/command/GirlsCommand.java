package com.cuddly.heartbound.command;

import com.cuddly.heartbound.entity.base.GirlEntity;
import com.cuddly.heartbound.entity.girls.CustomGirlEntity;
import com.cuddly.heartbound.networking.S2C.RefreshModelsS2CPacket;
import com.cuddly.heartbound.registries.GirlRegistry;
import com.cuddly.heartbound.util.Utils;
import com.cuddly.heartbound.util.json.CustomGirlLoader;
import com.cuddly.heartbound.util.managers.TamedGirlManager;
import com.cuddly.heartbound.util.variables.CustomGirlProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.Commands.CommandSelection;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.phys.Vec3;

public class GirlsCommand {
   private static final SuggestionProvider<CommandSourceStack> PROFILE_SUGGESTIONS = (context, builder) -> {
      CustomGirlLoader.LOADED_PROFILES.keySet().forEach(builder::suggest);
      return CompletableFuture.completedFuture(builder.build());
   };

   public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, CommandSelection environment) {
      dispatcher.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                           "girls"
                        )
                        .requires(src -> true))
                     .then(
                        ((LiteralArgumentBuilder)Commands.literal("locateAll").requires(src -> true))
                           .executes(ctx -> locateAllGirls((CommandSourceStack)ctx.getSource()))
                     ))
                  .then(
                     ((LiteralArgumentBuilder)Commands.literal("refreshJiggle").requires(src -> true))
                        .executes(ctx -> refresh((CommandSourceStack)ctx.getSource()))
                  ))
               .then(
                  ((LiteralArgumentBuilder)Commands.literal("showCustomGirlInfo").requires(src -> true))
                     .executes(ctx -> customGirlInfo((CommandSourceStack)ctx.getSource()))
               ))
            .then(
               ((LiteralArgumentBuilder)Commands.literal("spawnCustom").requires(src -> src.permissions().hasPermission(Permissions.COMMANDS_MODERATOR)))
                  .then(((RequiredArgumentBuilder)Commands.argument("id", StringArgumentType.string()).suggests(PROFILE_SUGGESTIONS).executes(ctx -> {
                     Vec3 pos = ((CommandSourceStack)ctx.getSource()).getPosition();
                     String id = StringArgumentType.getString(ctx, "id");
                     return spawnGirl((CommandSourceStack)ctx.getSource(), id, pos);
                  })).then(Commands.argument("pos", BlockPosArgument.blockPos()).executes(ctx -> {
                     String id = StringArgumentType.getString(ctx, "id");
                     BlockPos blockPos = BlockPosArgument.getBlockPos(ctx, "pos");
                     Vec3 pos = new Vec3((double)blockPos.getX() + 0.5, (double)blockPos.getY(), (double)blockPos.getZ() + 0.5);
                     return spawnGirl((CommandSourceStack)ctx.getSource(), id, pos);
                  })))
            )
      );
   }

   private static int refresh(CommandSourceStack source) throws CommandSyntaxException {
      ServerPlayNetworking.send(source.getPlayerOrException(), new RefreshModelsS2CPacket());
      source.sendSuccess(() -> Component.literal("Refreshed All Loaded Girl Models"), false);
      return 1;
   }

   private static int customGirlInfo(CommandSourceStack source) {
      ServerPlayer player = source.getPlayer();
      if (player == null) {
         return 0;
      } else if (CustomGirlLoader.REGISTERED_PROFILES.isEmpty()) {
         player.sendSystemMessage(Component.literal("§cYou have no profiles registered."));
         return 0;
      } else {
         for (CustomGirlProfile profile : CustomGirlLoader.REGISTERED_PROFILES.values()) {
            player.sendSystemMessage(Component.nullToEmpty("§d" + profile.id() + " → §b" + Utils.getReadableItemName(profile.tameItem())));
         }

         return 1;
      }
   }

   private static int locateAllGirls(CommandSourceStack source) {
      ServerPlayer player = source.getPlayer();
      if (player == null) {
         return 0;
      } else {
         ServerLevel world = source.getLevel();
         TamedGirlManager manager = TamedGirlManager.get(world);
         List<UUID> owned = manager.getGirlsOwnedBy(player.getUUID());
         if (owned.isEmpty()) {
            player.sendSystemMessage(Component.literal("§cYou have no tamed girls in this world."));
            return 0;
         } else {
            int found = 0;

            for (UUID entry : owned) {
               GirlEntity girl = (GirlEntity)world.getEntity(entry);
               found++;
               Vec3 pos = girl.position();
               String name = girl.getGirlDisplayName();
               player.sendSystemMessage(Component.literal("§d" + name + "§b → X: " + (int)pos.x + " Y: " + (int)pos.y + " Z: " + (int)pos.z));
            }

            player.sendSystemMessage(Component.literal("§aTotal girls found: §e" + found));
            return found;
         }
      }
   }

   private static int spawnGirl(CommandSourceStack source, String id, Vec3 pos) {
      ServerLevel world = source.getLevel();
      CustomGirlProfile profile = CustomGirlLoader.LOADED_PROFILES.get(id);
      if (profile == null) {
         source.sendFailure(Component.literal("Girl profile not found: " + id));
         return 0;
      } else {
         BlockPos blockPos = BlockPos.containing(pos);
         if (!ServerLevel.isInSpawnableBounds(blockPos)) {
            source.sendFailure(Component.literal("Invalid spawn position."));
            return 0;
         } else {
            CustomGirlEntity girl = GirlRegistry.CUSTOM_GIRL.create(world, EntitySpawnReason.COMMAND);
            if (girl == null) {
               source.sendFailure(Component.literal("Failed to create girl entity."));
               return 0;
            } else {
               girl.setProfile(profile, true);
               girl.teleportTo(pos.x, pos.y, pos.z);
               girl.setYRot(source.getRotation().y);
               girl.setXRot(0.0F);
               girl.finalizeSpawn(world, world.getCurrentDifficultyAt(girl.blockPosition()), EntitySpawnReason.COMMAND, null);
               if (!world.addFreshEntity(girl)) {
                  source.sendFailure(Component.literal("Failed to spawn entity in the world."));
                  return 0;
               } else {
                  source.sendSuccess(() -> Component.literal("Spawned girl: " + id), true);
                  return 1;
               }
            }
         }
      }
   }
}
