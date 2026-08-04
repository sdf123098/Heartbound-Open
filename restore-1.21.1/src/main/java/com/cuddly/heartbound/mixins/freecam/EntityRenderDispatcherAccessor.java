package com.cuddly.heartbound.mixins.freecam;

import net.minecraft.client.render.entity.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({EntityRenderDispatcher.class})
public interface EntityRenderDispatcherAccessor {
   @Accessor("renderShadows")
   boolean isRenderShadows();
}
