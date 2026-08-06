package com.cuddly.heartbound.screen;

import com.cuddly.heartbound.entity.base.GirlEntity;
import java.util.function.BiConsumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public record InventoryButtonAction(Component label, int requiredRelationshipLevel, BiConsumer<GirlEntity, Player> action, boolean closesScreen) {
   public InventoryButtonAction(Component label, int requiredRelationshipLevel, BiConsumer<GirlEntity, Player> action) {
      this(label, requiredRelationshipLevel, action, true);
   }
}
