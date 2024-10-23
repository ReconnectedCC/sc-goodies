package io.sc3.goodies.client.enderstorage

import io.sc3.goodies.Registration
import io.sc3.goodies.client.enderstorage.EnderStorageBlockEntityRenderer.Companion.renderChest
import io.sc3.goodies.enderstorage.Frequency
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry.DynamicItemRenderer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.util.math.Direction

object EnderStorageItemRenderer : DynamicItemRenderer {
  private val defaultFrequency = Frequency()

  override fun render(stack: ItemStack, mode: ModelTransformationMode, matrices: MatrixStack,
                      vertexConsumers: VertexConsumerProvider, light: Int, overlay: Int) {
    val frequency = Frequency.fromStack(stack) ?: defaultFrequency
    val personal = frequency.personal || (stack.get(Registration.ModComponents.TEMP_CRAFTING_PERSONAL) ?: false)
    val changesEnabled = stack.get(Registration.ModComponents.COMPUTER_CHANGES_ENABLED) ?: false
    renderChest(matrices, vertexConsumers, Direction.NORTH, 0.0f, frequency, changesEnabled, light, overlay, personal)
  }
}
