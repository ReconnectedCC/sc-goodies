package io.sc3.goodies.datagen.recipes

import net.minecraft.advancement.AdvancementEntry
import net.minecraft.data.server.recipe.RecipeExporter
import net.minecraft.recipe.Recipe
import net.minecraft.recipe.ShapedRecipe
import net.minecraft.recipe.ShapelessRecipe
import net.minecraft.registry.RegistryWrapper
import net.minecraft.util.Identifier

/**
 * Wraps a [RecipeExporter] so that the vanilla recipe built by a recipe json builder is swapped out for a mod recipe
 * before it gets serialised. The generated JSON then uses the mod recipe serializer, keeping the custom crafting
 * behaviour (component copying, crafting remainders, ...) that a plain `minecraft:crafting_*` recipe does not have.
 */
fun RecipeExporter.mapRecipe(transform: (Recipe<*>) -> Recipe<*>): RecipeExporter =
  object : RecipeExporter {
    override fun accept(recipeId: Identifier, recipe: Recipe<*>, advancement: AdvancementEntry?) =
      this@mapRecipe.accept(recipeId, transform(recipe), advancement)

    override fun getAdvancementBuilder() = this@mapRecipe.advancementBuilder
  }

/** Re-creates the generated shapeless recipe as a mod shapeless recipe type. */
fun RecipeExporter.mapShapeless(
  lookup: RegistryWrapper.WrapperLookup,
  factory: (group: String, category: net.minecraft.recipe.book.CraftingRecipeCategory,
            output: net.minecraft.item.ItemStack,
            input: net.minecraft.util.collection.DefaultedList<net.minecraft.recipe.Ingredient>) -> ShapelessRecipe
) = mapRecipe { recipe ->
  recipe as ShapelessRecipe
  factory(recipe.group, recipe.category, recipe.getResult(lookup), recipe.ingredients)
}

/** Re-creates the generated shaped recipe as a mod shaped recipe type. */
fun RecipeExporter.mapShaped(
  lookup: RegistryWrapper.WrapperLookup,
  factory: (group: String, category: net.minecraft.recipe.book.CraftingRecipeCategory,
            raw: net.minecraft.recipe.RawShapedRecipe,
            output: net.minecraft.item.ItemStack) -> ShapedRecipe
) = mapRecipe { recipe ->
  recipe as ShapedRecipe
  factory(recipe.group, recipe.category, recipe.raw, recipe.getResult(lookup))
}
