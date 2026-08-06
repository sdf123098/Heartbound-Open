package com.cuddly.heartbound.client.rendering.renderers;

import com.cuddly.heartbound.client.models.BiaModel;
import com.cuddly.heartbound.entity.girls.BiaEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;

public class BiaRenderer extends AbstractGirlRenderer<BiaEntity> {
   public BiaRenderer(Context ctx) {
      super(ctx, new BiaModel());
   }
}
