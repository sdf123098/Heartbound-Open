package com.cuddly.heartbound.client;

import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public final class ClientUtils {
   private ClientUtils() {
   }

   public static boolean assetExistsClient(Identifier path) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client != null && client.getResourceManager() != null) {
         ResourceManager manager = client.getResourceManager();
         Optional<Resource> resource = manager.getResource(path);
         return resource.isPresent();
      } else {
         return false;
      }
   }
}
