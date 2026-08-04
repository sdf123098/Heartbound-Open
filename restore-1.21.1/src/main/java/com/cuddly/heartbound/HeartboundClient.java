package com.cuddly.heartbound;

import com.cuddly.heartbound.client.HeartboundKeybinds;
import com.cuddly.heartbound.client.gui.screen.FusionTableScreen;
import com.cuddly.heartbound.client.gui.screen.GirlInventoryScreen;
import com.cuddly.heartbound.client.rendering.renderers.AbstractGirlRenderer;
import com.cuddly.heartbound.client.rendering.renderers.AlyRenderer;
import com.cuddly.heartbound.client.rendering.renderers.BiaRenderer;
import com.cuddly.heartbound.client.rendering.renderers.CoppieRenderer;
import com.cuddly.heartbound.client.rendering.renderers.CustomGirlRenderer;
import com.cuddly.heartbound.client.rendering.renderers.EllieRenderer;
import com.cuddly.heartbound.client.rendering.renderers.JennyRenderer;
import com.cuddly.heartbound.client.rendering.renderers.KoboldRenderer;
import com.cuddly.heartbound.client.rendering.renderers.SlimeRenderer;
import com.cuddly.heartbound.component.HeartboundDataComponentTypes;
import com.cuddly.heartbound.config.ModBindings;
import com.cuddly.heartbound.config.ModConfig;
import com.cuddly.heartbound.freecam.Freecam;
import com.cuddly.heartbound.item.HeartboundItems;
import com.cuddly.heartbound.networking.HeartboundClientPackets;
import com.cuddly.heartbound.networking.C2S.CumKeybindC2SPacket;
import com.cuddly.heartbound.networking.C2S.ThrustKeybindC2SPacket;
import com.cuddly.heartbound.networking.C2S.TransformSceneKeybindC2SPacket;
import com.cuddly.heartbound.registries.GirlRegistry;
import com.cuddly.heartbound.registries.HeartboundHudRegistry;
import com.cuddly.heartbound.registries.HeartboundScreenHandlerRegistry;
import com.cuddly.heartbound.transformation.TransformablePlayer;
import com.cuddly.heartbound.util.SceneKeyframeEventReloader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;

public class HeartboundClient implements ClientModInitializer {
   private static boolean thrustToggleState = false;
   private static boolean lastSentThrustState = false;

   public void onInitializeClient() {
      ModConfig.init();
      ModBindings.forEach(KeyBindingHelper::registerKeyBinding);
      ClientTickEvents.START_CLIENT_TICK.register(Freecam::preTick);
      ClientTickEvents.END_CLIENT_TICK.register(Freecam::postTick);
      HandledScreens.register(HeartboundScreenHandlerRegistry.GIRL_INVENTORY_SCREEN_HANDLER, GirlInventoryScreen::new);
      HandledScreens.register(HeartboundScreenHandlerRegistry.FUSION_TABLE_SCREEN_HANDLER, FusionTableScreen::new);
      EntityRendererRegistry.register(GirlRegistry.JENNY, JennyRenderer::new);
      EntityRendererRegistry.register(GirlRegistry.ELLIE, EllieRenderer::new);
      EntityRendererRegistry.register(GirlRegistry.BIA, BiaRenderer::new);
      EntityRendererRegistry.register(GirlRegistry.SLIME, SlimeRenderer::new);
      EntityRendererRegistry.register(GirlRegistry.KOBOLD, KoboldRenderer::new);
      EntityRendererRegistry.register(GirlRegistry.COPPIE, CoppieRenderer::new);
      EntityRendererRegistry.register(GirlRegistry.CUSTOM_GIRL, CustomGirlRenderer::new);
      EntityRendererRegistry.register(GirlRegistry.ALY, AlyRenderer::new);
      registerModelPredicates();
      HeartboundKeybinds.register();
      HeartboundClientPackets.registerS2CPackets();
      HeartboundHudRegistry.register();
      handleKeybinds();
      SceneKeyframeEventReloader.registerReloader();
      ClientTickEvents.END_CLIENT_TICK.register(client -> {
         boolean current = ModConfig.INSTANCE.girls.disableShading;
         if (current != AbstractGirlRenderer.IS_SHADING_DISABLED) {
            AbstractGirlRenderer.updateShadingState();
         }
      });
   }

   public static boolean areIrisShadersDisabled() {
      try {
         Class<?> irisApiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
         Object irisApi = irisApiClass.getMethod("getInstance").invoke(null);
         return !(Boolean)irisApiClass.getMethod("isShaderPackInUse").invoke(irisApi);
      } catch (Throwable var2) {
         return true;
      }
   }

   private static void handleKeybinds() {
      ClientTickEvents.END_CLIENT_TICK.register(client -> {
         if (client.player != null) {
            boolean holdMode = true;
            boolean newThrustState;
            if (holdMode) {
               newThrustState = HeartboundKeybinds.thrustKey.isPressed();
            } else {
               if (HeartboundKeybinds.thrustKey.wasPressed()) {
                  thrustToggleState = !thrustToggleState;
               }

               newThrustState = thrustToggleState;
            }

            if (newThrustState != lastSentThrustState) {
               lastSentThrustState = newThrustState;
               ClientPlayNetworking.send(new ThrustKeybindC2SPacket(newThrustState));
            }

            if (HeartboundKeybinds.cumKey.wasPressed()) {
               ClientPlayNetworking.send(new CumKeybindC2SPacket(true));
            }

            if (HeartboundKeybinds.transformSceneKey.wasPressed()) {
               TransformablePlayer tp = (TransformablePlayer)client.player;
               if (tp.heartbound$isTransformed()) {
                  ClientPlayNetworking.send(new TransformSceneKeybindC2SPacket());
               }
            }
         }
      });
   }

   private static void registerModelPredicates() {
      ModelPredicateProviderRegistry.register(
         HeartboundItems.LOVE_BALL,
         Identifier.of("heartbound", "captured"),
         (stack, world, entity, seed) -> stack.contains(HeartboundDataComponentTypes.STORED_ENTITY) ? 1.0F : 0.0F
      );
   }

   public static record GirlScreenData(int entityId) {
      public static final PacketCodec<RegistryByteBuf, HeartboundClient.GirlScreenData> PACKET_CODEC = PacketCodec.tuple(
         PacketCodecs.VAR_INT, HeartboundClient.GirlScreenData::entityId, HeartboundClient.GirlScreenData::new
      );
   }
}
