package io.sc3.goodies.tomes

import net.minecraft.enchantment.EnchantmentLevelEntry
import net.minecraft.item.EnchantedBookItem
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Formatting.GRAY
import io.sc3.goodies.Registration.ModItems
import io.sc3.goodies.util.BaseItem
import net.minecraft.item.Item
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.text.Text.translatable as trans

class AncientTomeItem(settings: Settings) : BaseItem(settings) {
  override fun isEnchantable(stack: ItemStack) = false
  override fun hasGlint(stack: ItemStack) = true

  override fun appendTooltip(stack: ItemStack, context: Item.TooltipContext, tooltip: List<Text>, type: TooltipType) {
    val ench = stackEnchantment(stack)
    if (ench != null) {
      val lvl = ench.maxLevel + 1
      val text = trans("$translationKey.level_tooltip", trans(ench.translationKey), trans("enchantment.level.$lvl"))
        .formatted(GRAY)
      tooltip.add(text)
    }

    super.appendTooltip(stack,context,tooltip,type)
  }

  companion object {
    fun stackEnchantment(stack: ItemStack) =
      EnchantedBookItem.getEnchantmentNbt(stack)?.let { fromNbt(it) }?.keys?.firstOrNull()

    fun getTomeStacks(): List<ItemStack> =
      TomeEnchantments.validEnchantments.map { ench -> ItemStack(ModItems.ancientTome)
        .also { EnchantedBookItem.addEnchantment(it, EnchantmentLevelEntry(ench, ench.maxLevel)) } }
  }
}
