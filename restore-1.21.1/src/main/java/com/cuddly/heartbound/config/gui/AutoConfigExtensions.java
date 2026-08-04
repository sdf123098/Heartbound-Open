package com.cuddly.heartbound.config.gui;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.gui.registry.GuiRegistry;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;

public class AutoConfigExtensions {
   static final Text RESET_TEXT = Text.translatable("text.cloth-config.reset_value");
   static final ConfigEntryBuilder ENTRY_BUILDER = ConfigEntryBuilder.create();

   private AutoConfigExtensions() {
   }

   public static void apply(Class<? extends ConfigData> configClass) {
      GuiRegistry registry = AutoConfig.getGuiRegistry(configClass);
      ModBindingsConfigImpl.apply(registry);
      BoundedContinuousImpl.apply(registry);
   }

   static Predicate<Field> isField(Class<?> declaringClass, String... fieldNames) {
      return field -> field.getDeclaringClass().equals(declaringClass) && Arrays.asList(fieldNames).contains(field.getName());
   }

   static Predicate<Field> isArrayOrListOfType(Type... types) {
      return field -> {
         if (field.getType().isArray()) {
            Class<?> component = field.getType().getComponentType();
            return Arrays.asList(types).contains(component);
         } else {
            return isListOfType(types).test(field);
         }
      };
   }

   static Predicate<Field> isNotArrayOrListOfType(Type... types) {
      return field -> {
         if (field.getType().isArray()) {
            Class<?> component = field.getType().getComponentType();
            return Arrays.stream(types).noneMatch(component::equals);
         } else {
            return isNotListOfType(types).test(field);
         }
      };
   }

   static Predicate<Field> isListOfType(Type... types) {
      return field -> {
         if (List.class.isAssignableFrom(field.getType()) && field.getGenericType() instanceof ParameterizedType generic) {
            Type[] args = generic.getActualTypeArguments();
            return args.length == 1 && Arrays.asList(types).contains(args[0]);
         } else {
            return false;
         }
      };
   }

   static Predicate<Field> isNotListOfType(Type... types) {
      return field -> {
         if (List.class.isAssignableFrom(field.getType()) && field.getGenericType() instanceof ParameterizedType generic) {
            Type[] args = generic.getActualTypeArguments();
            return args.length == 1 && Arrays.stream(types).noneMatch(args[0]::equals);
         } else {
            return false;
         }
      };
   }
}
