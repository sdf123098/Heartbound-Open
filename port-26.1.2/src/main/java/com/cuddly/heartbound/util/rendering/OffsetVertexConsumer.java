package com.cuddly.heartbound.util.rendering;

import com.mojang.blaze3d.vertex.VertexConsumer;

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
   public VertexConsumer addVertex(float x, float y, float z) {
      this.inner.addVertex(x, y, z);
      return this;
   }

   @Override
   public VertexConsumer setColor(int red, int green, int blue, int alpha) {
      this.inner.setColor(red, green, blue, alpha);
      return this;
   }

   @Override
   public VertexConsumer setColor(int argb) {
      this.inner.setColor(argb);
      return this;
   }

   @Override
   public VertexConsumer setUv(float u, float v) {
      if (this.inner != null) {
         this.inner.setUv(u + this.offsetU, v + this.offsetV);
      }

      return this;
   }

   @Override
   public VertexConsumer setUv1(int u, int v) {
      this.inner.setUv1(u, v);
      return this;
   }

   @Override
   public VertexConsumer setUv2(int u, int v) {
      this.inner.setUv2(u, v);
      return this;
   }

   @Override
   public VertexConsumer setNormal(float x, float y, float z) {
      this.inner.setNormal(x, y, z);
      return this;
   }

   @Override
   public VertexConsumer setLineWidth(float width) {
      this.inner.setLineWidth(width);
      return this;
   }
}
