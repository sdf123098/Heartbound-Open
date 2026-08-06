package com.cuddly.heartbound.client.rendering.renderers;

import com.cuddly.heartbound.client.models.SlimeModel;
import com.cuddly.heartbound.entity.girls.SlimeEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;

public class SlimeRenderer extends AbstractGirlRenderer<SlimeEntity> {
   public SlimeRenderer(Context ctx) {
      super(ctx, new SlimeModel());
   }
}
