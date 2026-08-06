package com.cuddly.heartbound.entity;

import com.cuddly.heartbound.entity.base.GirlSceneEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.Level;

public class HeartboundEntities {
   private static final List<Runnable> ATTRIBUTE_REGISTRATIONS = new ArrayList<>();
   private static final List<Item> AUTO_SPAWN_EGGS = new ArrayList<>();
   private static final List<EntityType<? extends Mob>> GIRLS = new ArrayList<>();

   public static <T extends GirlSceneEntity> EntityType<T> registerGirl(
      String id, BiFunction<EntityType<T>, Level, T> factory, float width, float height, Supplier<Builder> attributes
   ) {
      return registerGirl(id, factory, width, height, true, 16738740, 16758465, attributes);
   }

   public static <T extends GirlSceneEntity> EntityType<T> registerGirl(
      String id, BiFunction<EntityType<T>, Level, T> factory, float width, float height, int primaryColor, int secondaryColor, Supplier<Builder> attributes
   ) {
      return registerGirl(id, factory, width, height, true, primaryColor, secondaryColor, attributes);
   }

   public static <T extends GirlSceneEntity> EntityType<T> registerGirl(
      String id, BiFunction<EntityType<T>, Level, T> factory, float width, float height, boolean createSpawnEgg, Supplier<Builder> attributes
   ) {
      return registerGirl(id, factory, width, height, createSpawnEgg, 16738740, 16758465, attributes);
   }

   public static <T extends GirlSceneEntity> EntityType<T> registerGirl(
      String id,
      BiFunction<EntityType<T>, Level, T> factory,
      float width,
      float height,
      boolean createSpawnEgg,
      int primaryColor,
      int secondaryColor,
      Supplier<Builder> attributes
   ) {
      try {
         Identifier identifier = Identifier.fromNamespaceAndPath("heartbound", id);
         EntityType<T> type = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            identifier,
            net.minecraft.world.entity.EntityType.Builder.of(factory::apply, MobCategory.CREATURE)
               .sized(width, height)
               .build(ResourceKey.create(Registries.ENTITY_TYPE, identifier))
         );
         ATTRIBUTE_REGISTRATIONS.add(() -> FabricDefaultAttributeRegistry.register(type, attributes.get()));
         if (createSpawnEgg) {
            Identifier eggId = Identifier.fromNamespaceAndPath("heartbound", id + "_spawn_egg");
            ResourceKey<Item> eggKey = ResourceKey.create(Registries.ITEM, eggId);
            Item egg = Registry.register(
               BuiltInRegistries.ITEM,
               eggKey,
               new SpawnEggItem(new Properties().setId(eggKey).component(DataComponents.ENTITY_DATA, TypedEntityData.of(type, new CompoundTag())))
            );
            AUTO_SPAWN_EGGS.add(egg);
         }

         GIRLS.add(type);
         return type;
      } catch (Exception var12) {
         throw new RuntimeException("Failed to register girl entity: " + id, var12);
      }
   }

   public static void registerAttributes() {
      ATTRIBUTE_REGISTRATIONS.forEach(Runnable::run);
   }

   public static Item getFirstSpawnEgg() {
      return AUTO_SPAWN_EGGS.isEmpty() ? null : AUTO_SPAWN_EGGS.get(0);
   }

   public static List<EntityType<? extends Mob>> getAllGirls() {
      return List.copyOf(GIRLS);
   }

   public static List<Item> getAllSpawnEggs() {
      return List.copyOf(AUTO_SPAWN_EGGS);
   }
}
