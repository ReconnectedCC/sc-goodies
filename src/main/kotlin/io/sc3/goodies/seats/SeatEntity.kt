package io.sc3.goodies.seats

import io.sc3.library.ext.optUuid
import io.sc3.library.ext.putNullableUuid
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.*

class SeatEntity : Entity {
  private var ownerUuid: UUID? = null

  /** Try to get the current player sitting on this seat, either by the passenger entity, or our stored owner UUID. */
  val ownerDisplayName
    get() = firstPassenger?.displayName
      ?: ownerUuid?.let { world.server?.playerManager?.getPlayer(it)?.displayName }

  constructor(entityType: EntityType<out SeatEntity>, world: World) : super(entityType, world)

  constructor(
    entityType: EntityType<out SeatEntity>,
    world: World,
    seatPos: Vec3d,
    ownerUuid: UUID
  ) : super(entityType, world) {
    this.setPosition(seatPos)
    this.ownerUuid = ownerUuid
  }

  /**
   * The player sitting pose is halfway through their height, so subtract a bit more to align them with the SeatEntity.
   */
  override fun getMountedHeightOffset() = -0.3

  override fun tick() {
    super.tick()

    val passenger = firstPassenger as? PlayerEntity

    // If the block is no longer a valid seat, discard this seat entity
    val state = world.getBlockState(blockPos)
    if (!Seats.isBlockSeat(world, blockPos, state, null, passenger)
      || (!world.isClient && !Seats.invokeCanSitEvent(world, blockPos, state, passenger).isAccepted)) {
      passenger?.stopRiding()
      discard()
      return
    }

    if (!world.isClient || (passenger != null && passenger.isMainPlayer)) {
      if (passenger == null
        || passenger.world != world
        || !passenger.pos.isInRange(pos, 2.0)
        || !passenger.isAlive) {
        passenger?.stopRiding()
        discard()
        return
      }
    }
  }

  override fun initDataTracker() {}

  override fun readCustomDataFromNbt(nbt: NbtCompound) {
    ownerUuid = nbt.optUuid("Owner")
  }

  override fun writeCustomDataToNbt(nbt: NbtCompound) {
    nbt.putNullableUuid("Owner", ownerUuid)
  }
}
