package io.sc3.goodies.util

import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import io.sc3.library.Tooltips.addDescLines
import net.minecraft.client.item.TooltipType

open class BaseItem(
  settings: Settings
) : Item(settings) {
  override fun appendTooltip(
    stack: ItemStack,
    context: TooltipContext,
    tooltip: MutableList<Text>,
    type: TooltipType
  ) {
    super.appendTooltip(stack, context, tooltip, type)
    addDescLines(tooltip, getTranslationKey(stack))
  }
}
