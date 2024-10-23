package io.sc3.goodies.datagen.recipes

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.sc3.goodies.ironstorage.IronShulkerBlock
import io.sc3.goodies.ironstorage.IronShulkerItem
import io.sc3.library.recipe.ExtendedShapedRecipe
import io.sc3.library.recipe.ShapedRecipeSpec
import net.minecraft.block.Block
import net.minecraft.block.ShulkerBoxBlock
import net.minecraft.inventory.RecipeInputInventory
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.recipe.RawShapedRecipe
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.ShapedRecipe
import net.minecraft.recipe.book.CraftingRecipeCategory
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.registry.RegistryWrapper
import net.minecraft.util.DyeColor

class IronShulkerRecipe(
  group: String,
  category: CraftingRecipeCategory,
  rawShapedRecipe: RawShapedRecipe,
  private val outputItem: ItemStack,
) : ExtendedShapedRecipe(group, category, rawShapedRecipe, outputItem) {

  override fun craft(inventory: RecipeInputInventory, lookup: RegistryWrapper.WrapperLookup): ItemStack? {
    val shulkerStack = shulkerItem(inventory)
    // No shulker found - disallow craft
    if (shulkerStack.isEmpty) return ItemStack.EMPTY

    val color = shulkerColor(shulkerStack)
    val variant = (outputItem.item as IronShulkerItem).block.variant
    val resultBlock = if (color != null) variant.dyedShulkerBlocks[color]!! else variant.shulkerBlock

    val result = ItemStack(resultBlock)
    result.applyComponentsFrom(shulkerStack.components)
    return result
  }

  companion object {
    private fun isShulkerItem(stack: ItemStack): Boolean =
      Block.getBlockFromItem(stack.item) is ShulkerBoxBlock || stack.item is IronShulkerItem

    fun shulkerColor(stack: ItemStack): DyeColor? =
      when (val block = Block.getBlockFromItem(stack.item)) {
        is ShulkerBoxBlock -> block.color
        is IronShulkerBlock -> block.color
        else -> null
      }

    fun shulkerItem(inv: RecipeInputInventory): ItemStack {
      var shulkerStack = ItemStack.EMPTY

      for (i in 0 until inv.size()) {
        val stack = inv.getStack(i)
        if (isShulkerItem(stack)) {
          // Crafting with two shulkers (should never happen) - disallow craft
          if (!shulkerStack.isEmpty) return ItemStack.EMPTY
          shulkerStack = stack
        }
      }

      return shulkerStack
    }
  }

  override fun getSerializer(): RecipeSerializer<*> = IronShulkerRecipeSerializer
}

object IronShulkerRecipeSerializer : RecipeSerializer<IronShulkerRecipe> {
  override fun codec(): MapCodec<IronShulkerRecipe> {
    return ShapedRecipeSpec.codec(::IronShulkerRecipe);
  }

  override fun packetCodec(): PacketCodec<RegistryByteBuf, IronShulkerRecipe> {
    return ShapedRecipeSpec.packetCodec(::IronShulkerRecipe)
  }
}
