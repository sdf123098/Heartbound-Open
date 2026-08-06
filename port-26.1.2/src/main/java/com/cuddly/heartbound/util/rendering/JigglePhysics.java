package com.cuddly.heartbound.util.rendering;

import net.minecraft.world.phys.Vec3;

public class JigglePhysics {
   private Vec3 velocity = Vec3.ZERO;
   private Vec3 displacement = Vec3.ZERO;
   private Vec3 prevDisplacement = Vec3.ZERO;
   private final double stiffness;
   private final double damping;

   public JigglePhysics(double stiffness, double damping) {
      this.stiffness = stiffness;
      this.damping = damping;
   }

   public void update(Vec3 force) {
      this.prevDisplacement = this.displacement;
      Vec3 acceleration = force.subtract(this.displacement.scale(this.stiffness)).subtract(this.velocity.scale(this.damping));
      this.velocity = this.velocity.add(acceleration);
      this.displacement = this.displacement.add(this.velocity);
   }

   public Vec3 getDisplacement() {
      return this.displacement;
   }

   public Vec3 getInterpolatedDisplacement(double alpha) {
      return this.prevDisplacement.lerp(this.displacement, alpha);
   }

   public void reset() {
      this.velocity = Vec3.ZERO;
      this.displacement = Vec3.ZERO;
      this.prevDisplacement = Vec3.ZERO;
   }

   public void dampen(double factor) {
      this.velocity = this.velocity.scale(factor);
      this.displacement = this.displacement.scale(factor);
   }
}
