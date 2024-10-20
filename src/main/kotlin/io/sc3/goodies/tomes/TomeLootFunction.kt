package io.sc3.goodies.tomes

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.item.ItemStack
import net.minecraft.loot.condition.LootCondition
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.function.ConditionalLootFunction
import net.minecraft.loot.function.LootFunctionType

class TomeLootFunction(conditions: List<LootCondition>) : ConditionalLootFunction(conditions.toMutableList()) {

  override fun process(stack: ItemStack, context: LootContext): ItemStack {
    TomeEnchantments.applyRandomEnchantment(stack, context.random)
    return stack
  }

  override fun getType() = TomeLootFunction.type

  companion object {
    val CODEC: MapCodec<TomeLootFunction> =  RecordCodecBuilder.mapCodec {
        instance -> addConditionsField(instance).apply(instance, ::TomeLootFunction)
    }

    val type = LootFunctionType(CODEC.codec())
  }
}
