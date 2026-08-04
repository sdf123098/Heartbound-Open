package com.cuddly.heartbound.item;

import com.cuddly.heartbound.component.HeartboundDataComponentTypes;
import com.cuddly.heartbound.entity.girls.CustomGirlEntity;
import com.cuddly.heartbound.registries.GirlRegistry;
import com.cuddly.heartbound.util.json.CustomGirlLoader;
import com.cuddly.heartbound.util.variables.CustomGirlProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Item.Settings;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class CustomGirlSpawnEggItem extends Item {
   public CustomGirlSpawnEggItem(Settings settings) {
      super(settings);
   }

   @Override
   public Text getName(ItemStack stack) {
      String profileId = this.getProfileIdFromStack(stack);
      if (profileId != null) {
         CustomGirlProfile profile = CustomGirlLoader.getGirlOrDefault(profileId);
         return Text.translatable("item.heartbound.custom_girl_spawn_egg.named", profile.name());
      } else {
         return Text.translatable("item.heartbound.custom_girl_spawn_egg");
      }
   }

   private String getProfileIdFromStack(ItemStack stack) {
      return stack.get(HeartboundDataComponentTypes.GIRL_PROFILE_ID);
   }

   @Override
   public ActionResult useOnBlock(ItemUsageContext context) {
      World world = context.getWorld();
      if (!(world instanceof ServerWorld)) {
         return ActionResult.SUCCESS;
      } else {
         ItemStack itemStack = context.getStack();
         String profileId = this.getProfileIdFromStack(itemStack);
         if (profileId == null) {
            return ActionResult.FAIL;
         } else {
            BlockPos blockPos = context.getBlockPos();
            Direction direction = context.getSide();
            BlockPos spawnPos = blockPos.offset(direction);
            if (!world.getBlockState(spawnPos).isReplaceable()) {
               return ActionResult.CONSUME;
            } else {
               CustomGirlEntity entity = GirlRegistry.CUSTOM_GIRL.create((ServerWorld)world);
               if (entity != null) {
                  CustomGirlProfile profile = CustomGirlLoader.getGirlOrDefault(profileId);
                  entity.setProfile(profile, true);
                  entity.refreshPositionAndAngles((double)spawnPos.getX() + 0.5, (double)spawnPos.getY(), (double)spawnPos.getZ() + 0.5, 0.0F, 0.0F);
                  world.spawnEntity(entity);
                  world.emitGameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, spawnPos);
                  PlayerEntity player = context.getPlayer();
                  if (player != null && !player.getAbilities().creativeMode) {
                     itemStack.decrement(1);
                  }
               }

               return ActionResult.CONSUME;
            }
         }
      }
   }
}
