package io.sc3.goodies.seats

import net.minecraft.client.render.Frustum
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier

class SeatEntityRenderer(ctx: EntityRendererFactory.Context) : EntityRenderer<SeatEntity>(ctx) {
  override fun getTexture(entity: SeatEntity): Identifier? = null
  override fun shouldRender(entity: SeatEntity, frustum: Frustum?, x: Double, y: Double, z: Double) = false
  override fun render(entity: SeatEntity, yaw: Float, tickDelta: Float, matrices: MatrixStack?,
    vertexConsumers: VertexConsumerProvider, light: Int) {}
}
