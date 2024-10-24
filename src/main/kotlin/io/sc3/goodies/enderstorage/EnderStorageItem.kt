package io.sc3.goodies.enderstorage

import io.sc3.goodies.Registration
import net.minecraft.block.Block
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import java.util.*

class EnderStorageItem(block: Block, settings: Settings) : BlockItem(block, settings) {
  override fun onCraft(stack: ItemStack, world: World) {
    craft(stack, world, null)
  }

  override fun onCraftByPlayer(stack: ItemStack, world: World, player: PlayerEntity?) {
    craft(stack, world, player)
  }

  /**
   * If `wasPersonal` was set, set this player to the new owner of the chest.
   * @see io.sc3.goodies.datagen.recipes.ModifiedEnderStorageRecipe
   */
  fun craft(stack: ItemStack, world: World, player: PlayerEntity?) {
    val wasPersonal = stack.get(Registration.ModComponents.TEMP_CRAFTING_PERSONAL)
    if (wasPersonal == true && player != null) {
      val oldFrequency = Frequency.fromStack(stack) ?: Frequency()
      val frequency = oldFrequency.copy(owner = Optional.of(player.uuid), ownerName = Optional.of(player.gameProfile.name))
      stack.set(Registration.ModComponents.FREQUENCY, frequency)
      stack.remove(Registration.ModComponents.TEMP_CRAFTING_PERSONAL)
    }
  }
}
