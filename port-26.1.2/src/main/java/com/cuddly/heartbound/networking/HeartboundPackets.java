package com.cuddly.heartbound.networking;

import com.cuddly.heartbound.Heartbound;
import com.cuddly.heartbound.advancement.criterion.HeartboundCriteria;
import com.cuddly.heartbound.entity.base.GirlSceneEntity;
import com.cuddly.heartbound.entity.base.tamable.TameableGirlEntity;
import com.cuddly.heartbound.entity.girls.KoboldEntity;
import com.cuddly.heartbound.item.items.XPill;
import com.cuddly.heartbound.networking.C2S.AnimationFinishC2SPacket;
import com.cuddly.heartbound.networking.C2S.AnimationSyncC2SPacket;
import com.cuddly.heartbound.networking.C2S.BonePosSyncC2SPacket;
import com.cuddly.heartbound.networking.C2S.CumKeybindC2SPacket;
import com.cuddly.heartbound.networking.C2S.GirlCustomizeC2SPacket;
import com.cuddly.heartbound.networking.C2S.InventoryButtonC2SPacket;
import com.cuddly.heartbound.networking.C2S.KoboldCustomizeC2SPacket;
import com.cuddly.heartbound.networking.C2S.PlayerAnimLockC2SPacket;
import com.cuddly.heartbound.networking.C2S.PlayerStripStartC2SPacket;
import com.cuddly.heartbound.networking.C2S.PlayerStripToggleC2SPacket;
import com.cuddly.heartbound.networking.C2S.RegisterCustomGirlMessageC2SPacket;
import com.cuddly.heartbound.networking.C2S.RegisterCustomGirlRandomSoundC2SPacket;
import com.cuddly.heartbound.networking.C2S.RegisterCustomGirlSoundC2SPacket;
import com.cuddly.heartbound.networking.C2S.RemovePreviewEntityC2SPacket;
import com.cuddly.heartbound.networking.C2S.ScenePhaseSyncC2SPacket;
import com.cuddly.heartbound.networking.C2S.SetGUIOpenStateC2SPacket;
import com.cuddly.heartbound.networking.C2S.SoundEventSyncC2SPacket;
import com.cuddly.heartbound.networking.C2S.StartSceneC2SPacket;
import com.cuddly.heartbound.networking.C2S.StopSceneOnServerC2SPacket;
import com.cuddly.heartbound.networking.C2S.ThrustKeybindC2SPacket;
import com.cuddly.heartbound.networking.C2S.TransformAnimationFinishC2SPacket;
import com.cuddly.heartbound.networking.C2S.TransformRequestC2SPacket;
import com.cuddly.heartbound.networking.C2S.TransformRevertC2SPacket;
import com.cuddly.heartbound.networking.C2S.TransformSceneKeybindC2SPacket;
import com.cuddly.heartbound.networking.C2S.TransformStartSceneC2SPacket;
import com.cuddly.heartbound.networking.C2S.TransformStopSceneC2SPacket;
import com.cuddly.heartbound.networking.C2S.TransformThrustKeyframeC2SPacket;
import com.cuddly.heartbound.networking.S2C.ClothingArmorVisibilityS2CPacket;
import com.cuddly.heartbound.networking.S2C.OpenCustomizeScreenS2CPacket;
import com.cuddly.heartbound.networking.S2C.OpenKoboldCustomizeScreenS2CPacket;
import com.cuddly.heartbound.networking.S2C.PlayAttackAnimationS2CPacket;
import com.cuddly.heartbound.networking.S2C.PlayCumHudAnimationS2CPacket;
import com.cuddly.heartbound.networking.S2C.RefreshModelsS2CPacket;
import com.cuddly.heartbound.networking.S2C.RunAnimEventsS2CPacket;
import com.cuddly.heartbound.networking.S2C.SceneOptionsS2CPacket;
import com.cuddly.heartbound.networking.S2C.TransformSceneOptionsS2CPacket;
import com.cuddly.heartbound.screen.GirlInventoryScreenHandlerFactory;
import com.cuddly.heartbound.transformation.GirlTransformationInfo;
import com.cuddly.heartbound.transformation.TransformSceneRegistry;
import com.cuddly.heartbound.transformation.TransformablePlayer;
import com.cuddly.heartbound.util.variables.Scene;
import java.util.List;
import java.util.Objects;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class HeartboundPackets {
   public static void registerPackets() {
      Heartbound.LOGGER.info("Registering Packet Codecs for Heartbound");
      PayloadTypeRegistry.serverboundPlay().register(InventoryButtonC2SPacket.ID, InventoryButtonC2SPacket.CODEC);
      PayloadTypeRegistry.serverboundPlay().register(BonePosSyncC2SPacket.ID, BonePosSyncC2SPacket.CODEC);
      PayloadTypeRegistry.serverboundPlay().register(AnimationSyncC2SPacket.ID, AnimationSyncC2SPacket.CODEC);
      PayloadTypeRegistry.serverboundPlay().register(CumKeybindC2SPacket.ID, CumKeybindC2SPacket.CODEC);
      PayloadTypeRegistry.serverboundPlay().register(ThrustKeybindC2SPacket.ID, ThrustKeybindC2SPacket.CODEC);
      PayloadTypeRegistry.serverboundPlay().register(StartSceneC2SPacket.ID, StartSceneC2SPacket.CODEC);
      PayloadTypeRegistry.serverboundPlay().register(SetGUIOpenStateC2SPacket.ID, SetGUIOpenStateC2SPacket.CODEC);
      PayloadTypeRegistry.serverboundPlay().register(AnimationFinishC2SPacket.ID, AnimationFinishC2SPacket.CODEC);
      PayloadTypeRegistry.serverboundPlay().register(ScenePhaseSyncC2SPacket.ID, ScenePhaseSyncC2SPacket.CODEC);
      PayloadTypeRegistry.serverboundPlay().register(StopSceneOnServerC2SPacket.ID, StopSceneOnServerC2SPacket.CODEC);
      PayloadTypeRegistry.serverboundPlay().register(SoundEventSyncC2SPacket.ID, SoundEventSyncC2SPacket.CODEC);
      PayloadTypeRegistry.serverboundPlay().register(RegisterCustomGirlMessageC2SPacket.ID, RegisterCustomGirlMessageC2SPacket.CODEC);
      PayloadTypeRegistry.serverboundPlay().register(RegisterCustomGirlSoundC2SPacket.ID, RegisterCustomGirlSoundC2SPacket.CODEC);
      PayloadTypeRegistry.serverboundPlay().register(RegisterCustomGirlRandomSoundC2SPacket.ID, RegisterCustomGirlRandomSoundC2SPacket.CODEC);
      PayloadTypeRegistry.serverboundPlay().register(GirlCustomizeC2SPacket.ID, GirlCustomizeC2SPacket.CODEC);
      PayloadTypeRegistry.serverboundPlay().register(KoboldCustomizeC2SPacket.ID, KoboldCustomizeC2SPacket.CODEC);
      PayloadTypeRegistry.serverboundPlay().register(RemovePreviewEntityC2SPacket.ID, RemovePreviewEntityC2SPacket.CODEC);
      PayloadTypeRegistry.serverboundPlay().register(TransformRequestC2SPacket.ID, TransformRequestC2SPacket.CODEC);
      PayloadTypeRegistry.serverboundPlay().register(TransformRevertC2SPacket.ID, TransformRevertC2SPacket.CODEC);
      PayloadTypeRegistry.serverboundPlay().register(PlayerStripToggleC2SPacket.ID, PlayerStripToggleC2SPacket.CODEC);
      PayloadTypeRegistry.serverboundPlay().register(PlayerStripStartC2SPacket.ID, PlayerStripStartC2SPacket.CODEC);
      PayloadTypeRegistry.serverboundPlay().register(PlayerAnimLockC2SPacket.ID, PlayerAnimLockC2SPacket.CODEC);
      PayloadTypeRegistry.serverboundPlay().register(TransformSceneKeybindC2SPacket.ID, TransformSceneKeybindC2SPacket.CODEC);
      PayloadTypeRegistry.serverboundPlay().register(TransformStartSceneC2SPacket.ID, TransformStartSceneC2SPacket.CODEC);
      PayloadTypeRegistry.serverboundPlay().register(TransformAnimationFinishC2SPacket.ID, TransformAnimationFinishC2SPacket.CODEC);
      PayloadTypeRegistry.serverboundPlay().register(TransformThrustKeyframeC2SPacket.ID, TransformThrustKeyframeC2SPacket.CODEC);
      PayloadTypeRegistry.serverboundPlay().register(TransformStopSceneC2SPacket.ID, TransformStopSceneC2SPacket.CODEC);
      PayloadTypeRegistry.clientboundPlay().register(ClothingArmorVisibilityS2CPacket.ID, ClothingArmorVisibilityS2CPacket.CODEC);
      PayloadTypeRegistry.clientboundPlay().register(SceneOptionsS2CPacket.ID, SceneOptionsS2CPacket.CODEC);
      PayloadTypeRegistry.clientboundPlay().register(PlayCumHudAnimationS2CPacket.ID, PlayCumHudAnimationS2CPacket.CODEC);
      PayloadTypeRegistry.clientboundPlay().register(OpenCustomizeScreenS2CPacket.ID, OpenCustomizeScreenS2CPacket.CODEC);
      PayloadTypeRegistry.clientboundPlay().register(RefreshModelsS2CPacket.ID, RefreshModelsS2CPacket.CODEC);
      PayloadTypeRegistry.clientboundPlay().register(PlayAttackAnimationS2CPacket.ID, PlayAttackAnimationS2CPacket.CODEC);
      PayloadTypeRegistry.clientboundPlay().register(OpenKoboldCustomizeScreenS2CPacket.ID, OpenKoboldCustomizeScreenS2CPacket.CODEC);
      PayloadTypeRegistry.clientboundPlay().register(RunAnimEventsS2CPacket.ID, RunAnimEventsS2CPacket.CODEC);
      PayloadTypeRegistry.clientboundPlay().register(TransformSceneOptionsS2CPacket.ID, TransformSceneOptionsS2CPacket.CODEC);
   }

   public static void registerC2SPackets() {
      Heartbound.LOGGER.info("Registering C2S Packets for Heartbound");
      ServerPlayNetworking.registerGlobalReceiver(
         InventoryButtonC2SPacket.ID,
         (packet, context) -> Objects.requireNonNull(context.server())
               .execute(
                  () -> {
                     if (context.player().level().getEntity(packet.entityId()) instanceof TameableGirlEntity girl) {
                        String var4 = packet.actionId();
                        switch (var4) {
                           case "stripOrDressup":
                              girl.requestStrip();
                              break;
                           case "breakUp":
                              girl.breakUpParticles(context.player());
                              break;
                           case "setBase":
                              girl.setBasePosHere();
                              BlockPos pos = girl.blockPosition();
                              context.player().sendOverlayMessage(Component.translatable("msg.heartbound.baseSet", pos.getX(), pos.getY(), pos.getZ()));
                              break;
                           case "sex": {
                              ServerPlayer sp = context.player();
                              if (sp.containerMenu != sp.inventoryMenu) {
                                 sp.containerMenu.removed(sp);
                                 sp.containerMenu = sp.inventoryMenu;
                              }

                              ServerPlayNetworking.send(
                                 sp,
                                 new SceneOptionsS2CPacket(
                                    girl.getId(), girl.getCurrentRelationshipLevel(), new ItemStack(girl.isAttractedTo()), girl.getScenes()
                                 )
                              );
                              break;
                           }
                           case "goToBase":
                              girl.teleportToBase();
                              break;
                           case "roamAtBase":
                              girl.setRoaming(!girl.isRoaming());
                              if (girl.isRoaming()) {
                                 girl.setFollowing(false);
                                 girl.setSitting(false);
                                 girl.setMining(false);
                              }
                              break;
                           case "chopTrees":
                              girl.setChopping(!girl.isChopping());
                              if (girl.isChopping()) {
                                 girl.setRoaming(true);
                                 girl.setFollowing(false);
                                 girl.setSitting(false);
                                 girl.setMining(false);
                              }
                              break;
                           case "mineOres":
                              if (!girl.isFollowing()) {
                                 context.player().sendOverlayMessage(Component.translatable("msg.heartbound.mustFollowToMine"));
                              } else {
                                 girl.setMining(!girl.isMining());
                                 if (girl.isMining()) {
                                    girl.setSitting(false);
                                 }
                              }
                              break;
                           case "sit":
                              boolean newSit = !girl.isSitting();
                              girl.setSitting(newSit);
                              if (newSit) {
                                 girl.setRoaming(false);
                                 girl.setChopping(false);
                                 girl.setMining(false);
                                 girl.setFollowing(false);
                              }
                              break;
                           case "follow":
                              boolean newFollow = !girl.isFollowing();
                              girl.setFollowing(newFollow);
                              if (newFollow) {
                                 girl.setRoaming(false);
                                 girl.setChopping(false);
                              }
                              break;
                           case "openInventory": {
                              ServerPlayer sp = context.player();
                              if (sp.containerMenu != sp.inventoryMenu) {
                                 sp.containerMenu.removed(sp);
                                 sp.containerMenu = sp.inventoryMenu;
                              }

                              sp.openMenu(new GirlInventoryScreenHandlerFactory(girl));
                              girl.setGUIOpenState(true, sp);
                              break;
                           }
                           case "customize": {
                              ServerPlayer sp = context.player();
                              if (sp.containerMenu != sp.inventoryMenu) {
                                 sp.containerMenu.removed(sp);
                                 sp.containerMenu = sp.inventoryMenu;
                              }

                              ServerPlayNetworking.send(sp, new OpenCustomizeScreenS2CPacket(girl.getId(), girl.createTempClone().getId()));
                              break;
                           }
                           default:
                              Heartbound.LOGGER.warn("Unknown Girl interaction: " + packet.actionId());
                        }
                     }
                  }
               )
      );
      ServerPlayNetworking.registerGlobalReceiver(BonePosSyncC2SPacket.ID, (packet, context) -> {
         MinecraftServer server = context.server();
         if (server != null) {
            server.execute(() -> {
               Level world = context.player().level();
               Entity entity = world.getEntity(packet.entityId());
               Vec3 pos = packet.position();
               if (pos != null) {
                  if (!Double.isNaN(pos.x) && !Double.isNaN(pos.y) && !Double.isNaN(pos.z)) {
                     if (!(Math.abs(pos.x) > 1000.0) && !(Math.abs(pos.y) > 1000.0) && !(Math.abs(pos.z) > 1000.0)) {
                        if (entity instanceof GirlSceneEntity girl) {
                           if (!girl.isCurrentScenePlayer(context.player())) {
                              return;
                           }

                           girl.setPassengerBonePosition(pos);
                        } else if (entity instanceof Player targetPlayer) {
                           TransformablePlayer tp = (TransformablePlayer)targetPlayer;
                           if (tp.heartbound$isTransformSceneActive()) {
                              tp.heartbound$setPassengerBonePosition(pos);
                           }
                        }
                     }
                  }
               }
            });
         }
      });
      ServerPlayNetworking.registerGlobalReceiver(
         AnimationSyncC2SPacket.ID, (packet, context) -> Objects.requireNonNull(context.server()).execute(() -> {
               if (context.player().level().getEntity(packet.entityId()) instanceof GirlSceneEntity girl) {
                  girl.setOverrideAnim(packet.animationState());
                  girl.setOverrideLoop(packet.loopState());
                  girl.setOverrideHold(packet.holdState());
               }
            })
      );
      ServerPlayNetworking.registerGlobalReceiver(
         CumKeybindC2SPacket.ID, (packet, context) -> Objects.requireNonNull(context.server()).execute(() -> {
               if (packet.pressed()) {
                  ServerPlayer player = context.player();
                  Entity entity = player.getVehicle();
                  if (entity instanceof GirlSceneEntity girl) {
                     girl.tryTriggerCum();
                  } else if (entity instanceof Player targetPlayer) {
                     TransformablePlayer tp = (TransformablePlayer)targetPlayer;
                     if (tp.heartbound$isTransformSceneActive()) {
                        tp.heartbound$tryTransformTriggerCum();
                     }
                  } else {
                     TransformablePlayer tp = (TransformablePlayer)player;
                     if (tp.heartbound$isTransformSceneActive()) {
                        tp.heartbound$tryTransformTriggerCum();
                     }
                  }
               }
            })
      );
      ServerPlayNetworking.registerGlobalReceiver(
         ThrustKeybindC2SPacket.ID, (packet, context) -> Objects.requireNonNull(context.server()).execute(() -> {
               ServerPlayer player = context.player();
               Entity entity = player.getVehicle();
               if (entity instanceof GirlSceneEntity girl) {
                  girl.setThrusting(packet.held());
               } else if (entity instanceof Player targetPlayer) {
                  TransformablePlayer tp = (TransformablePlayer)targetPlayer;
                  if (tp.heartbound$isTransformSceneActive()) {
                     tp.heartbound$setTransformThrusting(packet.held());
                  }
               }

               // 修复：骑乘非场景实体（如 Kobold 坐骑）时，thrust 也必须传给玩家自身的变身场景，
               // 否则按住 thrust 键时变身动画停留在 slow 导致姿势错误
               TransformablePlayer selfTp = (TransformablePlayer)player;
               if (selfTp.heartbound$isTransformSceneActive()) {
                  selfTp.heartbound$setTransformThrusting(packet.held());
               }
            })
      );
      ServerPlayNetworking.registerGlobalReceiver(
         StartSceneC2SPacket.ID, (packet, context) -> Objects.requireNonNull(context.server()).execute(() -> {
               if (context.player().level().getEntity(packet.entityId()) instanceof GirlSceneEntity girl) {
                  if (girl.isTemporary()) {
                     Heartbound.LOGGER.warn("StartScene request targeted temporary preview entity (id={}) - ignoring", packet.entityId());
                     return;
                  }

                  girl.startScene(context.player(), packet.scene());
               }
            })
      );
      ServerPlayNetworking.registerGlobalReceiver(
         SetGUIOpenStateC2SPacket.ID, (packet, context) -> Objects.requireNonNull(context.server()).execute(() -> {
               if (context.player().level().getEntity(packet.entityId()) instanceof GirlSceneEntity girl) {
                  girl.setGUIOpenState(packet.data(), null);
               }
            })
      );
      ServerPlayNetworking.registerGlobalReceiver(
         AnimationFinishC2SPacket.ID, (packet, context) -> Objects.requireNonNull(context.server()).execute(() -> {
               if (context.player().level().getEntity(packet.entityId()) instanceof GirlSceneEntity girl) {
                  girl.animationFinished();
               }
            })
      );
      ServerPlayNetworking.registerGlobalReceiver(
         ScenePhaseSyncC2SPacket.ID, (packet, context) -> Objects.requireNonNull(context.server()).execute(() -> {
               if (context.player().level().getEntity(packet.entityId()) instanceof GirlSceneEntity girl) {
                  girl.playPhase(packet.phase());
               }
            })
      );
      ServerPlayNetworking.registerGlobalReceiver(
         StopSceneOnServerC2SPacket.ID, (packet, context) -> Objects.requireNonNull(context.server()).execute(() -> {
               if (context.player().level().getEntity(packet.entityId()) instanceof GirlSceneEntity girl) {
                  girl.stopScene();
               }
            })
      );
      ServerPlayNetworking.registerGlobalReceiver(
         SoundEventSyncC2SPacket.ID, (packet, context) -> Objects.requireNonNull(context.server()).execute(() -> {
               if (context.player().level().getEntity(packet.entityId()) instanceof GirlSceneEntity girl) {
                  girl.handleAnimationEventServer(packet.soundEvent());
               }
            })
      );
      ServerPlayNetworking.registerGlobalReceiver(
         GirlCustomizeC2SPacket.ID, (packet, context) -> Objects.requireNonNull(context.server()).execute(() -> {
               if (context.player().level().getEntity(packet.entityId()) instanceof GirlSceneEntity girl) {
                  girl.setBreastSize(packet.breastSize());
                  girl.setBreastOffset(packet.breastOffset());
               }
            })
      );
      ServerPlayNetworking.registerGlobalReceiver(
         KoboldCustomizeC2SPacket.ID, (packet, context) -> Objects.requireNonNull(context.server()).execute(() -> {
               if (context.player().level().getEntity(packet.entityId()) instanceof KoboldEntity girl) {
                  girl.setBodySize(packet.bodySize());
                  girl.setKoboldBreastSize(packet.breastSize());
                  girl.setPrimaryColor(packet.primaryColor());
                  girl.setSecondaryColor(packet.secondaryColor());
                  girl.setIrisColor(packet.irisColor());
                  girl.setTopHornType(packet.topHornType());
                  girl.setBottomHornType(packet.bottomHornType());
               }
            })
      );
      ServerPlayNetworking.registerGlobalReceiver(
         RemovePreviewEntityC2SPacket.ID, (packet, context) -> Objects.requireNonNull(context.server()).execute(() -> {
               ServerLevel world = context.player().level();
               if (world.getEntity(packet.previewEntityId()) instanceof KoboldEntity kobold) {
                  kobold.discard();
               }

               if (world.getEntity(packet.entityId()) instanceof GirlSceneEntity girl) {
                  girl.setCreatedCloneState(false);
               }
            })
      );
      ServerPlayNetworking.registerGlobalReceiver(
         TransformRequestC2SPacket.ID, (packet, context) -> Objects.requireNonNull(context.server()).execute(() -> {
               ServerPlayer player = context.player();
               String girlId = packet.girlId();
               GirlTransformationInfo info = GirlTransformationInfo.REGISTRY.get(girlId);
               if (info == null) {
                  Heartbound.LOGGER.warn("Transform request for unknown girl: {}", girlId);
               } else {
                  if (!player.isCreative()) {
                     for (InteractionHand hand : InteractionHand.values()) {
                        ItemStack handStack = player.getItemInHand(hand);
                        if (handStack.getItem() instanceof XPill) {
                           handStack.shrink(1);
                           break;
                        }
                     }
                  }

                  TransformablePlayer tp = (TransformablePlayer)player;
                  tp.heartbound$lockAnimForTicks(20);
                  tp.heartbound$setTransformGirlId(girlId);
                  HeartboundCriteria.TRANSFORM.trigger(player);
                  player.refreshDimensions();
               }
            })
      );
      ServerPlayNetworking.registerGlobalReceiver(
         TransformRevertC2SPacket.ID, (packet, context) -> Objects.requireNonNull(context.server()).execute(() -> {
               ServerPlayer player = context.player();
               TransformablePlayer tp = (TransformablePlayer)player;
               tp.heartbound$lockAnimForTicks(20);
               tp.heartbound$setTransformGirlId("");
               player.refreshDimensions();
            })
      );
      ServerPlayNetworking.registerGlobalReceiver(
         PlayerStripStartC2SPacket.ID, (packet, context) -> Objects.requireNonNull(context.server()).execute(() -> {
               ServerPlayer player = context.player();
               TransformablePlayer tp = (TransformablePlayer)player;
               if (tp.heartbound$isTransformed()) {
                  tp.heartbound$setStripping(true);
               }
            })
      );
      ServerPlayNetworking.registerGlobalReceiver(
         PlayerStripToggleC2SPacket.ID, (packet, context) -> Objects.requireNonNull(context.server()).execute(() -> {
               ServerPlayer player = context.player();
               TransformablePlayer tp = (TransformablePlayer)player;
               if (tp.heartbound$isTransformed()) {
                  tp.heartbound$setStripped(!tp.heartbound$isStripped());
                  tp.heartbound$setStripping(false);
               }
            })
      );
      ServerPlayNetworking.registerGlobalReceiver(
         PlayerAnimLockC2SPacket.ID, (packet, context) -> Objects.requireNonNull(context.server()).execute(() -> {
               ServerPlayer player = context.player();
               TransformablePlayer tp = (TransformablePlayer)player;
               if (tp.heartbound$isTransformed()) {
                  if (packet.locked()) {
                     tp.heartbound$lockAnimForTicks(100);
                  } else {
                     tp.heartbound$setAnimLocked(false);
                  }
               }
            })
      );
      ServerPlayNetworking.registerGlobalReceiver(
         TransformSceneKeybindC2SPacket.ID, (packet, context) -> Objects.requireNonNull(context.server()).execute(() -> {
               ServerPlayer player = context.player();
               TransformablePlayer tp = (TransformablePlayer)player;
               if (tp.heartbound$isTransformed()) {
                  if (!tp.heartbound$isTransformSceneActive() && !tp.heartbound$isWaitingForTarget() && !tp.heartbound$isWaitingForBed()) {
                     List<Scene> scenes = TransformSceneRegistry.getScenes(tp.heartbound$getTransformGirlId(), player.level());
                     if (!scenes.isEmpty()) {
                        ServerPlayNetworking.send(player, new TransformSceneOptionsS2CPacket(scenes));
                     }
                  } else {
                     tp.heartbound$stopTransformScene();
                  }
               }
            })
      );
      ServerPlayNetworking.registerGlobalReceiver(
         TransformStartSceneC2SPacket.ID, (packet, context) -> Objects.requireNonNull(context.server()).execute(() -> {
               ServerPlayer player = context.player();
               TransformablePlayer tp = (TransformablePlayer)player;
               if (tp.heartbound$isTransformed()) {
                  if (!tp.heartbound$isTransformSceneActive()) {
                     tp.heartbound$startTransformScene(packet.scene());
                  }
               }
            })
      );
      ServerPlayNetworking.registerGlobalReceiver(
         TransformAnimationFinishC2SPacket.ID, (packet, context) -> Objects.requireNonNull(context.server()).execute(() -> {
               ServerPlayer player = context.player();
               TransformablePlayer tp = (TransformablePlayer)player;
               if (tp.heartbound$isTransformSceneActive()) {
                  tp.heartbound$transformAnimationFinished();
               }
            })
      );
      ServerPlayNetworking.registerGlobalReceiver(
         TransformThrustKeyframeC2SPacket.ID, (packet, context) -> Objects.requireNonNull(context.server()).execute(() -> {
               ServerPlayer player = context.player();
               TransformablePlayer tp = (TransformablePlayer)player;
               if (tp.heartbound$isTransformSceneActive()) {
                  tp.heartbound$handleTransformSceneThrust();
               }
            })
      );
      ServerPlayNetworking.registerGlobalReceiver(
         TransformStopSceneC2SPacket.ID, (packet, context) -> Objects.requireNonNull(context.server()).execute(() -> {
               ServerPlayer player = context.player();
               TransformablePlayer tp = (TransformablePlayer)player;
               tp.heartbound$stopTransformScene();
            })
      );
   }
}
