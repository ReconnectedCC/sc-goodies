package io.sc3.goodies.misc

import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import io.sc3.goodies.util.BaseItem
import net.minecraft.component.type.FoodComponent

class PopcornItem(settings: Settings) : BaseItem(settings) {
  override fun finishUsing(stack: ItemStack, world: World, user: LivingEntity): ItemStack {
    // Don't consume the item when finished eating.
    return stack
  }

  companion object {
    val foodComponent: FoodComponent = FoodComponent.Builder()
      .nutrition(0)
      .saturationModifier(0.0f)
      .alwaysEdible()
      .snack()
      .build()
  }
}
