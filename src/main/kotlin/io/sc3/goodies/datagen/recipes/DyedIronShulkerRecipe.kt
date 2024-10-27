package io.sc3.goodies.datagen.recipes

import io.sc3.goodies.ScGoodiesItemTags
import io.sc3.goodies.datagen.recipes.IronShulkerRecipe.Companion.shulkerItem
import io.sc3.goodies.ironstorage.IronShulkerItem
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags.DYES
import net.minecraft.item.DyeItem
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Ingredient.fromTag
import net.minecraft.recipe.SpecialCraftingRecipe
import net.minecraft.recipe.SpecialRecipeSerializer
import net.minecraft.recipe.book.CraftingRecipeCategory
import net.minecraft.recipe.input.CraftingRecipeInput
import net.minecraft.registry.RegistryWrapper
import net.minecraft.util.DyeColor
import net.minecraft.world.World

class DyedIronShulkerRecipe(category: CraftingRecipeCategory) : SpecialCraftingRecipe(category) {
  private val ironShulker = fromTag(ScGoodiesItemTags.ANY_IRON_SHULKER_BOX)
  private val dye = fromTag(DYES)

  override fun matches(input: CraftingRecipeInput, world: World?): Boolean {
    var hasShulker = false
    var hasDye = false

    for (i in 0 until input.stackCount) {
      val stack = input.getStackInSlot(i)
      if (stack.isEmpty) continue

      when {
        ironShulker.test(stack) -> {
          if (hasShulker) return false
          hasShulker = true
        }

        dye.test(stack) -> {
          if (hasDye) return false
          hasDye = true
        }

        else -> return false
      }
    }

    return hasShulker && hasDye
  }

  override fun craft(input: CraftingRecipeInput, lookup: RegistryWrapper.WrapperLookup?): ItemStack? {
    val shulkerStack = shulkerItem(input)
    // No shulker found - disallow craft
    if (shulkerStack.isEmpty) return ItemStack.EMPTY

    val color = dyeItem(input) ?: return ItemStack.EMPTY
    val variant = (shulkerStack.item as IronShulkerItem).block.variant
    val resultBlock = variant.dyedShulkerBlocks[color]

    val result = ItemStack(resultBlock)
    result.applyComponentsFrom(shulkerStack.components)
    return result
  }

  private fun dyeItem(inv: CraftingRecipeInput): DyeColor? {
    for (i in 0 until inv.stackCount) {
      val stack = inv.stacks[i]
      if (stack.isEmpty) continue

      val item = stack.item
      if (item is DyeItem) return item.color
    }

    return null
  }

  override fun fits(w: Int, h: Int) = w * h >= 2
  override fun getSerializer() = recipeSerializer

  companion object {
    val recipeSerializer = SpecialRecipeSerializer(::DyedIronShulkerRecipe)
  }
}
