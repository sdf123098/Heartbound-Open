package com.cuddly.heartbound.client.rendering;

import com.cuddly.heartbound.Heartbound;
import com.cuddly.heartbound.client.rendering.renderers.TransformedPlayerRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.constant.DataTickets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.player.Player;

@Environment(EnvType.CLIENT)
public class TransformedPlayerRenderManager {
   private static TransformedPlayerRenderer renderer;
   private static Context cachedContext;
   private static final Map<UUID, String> lastRenderedGirlIdMap = new HashMap<>();

   public static void captureContext(Context context) {
      cachedContext = context;
   }

   public static void render(
      Player player, String girlId, PoseStack matrices, SubmitNodeCollector collector, CameraRenderState cameraRenderState
   ) {
      if (cachedContext != null) {
         if (renderer == null) {
            renderer = new TransformedPlayerRenderer(cachedContext);
         }

         TransformedPlayerAnimatable.INSTANCE.setCurrentGirlId(girlId);
         TransformedPlayerAnimatable.INSTANCE.setCurrentEntity(player);
         renderer.setGirlId(girlId);
         UUID playerId = player.getUUID();
         String lastGirlId = lastRenderedGirlIdMap.getOrDefault(playerId, "");
         if (!girlId.equals(lastGirlId) && !lastGirlId.isEmpty()) {
            TransformedPlayerAnimatable.INSTANCE.resetAnimationState((long)player.getId());
         }

         if (!girlId.equals(lastGirlId)) {
            Heartbound.LOGGER.info("[TransformedPlayerRenderManager] start rendering player {} ({}) as girl '{}'", player.getScoreboardName(), player.getId(), girlId);
         }

         lastRenderedGirlIdMap.put(playerId, girlId);

         matrices.pushPose();
         TransformedPlayerRenderer.TransformedPlayerRenderState state = null;
         try {
            TransformedPlayerAnimatable anim = TransformedPlayerAnimatable.INSTANCE;
            AnimatableInstanceCache cache = anim.getAnimatableInstanceCache();
            Object manager = cache == null ? null : cache.getManagerForId(player.getId());
            Heartbound.LOGGER.info(
               "[TransformedPlayerRenderManager] diag: instanceCache={} manager(player {} id {})={} animatable={}",
               cache, player.getScoreboardName(), player.getId(), manager, anim
            );
            float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
            state = renderer.createRenderState(player, partialTick);
            renderer.extractRenderState(player, state, partialTick);
            renderer.submit(state, matrices, collector, cameraRenderState);
         } catch (Exception var12) {
            Heartbound.LOGGER.error("Failed to render transformed player model for girl '{}'", girlId, var12);
            if (state != null) {
               Heartbound.LOGGER.error(
                  "[TransformedPlayerRenderManager] diag: state={} dataMap keys={} hasManager={}",
                  state, state.getDataMap().keySet(), state.getDataMap().containsKey(DataTickets.ANIMATABLE_MANAGER)
               );
            }
         } finally {
            matrices.popPose();
         }
         }
   }
}
