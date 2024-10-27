package io.sc3.goodies.tomes

import io.sc3.goodies.Registration.ModItems
import io.sc3.goodies.util.BaseItem
import io.sc3.library.ext.EnchantmentExt
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.ItemEnchantmentsComponent
import net.minecraft.enchantment.Enchantment
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.text.Text
import net.minecraft.util.Formatting.GRAY
import net.minecraft.util.Util
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
      val realEnch: RegistryEntry<Enchantment> = EnchantmentExt.getEnchantment(context.registryLookup!!, ench);
      val text = trans("$translationKey.level_tooltip", trans(
          Util.createTranslationKey(
            "enchantment",
            ench.value
          )
        ), trans("enchantment.level.${realEnch.value().maxLevel+1}"))
        .formatted(GRAY)
      tooltip.add(text)
    }

    super.appendTooltip(stack, context, tooltip, type)
  }

  companion object {
    fun stackEnchantment(stack: ItemStack): RegistryKey<Enchantment>?
      = stack.get(DataComponentTypes.STORED_ENCHANTMENTS)?.enchantments?.firstOrNull()?.key?.get()

    fun getTomeStacks(wrapperLookup: RegistryWrapper.WrapperLookup): List<ItemStack> =
      TomeEnchantments.validEnchantments.map { enchKey -> ItemStack(ModItems.ancientTome)
        .also {
          val ench = EnchantmentExt.getEnchantment(wrapperLookup, enchKey);
          val b = ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT.withShowInTooltip(false))
          b.add(ench, ench.value().maxLevel)
          it.set(DataComponentTypes.STORED_ENCHANTMENTS, b.build())
        } }
  }
}
