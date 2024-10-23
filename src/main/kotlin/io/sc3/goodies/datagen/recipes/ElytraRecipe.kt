package io.sc3.goodies.datagen.recipes

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.sc3.goodies.ScGoodiesItemTags
import io.sc3.library.recipe.ShapelessRecipeSpec
import net.minecraft.inventory.RecipeInputInventory
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.ShapelessRecipe
import net.minecraft.recipe.book.CraftingRecipeCategory
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.registry.RegistryWrapper
import net.minecraft.util.collection.DefaultedList
import java.awt.Shape
import java.util.function.Function

class ElytraRecipe(
  group: String,
  category: CraftingRecipeCategory,
  outputStack: ItemStack,
  val input: DefaultedList<Ingredient>
) : ShapelessRecipe(group, category, outputStack, input) {
  override fun craft(inv: RecipeInputInventory, lookup: RegistryWrapper.WrapperLookup): ItemStack? {
    val output = getResult(lookup)

    for (i in 0 until inv.size()) {
      val stack = inv.getStack(i)
      if (stack.isIn(ScGoodiesItemTags.ELYTRA)) {
        // Copy the NBT from the old elytra, this will copy damage, custom name, and enchantments
        val out = output.copy()
        out.applyComponentsFrom(stack.components)
        return out
      }
    }

    return ItemStack.EMPTY
  }

  override fun getSerializer() = ElytraRecipeSerializer
}

object ElytraRecipeSerializer : RecipeSerializer<ElytraRecipe> {
  private fun make(spec: ShapelessRecipeSpec) = ElytraRecipe(
    spec.group, spec.category, spec.output, spec.input
  )
  override fun codec(): MapCodec<ElytraRecipe> {
    return ShapelessRecipeSpec.codec(::ElytraRecipe)
  }

  override fun packetCodec(): PacketCodec<RegistryByteBuf, ElytraRecipe> {
    return ShapelessRecipeSpec.packetCodec(::ElytraRecipe)
  }
}
