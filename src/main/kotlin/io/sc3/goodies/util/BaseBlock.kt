package io.sc3.goodies.util

import net.minecraft.block.Block
import net.minecraft.item.ItemStack
import net.minecraft.item.Item
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.text.Text
import io.sc3.library.Tooltips.addDescLines

abstract class BaseBlock(settings: Settings) : Block(settings) {
  override fun appendTooltip(stack: ItemStack, context: Item.TooltipContext, tooltip: List<Text>, type: TooltipType) {
    super.appendTooltip(stack, context, tooltip, type)
    addDescLines(tooltip.toMutableList(), translationKey)
  }
}
