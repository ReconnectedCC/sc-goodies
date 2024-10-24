package io.sc3.goodies.hoverboots

import dev.emi.trinkets.api.SlotReference
import dev.emi.trinkets.api.TrinketItem
import dev.emi.trinkets.api.TrinketsApi
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.DyeColor
import io.sc3.goodies.ScGoodies.modId
import io.sc3.library.Tooltips.addDescLines
import net.fabricmc.fabric.impl.`object`.builder.FabricEntityTypeImpl.Builder.Living
import net.minecraft.client.item.TooltipType
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.entry.RegistryEntry
import java.util.*

class HoverBootsItem(
  val color: DyeColor,
  settings: Settings
) : TrinketItem(settings) {
  override fun getTranslationKey() = "item.$modId.hover_boots"

  override fun appendTooltip(
    stack: ItemStack,
    context: TooltipContext,
    tooltip: MutableList<Text>,
    type: TooltipType
  ) {
    super.appendTooltip(stack, context,tooltip, type)
    addDescLines(tooltip, getTranslationKey(stack))
  }

  override fun tick(stack: ItemStack, slot: SlotReference, entity: LivingEntity) {
    if (!entity.isOnGround && entity.fallDistance < 5.0f && entity.velocity.y < 0.0f) {
      entity.velocity = entity.velocity.multiply(1.0, 0.9, 1.0)
      entity.velocityDirty = true
    }
  }

  override fun onEquip(stack: ItemStack, slot: SlotReference, entity: LivingEntity) {
    super.onEquip(stack, slot, entity)
    val uuid: UUID = UUID.fromString("c73b4792-b31e-4ab5-8b8b-5cfe9ab0496e")
    val attributeInstance = entity.getAttributeInstance(EntityAttributes.GENERIC_STEP_HEIGHT) ?: return;
    attributeInstance.removeModifier(uuid);
    attributeInstance.addPersistentModifier(
      EntityAttributeModifier(
        uuid,
        "hover_boots",
        0.4,
        EntityAttributeModifier.Operation.ADD_VALUE
      )
    )
    //entity.stepHeight = 1.0f
  }

  override fun onUnequip(stack: ItemStack, slot: SlotReference, entity: LivingEntity) {
    super.onUnequip(stack, slot, entity)
    entity.getAttributeInstance(EntityAttributes.GENERIC_STEP_HEIGHT)
      ?.removeModifier(UUID.fromString("c73b4792-b31e-4ab5-8b8b-5cfe9ab0496e"));
    //entity.stepHeight = 0.5f
  }

  companion object {
    private fun hoverBootsEquipped(entity: LivingEntity): Boolean {
      val component = TrinketsApi.getTrinketComponent(entity).orElse(null) ?: return false
      return component.isEquipped { it.item is HoverBootsItem }
    }

    @JvmStatic
    fun onLivingJump(entity: LivingEntity) {
      if (!hoverBootsEquipped(entity)) return

      if (entity.isSprinting) {
        entity.addVelocity(entity.velocity.x * 0.5, 0.4, entity.velocity.z * 0.5)
      } else {
        entity.addVelocity(0.0, 0.4, 0.0)
      }
    }

    @JvmStatic
    fun modifyFallDistance(entity: LivingEntity, fallDistance: Float): Float {
      if (fallDistance < 3 || !hoverBootsEquipped(entity)) return fallDistance
      return fallDistance * 0.3f
    }
  }
}
