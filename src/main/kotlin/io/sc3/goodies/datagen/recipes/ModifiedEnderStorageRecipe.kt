package io.sc3.goodies.datagen.recipes

import io.sc3.goodies.Registration
import io.sc3.goodies.Registration.ModItems
import io.sc3.goodies.enderstorage.EnderStorageStackData
import io.sc3.goodies.enderstorage.Frequency
import io.sc3.library.recipe.itemDyeColor
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.DYES
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.recipe.Ingredient.fromTag
import net.minecraft.recipe.Ingredient.ofItems
import net.minecraft.recipe.SpecialCraftingRecipe
import net.minecraft.recipe.SpecialRecipeSerializer
import net.minecraft.recipe.book.CraftingRecipeCategory
import net.minecraft.recipe.input.CraftingRecipeInput
import net.minecraft.registry.RegistryWrapper
import net.minecraft.world.World
import java.util.*

class ModifiedEnderStorageRecipe(category: CraftingRecipeCategory) : SpecialCraftingRecipe(category) {
  private val enderStorage = ofItems(ModItems.enderStorage)
  private val dye = fromTag(DYES)
  private val diamond = ofItems(Items.DIAMOND)
  private val emerald = ofItems(Items.EMERALD)

  /** Gets the dyes in the top three slots of the inventory, or null if any slot is not a dye. */

  /*
  FIXME: THIS IS SORTA BROKEN
  1. if you don't leave a dye in the top right corner, it will not craft
  2. if you leave a dye in the top middle, it will craft if you move the ender storage to the right
  Almost as if the crafing grid resizes to the smallest rectangle containing all non-empty slots, and then the recipe is applied to that rectangle. So if you leave a dye in the middle, it will resize to 2x2 and then the recipe will match.
  This is crazy
  Crazy? I was crazy once...
  They put me in a room...
  A rubber room...
  A rubber room with rats...
  And those rats? they make me crazy!
   */
  //TODO: Add EMI support for this recipe
  private fun getDyes(inv: CraftingRecipeInput): Array<ItemStack>? {
    val dyes: Array<ItemStack> = Array(3) { ItemStack.EMPTY }
    for (i in 0 until 3) {
      val stack = inv.getStackInSlot(i,0)
      if (!stack.isEmpty && !dye.test(stack)) return null
      dyes[i] = stack
    }
    return dyes
  }

  /** Gets the single ender storage item in the inventory, or null if one was not found, or there was more than one. */
  private fun getEnderStorage(input: CraftingRecipeInput): ItemStack? {
    var foundStorage: ItemStack? = null

    for (i in 0 until input.size) {
      val stack = input.stacks[i]
      if (stack.isEmpty || !enderStorage.test(stack)) continue

      // Ensure there is only one ender storage item - fail if there is more than one
      if (foundStorage != null) return null
      foundStorage = stack
    }

    return foundStorage
  }

  /** Gets a diamond OR an emerald from the inventory. */
  private fun getPersonalState(inv: CraftingRecipeInput): PersonalState? {
    var foundDiamond = false
    var foundEmerald = false

    for (i in 0 until inv.stackCount) {
      val stack = inv.stacks[i]
      if (stack.isEmpty) continue

      if (diamond.test(stack)) {
        if (foundDiamond) return null // Fail if there is more than one diamond
        foundDiamond = true
      }

      if (emerald.test(stack)) {
        if (foundEmerald) return null // Fail if there is more than one emerald
        foundEmerald = true
      }
    }

    return when {
      foundEmerald -> PersonalState.ALLOW_COMPUTER_CHANGES
      foundDiamond -> PersonalState.PERSONAL
      else -> PersonalState.NOT_PERSONAL
    }
  }
  private fun areUnwantedItemsPresent(inv: CraftingRecipeInput): Boolean {
    if (inv.stackCount > 4) return true
    for (stack in inv.stacks) {
      if (stack.isEmpty) continue

      if (!enderStorage.test(stack) && !dye.test(stack) && !diamond.test(stack) && !emerald.test(stack)) {
        return true
      }
    }

    return false
  }
  override fun matches(input: CraftingRecipeInput, world: World): Boolean {
    if (getEnderStorage(input) == null) return false
    println("Found ender storage")
    // Will return null if there is more than one diamond or emerald
    val personalState = getPersonalState(input) ?: return false
    println("Found personal state: $personalState")

    if (areUnwantedItemsPresent(input)) return false
    println("No unwanted items present")

    // Require either a personal state or three dyes
    return getDyes(input) != null || personalState != PersonalState.NOT_PERSONAL
  }

  override fun craft(inv: CraftingRecipeInput, lookup: RegistryWrapper.WrapperLookup): ItemStack {
    val dyes = getDyes(inv)
    val enderStorage = getEnderStorage(inv) ?: return ItemStack.EMPTY
    val personalState = getPersonalState(inv) ?: return ItemStack.EMPTY

    val result = enderStorage.copyWithCount(1)

    val oldComputerChangesEnabled = EnderStorageStackData.computerChangesEnabled(result)
    val oldFrequency = Frequency.fromStack(enderStorage) ?: Frequency()

    val frequency = oldFrequency.copy(
      left   = dyes?.get(0)?.let { itemDyeColor(it) } ?: oldFrequency.left,
      middle = dyes?.get(1)?.let { itemDyeColor(it) } ?: oldFrequency.middle,
      right  = dyes?.get(2)?.let { itemDyeColor(it) } ?: oldFrequency.right,

      // Remove the owner. It will be added back later in EnderStorageItem.onCraft(), overwritten with the player that
      // crafted the item
      owner = Optional.empty(),
      ownerName = Optional.empty()
    )

    result.set(Registration.ModComponents.FREQUENCY, frequency)
    result.set(Registration.ModComponents.COMPUTER_CHANGES_ENABLED,
      if (oldComputerChangesEnabled && personalState == PersonalState.PERSONAL) {
        // If crafting an emerald chest with just a diamond, remove the emerald
        false
      } else {
        oldComputerChangesEnabled || personalState == PersonalState.ALLOW_COMPUTER_CHANGES
      })


    result.set(
      Registration.ModComponents.TEMP_CRAFTING_PERSONAL, // Temp flag to check in EnderStorageItem.onCraft
      oldFrequency.personal || personalState.isPersonal
    )

    return result
  }

  override fun fits(w: Int, h: Int) = w >= 3 && h >= 2
  override fun getSerializer() = recipeSerializer

  companion object {
    val recipeSerializer = SpecialRecipeSerializer(::ModifiedEnderStorageRecipe)
  }

  enum class PersonalState(val isPersonal: Boolean) {
    NOT_PERSONAL(false),
    PERSONAL(true),
    ALLOW_COMPUTER_CHANGES(true)
  }
}
