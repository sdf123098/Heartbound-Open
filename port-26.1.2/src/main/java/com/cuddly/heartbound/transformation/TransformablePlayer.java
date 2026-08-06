package com.cuddly.heartbound.transformation;

import com.cuddly.heartbound.util.variables.Scene;
import com.cuddly.heartbound.util.variables.ScenePhase;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public interface TransformablePlayer {
   String heartbound$getTransformGirlId();

   void heartbound$setTransformGirlId(String var1);

   boolean heartbound$isStripped();

   void heartbound$setStripped(boolean var1);

   boolean heartbound$isStripping();

   void heartbound$setStripping(boolean var1);

   boolean heartbound$isAnimLocked();

   void heartbound$setAnimLocked(boolean var1);

   void heartbound$lockAnimForTicks(int var1);

   default boolean heartbound$isTransformed() {
      String id = this.heartbound$getTransformGirlId();
      return id != null && !id.isEmpty();
   }

   Scene heartbound$getTransformScene();

   void heartbound$setTransformScene(Scene var1);

   ScenePhase heartbound$getTransformScenePhase();

   void heartbound$setTransformScenePhase(ScenePhase var1);

   float heartbound$getTransformSceneProgress();

   void heartbound$setTransformSceneProgress(float var1);

   float heartbound$getTransformCumThreshold();

   void heartbound$setTransformCumThreshold(float var1);

   boolean heartbound$isTransformThrusting();

   void heartbound$setTransformThrusting(boolean var1);

   int heartbound$getTransformIntroIndex();

   void heartbound$setTransformIntroIndex(int var1);

   int heartbound$getTransformStationaryIndex();

   void heartbound$setTransformStationaryIndex(int var1);

   int heartbound$getTransformStationaryLoop();

   void heartbound$setTransformStationaryLoop(int var1);

   int heartbound$getTransformStationaryLoopThreshold();

   void heartbound$setTransformStationaryLoopThreshold(int var1);

   Optional<UUID> heartbound$getTransformScenePartner();

   void heartbound$setTransformScenePartner(@Nullable Player var1);

   boolean heartbound$isWaitingForBed();

   void heartbound$setWaitingForBed(boolean var1);

   boolean heartbound$isWaitingForTarget();

   void heartbound$setWaitingForTarget(boolean var1);

   Vec3 heartbound$getPassengerBonePosition();

   void heartbound$setPassengerBonePosition(Vec3 var1);

   void heartbound$startTransformScene(Scene var1);

   void heartbound$stopTransformScene();

   void heartbound$transformAnimationFinished();

   void heartbound$handleTransformSceneThrust();

   void heartbound$tryTransformTriggerCum();

   default boolean heartbound$isTransformSceneActive() {
      return this.heartbound$getTransformScenePhase() != ScenePhase.NONE;
   }
}
