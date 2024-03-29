package io.sc3.goodies.datagen.recipes

import io.sc3.goodies.Registration
import io.sc3.goodies.Registration.ModItems
import io.sc3.goodies.enderstorage.EnderStorageBlock.Companion.NBT_COMPUTER_CHANGES_ENABLED
import io.sc3.goodies.enderstorage.EnderStorageBlock.Companion.NBT_FREQUENCY
import io.sc3.goodies.enderstorage.EnderStorageBlock.Companion.NBT_TEMP_CRAFTING_PERSONAL
import io.sc3.goodies.enderstorage.Frequency
import io.sc3.library.recipe.itemDyeColor
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags.DYES
import net.minecraft.inventory.RecipeInputInventory
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.recipe.Ingredient.fromTag
import net.minecraft.recipe.Ingredient.ofItems
import net.minecraft.recipe.SpecialCraftingRecipe
import net.minecraft.recipe.SpecialRecipeSerializer
import net.minecraft.recipe.book.CraftingRecipeCategory
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.util.Identifier
import net.minecraft.util.collection.DefaultedList
import net.minecraft.world.World

class ModifiedEnderStorageRecipe(id: Identifier, category: CraftingRecipeCategory) : SpecialCraftingRecipe(id, category) {
  private val enderStorage = ofItems(ModItems.enderStorage)
  private val dye = fromTag(DYES)
  private val diamond = ofItems(Items.DIAMOND)
  private val emerald = ofItems(Items.EMERALD)

  /** Gets the dyes in the top three slots of the inventory, or null if any slot is not a dye. */
  private fun getDyes(inv: RecipeInputInventory): Array<ItemStack>? {
    var dyes: Array<ItemStack>? = null

    for (i in 0 until 3) {
      val stack = inv.getStack(i)
      if (stack.isEmpty || !dye.test(stack)) return null

      if (dyes == null) dyes = Array(3) { ItemStack.EMPTY }
      dyes[i] = stack
    }

    return dyes
  }

  /** Gets the single ender storage item in the inventory, or null if one was not found, or there was more than one. */
  private fun getEnderStorage(inv: RecipeInputInventory): ItemStack? {
    var foundStorage: ItemStack? = null

    for (i in 0 until inv.size()) {
      val stack = inv.getStack(i)
      if (stack.isEmpty || !enderStorage.test(stack)) continue

      // Ensure there is only one ender storage item - fail if there is more than one
      if (foundStorage != null) return null
      foundStorage = stack
    }

    return foundStorage
  }

  /** Gets a diamond OR an emerald from the inventory. */
  private fun getPersonalState(inv: RecipeInputInventory): PersonalState? {
    var foundDiamond = false
    var foundEmerald = false

    for (i in 0 until inv.size()) {
      val stack = inv.getStack(i)
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

  override fun matches(inv: RecipeInputInventory, world: World): Boolean {
    if (getEnderStorage(inv) == null) return false

    // Will return null if there is more than one diamond or emerald
    val personalState = getPersonalState(inv) ?: return false

    // Require either a personal state or three dyes
    return getDyes(inv) != null || personalState != PersonalState.NOT_PERSONAL
  }

  override fun craft(inv: RecipeInputInventory, registryManager: DynamicRegistryManager): ItemStack {
    val dyes = getDyes(inv)
    val enderStorage = getEnderStorage(inv) ?: return ItemStack.EMPTY
    val personalState = getPersonalState(inv) ?: return ItemStack.EMPTY

    val result = enderStorage.copyWithCount(1)

    val nbt = BlockItem.getBlockEntityNbt(result) ?: NbtCompound()
    val oldComputerChangesEnabled = nbt.getBoolean(NBT_COMPUTER_CHANGES_ENABLED)
    val oldFrequency = Frequency.fromStack(enderStorage) ?: Frequency()

    val frequency = oldFrequency.copy(
      left   = dyes?.get(0)?.let { itemDyeColor(it) } ?: oldFrequency.left,
      middle = dyes?.get(1)?.let { itemDyeColor(it) } ?: oldFrequency.middle,
      right  = dyes?.get(2)?.let { itemDyeColor(it) } ?: oldFrequency.right,

      // Remove the owner. It will be added back later in EnderStorageItem.onCraft(), overwritten with the player that
      // crafted the item
      owner = null,
      ownerName = null
    )

    nbt.put(NBT_FREQUENCY, frequency.toNbt())

    nbt.putBoolean(
      NBT_COMPUTER_CHANGES_ENABLED,
      if (oldComputerChangesEnabled && personalState == PersonalState.PERSONAL) {
        // If crafting an emerald chest with just a diamond, remove the emerald
        false
      } else {
        oldComputerChangesEnabled || personalState == PersonalState.ALLOW_COMPUTER_CHANGES
      }
    )

    nbt.putBoolean(
      NBT_TEMP_CRAFTING_PERSONAL, // Temp flag to check in EnderStorageItem.onCraft
      oldFrequency.personal || personalState.isPersonal
    )

    // Write the new NBT to the item
    BlockItem.setBlockEntityNbt(result, Registration.ModBlockEntities.enderStorage, nbt)
    return result
  }

  override fun getRemainder(inv: RecipeInputInventory): DefaultedList<ItemStack> {
    val out = DefaultedList.ofSize(inv.size(), ItemStack.EMPTY)

    for (i in 0 until inv.size()) {
      val stack = inv.getStack(i)
      if (diamond.test(stack) || emerald.test(stack)) {
        out[i] = stack.copy() // Don't consume the diamond or emerald
      }
    }

    return out
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
