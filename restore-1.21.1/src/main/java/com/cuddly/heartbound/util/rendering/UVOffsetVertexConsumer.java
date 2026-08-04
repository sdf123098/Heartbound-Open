package com.cuddly.heartbound.util.rendering;

import net.minecraft.client.render.VertexConsumer;

public class UVOffsetVertexConsumer implements VertexConsumer {
   private final VertexConsumer parent;
   private final float uOffset;
   private final float vOffset;

   public UVOffsetVertexConsumer(VertexConsumer parent, float uOffset, float vOffset) {
      this.parent = parent;
      this.uOffset = uOffset;
      this.vOffset = vOffset;
   }

   @Override
   public void vertex(float x, float y, float z, int color, float u, float v, int overlay, int light, float normalX, float normalY, float normalZ) {
      this.parent.vertex(x, y, z, color, u + this.uOffset, v + this.vOffset, overlay, light, normalX, normalY, normalZ);
   }

   @Override
   public VertexConsumer texture(float u, float v) {
      this.parent.texture(u + this.uOffset, v + this.vOffset);
      return this;
   }

   @Override
   public VertexConsumer vertex(float x, float y, float z) {
      this.parent.vertex(x, y, z);
      return this;
   }

   @Override
   public VertexConsumer color(int red, int green, int blue, int alpha) {
      this.parent.color(red, green, blue, alpha);
      return this;
   }

   @Override
   public VertexConsumer overlay(int u, int v) {
      this.parent.overlay(u, v);
      return this;
   }

   @Override
   public VertexConsumer light(int u, int v) {
      this.parent.light(u, v);
      return this;
   }

   @Override
   public VertexConsumer normal(float x, float y, float z) {
      this.parent.normal(x, y, z);
      return this;
   }
}
