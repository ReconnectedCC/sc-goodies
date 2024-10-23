package io.sc3.goodies.datagen.recipes

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.sc3.goodies.ScGoodiesItemTags
import io.sc3.library.recipe.ShapelessRecipeSpec
import net.minecraft.inventory.RecipeInputInventory
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.ShapelessRecipe
import net.minecraft.recipe.book.CraftingRecipeCategory
import net.minecraft.util.collection.DefaultedList
import java.util.function.Function


class DragonScaleRecipe(
  group: String,
  category: CraftingRecipeCategory,
  outputStack: ItemStack,
  input: DefaultedList<Ingredient>,
) : ShapelessRecipe(group, category, outputStack, input) {
  override fun getRemainder(inv: RecipeInputInventory): DefaultedList<ItemStack> {
    val remainder = DefaultedList.ofSize(inv.size(), ItemStack.EMPTY)
    for (i in 0 until remainder.size) {
      val stack = inv.getStack(i)
      if (stack.isIn(ScGoodiesItemTags.ELYTRA)) {
        remainder[i] = stack.copy()
      }
    }

    return remainder
  }

  override fun isIgnoredInRecipeBook() = true
  override fun getSerializer() = DragonScaleRecipeSerializer
}

object DragonScaleRecipeSerializer : RecipeSerializer<DragonScaleRecipe> {
  private fun make(spec: ShapelessRecipeSpec) = DragonScaleRecipe(
    spec.group, spec.category, spec.output, spec.input
  )

  override fun codec(): MapCodec<DragonScaleRecipe>? {
    return RecordCodecBuilder.mapCodec { instance ->
      instance.group(
        Codec.STRING.fieldOf("group").forGetter { r -> r.group },
        CraftingRecipeCategory.CODEC.fieldOf("category").forGetter { r -> r.category },
        ItemStack.CODEC.fieldOf("output").forGetter { z -> z.getResult(null) }, /*TODO(Pretty damn sure getResult null will crash)*/
        Ingredient.DISALLOW_EMPTY_CODEC.listOf()
            .xmap({ i -> DefaultedList.copyOf(Ingredient.EMPTY, *i.toTypedArray())}, Function.identity())
            .fieldOf("ingredient")
            .forGetter { r -> r.ingredients },
      ).apply(instance, ::DragonScaleRecipe)
    }
  }

}
