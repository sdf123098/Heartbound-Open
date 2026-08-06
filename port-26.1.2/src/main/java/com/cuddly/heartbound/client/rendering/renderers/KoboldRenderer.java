package com.cuddly.heartbound.client.rendering.renderers;

import com.cuddly.heartbound.client.models.KoboldModel;
import com.cuddly.heartbound.entity.girls.KoboldEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;

public class KoboldRenderer extends AbstractGirlRenderer<KoboldEntity> {
   public KoboldRenderer(Context ctx) {
      super(ctx, new KoboldModel());
   }
}
