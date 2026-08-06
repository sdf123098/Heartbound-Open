package com.cuddly.heartbound.networking;

import com.cuddly.heartbound.Heartbound;
import com.cuddly.heartbound.client.gui.screen.GirlCustomizeScreen;
import com.cuddly.heartbound.client.gui.screen.GirlSceneScreen;
import com.cuddly.heartbound.client.gui.screen.KoboldCustomizeScreen;
import com.cuddly.heartbound.client.gui.screen.TransformSceneScreen;
import com.cuddly.heartbound.client.gui.screen.hud.SceneProgressOverlay;
import com.cuddly.heartbound.client.models.AbstractGirlModel;
import com.cuddly.heartbound.entity.base.GirlSceneEntity;
import com.cuddly.heartbound.networking.S2C.ClothingArmorVisibilityS2CPacket;
import com.cuddly.heartbound.networking.S2C.OpenCustomizeScreenS2CPacket;
import com.cuddly.heartbound.networking.S2C.OpenKoboldCustomizeScreenS2CPacket;
import com.cuddly.heartbound.networking.S2C.PlayAttackAnimationS2CPacket;
import com.cuddly.heartbound.networking.S2C.PlayCumHudAnimationS2CPacket;
import com.cuddly.heartbound.networking.S2C.RefreshModelsS2CPacket;
import com.cuddly.heartbound.networking.S2C.RunAnimEventsS2CPacket;
import com.cuddly.heartbound.networking.S2C.SceneOptionsS2CPacket;
import com.cuddly.heartbound.networking.S2C.TransformSceneOptionsS2CPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.EquipmentSlot;

@Environment(EnvType.CLIENT)
public class HeartboundClientPackets {
   public static void registerS2CPackets() {
      Heartbound.LOGGER.info("Registering S2C Packets for Heartbound");
      ClientPlayNetworking.registerGlobalReceiver(ClothingArmorVisibilityS2CPacket.ID, (packet, context) -> context.client().execute(() -> {
            ClientLevel world = context.client().level;
            if (world != null) {
               if (world.getEntity(packet.entityId()) instanceof GirlSceneEntity girl) {
                  int i = 0;

                  for (EquipmentSlot slot : EquipmentSlot.values()) {
                     girl.armorVisibility.put(slot, packet.armor().get(i));
                     i++;
                  }

                  girl.applyClothingAndArmor();
               }
            }
         }));
      ClientPlayNetworking.registerGlobalReceiver(
         SceneOptionsS2CPacket.ID,
         (packet, context) -> context.client()
               .execute(
                  () -> Minecraft.getInstance()
                        .setScreen(new GirlSceneScreen(packet.entityId(), packet.currentRelationshipLevel(), packet.attractedTo(), packet.options()))
               )
      );
      ClientPlayNetworking.registerGlobalReceiver(
         OpenCustomizeScreenS2CPacket.ID,
         (packet, context) -> context.client()
               .execute(() -> Minecraft.getInstance().setScreen(new GirlCustomizeScreen(packet.entityId(), packet.previewEntityId())))
      );
      ClientPlayNetworking.registerGlobalReceiver(
         PlayCumHudAnimationS2CPacket.ID, (packet, context) -> context.client().execute(SceneProgressOverlay::triggerCumAnimation)
      );
      ClientPlayNetworking.registerGlobalReceiver(RefreshModelsS2CPacket.ID, (packet, context) -> AbstractGirlModel.refreshAllModels());
      ClientPlayNetworking.registerGlobalReceiver(PlayAttackAnimationS2CPacket.ID, (packet, context) -> context.client().execute(() -> {
            ClientLevel world = context.client().level;
            if (world != null) {
               if (world.getEntity(packet.entityId()) instanceof GirlSceneEntity girl) {
                  girl.triggerSwing();
               }
            }
         }));
      ClientPlayNetworking.registerGlobalReceiver(RunAnimEventsS2CPacket.ID, (packet, context) -> context.client().execute(() -> {
            ClientLevel world = context.client().level;
            if (world != null) {
               if (world.getEntity(packet.entityId()) instanceof GirlSceneEntity girl) {
                  girl.handleAnimationEventClient(packet.event());
               }
            }
         }));
      ClientPlayNetworking.registerGlobalReceiver(
         OpenKoboldCustomizeScreenS2CPacket.ID,
         (packet, context) -> context.client()
               .execute(() -> Minecraft.getInstance().setScreen(new KoboldCustomizeScreen(packet.entityId(), packet.previewEntityId())))
      );
      ClientPlayNetworking.registerGlobalReceiver(
         TransformSceneOptionsS2CPacket.ID,
         (packet, context) -> context.client().execute(() -> Minecraft.getInstance().setScreen(new TransformSceneScreen(packet.scenes())))
      );
   }
}
