package com.cuddly.heartbound.util.rendering;

import com.cuddly.heartbound.client.rendering.renderers.AbstractGirlRenderer;
import net.minecraft.client.render.VertexConsumer;

public class UnlitNormalVertexConsumer implements VertexConsumer {
   private final VertexConsumer parent;

   public UnlitNormalVertexConsumer(VertexConsumer parent) {
      if (parent instanceof UnlitNormalVertexConsumer unlit) {
         this.parent = unlit.parent;
      } else {
         this.parent = parent;
      }
   }

   @Override
   public void vertex(float x, float y, float z, int color, float u, float v, int overlay, int light, float normalX, float normalY, float normalZ) {
      float ny = AbstractGirlRenderer.IS_GUI_RENDERING ? -1.0F : 1.0F;
      this.parent.vertex(x, y, z, color, u, v, overlay, light, 0.0F, ny, 0.0F);
   }

   @Override
   public VertexConsumer normal(float x, float y, float z) {
      float ny = AbstractGirlRenderer.IS_GUI_RENDERING ? -1.0F : 1.0F;
      this.parent.normal(0.0F, ny, 0.0F);
      return this;
   }

   @Override
   public VertexConsumer light(int u, int v) {
      this.parent.light(u, v);
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
   public VertexConsumer texture(float u, float v) {
      this.parent.texture(u, v);
      return this;
   }
}
