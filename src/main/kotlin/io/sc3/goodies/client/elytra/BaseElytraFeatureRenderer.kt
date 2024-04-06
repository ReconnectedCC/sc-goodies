package io.sc3.goodies.client.elytra

import io.sc3.goodies.ScGoodies.ModId
import io.sc3.goodies.elytra.BaseElytraItem
import io.sc3.goodies.elytra.DyedElytraItem
import io.sc3.goodies.elytra.SpecialElytraItem
import io.sc3.library.ext.event
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.feature.FeatureRenderer
import net.minecraft.client.render.entity.feature.FeatureRendererContext
import net.minecraft.client.render.entity.model.ElytraEntityModel
import net.minecraft.client.render.entity.model.EntityModelLayers
import net.minecraft.client.render.entity.model.EntityModelLoader
import net.minecraft.client.render.entity.model.PlayerEntityModel
import net.minecraft.client.render.item.ItemRenderer.getArmorGlintConsumer
import net.minecraft.client.texture.MissingSprite
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.EquipmentSlot
import net.minecraft.util.DyeColor
import net.minecraft.util.Identifier

class BaseElytraFeatureRenderer(
  ctx: FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>>,
  loader: EntityModelLoader
) : FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>>(ctx) {
  private val dyedTextures = DyeColor.entries.associateWith {
    ModId("textures/entity/elytra/elytra_${it.getName()}.png")
  }

  private val elytra = ElytraEntityModel<AbstractClientPlayerEntity>(loader.getModelPart(EntityModelLayers.ELYTRA))

  private fun elytraEventTexture(player: AbstractClientPlayerEntity, item: BaseElytraItem): Identifier? =
    ELYTRA_TEXTURE_EVENT.invoker().invoke(player, item)

  private fun elytraBaseTexture(player: AbstractClientPlayerEntity, item: BaseElytraItem): Identifier =
    when (item) {
      is SpecialElytraItem -> item.type.modelTexture
      is DyedElytraItem -> dyedTextures[item.color]!!
      else -> MissingSprite.getMissingSpriteId()
    }

  private fun elytraModel(
    player: AbstractClientPlayerEntity,
    texture: Identifier?
  ): ElytraEntityModel<AbstractClientPlayerEntity> =
    ELYTRA_MODEL_EVENT.invoker().invoke(player, texture) ?: elytra

  override fun render(matrices: MatrixStack, consumers: VertexConsumerProvider, light: Int,
                      player: AbstractClientPlayerEntity, limbAngle: Float, limbDistance: Float, tickDelta: Float,
                      animationProgress: Float, headYaw: Float, headPitch: Float) {
    val stack = player.getEquippedStack(EquipmentSlot.CHEST)
    val item = stack.item
    if (item !is BaseElytraItem) return

    val eventTexture = elytraEventTexture(player, item)
    val model = elytraModel(player, eventTexture)
    val texture = eventTexture ?: elytraBaseTexture(player, item)

    matrices.push()
    matrices.translate(0.0, 0.0, 0.125)

    contextModel.copyStateTo(model)
    model.setAngles(player, limbAngle, limbDistance, animationProgress, headYaw, headPitch)

    val consumer = getArmorGlintConsumer(consumers, RenderLayer.getArmorCutoutNoCull(texture), false, stack.hasGlint())
    model.render(matrices, consumer, light, OverlayTexture.DEFAULT_UV, 1.0f, 1.0f, 1.0f, 1.0f)

    matrices.pop()
  }

  companion object {
    val ELYTRA_TEXTURE_EVENT = event<(player: AbstractClientPlayerEntity, item: BaseElytraItem) -> Identifier?> { cb ->
      { entity, item -> cb.firstNotNullOfOrNull { it(entity, item) }}
    }

    val ELYTRA_MODEL_EVENT = event<(
      player: AbstractClientPlayerEntity,
      texture: Identifier?
    ) -> ElytraEntityModel<AbstractClientPlayerEntity>?> { cb ->
      { entity, texture -> cb.firstNotNullOfOrNull { it(entity, texture) }}
    }
  }
}
