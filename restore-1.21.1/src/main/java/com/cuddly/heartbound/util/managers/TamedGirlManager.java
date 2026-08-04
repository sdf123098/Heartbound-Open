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
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Uuids;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentState.Type;

public class TamedGirlManager extends PersistentState {
   private final Map<UUID, List<UUID>> girls = new HashMap<>();
   public static final Codec<TamedGirlManager> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(Codec.unboundedMap(Uuids.CODEC, Codec.list(Uuids.CODEC)).fieldOf("girls").forGetter(manager -> manager.girls))
            .apply(instance, map -> {
               TamedGirlManager manager = new TamedGirlManager();
               map.forEach((owner, list) -> manager.girls.put(owner, new ArrayList<>(list)));
               return manager;
            })
   );
   public static final Type<TamedGirlManager> TYPE = new Type<>(
      TamedGirlManager::new, (nbt, registryLookup) -> CODEC.parse(NbtOps.INSTANCE, nbt).result().orElseGet(TamedGirlManager::new), DataFixTypes.LEVEL
   );

   @Override
   public NbtCompound writeNbt(NbtCompound nbt, WrapperLookup registryLookup) {
      CODEC.encodeStart(NbtOps.INSTANCE, this).result().ifPresent(element -> {
         if (element instanceof NbtCompound compound) {
            nbt.copyFrom(compound);
         }
      });
      return nbt;
   }

   public static TamedGirlManager get(ServerWorld world) {
      return world.getPersistentStateManager().getOrCreate(TYPE, "Heartbound_girls");
   }

   public void registerGirl(TameableGirlEntity girl) {
      if (girl.isTamed()) {
         LivingEntity owner = girl.getOwner();
         if (owner != null) {
            UUID ownerId = owner.getUuid();
            this.girls.computeIfAbsent(ownerId, k -> new ArrayList<>());
            this.girls.get(ownerId).removeIf(e -> e.equals(girl.getUuid()));
            this.girls.get(ownerId).add(girl.getUuid());
            this.markDirty();
         }
      }
   }

   public void removeGirl(UUID girlId) {
      for (UUID owner : this.girls.keySet()) {
         this.girls.get(owner).removeIf(e -> e.equals(girlId));
      }

      this.markDirty();
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

   public void cleanupDeadGirls(ServerWorld world) {
      for (UUID owner : new HashSet<>(this.girls.keySet())) {
         List<UUID> list = this.girls.get(owner);
         list.removeIf(entry -> world.getEntity(entry) == null || !world.getEntity(entry).isAlive());
         if (list.isEmpty()) {
            this.girls.remove(owner);
         }
      }

      this.markDirty();
   }
}
