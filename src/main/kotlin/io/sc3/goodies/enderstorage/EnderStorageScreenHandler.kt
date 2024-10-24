package io.sc3.goodies.enderstorage

import io.sc3.goodies.Registration.ModScreens
import io.sc3.goodies.enderstorage.EnderStorageBlockEntity.ScreenData
import io.sc3.goodies.util.ChestScreenHandler
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SimpleInventory

class EnderStorageScreenHandler : ChestScreenHandler {
  val screenData: ScreenData

  // Logical client
  constructor(
    syncId: Int,
    playerInv: PlayerInventory,
    screenData: ScreenData
  ) : super(syncId, playerInv, SimpleInventory(3 * 9), ModScreens.enderStorage, rows = 3, yStart = 35, playerYStart = 49) {
    this.screenData = screenData
  }

  // Logical server
  constructor(
    syncId: Int,
    playerInv: PlayerInventory,
    inventory: Inventory,
    screenData: ScreenData
  ) : super(syncId, playerInv, inventory, ModScreens.enderStorage, rows = 3, yStart = 35, playerYStart = 49) {
    this.screenData = screenData
  }

  override fun onClosed(player: PlayerEntity) {
    super.onClosed(player)

    val world = player.world
    if (player.world == null || player.world.isClient) return

    val be = world.getBlockEntity(screenData.pos) as? EnderStorageBlockEntity ?: return
    be.removeViewer(player)
  }
}
