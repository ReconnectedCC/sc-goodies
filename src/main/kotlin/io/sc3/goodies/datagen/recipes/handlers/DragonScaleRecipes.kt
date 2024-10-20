package io.sc3.goodies.datagen.recipes.handlers

import io.sc3.goodies.Registration
import io.sc3.goodies.ScGoodies.ModId
import io.sc3.goodies.ScGoodiesItemTags
import io.sc3.goodies.datagen.recipes.DragonScaleRecipeSerializer
import io.sc3.goodies.elytra.DyedElytraItem.Companion.dyedElytraItems
import io.sc3.library.recipe.RecipeHandler
import net.minecraft.data.server.recipe.RecipeExporter
import net.minecraft.data.server.recipe.RecipeProvider.conditionsFromItem
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder
import net.minecraft.item.Items
import net.minecraft.recipe.book.RecipeCategory
import net.minecraft.registry.Registries.RECIPE_SERIALIZER
import net.minecraft.registry.Registry.register
import net.minecraft.util.DyeColor
import io.sc3.library.recipe.offerTo

object DragonScaleRecipes : RecipeHandler {
  override fun registerSerializers() {
    register(RECIPE_SERIALIZER, ModId("dragon_scale_elytra"), DragonScaleRecipeSerializer)
  }

  override fun generateRecipes(exporter: RecipeExporter) {
    ShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, dyedElytraItems[DyeColor.BLACK]!!)
      .criterion("has_elytra", conditionsFromItem(Items.ELYTRA))
      .input(Registration.ModItems.dragonScale)
      .input(ScGoodiesItemTags.ELYTRA)
      .offerTo(exporter, ModId("dragon_scale_elytra"))
  }
}
