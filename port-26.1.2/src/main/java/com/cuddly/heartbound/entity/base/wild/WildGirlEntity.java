package com.cuddly.heartbound.entity.base.wild;

import com.cuddly.heartbound.advancement.criterion.HeartboundCriteria;
import com.cuddly.heartbound.component.HeartboundDataComponentTypes;
import com.cuddly.heartbound.entity.base.GirlSceneEntity;
import com.cuddly.heartbound.item.HeartboundItems;
import com.cuddly.heartbound.networking.S2C.SceneOptionsS2CPacket;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.Level;

public abstract class WildGirlEntity extends GirlSceneEntity {
   private static final List<EquipmentSlot> EQUIPMENT_INIT_ORDER = List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);

   protected WildGirlEntity(EntityType<? extends GirlSceneEntity> entityType, Level world) {
      super(entityType, world);
   }

   @Override
   protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance localDifficulty) {
      if (random.nextFloat() < 1.0F) {
         this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.BOW));
         int i = random.nextInt(4);
         switch (i) {
            case 0:
               this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
               break;
            case 1:
               this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
               break;
            case 2:
               this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
               break;
            case 3:
               this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
               break;
            case 4:
               this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
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
            ItemStack itemStack = this.getItemBySlot(equipmentSlot);
            if (!bl && random.nextFloat() < f) {
               break;
            }

            bl = false;
            if (itemStack.isEmpty()) {
               Item item = getEquipmentForSlot(equipmentSlot, num);
               if (item != null) {
                  this.setItemSlot(equipmentSlot, new ItemStack(item));
               }
            }
         }
      }

      super.populateDefaultEquipmentSlots(random, localDifficulty);
   }

   @Override
   public boolean useUpRelationShipLevels() {
      return true;
   }

   @Override
   protected void registerGoals() {
      super.registerGoals();
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(1, new TemptGoal(this, 1.0, Ingredient.of(this.isAttractedTo()), false));
      this.goalSelector.addGoal(2, new RandomStrollGoal(this, 1.0));
      this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
      this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
      this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
   }

   @Override
   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (!this.level().isClientSide() && !this.isSceneActive()) {
         if (stack.is(HeartboundItems.MASTER_RING)) {
            if (stack.has(HeartboundDataComponentTypes.STORED_ENTITY)) {
               player.sendOverlayMessage(Component.translatable("item.heartbound.master_ring.already_contains").withStyle(ChatFormatting.RED));
               return InteractionResult.FAIL;
            }

            TagValueOutput saveOutput = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
            this.save(saveOutput);
            CompoundTag entityNbt = saveOutput.buildResult();
            String entityTypeId = EntityType.getKey(this.getType()).toString();
            entityNbt.putString("id", entityTypeId);
            stack.set(HeartboundDataComponentTypes.STORED_ENTITY, entityNbt);
            this.discard();
            player.sendOverlayMessage(Component.translatable("item.heartbound.master_ring.stored", this.getGirlDisplayName()).withStyle(ChatFormatting.GREEN));
            if (player instanceof ServerPlayer serverPlayer) {
               HeartboundCriteria.CAPTURE_MASTER_RING.trigger(serverPlayer);
            }

            return InteractionResult.SUCCESS;
         }

         if (stack.is(this.isAttractedTo()) && this.getCurrentRelationshipLevel() < this.maxRelationshipLevel()) {
            stack.consume(1, player);
            player.sendOverlayMessage(Component.translatable("msg.heartbound.sheLikedTheGift"));
            this.setCurrentRelationshipLevel(this.getCurrentRelationshipLevel() + 1);
            if (this.getCurrentRelationshipLevel() >= this.maxRelationshipLevel() && player instanceof ServerPlayer serverPlayer) {
               HeartboundCriteria.MAX_RELATIONSHIP.trigger(serverPlayer);
            }

            this.level().broadcastEntityEvent(this, (byte)14);
            return InteractionResult.SUCCESS;
         }

         if (!player.getItemInHand(InteractionHand.MAIN_HAND).is(this.isAttractedTo())) {
            this.setGUIOpenState(true, player);
            ServerPlayNetworking.send(
               (ServerPlayer)player,
               new SceneOptionsS2CPacket(this.getId(), this.getCurrentRelationshipLevel(), new ItemStack(this.isAttractedTo()), this.getScenes())
            );
            return InteractionResult.SUCCESS;
         }
      }

      return super.mobInteract(player, hand);
   }

   @Override
   public void setFollowing(boolean follow) {
   }

   @Override
   public void setSitting(boolean sitting) {
   }
}
