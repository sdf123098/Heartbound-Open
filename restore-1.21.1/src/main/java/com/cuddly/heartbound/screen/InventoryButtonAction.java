package com.cuddly.heartbound.screen;

import com.cuddly.heartbound.entity.base.GirlEntity;
import java.util.function.BiConsumer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public record InventoryButtonAction(Text label, int requiredRelationshipLevel, BiConsumer<GirlEntity, PlayerEntity> action, boolean closesScreen) {
   public InventoryButtonAction(Text label, int requiredRelationshipLevel, BiConsumer<GirlEntity, PlayerEntity> action) {
      this(label, requiredRelationshipLevel, action, true);
   }
}
