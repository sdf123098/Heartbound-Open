package com.cuddly.heartbound.jei;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public record FusionRecipe(Ingredient input1, Ingredient input2, Ingredient input3, ItemStack output) {
}
