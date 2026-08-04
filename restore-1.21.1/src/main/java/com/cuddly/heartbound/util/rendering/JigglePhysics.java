package com.cuddly.heartbound.util.rendering;

import net.minecraft.util.math.Vec3d;

public class JigglePhysics {
   private Vec3d velocity = Vec3d.ZERO;
   private Vec3d displacement = Vec3d.ZERO;
   private Vec3d prevDisplacement = Vec3d.ZERO;
   private final double stiffness;
   private final double damping;

   public JigglePhysics(double stiffness, double damping) {
      this.stiffness = stiffness;
      this.damping = damping;
   }

   public void update(Vec3d force) {
      this.prevDisplacement = this.displacement;
      Vec3d acceleration = force.subtract(this.displacement.multiply(this.stiffness)).subtract(this.velocity.multiply(this.damping));
      this.velocity = this.velocity.add(acceleration);
      this.displacement = this.displacement.add(this.velocity);
   }

   public Vec3d getDisplacement() {
      return this.displacement;
   }

   public Vec3d getInterpolatedDisplacement(double alpha) {
      return this.prevDisplacement.lerp(this.displacement, alpha);
   }

   public void reset() {
      this.velocity = Vec3d.ZERO;
      this.displacement = Vec3d.ZERO;
      this.prevDisplacement = Vec3d.ZERO;
   }

   public void dampen(double factor) {
      this.velocity = this.velocity.multiply(factor);
      this.displacement = this.displacement.multiply(factor);
   }
}
