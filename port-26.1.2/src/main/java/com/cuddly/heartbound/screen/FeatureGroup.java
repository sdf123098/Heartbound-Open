package com.cuddly.heartbound.screen;

import com.cuddly.heartbound.entity.base.GirlEntity;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public record FeatureGroup(String titleKey, List<FeatureGroup.FeatureEntry> entries) {
   public static record FeatureEntry(Function<GirlEntity, Component> labelProvider, int requiredRelationshipLevel, BiConsumer<GirlEntity, Player> action) {
   }
}
