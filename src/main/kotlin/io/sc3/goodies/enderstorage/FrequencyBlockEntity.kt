package io.sc3.goodies.enderstorage

import io.sc3.goodies.enderstorage.EnderStorageBlock.Companion.NBT_COMPUTER_CHANGES_ENABLED
import io.sc3.goodies.enderstorage.EnderStorageBlock.Companion.NBT_FREQUENCY
import io.sc3.goodies.enderstorage.EnderStorageBlock.Companion.NBT_TEMP_CRAFTING_PERSONAL
import io.sc3.goodies.util.BaseBlockEntity
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.util.math.BlockPos

abstract class FrequencyBlockEntity(
  type: BlockEntityType<*>,
  pos: BlockPos,
  state: BlockState
) : BaseBlockEntity(type, pos, state) {
  var frequency = Frequency()
    set(value) {
      if (field != value) {
        onFrequencyChange(field, value)
      }

      field = value
      onUpdate()
    }

  val frequencyState
    get() = EnderStorageProvider.getState(frequency)

  var computerChangesEnabled = false
    set(value) {
      field = value
      onUpdate()
    }

  override fun readNbt(nbt: NbtCompound) {
    super.readNbt(nbt)
    frequency = Frequency.fromNbt(nbt.getCompound(NBT_FREQUENCY), world?.server)
    computerChangesEnabled = nbt.getBoolean(NBT_COMPUTER_CHANGES_ENABLED)
  }

  override fun writeNbt(nbt: NbtCompound) {
    super.writeNbt(nbt)
    nbt.put(NBT_FREQUENCY, frequency.toNbt())
    nbt.putBoolean(NBT_COMPUTER_CHANGES_ENABLED, computerChangesEnabled)
  }

  override fun toInitialChunkDataNbt(): NbtCompound = createNbt()

  override fun toUpdatePacket(): Packet<ClientPlayPacketListener> =
    BlockEntityUpdateS2CPacket.create(this)

  open fun onUpdate() {
    markDirty()

    val world = world ?: return
    val state = world.getBlockState(pos)

    world.updateListeners(pos, state, state, Block.NOTIFY_ALL)
    world.updateNeighborsAlways(pos, state.block)
  }

  open fun onFrequencyChange(oldValue: Frequency, newValue: Frequency) {}
}
