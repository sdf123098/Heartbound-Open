package com.cuddly.heartbound.client.rendering.renderers;

import com.cuddly.heartbound.client.models.EllieModel;
import com.cuddly.heartbound.entity.girls.EllieEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;

public class EllieRenderer extends AbstractGirlRenderer<EllieEntity> {
   public EllieRenderer(Context ctx) {
      super(ctx, new EllieModel());
   }
}
