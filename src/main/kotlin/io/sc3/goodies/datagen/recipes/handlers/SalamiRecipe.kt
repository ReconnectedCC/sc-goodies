package io.sc3.goodies.datagen.recipes.handlers

import io.sc3.goodies.Registration
import io.sc3.library.recipe.RecipeHandler
import net.minecraft.data.server.recipe.RecipeExporter
import net.minecraft.data.server.recipe.StonecuttingRecipeJsonBuilder
import net.minecraft.data.server.recipe.VanillaRecipeProvider
import net.minecraft.item.Items
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.book.RecipeCategory
import net.minecraft.registry.RegistryWrapper

object SalamiRecipe : RecipeHandler {
  override fun generateRecipes(exporter: RecipeExporter, wrapper: RegistryWrapper.WrapperLookup) {
    StonecuttingRecipeJsonBuilder.createStonecutting(
      Ingredient.ofItems(Items.COOKED_PORKCHOP),
      RecipeCategory.FOOD,
      Registration.ModItems.salami,
      4
    )
      .criterion("has_cooked_porkchop", VanillaRecipeProvider.conditionsFromItem(Items.COOKED_PORKCHOP))
      .offerTo(exporter, "salami_recipe")
  }
}
