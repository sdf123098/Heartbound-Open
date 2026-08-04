package com.cuddly.heartbound.util;

import com.cuddly.heartbound.entity.base.GirlSceneEntity;
import com.cuddly.heartbound.util.variables.CustomGirlProfile;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

@FunctionalInterface
public interface ProfileFactory<T extends GirlSceneEntity> {
   T create(EntityType<T> var1, World var2, CustomGirlProfile var3);
}
