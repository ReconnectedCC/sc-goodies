package io.sc3.goodies.ironstorage

import io.sc3.goodies.util.ChestUtil
import net.minecraft.block.BarrelBlock
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.LootableContainerBlockEntity
import net.minecraft.block.entity.ViewerCountManager
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventories
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.RegistryWrapper
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.text.Text.translatable
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class IronBarrelBlockEntity(
  private val variant: IronStorageVariant,
  pos: BlockPos,
  state: BlockState,
) : LootableContainerBlockEntity(variant.barrelBlockEntityType, pos, state) {
  private var inv: DefaultedList<ItemStack> =
    DefaultedList.ofSize(variant.size, ItemStack.EMPTY)

  val stateManager = object : ViewerCountManager() {
    override fun onContainerOpen(world: World, pos: BlockPos, state: BlockState) {
      ChestUtil.playSound(world, pos, SoundEvents.BLOCK_BARREL_OPEN)
      setOpen(state, true)
    }

    override fun onContainerClose(world: World, pos: BlockPos, state: BlockState) {
      ChestUtil.playSound(world, pos, SoundEvents.BLOCK_BARREL_CLOSE)
      setOpen(state, false)
    }

    override fun onViewerCountUpdate(world: World, pos: BlockPos, state: BlockState, oldViewerCount: Int,
                                     newViewerCount: Int) {
    }

    override fun isPlayerViewing(player: PlayerEntity): Boolean {
      val screen = player.currentScreenHandler as? IronChestScreenHandler ?: return false
      return screen.inv == this@IronBarrelBlockEntity
    }
  }

  override fun size() = variant.size

  override fun getContainerName(): Text =
    translatable("block.sc-goodies.${variant.shulkerId}")

  override fun readNbt(nbt: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
    super.readNbt(nbt, registryLookup)
    inv = DefaultedList.ofSize(variant.size, ItemStack.EMPTY)
    if (!super.readLootTable(nbt)) {
      Inventories.readNbt(nbt, inv, registryLookup)
    }
  }

  override fun writeNbt(nbt: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
    super.writeNbt(nbt, registryLookup)
    if (!super.writeLootTable(nbt)) {
      Inventories.writeNbt(nbt, inv, registryLookup)
    }
  }

  override fun createScreenHandler(syncId: Int, playerInventory: PlayerInventory) =
    IronChestScreenHandler(variant, syncId, playerInventory, this)

  override fun getHeldStacks() = inv

  override fun setHeldStacks(list: DefaultedList<ItemStack>) {
    inv = list
  }

  override fun onOpen(player: PlayerEntity) {
    if (!removed && !player.isSpectator) {
      stateManager.openContainer(player, world, pos, cachedState)
    }
  }

  override fun onClose(player: PlayerEntity) {
    if (!removed && !player.isSpectator) {
      stateManager.closeContainer(player, world, pos, cachedState)
    }
  }

  fun setOpen(state: BlockState, open: Boolean) {
    world?.setBlockState(pos, state.with(BarrelBlock.OPEN, open), Block.NOTIFY_ALL)
  }

  companion object {
    fun scheduledTick(world: World, pos: BlockPos) {
      val be = world.getBlockEntity(pos) as? IronBarrelBlockEntity ?: return
      if (!be.removed) {
        be.stateManager.updateViewerCount(be.world, be.pos, be.cachedState)
      }
    }
  }
}
