package io.sc3.goodies.datagen.recipes

import com.mojang.serialization.Codec
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
import net.minecraft.recipe.RawShapedRecipe
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.book.CraftingRecipeCategory
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.util.DyeColor

class IronShulkerRecipe(
  group: String,
  category: CraftingRecipeCategory,
  rawShapedRecipe: RawShapedRecipe,
  private val outputItem: ItemStack,
) : ExtendedShapedRecipe(group, category, rawShapedRecipe, outputItem) {

  override fun craft(inventory: RecipeInputInventory, manager: DynamicRegistryManager): ItemStack {
    val shulkerStack = shulkerItem(inventory)
    // No shulker found - disallow craft
    if (shulkerStack.isEmpty) return ItemStack.EMPTY

    val color = shulkerColor(shulkerStack)
    val variant = (outputItem.item as IronShulkerItem).block.variant
    val resultBlock = if (color != null) variant.dyedShulkerBlocks[color]!! else variant.shulkerBlock

    val result = ItemStack(resultBlock)
    result.nbt = shulkerStack.nbt?.copy()
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
  private fun make(spec: ShapedRecipeSpec) = IronShulkerRecipe(
    spec.group, spec.category, spec.rawShapedRecipe, spec.output
  )
  override fun codec(): Codec<IronShulkerRecipe> {
    return RecordCodecBuilder.create { instance ->
      instance.group(
        Codec.STRING.fieldOf("group").forGetter { r -> r.group },
        CraftingRecipeCategory.CODEC.fieldOf("category").forGetter { r -> r.category },
        RawShapedRecipe.CODEC.fieldOf("recipe").forGetter { r -> r.rawShapedRecipe },
        ItemStack.CODEC.fieldOf("output").forGetter { z -> z.getResult(null) } /*TODO(Pretty damn sure getResult null will crash)*/,
      ).apply(instance, ::IronShulkerRecipe)
    }
  }
  override fun read(buf: PacketByteBuf) = make(ShapedRecipeSpec.ofPacket(buf))
  override fun write(buf: PacketByteBuf, recipe: IronShulkerRecipe) = ShapedRecipeSpec.ofRecipe(recipe).write(buf)
}
