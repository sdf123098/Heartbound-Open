package com.cuddly.heartbound.item;

import com.cuddly.heartbound.component.HeartboundDataComponentTypes;
import com.cuddly.heartbound.entity.girls.CustomGirlEntity;
import com.cuddly.heartbound.registries.GirlRegistry;
import com.cuddly.heartbound.util.json.CustomGirlLoader;
import com.cuddly.heartbound.util.variables.CustomGirlProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class CustomGirlSpawnEggItem extends Item {
   public CustomGirlSpawnEggItem(Properties settings) {
      super(settings);
   }

   @Override
   public Component getName(ItemStack stack) {
      String profileId = this.getProfileIdFromStack(stack);
      if (profileId != null) {
         CustomGirlProfile profile = CustomGirlLoader.getGirlOrDefault(profileId);
         return Component.translatable("item.heartbound.custom_girl_spawn_egg.named", profile.name());
      } else {
         return Component.translatable("item.heartbound.custom_girl_spawn_egg");
      }
   }

   private String getProfileIdFromStack(ItemStack stack) {
      return stack.get(HeartboundDataComponentTypes.GIRL_PROFILE_ID);
   }

   @Override
   public InteractionResult useOn(UseOnContext context) {
      Level world = context.getLevel();
      if (!(world instanceof ServerLevel)) {
         return InteractionResult.SUCCESS;
      } else {
         ItemStack itemStack = context.getItemInHand();
         String profileId = this.getProfileIdFromStack(itemStack);
         if (profileId == null) {
            return InteractionResult.FAIL;
         } else {
            BlockPos blockPos = context.getClickedPos();
            Direction direction = context.getClickedFace();
            BlockPos spawnPos = blockPos.relative(direction);
            if (!world.getBlockState(spawnPos).canBeReplaced()) {
               return InteractionResult.CONSUME;
            } else {
               CustomGirlEntity entity = GirlRegistry.CUSTOM_GIRL.create((ServerLevel)world, EntitySpawnReason.SPAWN_ITEM_USE);
               if (entity != null) {
                  CustomGirlProfile profile = CustomGirlLoader.getGirlOrDefault(profileId);
                  entity.setProfile(profile, true);
                  entity.teleportTo((double)spawnPos.getX() + 0.5, (double)spawnPos.getY(), (double)spawnPos.getZ() + 0.5);
                  entity.setYRot(0.0F);
                  entity.setXRot(0.0F);
                  world.addFreshEntity(entity);
                  world.gameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, spawnPos);
                  Player player = context.getPlayer();
                  if (player != null && !player.getAbilities().instabuild) {
                     itemStack.shrink(1);
                  }
               }

               return InteractionResult.CONSUME;
            }
         }
      }
   }
}
