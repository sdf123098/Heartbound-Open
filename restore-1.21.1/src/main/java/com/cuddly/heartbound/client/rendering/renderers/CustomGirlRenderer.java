package com.cuddly.heartbound.client.rendering.renderers;

import com.cuddly.heartbound.client.models.CustomGirlModel;
import com.cuddly.heartbound.entity.girls.CustomGirlEntity;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;

public class CustomGirlRenderer extends AbstractGirlRenderer<CustomGirlEntity> {
   public CustomGirlRenderer(Context ctx) {
      super(ctx, new CustomGirlModel());
   }
}
