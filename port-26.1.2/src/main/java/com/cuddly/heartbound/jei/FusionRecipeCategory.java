package com.cuddly.heartbound.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class FusionRecipeCategory implements IRecipeCategory<FusionRecipe> {
   public static final IRecipeType<FusionRecipe> FUSION_TYPE = IRecipeType.create(Identifier.fromNamespaceAndPath("heartbound", "fusion"), FusionRecipe.class);
   private final IDrawable icon;
   private final IDrawable arrow;
   private final IDrawable slotBackground;

   public FusionRecipeCategory(IGuiHelper guiHelper) {
      this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Blocks.SMITHING_TABLE));
      this.arrow = guiHelper.getRecipeArrow();
      this.slotBackground = guiHelper.getSlotDrawable();
   }

   @Override
   public IRecipeType<FusionRecipe> getRecipeType() {
      return FUSION_TYPE;
   }

   public Component getTitle() {
      return Component.translatable("jei.heartbound.fusion");
   }

   @Override
   public int getWidth() {
      return 125;
   }

   @Override
   public int getHeight() {
      return 25;
   }

   @Override
   public IDrawable getIcon() {
      return this.icon;
   }

   public void setRecipe(IRecipeLayoutBuilder builder, FusionRecipe recipe, IFocusGroup focuses) {
      builder.addSlot(RecipeIngredientRole.INPUT, 8, 5).add(recipe.input1());
      builder.addSlot(RecipeIngredientRole.INPUT, 26, 5).add(recipe.input2());
      builder.addSlot(RecipeIngredientRole.INPUT, 44, 5).add(recipe.input3());
      builder.addSlot(RecipeIngredientRole.OUTPUT, 98, 4).add(recipe.output());
   }

   public void draw(FusionRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
      this.slotBackground.draw(guiGraphics, 7, 4);
      this.slotBackground.draw(guiGraphics, 25, 4);
      this.slotBackground.draw(guiGraphics, 43, 4);
      this.slotBackground.draw(guiGraphics, 97, 4);
      this.arrow.draw(guiGraphics, 70, 4);
   }
}
