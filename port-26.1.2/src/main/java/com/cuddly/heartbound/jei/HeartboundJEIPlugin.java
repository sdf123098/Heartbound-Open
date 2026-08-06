package com.cuddly.heartbound.jei;

import com.cuddly.heartbound.Heartbound;
import com.cuddly.heartbound.block.HeartboundBlocks;
import com.cuddly.heartbound.item.HeartboundItems;
import com.cuddly.heartbound.util.json.CustomGirlLoader;
import com.cuddly.heartbound.util.variables.CustomGirlProfile;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

@JeiPlugin
public class HeartboundJEIPlugin implements IModPlugin {
   public Identifier getPluginUid() {
      return Identifier.fromNamespaceAndPath("heartbound", "jei_plugin");
   }

   @Override
   public void registerCategories(IRecipeCategoryRegistration registration) {
      Heartbound.LOGGER.info("JEI: Registering Fusion Recipe Category");
      registration.addRecipeCategories(new FusionRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
   }

   @Override
   public void registerRecipes(IRecipeRegistration registration) {
      Heartbound.LOGGER.info("JEI: Registering Fusion Recipes");
      List<FusionRecipe> recipes = new ArrayList<>();

      for (CustomGirlProfile profile : CustomGirlLoader.REGISTERED_PROFILES.values()) {
         if (profile.tameItem() != null) {
            ItemStack input1 = new ItemStack(HeartboundItems.CUSTOM_GIRL_SPAWN_EGG);
            ItemStack input2 = new ItemStack(profile.tameItem());
            ItemStack input3 = new ItemStack(profile.tameItem());
            ItemStack output = HeartboundItems.createCustomGirlSpawnEgg(profile.id());
            recipes.add(new FusionRecipe(Ingredient.of(input1.getItem()), Ingredient.of(input2.getItem()), Ingredient.of(input3.getItem()), output));
            Heartbound.LOGGER.info("JEI: Added recipe for profile: " + profile.id());
         }
      }

      Heartbound.LOGGER.info("JEI: Total recipes registered: " + recipes.size());
      registration.addRecipes(FusionRecipeCategory.FUSION_TYPE, recipes);
   }

   @Override
   public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
      Heartbound.LOGGER.info("JEI: Registering Fusion Table as recipe catalyst");
      registration.addCraftingStation(FusionRecipeCategory.FUSION_TYPE, HeartboundBlocks.FUSION_TABLE);
   }
}
