package io.sc3.goodies.tomes

import io.sc3.goodies.Registration.ModItems
import io.sc3.goodies.ScGoodies.ModId
import io.sc3.goodies.tomes.AncientTomeItem.Companion.stackEnchantment
import io.sc3.goodies.util.AnvilEvents
import io.sc3.library.ext.EnchantmentExt
import net.fabricmc.fabric.api.loot.v3.LootTableEvents
import net.fabricmc.fabric.api.loot.v3.LootTableSource
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.ItemEnchantmentsComponent
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments.*
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.CraftingResultInventory
import net.minecraft.item.ItemStack
import net.minecraft.item.Items.ENCHANTED_BOOK
import net.minecraft.loot.LootPool
import net.minecraft.loot.LootTable
import net.minecraft.loot.LootTables.*
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.entry.EmptyEntry
import net.minecraft.loot.entry.ItemEntry
import net.minecraft.loot.provider.number.UniformLootNumberProvider
import net.minecraft.registry.Registries.LOOT_FUNCTION_TYPE
import net.minecraft.registry.Registry.register
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.screen.AnvilScreenHandler
import net.minecraft.screen.Property
import net.minecraft.text.Text

private const val UPGRADE_COST = 10
private const val UPGRADE_COST_MAXED = 30

object TomeEnchantments {
  val validEnchantments = listOf(
    FEATHER_FALLING,
    SWIFT_SNEAK,
    THORNS,
    SHARPNESS,
    SMITE,
    BANE_OF_ARTHROPODS,
    KNOCKBACK,
    FIRE_ASPECT,
    LOOTING,
    SWEEPING_EDGE,
    EFFICIENCY,
    UNBREAKING,
    FORTUNE,
    POWER,
    PUNCH,
    LUCK_OF_THE_SEA,
    LURE,
    LOYALTY,
    RIPTIDE,
    IMPALING,
    PIERCING
  )

  private const val maxTomeCount = 3.0f
  private const val lootWeightEmpty = 30
  private val lootWeights = mapOf(
    STRONGHOLD_LIBRARY_CHEST  to 10,
    SIMPLE_DUNGEON_CHEST      to 3,
    BASTION_TREASURE_CHEST    to 15,
    WOODLAND_MANSION_CHEST    to 10
  )

  fun init() {
    AnvilEvents.CHANGE.register(::onAnvilChange)

    register(LOOT_FUNCTION_TYPE, ModId("tome_enchant"), TomeLootFunction.type)
    LootTableEvents.MODIFY.register(::enhanceLootTables)

  }
//Required:
  private fun enhanceLootTables(lootTable: RegistryKey<LootTable>, builder: LootTable.Builder, source: LootTableSource, wrapper: RegistryWrapper.WrapperLookup) {
    val weight = lootWeights[lootTable] ?: return
    val entry = ItemEntry.builder(ModItems.ancientTome)
      .weight(weight)
      .quality(2)
      .apply { TomeLootFunction(emptyList()) }
      .build()

    builder.pool(LootPool.builder()
      .rolls(UniformLootNumberProvider.create(0.0f, maxTomeCount))
      .with(EmptyEntry.builder().weight(lootWeightEmpty))
      .with(entry))
  }

  fun applyRandomEnchantment(stack: ItemStack, context: LootContext) {
    val rawEnch = validEnchantments[context.random.nextInt(validEnchantments.size)]
    val ench = EnchantmentExt.getEnchantment(context.world.registryManager, rawEnch);

    val builder = ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT)
    builder.add(ench, ench.value().maxLevel)

    stack.set(DataComponentTypes.STORED_ENCHANTMENTS, builder.build().withShowInTooltip(false))
  }

  private fun onAnvilChange(handler: AnvilScreenHandler, left: ItemStack, right: ItemStack,
                            output: CraftingResultInventory, name: String?, baseCost: Int,
                            playerEntity: PlayerEntity, levelCost: Property): Boolean {
    if (left.isEmpty || right.isEmpty) return true

    if (right.isOf(ModItems.ancientTome)) {
      val rawTomeEnch = stackEnchantment(right) ?: return true
      val tomeEnch = EnchantmentExt.getEnchantment(playerEntity.world.registryManager, rawTomeEnch);

      var enchants = EnchantmentHelper.getEnchantments(left)
      val matched = enchants.getLevel(tomeEnch) ?: return true

      if (matched <= tomeEnch.value().maxLevel) {
        val lvl = matched + 1
        val b = ItemEnchantmentsComponent.Builder(enchants)
        b.set(tomeEnch, lvl)
        enchants = b.build()

        val cost = if (lvl > tomeEnch.value().maxLevel) UPGRADE_COST_MAXED else UPGRADE_COST

        applyOutput(name, left, enchants, cost, output, levelCost)
        return false
      }
    } else if (right.isOf(ENCHANTED_BOOK)) {
      var currentEnchants = EnchantmentHelper.getEnchantments(left)
      val newEnchants = EnchantmentHelper.getEnchantments(right)

      var isOver = false
      var isMatched = false

      newEnchants.enchantmentEntries.forEach { (ench) ->
        if (EnchantmentHelper.getLevel(ench, right) > ench.value().maxLevel) {
          isOver = true

          if (ench.value().isAcceptableItem(left) || left.isOf(ENCHANTED_BOOK)) {
            isMatched = true

            // Remove incompatible enchantments from the target book
            currentEnchants.enchantmentEntries.removeIf { (other) -> isIncompatible(other, ench) }

            val b = ItemEnchantmentsComponent.Builder(currentEnchants)
            b.set(ench, EnchantmentHelper.getLevel(ench, right))
            currentEnchants = b.build()
          }
        } else if (ench.value().isAcceptableItem(left)) {
          // Don't apply incompatible enchantments to the target item
          val incompatible = currentEnchants.enchantmentEntries.any { (other) -> isIncompatible(other, ench) }

          if (!incompatible) {
            val b = ItemEnchantmentsComponent.Builder(currentEnchants)
            b.set(ench, EnchantmentHelper.getLevel(ench, right))
            currentEnchants = b.build()
          }
        }
      }

      if (isOver && isMatched) {
        applyOutput(name, left, currentEnchants, UPGRADE_COST, output, levelCost)
        return false
      }
    }

    return true
  }

  private fun isIncompatible(otherEnch: RegistryEntry<Enchantment>, ench: RegistryEntry<Enchantment>) =
    !Enchantment.canBeCombined(ench, otherEnch)

  private fun applyOutput(name: String?, left: ItemStack, enchants: ItemEnchantmentsComponent, cost: Int,
                          output: CraftingResultInventory, levelCost: Property) {
    val out = left.copy()
    EnchantmentHelper.set(out, enchants)

    val finalCost = if (!name.isNullOrEmpty() && (out.name.string.isNotEmpty() || name != left.name.string)) {
      out.set(DataComponentTypes.ITEM_NAME, Text.of(name));
      cost + 1
    } else {
      cost
    }

    output.setStack(0, out)
    levelCost.set(finalCost)
  }
}
