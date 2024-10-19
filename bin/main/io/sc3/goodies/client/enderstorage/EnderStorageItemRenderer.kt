package io.sc3.goodies.client.enderstorage

import io.sc3.goodies.client.enderstorage.EnderStorageBlockEntityRenderer.Companion.renderChest
import io.sc3.goodies.enderstorage.EnderStorageBlock.Companion.NBT_COMPUTER_CHANGES_ENABLED
import io.sc3.goodies.enderstorage.EnderStorageBlock.Companion.NBT_TEMP_CRAFTING_PERSONAL
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
    val nbt = BlockItem.getBlockEntityNbt(stack)
    val frequency = Frequency.fromStack(stack) ?: defaultFrequency
    val personal = frequency.personal || (nbt?.getBoolean(NBT_TEMP_CRAFTING_PERSONAL) ?: false)
    val changesEnabled = BlockItem.getBlockEntityNbt(stack)?.getBoolean(NBT_COMPUTER_CHANGES_ENABLED) ?: false
    renderChest(matrices, vertexConsumers, Direction.NORTH, 0.0f, frequency, changesEnabled, light, overlay, personal)
  }
}
