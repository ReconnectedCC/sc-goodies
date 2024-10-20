package io.sc3.goodies.datagen.recipes.handlers

import io.sc3.goodies.Registration.ModItems
import io.sc3.goodies.ScGoodies.ModId
import io.sc3.goodies.datagen.recipes.ModifiedEnderStorageRecipe
import io.sc3.library.recipe.RecipeHandler
import io.sc3.library.recipe.specialRecipe
import net.minecraft.data.server.recipe.RecipeExporter
import net.minecraft.data.server.recipe.RecipeProvider
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder
import net.minecraft.item.Items.*
import net.minecraft.recipe.book.CraftingRecipeCategory
import net.minecraft.recipe.book.RecipeCategory
import net.minecraft.registry.Registries.RECIPE_SERIALIZER
import net.minecraft.registry.Registry

object EnderStorageRecipes : RecipeHandler {
  override fun registerSerializers() {
    // Dye/diamond/emerald recipe
    Registry.register(RECIPE_SERIALIZER, ModId("modify_ender_storage"), ModifiedEnderStorageRecipe.recipeSerializer)
  }

  override fun generateRecipes(exporter: RecipeExporter) {
    // Ender Storage
    ShapedRecipeJsonBuilder
      .create(RecipeCategory.DECORATIONS, ModItems.enderStorage)
      .pattern("BWB")
      .pattern("OCO")
      .pattern("BEB")
      .input('B', BLAZE_ROD)
      .input('W', WHITE_WOOL) // Specifically white, no others
      .input('O', OBSIDIAN)
      .input('C', CHEST)
      .input('E', ENDER_PEARL)
      .criterion("has_chest", RecipeProvider.conditionsFromItem(CHEST))
      .offerTo(exporter)

    // Dye/diamond/emerald recipe
    specialRecipe<ModifiedEnderStorageRecipe>(exporter, ModifiedEnderStorageRecipe(CraftingRecipeCategory.MISC))
  }
}
