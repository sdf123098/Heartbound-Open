package com.cuddly.heartbound.client.rendering.renderers;

import com.cuddly.heartbound.client.models.JennyModel;
import com.cuddly.heartbound.entity.girls.JennyEntity;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;

public class JennyRenderer extends AbstractGirlRenderer<JennyEntity> {
   public JennyRenderer(Context ctx) {
      super(ctx, new JennyModel());
   }
}
