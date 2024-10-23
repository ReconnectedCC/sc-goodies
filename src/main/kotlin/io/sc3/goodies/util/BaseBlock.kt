package io.sc3.goodies.util

import net.minecraft.block.Block
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import io.sc3.library.Tooltips.addDescLines
import net.minecraft.client.item.TooltipType
import net.minecraft.item.Item.TooltipContext

abstract class BaseBlock(settings: Settings) : Block(settings) {
  override fun appendTooltip(
    stack: ItemStack,
    context: TooltipContext,
    tooltip: MutableList<Text>,
    type: TooltipType
  ) {
    super.appendTooltip(stack, context, tooltip, type)
    addDescLines(tooltip, translationKey)
  }
}
