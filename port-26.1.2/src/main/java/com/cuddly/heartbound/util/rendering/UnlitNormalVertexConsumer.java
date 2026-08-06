package com.cuddly.heartbound.util.rendering;

import com.cuddly.heartbound.client.rendering.renderers.AbstractGirlRenderer;
import com.mojang.blaze3d.vertex.VertexConsumer;

public class UnlitNormalVertexConsumer implements VertexConsumer {
   private final VertexConsumer parent;

   public UnlitNormalVertexConsumer(VertexConsumer parent) {
      if (parent instanceof UnlitNormalVertexConsumer unlit) {
         this.parent = unlit.parent;
      } else {
         this.parent = parent;
      }
   }

   public void addVertex(float x, float y, float z, int color, float u, float v, int overlay, int light, float normalX, float normalY, float normalZ) {
      float ny = AbstractGirlRenderer.IS_GUI_RENDERING ? -1.0F : 1.0F;
      this.parent.addVertex(x, y, z, color, u, v, overlay, light, 0.0F, ny, 0.0F);
   }

   @Override
   public VertexConsumer setNormal(float x, float y, float z) {
      float ny = AbstractGirlRenderer.IS_GUI_RENDERING ? -1.0F : 1.0F;
      this.parent.setNormal(0.0F, ny, 0.0F);
      return this;
   }

   @Override
   public VertexConsumer setUv2(int u, int v) {
      this.parent.setUv2(u, v);
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
   public VertexConsumer setUv(float u, float v) {
      this.parent.setUv(u, v);
      return this;
   }

   @Override
   public VertexConsumer setLineWidth(float width) {
      this.parent.setLineWidth(width);
      return this;
   }
}
