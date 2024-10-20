package io.sc3.goodies.datagen.recipes.handlers

import io.sc3.goodies.Registration
import io.sc3.library.recipe.RecipeHandler
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags
import net.minecraft.data.server.recipe.RecipeExporter
import net.minecraft.data.server.recipe.RecipeProvider
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder
import net.minecraft.item.Items
import net.minecraft.recipe.book.RecipeCategory

object DimmableLightRecipe : RecipeHandler {
  override fun generateRecipes(exporter: RecipeExporter) {
    ShapedRecipeJsonBuilder
      .create(RecipeCategory.REDSTONE, Registration.ModBlocks.dimmableLight)
      .pattern(" Q ")
      .pattern("RLR")
      .pattern(" Q ")
      .input('Q', ConventionalItemTags.QUARTZ)
      .input('R', ConventionalItemTags.REDSTONE_DUSTS)
      .input('L', Items.REDSTONE_LAMP)
      .criterion("has_lamp", RecipeProvider.conditionsFromItem(Items.REDSTONE_LAMP))
      .offerTo(exporter)
  }
}
