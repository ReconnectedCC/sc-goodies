package io.sc3.goodies.datagen.recipes

import com.mojang.serialization.MapCodec
import io.sc3.goodies.ScGoodiesItemTags
import io.sc3.library.recipe.ShapelessRecipeSpec
import net.minecraft.item.ItemStack
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.ShapelessRecipe
import net.minecraft.recipe.book.CraftingRecipeCategory
import net.minecraft.recipe.input.CraftingRecipeInput
import net.minecraft.util.collection.DefaultedList


class DragonScaleRecipe(
  group: String,
  category: CraftingRecipeCategory,
  outputStack: ItemStack,
  input: DefaultedList<Ingredient>,
) : ShapelessRecipe(group, category, outputStack, input) {
  override fun getRemainder(input: CraftingRecipeInput): DefaultedList<ItemStack>? {
    val remainder = DefaultedList.ofSize(input.stackCount, ItemStack.EMPTY)
    for (i in 0 until remainder.size) {
      val stack = input.stacks[i]
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

  override fun codec(): MapCodec<DragonScaleRecipe> {
    return ShapelessRecipeSpec.codec(::DragonScaleRecipe)
  }

  override fun packetCodec(): PacketCodec<RegistryByteBuf, DragonScaleRecipe> {
    return ShapelessRecipeSpec.packetCodec(::DragonScaleRecipe)
  }
}
