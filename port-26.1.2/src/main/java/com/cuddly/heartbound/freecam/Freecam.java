package com.cuddly.heartbound.freecam;

import com.cuddly.heartbound.client.rendering.renderers.TransformedPlayerRenderer;
import com.cuddly.heartbound.config.ModBindings;
import com.cuddly.heartbound.config.ModConfig;
import com.cuddly.heartbound.freecam.tripod.TripodRegistry;
import com.cuddly.heartbound.freecam.tripod.TripodSlot;
import com.cuddly.heartbound.transformation.TransformablePlayer;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Input;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.renderer.texture.TickableTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.AvailableSince;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;

public class Freecam {
   public static final Minecraft MC = Minecraft.getInstance();
   public static final String MOD_ID = "freecam";
   private static boolean freecamEnabled = false;
   private static boolean tripodEnabled = false;
   private static boolean playerControlEnabled = false;
   private static boolean disableNextTick = false;
   private static final TripodRegistry tripods = new TripodRegistry();
   private static TripodSlot activeTripod = TripodSlot.NONE;
   private static FreeCamera freeCamera;
   private static CameraType rememberedF5 = null;

   @Internal
   public static void preTick(Minecraft mc) {
      if (disableNextTick && isEnabled()) {
         toggle();
      }

      disableNextTick = false;
      if (isEnabled() && mc.player != null && mc.player.input instanceof KeyboardInput && !isPlayerControlEnabled()) {
         ClientInput input = mc.player.input;
         boolean shift = !mc.player.isPassenger() && input.keyPresses.shift();
         input.keyPresses = new Input(false, false, false, false, false, shift, false);
      }
   }

   @Internal
   public static void postTick(Minecraft mc) {
      ModBindings.forEach(TickableTexture::tick);
   }

   @Internal
   public static void onDisconnect() {
      if (isEnabled()) {
         toggle();
      }

      tripods.clear();
   }

   @Internal
   public static boolean activateTripodHandler() {
      boolean activated = false;
      KeyMapping[] var1 = MC.options.keyHotbarSlots;
      int var2 = var1.length;

      for (int var3 = 0; var3 < var2; var3++) {
         for (KeyMapping combo = var1[var3]; combo.consumeClick(); activated = true) {
            toggleTripod(TripodSlot.ofKeyCode(combo.getDefaultKey().getValue()));
         }
      }

      return activated;
   }

   @Internal
   public static boolean resetTripodHandler() {
      boolean reset = false;
      KeyMapping[] var1 = MC.options.keyHotbarSlots;
      int var2 = var1.length;

      for (int var3 = 0; var3 < var2; var3++) {
         for (KeyMapping key = var1[var3]; key.consumeClick(); reset = true) {
            resetCamera(TripodSlot.ofKeyCode(key.getDefaultKey().getValue()));
         }
      }

      return reset;
   }

   @AvailableSince("0.3.1")
   public static void toggle() {
      if (tripodEnabled) {
         toggleTripod(activeTripod);
      } else {
         if (freecamEnabled) {
            onDisableFreecam();
         } else {
            onEnableFreecam();
         }

         freecamEnabled = !freecamEnabled;
         if (!freecamEnabled) {
            onDisabled();
         }
      }
   }

   private static void toggleTripod(TripodSlot tripod) {
      if (tripod != TripodSlot.NONE) {
         if (tripodEnabled) {
            if (activeTripod == tripod) {
               onDisableTripod();
               tripodEnabled = false;
            } else {
               onDisableTripod();
               onEnableTripod(tripod);
            }
         } else {
            if (freecamEnabled) {
               toggle();
            }

            onEnableTripod(tripod);
            tripodEnabled = true;
         }

         if (!tripodEnabled) {
            onDisabled();
         }
      }
   }

   @AvailableSince("1.1.8")
   public static void switchControls() {
      if (isEnabled()) {
         if (playerControlEnabled) {
            freeCamera.input = new KeyboardInput(MC.options);
         } else {
            MC.player.input = new KeyboardInput(MC.options);
            freeCamera.input = new InactiveInput();
         }

         playerControlEnabled = !playerControlEnabled;
      }
   }

   private static void onEnableTripod(TripodSlot tripod) {
      onEnable();
      FreecamPosition position = tripods.get(tripod);
      boolean chunkLoaded = false;
      if (position != null) {
         ChunkPos chunkPos = position.getChunkPos();
         chunkLoaded = MC.level.getChunkSource().hasChunk(chunkPos.x(), chunkPos.z());
      }

      if (!chunkLoaded) {
         resetCamera(tripod);
         position = null;
      }

      freeCamera = new FreeCamera(-420 - tripod.ordinal());
      if (position == null) {
         moveToPlayer();
      } else {
         moveToPosition(position);
      }

      freeCamera.spawn();
      MC.setCameraEntity(freeCamera);
      activeTripod = tripod;
      if (ModConfig.INSTANCE.notification.notifyTripod) {
         MC.player.sendOverlayMessage(Component.translatable("msg.freecam.openTripod", tripod));
      }
   }

