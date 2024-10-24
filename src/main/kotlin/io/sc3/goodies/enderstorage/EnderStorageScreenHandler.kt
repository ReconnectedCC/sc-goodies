package io.sc3.goodies.enderstorage

import com.mojang.serialization.MapCodec
import io.sc3.goodies.Registration.ModScreens
import io.sc3.goodies.enderstorage.EnderStorageBlockEntity.ScreenData
import io.sc3.goodies.util.ChestScreenHandler
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SimpleInventory
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.util.math.BlockPos

class EnderStorageScreenHandler(
  syncId: Int,
  playerInv: PlayerInventory,
  val screenData: ScreenData
) : ChestScreenHandler(syncId, playerInv, inv, ModScreens.enderStorage, rows = 3, yStart = 35, playerYStart = 49) {
  constructor(  syncId: Int,
                playerInv: PlayerInventory,
                inventory: Inventory,
                screenData: ScreenData) :
    this(
      syncId, playerInv, screenData, inv
    )

  override fun onClosed(player: PlayerEntity) {
    super.onClosed(player)

    val world = player.world
    if (player.world == null || player.world.isClient) return

    val be = world.getBlockEntity(screenData.pos) as? EnderStorageBlockEntity ?: return
    be.removeViewer(player)
  }
}
