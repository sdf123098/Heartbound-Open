package com.cuddly.heartbound.mixins.geckolib;

import com.cuddly.heartbound.util.rendering.GeoBoneExtension;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import software.bernie.geckolib.cache.object.GeoBone;

@Environment(EnvType.CLIENT)
@Mixin({GeoBone.class})
public abstract class GeoBoneMixin implements GeoBoneExtension {
   private boolean hidden;

   @Override
   public void setHiddenWithoutHidingChildren(boolean hidden) {
      this.hidden = hidden;
   }
}
