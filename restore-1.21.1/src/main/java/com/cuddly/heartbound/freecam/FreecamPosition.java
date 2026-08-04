package com.cuddly.heartbound.freecam;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.util.math.ChunkPos;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class FreecamPosition {
   private static final float DEG_TO_RAD = (float) (Math.PI / 180.0);
   public double x;
   public double y;
   public double z;
   public float pitch;
   public float yaw;
   private final Quaternionf orientation = new Quaternionf();
   private final Vector3f forward = new Vector3f();
   private final Vector3f up = new Vector3f();
   private final Vector3f right = new Vector3f();

   public FreecamPosition(Entity entity) {
      this.x = entity.getX();
      this.y = computeEyeY(entity);
      this.z = entity.getZ();
      this.setRotation(entity.getYaw(), entity.getPitch());
   }

   public void setRotation(float yaw, float pitch) {
      this.yaw = yaw;
      this.pitch = pitch;
      this.orientation.rotationYXZ(-yaw * (float) (Math.PI / 180.0), pitch * (float) (Math.PI / 180.0), 0.0F);
      this.forward.set(0.0F, 0.0F, 1.0F).rotate(this.orientation);
      this.up.set(0.0F, 1.0F, 0.0F).rotate(this.orientation);
      this.right.set(1.0F, 0.0F, 0.0F).rotate(this.orientation);
   }

   public void mirrorRotation() {
      this.setRotation(this.yaw + 180.0F, -this.pitch);
   }

   public void moveForward(double distance) {
      this.move(distance, 0.0, 0.0);
   }

   public void move(double fwd, double upAmount, double rightAmount) {
      this.x = this.x + (double)this.forward.x() * fwd + (double)this.up.x() * upAmount + (double)this.right.x() * rightAmount;
      this.y = this.y + (double)this.forward.y() * fwd + (double)this.up.y() * upAmount + (double)this.right.y() * rightAmount;
      this.z = this.z + (double)this.forward.z() * fwd + (double)this.up.z() * upAmount + (double)this.right.z() * rightAmount;
   }

   public ChunkPos getChunkPos() {
      return new ChunkPos((int)(this.x / 16.0), (int)(this.z / 16.0));
   }

   private static double computeEyeY(Entity entity) {
      if (entity.getPose() == EntityPose.SWIMMING) {
         return entity.getY();
      } else {
         float currentEye = entity.getEyeHeight(entity.getPose());
         float swimmingEye = entity.getEyeHeight(EntityPose.SWIMMING);
         return entity.getY() - (double)swimmingEye + (double)currentEye;
      }
   }
}
