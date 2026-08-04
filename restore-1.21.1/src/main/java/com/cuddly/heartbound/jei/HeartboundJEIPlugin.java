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
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;

@JeiPlugin
public class HeartboundJEIPlugin implements IModPlugin {
   public Identifier getPluginUid() {
      return Identifier.of("heartbound", "jei_plugin");
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
            recipes.add(new FusionRecipe(Ingredient.ofStacks(input1), Ingredient.ofStacks(input2), Ingredient.ofStacks(input3), output));
            Heartbound.LOGGER.info("JEI: Added recipe for profile: " + profile.id());
         }
      }

      Heartbound.LOGGER.info("JEI: Total recipes registered: " + recipes.size());
      registration.addRecipes(FusionRecipeCategory.FUSION_TYPE, recipes);
   }

   @Override
   public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
      Heartbound.LOGGER.info("JEI: Registering Fusion Table as recipe catalyst");
      registration.addRecipeCatalyst(new ItemStack(HeartboundBlocks.FUSION_TABLE), new RecipeType[]{FusionRecipeCategory.FUSION_TYPE});
   }
}
