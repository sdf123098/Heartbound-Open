package com.cuddly.heartbound.freecam;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;

public class Motion {
   public static final double DIAGONAL_MULTIPLIER = (double)Mth.sin((float)Math.toRadians(45.0));

   public static void doMotion(FreeCamera freeCamera, double hSpeed, double vSpeed) {
      float yaw = freeCamera.getYRot();
      double velocityX = 0.0;
      double velocityY = 0.0;
      double velocityZ = 0.0;
      Vec3 forward = Vec3.directionFromRotation(0.0F, yaw);
      Vec3 side = Vec3.directionFromRotation(0.0F, yaw + 90.0F);
      freeCamera.input.tick();
      Input keyPresses = freeCamera.input.keyPresses;
      hSpeed *= freeCamera.isSprinting() ? 1.5 : 1.0;
      boolean straight = false;
      if (keyPresses.forward()) {
         velocityX += forward.x * hSpeed;
         velocityZ += forward.z * hSpeed;
         straight = true;
      }

      if (keyPresses.backward()) {
         velocityX -= forward.x * hSpeed;
         velocityZ -= forward.z * hSpeed;
         straight = true;
      }

      boolean strafing = false;
      if (keyPresses.right()) {
         velocityZ += side.z * hSpeed;
         velocityX += side.x * hSpeed;
         strafing = true;
      }

      if (keyPresses.left()) {
         velocityZ -= side.z * hSpeed;
         velocityX -= side.x * hSpeed;
         strafing = true;
      }

      if (straight && strafing) {
         velocityX *= DIAGONAL_MULTIPLIER;
         velocityZ *= DIAGONAL_MULTIPLIER;
      }

      if (keyPresses.jump()) {
         velocityY += vSpeed;
      }

      if (keyPresses.shift()) {
         velocityY -= vSpeed;
      }

      freeCamera.setDeltaMovement(velocityX, velocityY, velocityZ);
   }
}
