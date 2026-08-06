package com.cuddly.heartbound.client;

import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

@Environment(EnvType.CLIENT)
public final class ClientUtils {
   private ClientUtils() {
   }

   public static boolean assetExistsClient(Identifier path) {
      Minecraft client = Minecraft.getInstance();
      if (client != null && client.getResourceManager() != null) {
         ResourceManager manager = client.getResourceManager();
         Optional<Resource> resource = manager.getResource(path);
         return resource.isPresent();
      } else {
         return false;
      }
   }
}
