package io.sc3.goodies.tomes

import io.sc3.goodies.Registration.ModItems
import io.sc3.goodies.util.BaseItem
import net.minecraft.client.item.TooltipType
import net.minecraft.command.argument.ItemStackArgumentType.itemStack
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.ItemEnchantmentsComponent
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Formatting.GRAY
import net.minecraft.text.Text.translatable as trans


class AncientTomeItem(settings: Settings) : BaseItem(settings) {
  override fun isEnchantable(stack: ItemStack) = false
  override fun hasGlint(stack: ItemStack) = true
  override fun appendTooltip(
    stack: ItemStack,
    context: TooltipContext,
    tooltip: MutableList<Text>,
    type: TooltipType
  ) {
    val ench = stackEnchantment(stack)
    if (ench != null) {
      val lvl = ench.maxLevel + 1
      val text = trans("$translationKey.level_tooltip", trans(ench.translationKey), trans("enchantment.level.$lvl"))
        .formatted(GRAY)
      tooltip.add(text)
    }

    super.appendTooltip(stack, context, tooltip, type)
  }

  companion object {
    fun stackEnchantment(stack: ItemStack)
      = stack.get(DataComponentTypes.STORED_ENCHANTMENTS)?.enchantments?.firstOrNull()?.value()

    fun getTomeStacks(): List<ItemStack> =
      TomeEnchantments.validEnchantments.map { ench -> ItemStack(ModItems.ancientTome)
        .also {
          val b = ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT)
          b.add(ench, ench.maxLevel)
          it.set(DataComponentTypes.STORED_ENCHANTMENTS, b.build())
        } }
  }
}
