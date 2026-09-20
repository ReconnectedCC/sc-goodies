package io.sc3.goodies.datagen.recipes.handlers

import io.sc3.goodies.ScGoodies.ModId
import io.sc3.goodies.ScGoodiesItemTags
import io.sc3.goodies.datagen.recipes.ElytraRecipe
import io.sc3.goodies.datagen.recipes.ElytraRecipeSerializer
import io.sc3.goodies.datagen.recipes.mapShapeless
import io.sc3.goodies.elytra.DyedElytraItem
import io.sc3.goodies.elytra.SpecialElytraType
import io.sc3.library.recipe.RecipeHandler
import net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder
import net.minecraft.data.server.recipe.RecipeExporter
import net.minecraft.data.server.recipe.RecipeProvider.conditionsFromTag
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder
import net.minecraft.item.DyeItem
import net.minecraft.recipe.book.RecipeCategory
import net.minecraft.registry.Registries.RECIPE_SERIALIZER
import net.minecraft.registry.Registry.register
import net.minecraft.registry.RegistryWrapper
import net.minecraft.util.DyeColor

object ElytraRecipes : RecipeHandler {
  override fun registerSerializers() {
    register(RECIPE_SERIALIZER, ModId("elytra"), ElytraRecipeSerializer)
  }

  override fun generateRecipes(exporter: RecipeExporter, wrapper: RegistryWrapper.WrapperLookup) {
    // The elytra recipes need the mod recipe type, so that the components (damage, enchantments, custom name) of the
    // elytra that was dyed are copied to the result.
    val elytraExporter = exporter.mapShapeless(wrapper, ::ElytraRecipe)

    // Dyed Elytra
    DyeColor.entries.forEach { color ->
      val elytra = DyedElytraItem.dyedElytraItems[color]!!
      ShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, elytra)
        .input(ScGoodiesItemTags.ELYTRA)
        .input(DyeItem.byColor(color))
        .isElytra()
        .offerTo(elytraExporter)
    }

    // Special Elytra
    SpecialElytraType.entries.forEach { type ->
      ShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, type.item)
        .input(ScGoodiesItemTags.ELYTRA)
        .apply { type.recipeColors.forEach { input(DyeItem.byColor(it)) } }
        .isElytra()
        .offerTo(elytraExporter)
    }
  }

  private fun CraftingRecipeJsonBuilder.isElytra() = apply {
    criterion("has_elytra", conditionsFromTag(ScGoodiesItemTags.ELYTRA))
    group("dyedElytra")
  }
}
