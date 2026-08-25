package io.sc3.goodies.enderstorage

import io.sc3.goodies.Registration.ModComponents
import io.sc3.goodies.enderstorage.EnderStorageStackData.COMPUTER_CHANGES_ENABLED_NBT_KEY
import io.sc3.goodies.enderstorage.EnderStorageStackData.FREQUENCY_NBT_KEY
import io.sc3.goodies.enderstorage.EnderStorageStackData.LEGACY_COMPUTER_CHANGES_ENABLED_NBT_KEY
import io.sc3.goodies.util.BaseBlockEntity
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.component.ComponentMap
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
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
    frequency = Frequency.fromNbt(nbt.getCompound(FREQUENCY_NBT_KEY), world?.server)
    computerChangesEnabled = when {
      nbt.contains(COMPUTER_CHANGES_ENABLED_NBT_KEY, NbtElement.NUMBER_TYPE.toInt()) ->
        nbt.getBoolean(COMPUTER_CHANGES_ENABLED_NBT_KEY)
      else -> nbt.getBoolean(LEGACY_COMPUTER_CHANGES_ENABLED_NBT_KEY)
    }
  }

  override fun writeNbt(nbt: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
    super.writeNbt(nbt, registryLookup)
    nbt.put(FREQUENCY_NBT_KEY, frequency.toNbt())
    nbt.putBoolean(COMPUTER_CHANGES_ENABLED_NBT_KEY, computerChangesEnabled)
  }

  override fun readComponents(components: BlockEntity.ComponentsAccess) {
    super.readComponents(components)
    components.get(ModComponents.FREQUENCY)?.let { frequency = it }
    components.get(ModComponents.COMPUTER_CHANGES_ENABLED)?.let { computerChangesEnabled = it }
  }

  override fun addComponents(builder: ComponentMap.Builder) {
    super.addComponents(builder)
    builder.add(ModComponents.FREQUENCY, frequency)
    builder.add(ModComponents.COMPUTER_CHANGES_ENABLED, computerChangesEnabled)
  }

  @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
  override fun removeFromCopiedStackNbt(nbt: NbtCompound) {
    super.removeFromCopiedStackNbt(nbt)
    nbt.remove(FREQUENCY_NBT_KEY)
    nbt.remove(COMPUTER_CHANGES_ENABLED_NBT_KEY)
    nbt.remove(LEGACY_COMPUTER_CHANGES_ENABLED_NBT_KEY)
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
