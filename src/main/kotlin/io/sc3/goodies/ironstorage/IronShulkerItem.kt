package io.sc3.goodies.ironstorage

import net.minecraft.component.DataComponentTypes
import net.minecraft.entity.ItemEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemUsage

class IronShulkerItem(val block: IronShulkerBlock, settings: Settings) : BlockItem(block, settings) {
  override fun canBeNested() = false

  override fun onItemEntityDestroyed(entity: ItemEntity) {
    val stack = entity.stack

    if (stack.components.get(DataComponentTypes.CONTAINER) == null) return
    val items = stack.components.get(DataComponentTypes.CONTAINER)!!

    ItemUsage.spawnItemContents(entity, items.iterateNonEmpty())
  }
}
