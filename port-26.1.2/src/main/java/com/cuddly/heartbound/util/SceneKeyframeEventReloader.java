package com.cuddly.heartbound.util;

import com.cuddly.heartbound.Heartbound;
import com.cuddly.heartbound.registries.SceneKeyframeEventRegistry;
import com.cuddly.heartbound.util.json.SceneKeyframeEventLoader;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

public class SceneKeyframeEventReloader {
   public static void registerReloader() {
      ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
         public Identifier getFabricId() {
            return Identifier.fromNamespaceAndPath("heartbound", "scene_keyframes");
         }

         public void onResourceManagerReload(ResourceManager manager) {
            Heartbound.LOGGER.info("[SceneKeyframeEventReloader] Reloading Scene Keyframes...");
            SceneKeyframeEventRegistry.clearAll();
            SceneKeyframeEventLoader.loadFromAssets(manager);
         }
      });
   }
}
