package pw.switchcraft.goodies.elytra

import net.minecraft.util.DyeColor
import net.minecraft.util.DyeColor.*
import net.minecraft.util.registry.Registry
import pw.switchcraft.goodies.ScGoodies.ModId

enum class SpecialElytraType(val type: String, val recipeColors: List<DyeColor>) {
  LESBIAN("lesbian", listOf(RED, ORANGE, WHITE, PINK, MAGENTA)),
  NON_BINARY("non_binary", listOf(YELLOW, WHITE, PURPLE, BLACK)),
  PRIDE("pride", listOf(RED, ORANGE, YELLOW, GREEN, BLUE, PURPLE)),
  TRANS("trans", listOf(LIGHT_BLUE, PINK, WHITE));

  val modelTexture = ModId("textures/entity/elytra/elytra_$type.png")

  val item: SpecialElytraItem by lazy {
    Registry.ITEM.get(ModId("elytra_$type")) as SpecialElytraItem
  }
}
