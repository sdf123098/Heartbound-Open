package com.cuddly.heartbound.util;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.player.Player;

public class EntityFlags {
   public static final EntityDataAccessor<Boolean> FULL_INVIS = SynchedEntityData.defineId(Player.class, EntityDataSerializers.BOOLEAN);
}
