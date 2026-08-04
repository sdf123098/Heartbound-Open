package com.cuddly.heartbound.util;

import com.cuddly.heartbound.Heartbound;
import com.cuddly.heartbound.registries.SceneKeyframeEventRegistry;
import com.cuddly.heartbound.util.json.SceneKeyframeEventLoader;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

public class SceneKeyframeEventReloader {
   public static void registerReloader() {
      ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
         public Identifier getFabricId() {
            return Identifier.of("heartbound", "scene_keyframes");
         }

         public void reload(ResourceManager manager) {
            Heartbound.LOGGER.info("[SceneKeyframeEventReloader] Reloading Scene Keyframes...");
            SceneKeyframeEventRegistry.clearAll();
            SceneKeyframeEventLoader.loadFromAssets(manager);
         }
      });
   }
}
