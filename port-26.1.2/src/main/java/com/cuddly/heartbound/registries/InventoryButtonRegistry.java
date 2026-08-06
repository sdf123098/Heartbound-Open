package com.cuddly.heartbound.registries;

import com.cuddly.heartbound.client.gui.screen.GirlFeatureScreen;
import com.cuddly.heartbound.entity.base.GirlEntity;
import com.cuddly.heartbound.networking.C2S.InventoryButtonC2SPacket;
import com.cuddly.heartbound.screen.FeatureGroup;
import com.cuddly.heartbound.screen.InventoryButtonAction;
import java.util.List;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.entity.player.Player;

public class InventoryButtonRegistry {
   public static final List<FeatureGroup> FEATURE_GROUPS = List.of(
      new FeatureGroup(
         "gui.heartbound.featureGroup.baseFeatures",
         List.of(
            new FeatureGroup.FeatureEntry(
               girl -> girl.isRoaming() ? Component.translatable("gui.heartbound.button.stopRoaming") : Component.translatable("gui.heartbound.button.roamAtBase"),
               1,
               (girl, player) -> ClientPlayNetworking.send(new InventoryButtonC2SPacket(girl.getId(), "roamAtBase"))
            ),
            new FeatureGroup.FeatureEntry(
               girl -> girl.isChopping() ? Component.translatable("gui.heartbound.button.stopChopping") : Component.translatable("gui.heartbound.button.chopTrees"),
               1,
               (girl, player) -> ClientPlayNetworking.send(new InventoryButtonC2SPacket(girl.getId(), "chopTrees"))
            )
         )
      ),
      new FeatureGroup(
         "gui.heartbound.featureGroup.playerFeatures",
         List.of(
            new FeatureGroup.FeatureEntry(
               girl -> girl.isMining() ? Component.translatable("gui.heartbound.button.stopMining") : Component.translatable("gui.heartbound.button.mineOres"),
               1,
               (girl, player) -> ClientPlayNetworking.send(new InventoryButtonC2SPacket(girl.getId(), "mineOres"))
            )
         )
      )
   );
   public static final List<InventoryButtonAction> BUTTONS_LEFT = List.of(
      new InventoryButtonAction(
         Component.translatable("gui.heartbound.button.breakUp"),
         0,
         (girl, player) -> ClientPlayNetworking.send(new InventoryButtonC2SPacket(girl.getId(), "breakUp"))
      ),
      new InventoryButtonAction(
         Component.translatable("gui.heartbound.button.setBase"),
         1,
         (girl, player) -> ClientPlayNetworking.send(new InventoryButtonC2SPacket(girl.getId(), "setBase"))
      ),
      new InventoryButtonAction(
         Component.translatable("gui.heartbound.button.goToBase"),
         1,
         (girl, player) -> ClientPlayNetworking.send(new InventoryButtonC2SPacket(girl.getId(), "goToBase"))
      ),
      new InventoryButtonAction(Component.translatable("gui.heartbound.button.baseFeature"), 1, (girl, player) -> openFeatureScreen(girl, player, 0), false),
      new InventoryButtonAction(Component.translatable("gui.heartbound.button.customize"), 1, (girl, player) -> {
         closeHandlerSilently(Minecraft.getInstance());
         ClientPlayNetworking.send(new InventoryButtonC2SPacket(girl.getId(), "customize"));
      }, false)
   );
   public static final List<InventoryButtonAction> BUTTONS_RIGHT = List.of(
      new InventoryButtonAction(
         Component.translatable("gui.heartbound.button.sit"), 2, (girl, player) -> ClientPlayNetworking.send(new InventoryButtonC2SPacket(girl.getId(), "sit"))
      ),
      new InventoryButtonAction(
         Component.translatable("gui.heartbound.button.followMe"),
         3,
         (girl, player) -> ClientPlayNetworking.send(new InventoryButtonC2SPacket(girl.getId(), "follow"))
      ),
      new InventoryButtonAction(Component.translatable("gui.heartbound.button.playerFeature"), 1, (girl, player) -> openFeatureScreen(girl, player, 1), false),
      new InventoryButtonAction(
         Component.translatable("gui.heartbound.button.strip"),
         4,
         (girl, player) -> ClientPlayNetworking.send(new InventoryButtonC2SPacket(girl.getId(), "stripOrDressup"))
      ),
      new InventoryButtonAction(Component.translatable("gui.heartbound.button.sex"), 4, (girl, player) -> {
         closeHandlerSilently(Minecraft.getInstance());
         ClientPlayNetworking.send(new InventoryButtonC2SPacket(girl.getId(), "sex"));
      }, false)
   );

   private static void openFeatureScreen(GirlEntity girl, Player player, int groupIndex) {
      if (groupIndex < FEATURE_GROUPS.size()) {
         FeatureGroup group = FEATURE_GROUPS.get(groupIndex);
         Minecraft client = Minecraft.getInstance();
         closeHandlerSilently(client);
         client.setScreen(new GirlFeatureScreen(girl, player, group));
      }
   }

   private static void closeHandlerSilently(Minecraft client) {
      LocalPlayer cp = client.player;
      if (cp != null && cp.containerMenu != cp.inventoryMenu) {
         cp.connection.send(new ServerboundContainerClosePacket(cp.containerMenu.containerId));
         cp.containerMenu = cp.inventoryMenu;
      }
   }
}
