package io.sc3.goodies.enderstorage

import io.sc3.goodies.Registration
import io.sc3.goodies.util.BaseBlockEntity
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.registry.RegistryWrapper
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

  override fun readNbt(nbt: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
    super.readNbt(nbt, registryLookup)
    frequency = Frequency.fromNbt(nbt.getCompound("frequency"), world?.server)
    computerChangesEnabled = nbt.getBoolean("computer_changes_enabled")
  }

  override fun writeNbt(nbt: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
    super.readNbt(nbt, registryLookup)
    nbt.put("frequency", frequency.toNbt())
    nbt.putBoolean("computer_changes_enabled", computerChangesEnabled)
  }

  override fun toInitialChunkDataNbt(registryLookup: RegistryWrapper.WrapperLookup): NbtCompound = createNbt(registryLookup)

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
