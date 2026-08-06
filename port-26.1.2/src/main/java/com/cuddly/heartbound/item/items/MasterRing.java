package com.cuddly.heartbound.item.items;

import com.cuddly.heartbound.component.HeartboundDataComponentTypes;
import com.cuddly.heartbound.entity.base.GirlEntity;
import com.cuddly.heartbound.util.json.CustomGirlLoader;
import com.cuddly.heartbound.util.variables.CustomGirlProfile;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.Vec3;

public class MasterRing extends Item {
   public MasterRing(Properties settings) {
      super(settings);
   }

   @Override
   public InteractionResult interactLivingEntity(ItemStack stack, Player user, LivingEntity entity, InteractionHand hand) {
      return InteractionResult.PASS;
   }

   @Override
   public InteractionResult useOn(UseOnContext context) {
      Level world = context.getLevel();
      Player user = context.getPlayer();
      ItemStack stack = context.getItemInHand();
      if (user == null) {
         return InteractionResult.PASS;
      } else if (world.isClientSide()) {
         return InteractionResult.SUCCESS;
      } else {
         CompoundTag entityNbt = stack.get(HeartboundDataComponentTypes.STORED_ENTITY);
         if (entityNbt == null) {
            user.sendOverlayMessage(Component.translatable("item.heartbound.master_ring.empty").withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
         } else {
            if (entityNbt.contains("Owner")) {
               int[] ownerArr = entityNbt.getIntArray("Owner").orElse(null);
               UUID ownerUuid = ownerArr != null && ownerArr.length == 4
                  ? new UUID((long)ownerArr[0] << 32 | ownerArr[1] & 0xFFFFFFFFL, (long)ownerArr[2] << 32 | ownerArr[3] & 0xFFFFFFFFL)
                  : null;
               if (ownerUuid != null && !user.getUUID().equals(ownerUuid)) {
                  user.sendOverlayMessage(Component.translatable("item.heartbound.master_ring.not_owner_release").withStyle(ChatFormatting.RED));
                  return InteractionResult.FAIL;
               }
            }

            BlockPos pos = context.getClickedPos().above();
            ServerLevel serverWorld = (ServerLevel)world;
            String entityTypeId = entityNbt.getStringOr("id", "");
            EntityType<?> entityType = EntityType.byString(entityTypeId).orElse(null);
            if (entityType == null) {
               user.sendOverlayMessage(Component.translatable("item.heartbound.master_ring.invalid_data").withStyle(ChatFormatting.RED));
               return InteractionResult.FAIL;
            } else if (entityType.create(serverWorld, EntitySpawnReason.LOAD) instanceof GirlEntity girl) {
               girl.load(TagValueInput.create(ProblemReporter.DISCARDING, serverWorld.registryAccess(), entityNbt));
               girl.setPos((double)pos.getX() + 0.5, (double)pos.getY(), (double)pos.getZ() + 0.5);
               girl.setYRot(user.getYRot());
               girl.setXRot(0.0F);
               girl.setDeltaMovement(Vec3.ZERO);
               girl.setYHeadRot(user.getYRot());
               girl.setYBodyRot(user.getYRot());
               if (serverWorld.addFreshEntity(girl)) {
                  stack.remove(HeartboundDataComponentTypes.STORED_ENTITY);
                  user.sendOverlayMessage(Component.translatable("item.heartbound.master_ring.released", girl.getGirlDisplayName()).withStyle(ChatFormatting.GREEN));
                  return InteractionResult.SUCCESS;
               } else {
                  user.sendOverlayMessage(Component.translatable("item.heartbound.master_ring.spawn_failed").withStyle(ChatFormatting.RED));
                  return InteractionResult.FAIL;
               }
            } else {
               user.sendOverlayMessage(Component.translatable("item.heartbound.master_ring.invalid_data").withStyle(ChatFormatting.RED));
               return InteractionResult.FAIL;
            }
         }
      }
   }

   @Override
   public Component getName(ItemStack stack) {
      CompoundTag entityNbt = stack.get(HeartboundDataComponentTypes.STORED_ENTITY);
      return (Component)(entityNbt != null ? Component.translatable("item.heartbound.master_ring.filled") : super.getName(stack));
   }

   @Override
   public boolean isFoil(ItemStack stack) {
      return stack.has(HeartboundDataComponentTypes.STORED_ENTITY);
   }

   @Override
   public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag type) {
      super.appendHoverText(stack, context, display, tooltip, type);
      CompoundTag entityNbt = stack.get(HeartboundDataComponentTypes.STORED_ENTITY);
      if (entityNbt != null) {
         String girlName = "Unknown";
         if (entityNbt.contains("GirlProfileID")) {
            String profileId = entityNbt.getStringOr("GirlProfileID", "");
            CustomGirlProfile profile = CustomGirlLoader.LOADED_PROFILES.get(profileId);
            if (profile != null) {
               girlName = profile.name();
            } else {
               girlName = profileId;
            }
         } else if (entityNbt.contains("CustomName")) {
            try {
               girlName = ComponentSerialization.CODEC
                  .parse(JsonOps.INSTANCE, JsonParser.parseString(entityNbt.getStringOr("CustomName", "")))
                  .result()
                  .orElse(Component.empty())
                  .getString();
            } catch (Exception var10) {
               girlName = "Unknown";
            }
         } else if (entityNbt.contains("id")) {
            String entityId = entityNbt.getStringOr("id", "");
            EntityType<?> entityType = EntityType.byString(entityId).orElse(null);
            if (entityType != null) {
               girlName = entityType.getDescription().getString();
            }
         }

         tooltip.accept(Component.translatable("item.heartbound.master_ring.tooltip.girl", girlName).withStyle(ChatFormatting.GRAY));
         if (entityNbt.contains("Owner")) {
            int[] ownerArr = entityNbt.getIntArray("Owner").orElse(null);
            UUID ownerUuid = ownerArr != null && ownerArr.length == 4
               ? new UUID((long)ownerArr[0] << 32 | ownerArr[1] & 0xFFFFFFFFL, (long)ownerArr[2] << 32 | ownerArr[3] & 0xFFFFFFFFL)
               : null;
            String ownerName = "Unknown";
            if (entityNbt.contains("OwnerName")) {
               ownerName = entityNbt.getStringOr("OwnerName", "");
            } else if (ownerUuid != null) {
               ownerName = ownerUuid.toString().substring(0, 8) + "...";
            }

            tooltip.accept(Component.translatable("item.heartbound.master_ring.tooltip.owner", ownerName).withStyle(ChatFormatting.GRAY));
         }
      }
   }
}
