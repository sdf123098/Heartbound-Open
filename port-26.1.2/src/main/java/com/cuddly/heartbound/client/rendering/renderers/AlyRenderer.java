package com.cuddly.heartbound.client.rendering.renderers;

import com.cuddly.heartbound.client.models.AlyModel;
import com.cuddly.heartbound.entity.girls.AlyEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;

public class AlyRenderer extends AbstractGirlRenderer<AlyEntity> {
   public AlyRenderer(Context ctx) {
      super(ctx, new AlyModel());
   }
}
