package com.cuddly.heartbound.util.managers;

import com.cuddly.heartbound.entity.base.tamable.TameableGirlEntity;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class TamedGirlManager extends SavedData {
   private final Map<UUID, List<UUID>> girls = new HashMap<>();
   public static final Codec<TamedGirlManager> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(Codec.unboundedMap(UUIDUtil.AUTHLIB_CODEC, Codec.list(UUIDUtil.AUTHLIB_CODEC)).fieldOf("girls").forGetter(manager -> manager.girls))
            .apply(instance, map -> {
               TamedGirlManager manager = new TamedGirlManager();
               map.forEach((owner, list) -> manager.girls.put(owner, new ArrayList<>(list)));
               return manager;
            })
   );
   public static final SavedDataType<TamedGirlManager> TYPE = new SavedDataType<>(
      Identifier.fromNamespaceAndPath("heartbound", "heartbound_girls"), TamedGirlManager::new, CODEC, DataFixTypes.LEVEL
   );

   public static TamedGirlManager get(ServerLevel world) {
      return world.getDataStorage().computeIfAbsent(TYPE);
   }

   public void registerGirl(TameableGirlEntity girl) {
      if (girl.isTamed()) {
         LivingEntity owner = girl.getOwner();
         if (owner != null) {
            UUID ownerId = owner.getUUID();
            this.girls.computeIfAbsent(ownerId, k -> new ArrayList<>());
            this.girls.get(ownerId).removeIf(e -> e.equals(girl.getUUID()));
            this.girls.get(ownerId).add(girl.getUUID());
            this.setDirty();
         }
      }
   }

   public void removeGirl(UUID girlId) {
      for (UUID owner : this.girls.keySet()) {
         this.girls.get(owner).removeIf(e -> e.equals(girlId));
      }

      this.setDirty();
   }

   public List<UUID> getGirlsOwnedBy(UUID owner) {
      return this.girls.getOrDefault(owner, Collections.emptyList());
   }

   public Collection<UUID> getAllGirls() {
      List<UUID> list = new ArrayList<>();

      for (List<UUID> entries : this.girls.values()) {
         list.addAll(entries);
      }

      return list;
   }

   public boolean containsGirl(UUID uuid) {
      return this.girls.values().stream().flatMap(Collection::stream).anyMatch(entry -> entry.equals(uuid));
   }

   public void cleanupDeadGirls(ServerLevel world) {
      for (UUID owner : new HashSet<>(this.girls.keySet())) {
         List<UUID> list = this.girls.get(owner);
         list.removeIf(entry -> world.getEntity(entry) == null || !world.getEntity(entry).isAlive());
         if (list.isEmpty()) {
            this.girls.remove(owner);
         }
      }

      this.setDirty();
   }
}
