package io.sc3.goodies.util

import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.text.Text
import net.minecraft.world.World
import io.sc3.library.Tooltips.addDescLines

open class BaseItem(
  settings: Settings
) : Item(settings) {
  override fun appendTooltip(stack: ItemStack, context: Item.TooltipContext, tooltip: List<Text>, type: TooltipType) {
    super.appendTooltip(stack, context, tooltip, type)
    addDescLines(tooltip.toMutableList(), getTranslationKey(stack))
  }
}
