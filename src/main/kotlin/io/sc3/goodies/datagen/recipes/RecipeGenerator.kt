package io.sc3.goodies.datagen.recipes

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider
import io.sc3.goodies.datagen.recipes.handlers.RECIPE_HANDLERS
import net.minecraft.data.server.recipe.RecipeExporter

class RecipeGenerator(out: FabricDataOutput) : FabricRecipeProvider(out) {
  override fun generate(exporter: RecipeExporter) {
    RECIPE_HANDLERS.forEach { it.generateRecipes(exporter) }
  }
}
