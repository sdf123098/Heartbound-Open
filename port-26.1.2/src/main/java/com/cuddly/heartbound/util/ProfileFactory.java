package com.cuddly.heartbound.util;

import com.cuddly.heartbound.entity.base.GirlSceneEntity;
import com.cuddly.heartbound.util.variables.CustomGirlProfile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

@FunctionalInterface
public interface ProfileFactory<T extends GirlSceneEntity> {
   T create(EntityType<T> var1, Level var2, CustomGirlProfile var3);
}
