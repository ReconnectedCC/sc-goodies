package io.sc3.goodies.datagen.recipes

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.sc3.goodies.ScGoodiesItemTags
import io.sc3.library.recipe.ShapelessRecipeSpec
import net.minecraft.inventory.RecipeInputInventory
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.ShapelessRecipe
import net.minecraft.recipe.book.CraftingRecipeCategory
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.util.collection.DefaultedList
import java.util.function.Function

class ElytraRecipe(
  group: String,
  category: CraftingRecipeCategory,
  outputStack: ItemStack,
  val input: DefaultedList<Ingredient>
) : ShapelessRecipe(group, category, outputStack, input) {
  override fun craft(inv: RecipeInputInventory, manager: DynamicRegistryManager): ItemStack {
    val output = getResult(manager)

    for (i in 0 until inv.size()) {
      val stack = inv.getStack(i)
      if (stack.isIn(ScGoodiesItemTags.ELYTRA)) {
        // Copy the NBT from the old elytra, this will copy damage, custom name, and enchantments
        val out = output.copy()
        out.nbt = stack.nbt?.copy()
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
  override fun codec(): Codec<ElytraRecipe> {
    return RecordCodecBuilder.create { instance ->
      instance.group(
        Codec.STRING.fieldOf("group").forGetter { r -> r.group },
        CraftingRecipeCategory.CODEC.fieldOf("category").forGetter { r -> r.category },
        ItemStack.CODEC.fieldOf("output").forGetter { z -> z.getResult(null) }, /*TODO(Pretty damn sure getResult null will crash)*/
        Ingredient.DISALLOW_EMPTY_CODEC.listOf()
          .xmap({ i -> DefaultedList.copyOf(Ingredient.EMPTY, *i.toTypedArray())}, Function.identity())
          .fieldOf("ingredient")
          .forGetter { r -> r.ingredients },
      ).apply(instance, ::ElytraRecipe)
    }
  }
  override fun read(buf: PacketByteBuf): ElytraRecipe = make(ShapelessRecipeSpec.ofPacket(buf))
  override fun write(buf: PacketByteBuf, recipe: ElytraRecipe) = ShapelessRecipeSpec.ofRecipe(recipe).write(buf)
}
