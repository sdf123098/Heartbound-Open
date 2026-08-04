package com.cuddly.heartbound.util.rendering;

import net.minecraft.client.render.VertexConsumer;

public class OffsetVertexConsumer implements VertexConsumer {
   private VertexConsumer inner;
   private float offsetU;
   private float offsetV;

   public void setup(VertexConsumer target, float uOffset, float vOffset) {
      this.inner = target instanceof OffsetVertexConsumer ovc ? ovc.inner : target;
      this.offsetU = uOffset;
      this.offsetV = vOffset;
   }

   public VertexConsumer getDelegate() {
      return this.inner;
   }

   @Override
   public VertexConsumer vertex(float x, float y, float z) {
      this.inner.vertex(x, y, z);
      return this;
   }

   @Override
   public VertexConsumer color(int red, int green, int blue, int alpha) {
      this.inner.color(red, green, blue, alpha);
      return this;
   }

   @Override
   public VertexConsumer texture(float u, float v) {
      if (this.inner != null) {
         this.inner.texture(u + this.offsetU, v + this.offsetV);
      }

      return this;
   }

   @Override
   public VertexConsumer overlay(int u, int v) {
      this.inner.overlay(u, v);
      return this;
   }

   @Override
   public VertexConsumer light(int u, int v) {
      this.inner.light(u, v);
      return this;
   }

   @Override
   public VertexConsumer normal(float x, float y, float z) {
      this.inner.normal(x, y, z);
      return this;
   }
}
