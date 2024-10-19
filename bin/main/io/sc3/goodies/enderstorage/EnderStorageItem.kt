package io.sc3.goodies.enderstorage

import io.sc3.goodies.enderstorage.EnderStorageBlock.Companion.NBT_FREQUENCY
import io.sc3.goodies.enderstorage.EnderStorageBlock.Companion.NBT_TEMP_CRAFTING_PERSONAL
import net.minecraft.block.Block
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.world.World

class EnderStorageItem(block: Block, settings: Settings) : BlockItem(block, settings) {
  /**
   * If `wasPersonal` was set, set this player to the new owner of the chest.
   * @see io.sc3.goodies.datagen.recipes.ModifiedEnderStorageRecipe
   */
  override fun onCraft(stack: ItemStack, world: World, player: PlayerEntity?) {
    val nbt = getBlockEntityNbt(stack) ?: return

    val wasPersonal = nbt.getBoolean(NBT_TEMP_CRAFTING_PERSONAL)
    if (wasPersonal && player != null) {
      val oldFrequency = Frequency.fromStack(stack) ?: Frequency()
      val frequency = oldFrequency.copy(owner = player.uuid, ownerName = player.gameProfile.name)
      nbt.put(NBT_FREQUENCY, frequency.toNbt())
      nbt.remove(NBT_TEMP_CRAFTING_PERSONAL)
    }
  }
}
