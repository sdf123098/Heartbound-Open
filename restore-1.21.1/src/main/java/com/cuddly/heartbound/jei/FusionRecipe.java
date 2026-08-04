package com.cuddly.heartbound.jei;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;

public record FusionRecipe(Ingredient input1, Ingredient input2, Ingredient input3, ItemStack output) {
}
