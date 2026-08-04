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
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class GirlsCommand {
   private static final SuggestionProvider<ServerCommandSource> PROFILE_SUGGESTIONS = (context, builder) -> {
      CustomGirlLoader.LOADED_PROFILES.keySet().forEach(builder::suggest);
      return CompletableFuture.completedFuture(builder.build());
   };

   public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, RegistrationEnvironment environment) {
      dispatcher.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal(
                           "girls"
                        )
                        .requires(src -> true))
                     .then(
                        ((LiteralArgumentBuilder)CommandManager.literal("locateAll").requires(src -> true))
                           .executes(ctx -> locateAllGirls((ServerCommandSource)ctx.getSource()))
                     ))
                  .then(
                     ((LiteralArgumentBuilder)CommandManager.literal("refreshJiggle").requires(src -> true))
                        .executes(ctx -> refresh((ServerCommandSource)ctx.getSource()))
                  ))
               .then(
                  ((LiteralArgumentBuilder)CommandManager.literal("showCustomGirlInfo").requires(src -> true))
                     .executes(ctx -> customGirlInfo((ServerCommandSource)ctx.getSource()))
               ))
            .then(
               ((LiteralArgumentBuilder)CommandManager.literal("spawnCustom").requires(src -> src.hasPermissionLevel(2)))
                  .then(((RequiredArgumentBuilder)CommandManager.argument("id", StringArgumentType.string()).suggests(PROFILE_SUGGESTIONS).executes(ctx -> {
                     Vec3d pos = ((ServerCommandSource)ctx.getSource()).getPosition();
                     String id = StringArgumentType.getString(ctx, "id");
                     return spawnGirl((ServerCommandSource)ctx.getSource(), id, pos);
                  })).then(CommandManager.argument("pos", BlockPosArgumentType.blockPos()).executes(ctx -> {
                     String id = StringArgumentType.getString(ctx, "id");
                     BlockPos blockPos = BlockPosArgumentType.getBlockPos(ctx, "pos");
                     Vec3d pos = new Vec3d((double)blockPos.getX() + 0.5, (double)blockPos.getY(), (double)blockPos.getZ() + 0.5);
                     return spawnGirl((ServerCommandSource)ctx.getSource(), id, pos);
                  })))
            )
      );
   }

   private static int refresh(ServerCommandSource source) throws CommandSyntaxException {
      ServerPlayNetworking.send(source.getPlayerOrThrow(), new RefreshModelsS2CPacket());
      source.sendFeedback(() -> Text.literal("Refreshed All Loaded Girl Models"), false);
      return 1;
   }

   private static int customGirlInfo(ServerCommandSource source) {
      ServerPlayerEntity player = source.getPlayer();
      if (player == null) {
         return 0;
      } else if (CustomGirlLoader.REGISTERED_PROFILES.isEmpty()) {
         player.sendMessage(Text.literal("§cYou have no profiles registered."), false);
         return 0;
      } else {
         for (CustomGirlProfile profile : CustomGirlLoader.REGISTERED_PROFILES.values()) {
            player.sendMessage(Text.of("§d" + profile.id() + " → §b" + Utils.getReadableItemName(profile.tameItem())), false);
         }

         return 1;
      }
   }

   private static int locateAllGirls(ServerCommandSource source) {
      ServerPlayerEntity player = source.getPlayer();
      if (player == null) {
         return 0;
      } else {
         ServerWorld world = source.getWorld();
         TamedGirlManager manager = TamedGirlManager.get(world);
         List<UUID> owned = manager.getGirlsOwnedBy(player.getUuid());
         if (owned.isEmpty()) {
            player.sendMessage(Text.literal("§cYou have no tamed girls in this world."), false);
            return 0;
         } else {
            int found = 0;

            for (UUID entry : owned) {
               GirlEntity girl = (GirlEntity)world.getEntity(entry);
               found++;
               Vec3d pos = girl.getPos();
               String name = girl.getGirlDisplayName();
               player.sendMessage(Text.literal("§d" + name + "§b → X: " + (int)pos.x + " Y: " + (int)pos.y + " Z: " + (int)pos.z), false);
            }

            player.sendMessage(Text.literal("§aTotal girls found: §e" + found), false);
            return found;
         }
      }
   }

   private static int spawnGirl(ServerCommandSource source, String id, Vec3d pos) {
      ServerWorld world = source.getWorld();
      CustomGirlProfile profile = CustomGirlLoader.LOADED_PROFILES.get(id);
      if (profile == null) {
         source.sendError(Text.literal("Girl profile not found: " + id));
         return 0;
      } else {
         BlockPos blockPos = BlockPos.ofFloored(pos);
         if (!ServerWorld.isValid(blockPos)) {
            source.sendError(Text.literal("Invalid spawn position."));
            return 0;
         } else {
            CustomGirlEntity girl = GirlRegistry.CUSTOM_GIRL.create(world);
            if (girl == null) {
               source.sendError(Text.literal("Failed to create girl entity."));
               return 0;
            } else {
               girl.setProfile(profile, true);
               girl.refreshPositionAndAngles(pos.x, pos.y, pos.z, source.getRotation().y, 0.0F);
               girl.initialize(world, world.getLocalDifficulty(girl.getBlockPos()), SpawnReason.COMMAND, null);
               if (!world.spawnEntity(girl)) {
                  source.sendError(Text.literal("Failed to spawn entity in the world."));
                  return 0;
               } else {
                  source.sendFeedback(() -> Text.literal("Spawned girl: " + id), true);
                  return 1;
               }
            }
         }
      }
   }
}
