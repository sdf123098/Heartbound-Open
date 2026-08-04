package com.cuddly.heartbound.entity;

import com.cuddly.heartbound.entity.base.GirlSceneEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.item.Item.Settings;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class HeartboundEntities {
   private static final List<Runnable> ATTRIBUTE_REGISTRATIONS = new ArrayList<>();
   private static final List<Item> AUTO_SPAWN_EGGS = new ArrayList<>();
   private static final List<EntityType<? extends MobEntity>> GIRLS = new ArrayList<>();

   public static <T extends GirlSceneEntity> EntityType<T> registerGirl(
      String id, BiFunction<EntityType<T>, World, T> factory, float width, float height, Supplier<Builder> attributes
   ) {
      return registerGirl(id, factory, width, height, true, 16738740, 16758465, attributes);
   }

   public static <T extends GirlSceneEntity> EntityType<T> registerGirl(
      String id, BiFunction<EntityType<T>, World, T> factory, float width, float height, int primaryColor, int secondaryColor, Supplier<Builder> attributes
   ) {
      return registerGirl(id, factory, width, height, true, primaryColor, secondaryColor, attributes);
   }

   public static <T extends GirlSceneEntity> EntityType<T> registerGirl(
      String id, BiFunction<EntityType<T>, World, T> factory, float width, float height, boolean createSpawnEgg, Supplier<Builder> attributes
   ) {
      return registerGirl(id, factory, width, height, createSpawnEgg, 16738740, 16758465, attributes);
   }

   public static <T extends GirlSceneEntity> EntityType<T> registerGirl(
      String id,
      BiFunction<EntityType<T>, World, T> factory,
      float width,
      float height,
      boolean createSpawnEgg,
      int primaryColor,
      int secondaryColor,
      Supplier<Builder> attributes
   ) {
      try {
         Identifier identifier = Identifier.of("heartbound", id);
         EntityType<T> type = Registry.register(
            Registries.ENTITY_TYPE,
            identifier,
            net.minecraft.entity.EntityType.Builder.create(factory::apply, SpawnGroup.CREATURE).dimensions(width, height).build(id)
         );
         ATTRIBUTE_REGISTRATIONS.add(() -> FabricDefaultAttributeRegistry.register(type, attributes.get()));
         if (createSpawnEgg) {
            Identifier eggId = Identifier.of("heartbound", id + "_spawn_egg");
            Item egg = Registry.register(Registries.ITEM, eggId, new SpawnEggItem(type, primaryColor, secondaryColor, new Settings()));
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

   public static List<EntityType<? extends MobEntity>> getAllGirls() {
      return List.copyOf(GIRLS);
   }

   public static List<Item> getAllSpawnEggs() {
      return List.copyOf(AUTO_SPAWN_EGGS);
   }
}
