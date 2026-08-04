package com.cuddly.heartbound.freecam;

import com.cuddly.heartbound.client.rendering.renderers.TransformedPlayerRenderer;
import com.cuddly.heartbound.config.ModBindings;
import com.cuddly.heartbound.config.ModConfig;
import com.cuddly.heartbound.freecam.tripod.TripodRegistry;
import com.cuddly.heartbound.freecam.tripod.TripodSlot;
import com.cuddly.heartbound.transformation.TransformablePlayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.texture.TextureTickListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.AvailableSince;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;

public class Freecam {
   public static final MinecraftClient MC = MinecraftClient.getInstance();
   public static final String MOD_ID = "freecam";
   private static boolean freecamEnabled = false;
   private static boolean tripodEnabled = false;
   private static boolean playerControlEnabled = false;
   private static boolean disableNextTick = false;
   private static final TripodRegistry tripods = new TripodRegistry();
   private static TripodSlot activeTripod = TripodSlot.NONE;
   private static FreeCamera freeCamera;
   private static Perspective rememberedF5 = null;

   @Internal
   public static void preTick(MinecraftClient mc) {
      if (disableNextTick && isEnabled()) {
         toggle();
      }

      disableNextTick = false;
      if (isEnabled() && mc.player != null && mc.player.input instanceof KeyboardInput && !isPlayerControlEnabled()) {
         Input input = new Input();
         input.movementForward = 0.0F;
         input.movementSideways = 0.0F;
         input.jumping = false;
         input.sneaking = !mc.player.hasVehicle() && mc.player.input.sneaking;
         input.pressingForward = false;
         input.pressingBack = false;
         input.pressingLeft = false;
         input.pressingRight = false;
         mc.player.input = input;
      }
   }

   @Internal
   public static void postTick(MinecraftClient mc) {
      ModBindings.forEach(TextureTickListener::tick);
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
      KeyBinding[] var1 = MC.options.hotbarKeys;
      int var2 = var1.length;

      for (int var3 = 0; var3 < var2; var3++) {
         for (KeyBinding combo = var1[var3]; combo.wasPressed(); activated = true) {
            toggleTripod(TripodSlot.ofKeyCode(combo.getDefaultKey().getCode()));
         }
      }

      return activated;
   }

   @Internal
   public static boolean resetTripodHandler() {
      boolean reset = false;
      KeyBinding[] var1 = MC.options.hotbarKeys;
      int var2 = var1.length;

      for (int var3 = 0; var3 < var2; var3++) {
         for (KeyBinding key = var1[var3]; key.wasPressed(); reset = true) {
            resetCamera(TripodSlot.ofKeyCode(key.getDefaultKey().getCode()));
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
            freeCamera.input = new Input();
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
         chunkLoaded = MC.world.getChunkManager().isChunkLoaded(chunkPos.x, chunkPos.z);
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
         MC.player.sendMessage(Text.translatable("msg.freecam.openTripod", tripod), true);
      }
   }

   private static void onDisableTripod() {
      tripods.put(activeTripod, new FreecamPosition(freeCamera));
      onDisable();
      if (MC.player != null && ModConfig.INSTANCE.notification.notifyTripod) {
         MC.player.sendMessage(Text.translatable("msg.freecam.closeTripod", activeTripod), true);
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
         MC.player.sendMessage(Text.translatable("msg.freecam.enable"), true);
      }
   }

   private static void onDisableFreecam() {
      onDisable();
      if (MC.player != null && ModConfig.INSTANCE.notification.notifyFreecam) {
         MC.player.sendMessage(Text.translatable("msg.freecam.disable"), true);
      }
   }

   private static void onEnable() {
      MC.chunkCullingEnabled = false;
      rememberedF5 = MC.options.getPerspective();
      if (MC.gameRenderer.getCamera().isThirdPerson()) {
         MC.options.setPerspective(Perspective.FIRST_PERSON);
      }
   }

   private static void onDisable() {
      MC.chunkCullingEnabled = true;
      MC.setCameraEntity(MC.player);
      playerControlEnabled = false;
      freeCamera.despawn();
      freeCamera.input = new Input();
      freeCamera = null;
      if (MC.player != null) {
         MC.player.input = new KeyboardInput(MC.options);
      }
   }

   private static void onDisabled() {
      if (rememberedF5 != null) {
         MC.options.setPerspective(rememberedF5);
      }
   }

   private static void resetCamera(TripodSlot tripod) {
      if (tripodEnabled && activeTripod != TripodSlot.NONE && activeTripod == tripod && freeCamera != null) {
         moveToPlayer();
      } else {
         tripods.put(tripod, null);
      }

      if (ModConfig.INSTANCE.notification.notifyTripod) {
         MC.player.sendMessage(Text.translatable("msg.freecam.tripodReset", tripod), true);
      }
   }

   @Experimental
   @AvailableSince("1.2.3")
   public static void moveToEntity(@Nullable Entity entity) {
      if (freeCamera != null) {
         if (entity == null) {
            moveToPlayer();
         } else {
            freeCamera.copyPositionAndRotation(entity);
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
         Vec3d sceneCamPos = getSceneCameraPos();
         if (sceneCamPos != null) {
            freeCamera.copyPositionAndRotation(MC.player);
            freeCamera.refreshPositionAndAngles(sceneCamPos.x, sceneCamPos.y, sceneCamPos.z, MC.player.getYaw(), MC.player.getPitch());
            freeCamera.applyPerspective(ModConfig.INSTANCE.visual.perspective);
         } else {
            freeCamera.copyPositionAndRotation(MC.player);
            freeCamera.applyPerspective(ModConfig.INSTANCE.visual.perspective);
         }
      }
   }

   @Nullable
   private static Vec3d getSceneCameraPos() {
      if (MC.player == null) {
         return null;
      } else {
         TransformablePlayer tp = (TransformablePlayer)MC.player;
         if (tp.heartbound$isTransformSceneActive()) {
            return TransformedPlayerRenderer.getGirlCamPos();
         } else {
            if (MC.player.getVehicle() instanceof PlayerEntity ridden) {
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
}
