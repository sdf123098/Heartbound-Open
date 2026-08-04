package com.cuddly.heartbound.client.rendering.renderers;

import com.cuddly.heartbound.client.models.AlyModel;
import com.cuddly.heartbound.entity.girls.AlyEntity;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;

public class AlyRenderer extends AbstractGirlRenderer<AlyEntity> {
   public AlyRenderer(Context ctx) {
      super(ctx, new AlyModel());
   }
}
