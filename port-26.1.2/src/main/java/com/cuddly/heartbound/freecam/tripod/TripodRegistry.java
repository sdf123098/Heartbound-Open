package com.cuddly.heartbound.freecam.tripod;

import com.cuddly.heartbound.freecam.Freecam;
import com.cuddly.heartbound.freecam.FreecamPosition;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.world.level.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;

public class TripodRegistry {
   private final Map<DimensionType, Map<TripodSlot, FreecamPosition>> tripods = new HashMap<>();

   @Nullable
   public FreecamPosition get(TripodSlot tripod) {
      return this.get(dimension(), tripod);
   }

   @Nullable
   public FreecamPosition get(DimensionType dimension, TripodSlot tripod) {
      return Optional.ofNullable(this.tripods.get(dimension)).map(positions -> positions.get(tripod)).orElse(null);
   }

   public void put(TripodSlot tripod, @Nullable FreecamPosition position) {
      this.put(dimension(), tripod, position);
   }

   public void put(DimensionType dimension, TripodSlot tripod, @Nullable FreecamPosition position) {
      this.tripods.computeIfAbsent(dimension, TripodRegistry::newEntry).put(tripod, position);
   }

   public void clear() {
      this.tripods.clear();
   }

   private static DimensionType dimension() {
      return Freecam.MC.level.dimensionType();
   }

   private static Map<TripodSlot, FreecamPosition> newEntry(DimensionType dimension) {
      return new EnumMap<>(TripodSlot.class);
   }
}
