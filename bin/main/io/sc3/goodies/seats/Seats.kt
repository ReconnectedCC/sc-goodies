package io.sc3.goodies.seats

import io.sc3.goodies.Registration.ModEntities.seatEntity
import io.sc3.goodies.ScGoodies.modId
import io.sc3.library.SeatBlock
import io.sc3.library.ext.event
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text.translatable
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.world.World

/** If a sitting player is this far away from the seat, they will be dismounted. */
const val MIN_SIT_DISTANCE = 1.0
/** If a player is this far away from a seat, they will not be able to sit on it. */
const val MAX_SIT_DISTANCE = 2.0

object Seats {
  @JvmField
  val CAN_SIT_EVENT = event<(
    world: ServerWorld,
    pos: BlockPos,
    blockState: BlockState,
    player: ServerPlayerEntity
  ) -> ActionResult> { cb ->
    iter@{ world, pos, blockState, player ->
      cb.forEach {
        val result = it(world, pos, blockState, player)
        if (result != ActionResult.PASS) return@iter result
      }
      ActionResult.SUCCESS // Default to success if no listeners
    }
  }

  internal fun init() {
    UseBlockCallback.EVENT.register(::onUseBlock)
  }

  private fun onUseBlock(player: PlayerEntity, world: World, hand: Hand, hitResult: BlockHitResult): ActionResult {
    // Initial checks - if the player is sneaking, already mounted, holding anything, or a spectator, don't do anything
    // TODO: Check both hands?
    if (player.isSpectator
      || player.isSneaking
      || player.hasVehicle()
      || !player.getStackInHand(hand).isEmpty) {
      return ActionResult.PASS
    }

    val pos = hitResult.blockPos
    if (!pos.isWithinDistance(player.pos, MAX_SIT_DISTANCE)) {
      return ActionResult.PASS
    }

    // Basic seat check - check if the block is a seat, and it can be sat on
    val blockState = world.getBlockState(pos)
    if (!isBlockSeat(world, pos, blockState, hitResult, player)) {
      return ActionResult.PASS
    }

    if (world.isClient) return ActionResult.SUCCESS // Pass the event to the server

    // Server-only seat check - allow other mods to cancel the event
    if (!invokeCanSitEvent(world, pos, blockState, player).isAccepted) {
      return ActionResult.FAIL
    }

    // Check there are no other seats at the position
    world.getEntitiesByType(seatEntity, Box(pos)) { true }.firstOrNull()?.let { seat ->
      val name = seat.ownerDisplayName

      // Send a message to the player that the seat is already occupied
      if (name != null) {
        player.sendMessage(translatable("block.$modId.seat.occupied.player", name), true)
      } else {
        player.sendMessage(translatable("block.$modId.seat.occupied.unknown"), true)
      }

      return ActionResult.FAIL
    }

    // Create the seat entity and have the player mount it
    val seatPos = (blockState.block as SeatBlock).getSeatPos(world, pos, blockState)
    val seat = SeatEntity(seatEntity, world, seatPos, player.uuid)
    world.spawnEntity(seat)
    player.startRiding(seat)

    return ActionResult.SUCCESS
  }

  /** Client and server check to see if the block at the given position is a seat block. */
  fun isBlockSeat(
    world: World,
    pos: BlockPos,
    blockState: BlockState,
    hitResult: BlockHitResult?,
    player: PlayerEntity?
  ): Boolean {
    val block = blockState.block
    if (block !is SeatBlock) return false
    return block.canSitOn(world, pos, blockState, hitResult, player).isAccepted && isNothingAbove(world, pos)
  }

  private fun isNothingAbove(world: World, pos: BlockPos): Boolean {
    val above = pos.up()
    val state = world.getBlockState(above)
    return state.getCollisionShape(world, above).isEmpty // this will internally check AbstractBlock.Settings.collidable
  }

  /** Server-only check to see if the player can sit on the block at the given position. */
  fun invokeCanSitEvent(world: World, pos: BlockPos, blockState: BlockState, player: PlayerEntity?): ActionResult {
    if (player == null || world.isClient) return ActionResult.PASS
    val serverWorld = world as ServerWorld
    val serverPlayer = player as ServerPlayerEntity
    val result = CAN_SIT_EVENT.invoker().invoke(serverWorld, pos, blockState, serverPlayer)
    return result
  }
}
