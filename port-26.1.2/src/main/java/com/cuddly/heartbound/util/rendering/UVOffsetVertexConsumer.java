package com.cuddly.heartbound.util.rendering;

import com.mojang.blaze3d.vertex.VertexConsumer;

public class UVOffsetVertexConsumer implements VertexConsumer {
   private final VertexConsumer parent;
   private final float uOffset;
   private final float vOffset;

   public UVOffsetVertexConsumer(VertexConsumer parent, float uOffset, float vOffset) {
      this.parent = parent;
      this.uOffset = uOffset;
      this.vOffset = vOffset;
   }

   public void addVertex(float x, float y, float z, int color, float u, float v, int overlay, int light, float normalX, float normalY, float normalZ) {
      this.parent.addVertex(x, y, z, color, u + this.uOffset, v + this.vOffset, overlay, light, normalX, normalY, normalZ);
   }

   @Override
   public VertexConsumer setUv(float u, float v) {
      this.parent.setUv(u + this.uOffset, v + this.vOffset);
      return this;
   }

   @Override
   public VertexConsumer addVertex(float x, float y, float z) {
      this.parent.addVertex(x, y, z);
      return this;
   }

   @Override
   public VertexConsumer setColor(int red, int green, int blue, int alpha) {
      this.parent.setColor(red, green, blue, alpha);
      return this;
   }

   @Override
   public VertexConsumer setColor(int argb) {
      this.parent.setColor(argb);
      return this;
   }

   @Override
   public VertexConsumer setUv1(int u, int v) {
      this.parent.setUv1(u, v);
      return this;
   }

   @Override
   public VertexConsumer setUv2(int u, int v) {
      this.parent.setUv2(u, v);
      return this;
   }

   @Override
   public VertexConsumer setNormal(float x, float y, float z) {
      this.parent.setNormal(x, y, z);
      return this;
   }

   @Override
   public VertexConsumer setLineWidth(float width) {
      this.parent.setLineWidth(width);
      return this;
   }
}
