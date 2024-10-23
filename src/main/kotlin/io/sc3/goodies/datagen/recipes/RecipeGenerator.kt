package io.sc3.goodies.datagen.recipes

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider
import io.sc3.goodies.datagen.recipes.handlers.RECIPE_HANDLERS
import net.minecraft.data.server.recipe.RecipeExporter
import net.minecraft.registry.RegistryWrapper
import java.util.concurrent.CompletableFuture

class RecipeGenerator(out: FabricDataOutput,
                      registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup>
) : FabricRecipeProvider(out, registriesFuture) {
  override fun generate(exporter: RecipeExporter) {
    RECIPE_HANDLERS.forEach { it.generateRecipes(exporter) }
  }
}