   private static void onDisableTripod() {
      tripods.put(activeTripod, new FreecamPosition(freeCamera));
      onDisable();
      if (MC.player != null && ModConfig.INSTANCE.notification.notifyTripod) {
         MC.player.sendOverlayMessage(Component.translatable("msg.freecam.closeTripod", activeTripod));
      }

      activeTripod = TripodSlot.NONE;
   }

   private static void onEnableFreecam() {
      onEnable();
      freeCamera = new FreeCamera(-420);
      moveToPlayer();
      freeCamera.spawn();
      MC.setCameraEntity(freeCamera);
      if (ModConfig.INSTANCE.notification.notifyFreecam) {
         MC.player.sendOverlayMessage(Component.translatable("msg.freecam.enable"));
      }
   }

   private static void onDisableFreecam() {
      onDisable();
      if (MC.player != null && ModConfig.INSTANCE.notification.notifyFreecam) {
         MC.player.sendOverlayMessage(Component.translatable("msg.freecam.disable"));
      }
   }

   private static void onEnable() {
      MC.smartCull = false;
      rememberedF5 = MC.options.getCameraType();
      if (MC.gameRenderer.getMainCamera().isDetached()) {
         MC.options.setCameraType(CameraType.FIRST_PERSON);
      }
   }

   private static void onDisable() {
      MC.smartCull = true;
      MC.setCameraEntity(MC.player);
      playerControlEnabled = false;
      freeCamera.despawn();
      freeCamera.input = new InactiveInput();
      freeCamera = null;
      if (MC.player != null) {
         MC.player.input = new KeyboardInput(MC.options);
      }
   }

   private static void onDisabled() {
      if (rememberedF5 != null) {
         MC.options.setCameraType(rememberedF5);
      }
   }

   private static void resetCamera(TripodSlot tripod) {
      if (tripodEnabled && activeTripod != TripodSlot.NONE && activeTripod == tripod && freeCamera != null) {
         moveToPlayer();
      } else {
         tripods.put(tripod, null);
      }

      if (ModConfig.INSTANCE.notification.notifyTripod) {
         MC.player.sendOverlayMessage(Component.translatable("msg.freecam.tripodReset", tripod));
      }
   }

   @Experimental
   @AvailableSince("1.2.3")
   public static void moveToEntity(@Nullable Entity entity) {
      if (freeCamera != null) {
         if (entity == null) {
            moveToPlayer();
         } else {
            freeCamera.copyPosition(entity);
         }
      }
   }

   @Experimental
   @AvailableSince("1.2.3")
   public static void moveToPosition(@Nullable FreecamPosition position) {
      if (freeCamera != null) {
         if (position == null) {
            moveToPlayer();
         } else {
            freeCamera.applyPosition(position);
         }
      }
   }

   @Experimental
   @AvailableSince("1.2.3")
   public static void moveToPlayer() {
      if (freeCamera != null) {
         Vec3 sceneCamPos = getSceneCameraPos();
         if (sceneCamPos != null) {
            freeCamera.copyPosition(MC.player);
            freeCamera.setPos(sceneCamPos.x, sceneCamPos.y, sceneCamPos.z);
            freeCamera.setYRot(MC.player.getYRot());
            freeCamera.setXRot(MC.player.getXRot());
            freeCamera.applyPerspective(ModConfig.INSTANCE.visual.perspective);
         } else {
            freeCamera.copyPosition(MC.player);
            freeCamera.applyPerspective(ModConfig.INSTANCE.visual.perspective);
         }
      }
   }

   @Nullable
   private static Vec3 getSceneCameraPos() {
      if (MC.player == null) {
         return null;
      } else {
         TransformablePlayer tp = (TransformablePlayer)MC.player;
         if (tp.heartbound$isTransformSceneActive()) {
            return TransformedPlayerRenderer.getGirlCamPos();
         } else {
            if (MC.player.getVehicle() instanceof Player ridden) {
               TransformablePlayer riddenTp = (TransformablePlayer)ridden;
               if (riddenTp.heartbound$isTransformSceneActive()) {
                  return TransformedPlayerRenderer.getBoyCamPos();
               }
            }

            return null;
         }
      }
   }

   @AvailableSince("0.4.0")
   public static FreeCamera getFreeCamera() {
      return freeCamera;
   }

   @AvailableSince("1.2.3")
   public static void disableNextTick() {
      disableNextTick = true;
   }

   @AvailableSince("0.2.2")
   public static boolean isEnabled() {
      return freecamEnabled || tripodEnabled;
   }

   @Experimental
   @AvailableSince("1.0.0")
   public static boolean isPlayerControlEnabled() {
      return playerControlEnabled;
   }

   private static final class InactiveInput extends KeyboardInput {
      private InactiveInput() {
         super(MC.options);
      }

      @Override
      public void tick() {
      }
   }
}
