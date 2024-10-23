package io.sc3.goodies.enderstorage

import io.sc3.goodies.ScGoodies.modId
import net.minecraft.datafixer.DataFixTypes
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.server.MinecraftServer
import net.minecraft.util.collection.DefaultedList
import net.minecraft.world.PersistentState
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

object EnderStorageProvider {
  private val log = LoggerFactory.getLogger("ScGoodies/EnderStorageProvider")!!

  const val INVENTORY_SIZE = 27

  lateinit var state: EnderStorageState

  private fun createState(): EnderStorageState {
    val state = EnderStorageState()
    state.markDirty()
    return state
  }

  fun getInventory(
    server: MinecraftServer,
    frequency: Frequency,
    be: EnderStorageBlockEntity? = null,
    create: Boolean = true
  ): EnderStorageInventory? {
    // NB: We can execute this task on the server thread and re-join later, but I think it's probably better to leave it
    // to the caller to ensure they are only running this on the server thread.
    if (!server.isOnThread) {
      throw IllegalStateException("Not on server thread!")
    }

    state = server.overworld.persistentStateManager.getOrCreate(
      PersistentState.Type(this::createState, { nbt, _ -> EnderStorageState.fromNbt(server, nbt) }, DataFixTypes.LEVEL),
      "$modId-ender-storage"
    )

    val inv = if (create) {
      state.inventories.computeIfAbsent(frequency) { EnderStorageInventory(server) }
    } else {
      state.inventories[frequency] ?: return null
    }

    be?.let { inv.addBlockEntity(it) }
    return inv
  }

  fun getState(frequency: Frequency): FrequencyState {
    return state.states.computeIfAbsent(frequency) { FrequencyState() }
  }

  class EnderStorageInventory(private val server: MinecraftServer) : Inventory {
    val items: DefaultedList<ItemStack> = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY)

    /**
     * Use a concurrent hash set here. Even though this is only ever accessed on the server thread, it's still possible
     * for a call to `markDirty` to load adjacent chunks (during `updateComparators`, even though there's guards against
     * chunk loading in there?!). This would cause a CME if the adjacent chunk had an Ender Storage in it, which would
     * call [EnderStorageBlockEntity]'s BLOCK_ENTITY_LOAD, and in turn add a block entity to this list while it's being
     * iterated over. The concurrent set will only operate on a snapshot of the data, so worst case, a comparator just
     * might not get updated correctly during a chunk load.
     */
    private val blockEntities = ConcurrentHashMap.newKeySet<EnderStorageBlockEntity>()
    var viewerCount = 0

    override fun clear() {
      items.clear()
    }

    override fun size() = INVENTORY_SIZE

    override fun isEmpty() = items.all { it.isEmpty }

    override fun getStack(slot: Int) = items[slot]

    override fun removeStack(slot: Int, amount: Int): ItemStack {
      val result = Inventories.splitStack(items, slot, amount)
      if (!result.isEmpty) markDirty()
      return result
    }

    override fun removeStack(slot: Int): ItemStack = Inventories.removeStack(items, slot)

    override fun setStack(slot: Int, stack: ItemStack) {
      items[slot] = stack
      if (stack.count > stack.maxCount) {
        stack.count = stack.maxCount
      }
    }

    override fun markDirty() {
      checkOnThread()
      state.markDirty()
      blockEntities.forEach {
        if (!it.isRemoved) {
          it.markDirty()
        }
      }
    }

    override fun canPlayerUse(player: PlayerEntity) = true

    override fun onOpen(player: PlayerEntity) {
      if (!player.isSpectator) {
        viewerCount++
        updateViewers()
      }
    }

    override fun onClose(player: PlayerEntity) {
      if (!player.isSpectator) {
        viewerCount--
        updateViewers()
      }
    }

    fun updateViewers() {
      checkOnThread()
      blockEntities.forEach {
        if (!it.isRemoved) {
          it.updateViewerCount()
          it.world?.scheduleBlockTick(it.pos, it.cachedState.block, 5)
        }
      }
    }

    fun addBlockEntity(be: EnderStorageBlockEntity) {
      checkOnThread()
      blockEntities.add(be)
    }

    fun removeBlockEntity(be: EnderStorageBlockEntity) {
      checkOnThread()
      blockEntities.remove(be)
    }

    fun snapshotBlockEntities(): Set<EnderStorageBlockEntity> {
      checkOnThread()
      return blockEntities.toSet()
    }

    private fun checkOnThread() {
      if (!server.isOnThread && !server.isStopping) {
        // Don't throw an exception here, but log it, so we can track down the cause later
        log.error("EnderStorageInventory blockEntities accessed off-thread!", RuntimeException())
      }
    }
  }
}
