package com.cuddly.heartbound.entity.base.wild;

import com.cuddly.heartbound.advancement.criterion.HeartboundCriteria;
import com.cuddly.heartbound.component.HeartboundDataComponentTypes;
import com.cuddly.heartbound.entity.base.GirlSceneEntity;
import com.cuddly.heartbound.item.HeartboundItems;
import com.cuddly.heartbound.networking.S2C.SceneOptionsS2CPacket;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;

public abstract class WildGirlEntity extends GirlSceneEntity {
   private static final List<EquipmentSlot> EQUIPMENT_INIT_ORDER = List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);

   protected WildGirlEntity(EntityType<? extends GirlSceneEntity> entityType, World world) {
      super(entityType, world);
   }

   @Override
   protected void initEquipment(Random random, LocalDifficulty localDifficulty) {
      if (random.nextFloat() < 1.0F) {
         this.equipStack(EquipmentSlot.OFFHAND, new ItemStack(Items.BOW));
         int i = random.nextInt(4);
         switch (i) {
            case 0:
               this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
               break;
            case 1:
               this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
               break;
            case 2:
               this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
               break;
            case 3:
               this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
               break;
            case 4:
               this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
         }

         int num = random.nextInt(2);
         float f = 0.1F;
         if (random.nextFloat() < 0.095F) {
            num++;
         }

         if (random.nextFloat() < 0.095F) {
            num++;
         }

         if (random.nextFloat() < 0.095F) {
            num++;
         }

         boolean bl = true;

         for (EquipmentSlot equipmentSlot : EQUIPMENT_INIT_ORDER) {
            ItemStack itemStack = this.getEquippedStack(equipmentSlot);
            if (!bl && random.nextFloat() < f) {
               break;
            }

            bl = false;
            if (itemStack.isEmpty()) {
               Item item = getEquipmentForSlot(equipmentSlot, num);
               if (item != null) {
                  this.equipStack(equipmentSlot, new ItemStack(item));
               }
            }
         }
      }

      super.initEquipment(random, localDifficulty);
   }

   @Override
   public boolean useUpRelationShipLevels() {
      return true;
   }

   @Override
   protected void initGoals() {
      super.initGoals();
      this.goalSelector.add(0, new SwimGoal(this));
      this.goalSelector.add(1, new TemptGoal(this, 1.0, Ingredient.ofItems(this.isAttractedTo()), false));
      this.goalSelector.add(2, new WanderAroundGoal(this, 1.0));
      this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
      this.goalSelector.add(4, new LookAroundGoal(this));
      this.targetSelector.add(1, new RevengeGoal(this));
   }

   @Override
   public ActionResult interactMob(PlayerEntity player, Hand hand) {
      ItemStack stack = player.getStackInHand(hand);
      if (!this.getWorld().isClient() && !this.isSceneActive()) {
         if (stack.isOf(HeartboundItems.MASTER_RING)) {
            if (stack.contains(HeartboundDataComponentTypes.STORED_ENTITY)) {
               player.sendMessage(Text.translatable("item.heartbound.master_ring.already_contains").formatted(Formatting.RED), true);
               return ActionResult.FAIL;
            }

            NbtCompound entityNbt = new NbtCompound();
            this.saveNbt(entityNbt);
            String entityTypeId = EntityType.getId(this.getType()).toString();
            entityNbt.putString("id", entityTypeId);
            stack.set(HeartboundDataComponentTypes.STORED_ENTITY, entityNbt);
            this.discard();
            player.sendMessage(Text.translatable("item.heartbound.master_ring.stored", this.getGirlDisplayName()).formatted(Formatting.GREEN), true);
            if (player instanceof ServerPlayerEntity serverPlayer) {
               HeartboundCriteria.CAPTURE_MASTER_RING.trigger(serverPlayer);
            }

            return ActionResult.SUCCESS;
         }

         if (stack.isOf(this.isAttractedTo()) && this.getCurrentRelationshipLevel() < this.maxRelationshipLevel()) {
            stack.decrementUnlessCreative(1, player);
            player.sendMessage(Text.translatable("msg.heartbound.sheLikedTheGift"), true);
            this.setCurrentRelationshipLevel(this.getCurrentRelationshipLevel() + 1);
            if (this.getCurrentRelationshipLevel() >= this.maxRelationshipLevel() && player instanceof ServerPlayerEntity serverPlayer) {
               HeartboundCriteria.MAX_RELATIONSHIP.trigger(serverPlayer);
            }

            this.getWorld().sendEntityStatus(this, (byte)14);
            return ActionResult.SUCCESS;
         }

         if (!player.getStackInHand(Hand.MAIN_HAND).isOf(this.isAttractedTo())) {
            this.setGUIOpenState(true, player);
            ServerPlayNetworking.send(
               (ServerPlayerEntity)player,
               new SceneOptionsS2CPacket(this.getId(), this.getCurrentRelationshipLevel(), new ItemStack(this.isAttractedTo()), this.getScenes())
            );
            return ActionResult.SUCCESS;
         }
      }

      return super.interactMob(player, hand);
   }

   @Override
   public void setFollowing(boolean follow) {
   }

   @Override
   public void setSitting(boolean sitting) {
   }
}
