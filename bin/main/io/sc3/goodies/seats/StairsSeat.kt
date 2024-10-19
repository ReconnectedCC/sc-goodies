package io.sc3.goodies.seats

import io.sc3.library.SeatBlock
import net.minecraft.block.BlockState
import net.minecraft.block.StairsBlock
import net.minecraft.block.enums.BlockHalf
import net.minecraft.block.enums.StairShape
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

object StairsSeat : SeatBlock {
  private val StairShape.isInnerCorner: Boolean
    get() = this == StairShape.INNER_LEFT || this == StairShape.INNER_RIGHT

  override fun canSitOn(
    world: World,
    pos: BlockPos,
    state: BlockState,
    hitResult: BlockHitResult?,
    player: PlayerEntity?
  ): ActionResult {
    return if (state.get(StairsBlock.HALF) == BlockHalf.BOTTOM
      && !state.get(StairsBlock.SHAPE).isInnerCorner // TODO: Figure out the required offsets for corner stairs
      && (hitResult == null || !state.isSideSolidFullSquare(world, pos, hitResult.side))) {
      ActionResult.SUCCESS
    } else {
      ActionResult.PASS
    }
  }

  override fun getSeatPos(world: World, pos: BlockPos, state: BlockState): Vec3d {
    // Slightly offset the player forwards
    val facingVector = state.get(StairsBlock.FACING).opposite.vector
    return pos.toCenterPos().add(facingVector.x * 0.25, 0.0, facingVector.z * 0.25)
  }
}
