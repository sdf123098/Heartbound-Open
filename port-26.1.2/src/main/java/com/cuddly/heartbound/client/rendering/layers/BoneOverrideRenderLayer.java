package com.cuddly.heartbound.client.rendering.layers;

import com.cuddly.heartbound.client.rendering.renderers.AbstractGirlRenderer.GirlRenderState;
import com.cuddly.heartbound.entity.base.GirlSceneEntity;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.layer.GeoRenderLayer;

public class BoneOverrideRenderLayer<T extends GirlSceneEntity> extends GeoRenderLayer<T, Void, GirlRenderState> {
   public BoneOverrideRenderLayer(GeoRenderer<T, Void, GirlRenderState> renderer) {
      super(renderer);
   }
}
