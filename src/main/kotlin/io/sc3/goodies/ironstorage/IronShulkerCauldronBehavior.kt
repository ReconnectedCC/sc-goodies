package io.sc3.goodies.ironstorage

import net.minecraft.block.Block
import net.minecraft.block.LeveledCauldronBlock
import net.minecraft.block.cauldron.CauldronBehavior
import net.minecraft.block.cauldron.CauldronBehavior.WATER_CAULDRON_BEHAVIOR
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.ItemStack
import net.minecraft.stat.Stats
import net.minecraft.util.ActionResult
import net.minecraft.util.ItemActionResult

object IronShulkerCauldronBehavior {
  private val cleanShulker = CauldronBehavior { state, world, pos, player, hand, stack ->
    val block = Block.getBlockFromItem(stack.item) as? IronShulkerBlock
      ?: return@CauldronBehavior ItemActionResult.CONSUME

    if (!world.isClient) {
      val resultStack = ItemStack(block.variant.shulkerBlock)
      resultStack.applyComponentsFrom(stack.components)

      player.setStackInHand(hand, resultStack)
      player.incrementStat(Stats.CLEAN_SHULKER_BOX)
      LeveledCauldronBlock.decrementFluidLevel(state, world, pos)
    }

    ItemActionResult.success(world.isClient)
  }

  internal fun registerBehavior() {
    IronStorageVariant.entries.forEach { variant ->
      variant.dyedShulkerItems.values.forEach {
        WATER_CAULDRON_BEHAVIOR.map[it] = cleanShulker
      }
    }
  }
}
