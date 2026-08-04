package com.cuddly.heartbound.screen;

import com.cuddly.heartbound.entity.base.GirlEntity;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public record FeatureGroup(String titleKey, List<FeatureGroup.FeatureEntry> entries) {
   public static record FeatureEntry(Function<GirlEntity, Text> labelProvider, int requiredRelationshipLevel, BiConsumer<GirlEntity, PlayerEntity> action) {
   }
}
