package com.cuddly.heartbound.client.rendering;

import com.cuddly.heartbound.Heartbound;
import com.cuddly.heartbound.client.rendering.renderers.TransformedPlayerRenderer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;

@Environment(EnvType.CLIENT)
public class TransformedPlayerRenderManager {
   private static TransformedPlayerRenderer renderer;
   private static Context cachedContext;
   private static final Map<UUID, String> lastRenderedGirlIdMap = new HashMap<>();

   public static void captureContext(Context context) {
      cachedContext = context;
   }

   public static void render(
      PlayerEntity player, String girlId, float entityYaw, float partialTick, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light
   ) {
      if (cachedContext != null) {
         if (renderer == null) {
            renderer = new TransformedPlayerRenderer(cachedContext);
         }

         TransformedPlayerAnimatable.INSTANCE.setCurrentGirlId(girlId);
         TransformedPlayerAnimatable.INSTANCE.setCurrentEntity(player);
         renderer.setGirlId(girlId);
         UUID playerId = player.getUuid();
         String lastGirlId = lastRenderedGirlIdMap.getOrDefault(playerId, "");
         if (!girlId.equals(lastGirlId) && !lastGirlId.isEmpty()) {
            TransformedPlayerAnimatable.INSTANCE.resetAnimationState((long)player.getId());
         }

         lastRenderedGirlIdMap.put(playerId, girlId);

         try {
            renderer.render(player, entityYaw, partialTick, matrices, vertexConsumers, light);
         } catch (Exception var12) {
            Heartbound.LOGGER.error("Failed to render transformed player model for girl '{}': {}", girlId, var12.getMessage());

            try {
               matrices.pop();
            } catch (Exception var11) {
            }
         }
      }
   }
}
