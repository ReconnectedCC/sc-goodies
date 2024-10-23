package io.sc3.goodies.util

import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import io.sc3.library.Tooltips.addDescLines
import net.minecraft.client.item.TooltipType
import net.minecraft.item.Item.TooltipContext

abstract class BaseBlockWithEntity(settings: Settings) : BlockWithEntity(settings) {
  override fun appendTooltip(
    stack: ItemStack,
    context: TooltipContext,
    tooltip: MutableList<Text>,
    type: TooltipType
  ) {
    super.appendTooltip(stack, context, tooltip, type)
    addDescLines(tooltip, translationKey)
  }

  override fun getRenderType(state: BlockState) = BlockRenderType.MODEL

  override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean) {
    if (state.isOf(newState.block)) return

    val be = world.getBlockEntity(pos)
    if (be is BaseBlockEntity) be.onBroken()

    super.onStateReplaced(state, world, pos, newState, moved)
  }
}
