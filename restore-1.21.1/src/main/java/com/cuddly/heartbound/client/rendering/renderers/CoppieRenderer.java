package com.cuddly.heartbound.client.rendering.renderers;

import com.cuddly.heartbound.client.models.CoppieModel;
import com.cuddly.heartbound.entity.girls.CoppieEntity;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;

public class CoppieRenderer extends AbstractGirlRenderer<CoppieEntity> {
   public CoppieRenderer(Context ctx) {
      super(ctx, new CoppieModel());
   }
}
