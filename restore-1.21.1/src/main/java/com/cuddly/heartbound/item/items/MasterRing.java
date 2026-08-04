package com.cuddly.heartbound.item.items;

import com.cuddly.heartbound.component.HeartboundDataComponentTypes;
import com.cuddly.heartbound.entity.base.GirlEntity;
import com.cuddly.heartbound.util.json.CustomGirlLoader;
import com.cuddly.heartbound.util.variables.CustomGirlProfile;
import java.util.List;
import java.util.UUID;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Item.Settings;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.Text.Serialization;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class MasterRing extends Item {
   public MasterRing(Settings settings) {
      super(settings);
   }

   @Override
   public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
      return ActionResult.PASS;
   }

   @Override
   public ActionResult useOnBlock(ItemUsageContext context) {
      World world = context.getWorld();
      PlayerEntity user = context.getPlayer();
      ItemStack stack = context.getStack();
      if (user == null) {
         return ActionResult.PASS;
      } else if (world.isClient()) {
         return ActionResult.SUCCESS;
      } else {
         NbtCompound entityNbt = stack.get(HeartboundDataComponentTypes.STORED_ENTITY);
         if (entityNbt == null) {
            user.sendMessage(Text.translatable("item.heartbound.master_ring.empty").formatted(Formatting.RED), true);
            return ActionResult.FAIL;
         } else {
            if (entityNbt.contains("Owner")) {
               UUID ownerUuid = entityNbt.getUuid("Owner");
               if (!user.getUuid().equals(ownerUuid)) {
                  user.sendMessage(Text.translatable("item.heartbound.master_ring.not_owner_release").formatted(Formatting.RED), true);
                  return ActionResult.FAIL;
               }
            }

            BlockPos pos = context.getBlockPos().up();
            ServerWorld serverWorld = (ServerWorld)world;
            String entityTypeId = entityNbt.getString("id");
            EntityType<?> entityType = EntityType.get(entityTypeId).orElse(null);
            if (entityType == null) {
               user.sendMessage(Text.translatable("item.heartbound.master_ring.invalid_data").formatted(Formatting.RED), true);
               return ActionResult.FAIL;
            } else if (entityType.create(serverWorld) instanceof GirlEntity girl) {
               girl.readNbt(entityNbt);
               girl.refreshPositionAndAngles((double)pos.getX() + 0.5, (double)pos.getY(), (double)pos.getZ() + 0.5, user.getYaw(), 0.0F);
               girl.setVelocity(Vec3d.ZERO);
               girl.setHeadYaw(user.getYaw());
               girl.setBodyYaw(user.getYaw());
               if (serverWorld.spawnEntity(girl)) {
                  stack.remove(HeartboundDataComponentTypes.STORED_ENTITY);
                  user.sendMessage(Text.translatable("item.heartbound.master_ring.released", girl.getGirlDisplayName()).formatted(Formatting.GREEN), true);
                  return ActionResult.SUCCESS;
               } else {
                  user.sendMessage(Text.translatable("item.heartbound.master_ring.spawn_failed").formatted(Formatting.RED), true);
                  return ActionResult.FAIL;
               }
            } else {
               user.sendMessage(Text.translatable("item.heartbound.master_ring.invalid_data").formatted(Formatting.RED), true);
               return ActionResult.FAIL;
            }
         }
      }
   }

   @Override
   public Text getName(ItemStack stack) {
      NbtCompound entityNbt = stack.get(HeartboundDataComponentTypes.STORED_ENTITY);
      return (Text)(entityNbt != null ? Text.translatable("item.heartbound.master_ring.filled") : super.getName(stack));
   }

   @Override
   public boolean hasGlint(ItemStack stack) {
      return stack.contains(HeartboundDataComponentTypes.STORED_ENTITY);
   }

   @Override
   public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
      super.appendTooltip(stack, context, tooltip, type);
      NbtCompound entityNbt = stack.get(HeartboundDataComponentTypes.STORED_ENTITY);
      if (entityNbt != null) {
         String girlName = "Unknown";
         if (entityNbt.contains("GirlProfileID")) {
            String profileId = entityNbt.getString("GirlProfileID");
            CustomGirlProfile profile = CustomGirlLoader.LOADED_PROFILES.get(profileId);
            if (profile != null) {
               girlName = profile.name();
            } else {
               girlName = profileId;
            }
         } else if (entityNbt.contains("CustomName")) {
            try {
               girlName = Serialization.fromJson(entityNbt.getString("CustomName"), context.getRegistryLookup()).getString();
            } catch (Exception var9) {
               girlName = "Unknown";
            }
         } else if (entityNbt.contains("id")) {
            String entityId = entityNbt.getString("id");
            EntityType<?> entityType = EntityType.get(entityId).orElse(null);
            if (entityType != null) {
               girlName = entityType.getName().getString();
            }
         }

         tooltip.add(Text.translatable("item.heartbound.master_ring.tooltip.girl", girlName).formatted(Formatting.GRAY));
         if (entityNbt.contains("Owner")) {
            UUID ownerUuid = entityNbt.getUuid("Owner");
            String ownerName = "Unknown";
            if (entityNbt.contains("OwnerName")) {
               ownerName = entityNbt.getString("OwnerName");
            } else {
               ownerName = ownerUuid.toString().substring(0, 8) + "...";
            }

            tooltip.add(Text.translatable("item.heartbound.master_ring.tooltip.owner", ownerName).formatted(Formatting.GRAY));
         }
      }
   }
}
