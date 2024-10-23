package io.sc3.goodies.itemmagnet

import net.minecraft.network.PacketByteBuf
import io.sc3.library.networking.ScLibraryPacket
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.packet.CustomPayload

class ToggleItemMagnetPacket(override val payload: CustomPayload) : ScLibraryPacket() {
  override val id: CustomPayload.Id<CustomPayload> = ToggleItemMagnetPacket.id

  companion object {
    val id: CustomPayload.Id<CustomPayload> = CustomPayload.id<CustomPayload>("toggle_item_magnet")
    fun fromBytes(payload: CustomPayload): ToggleItemMagnetPacket {
      return ToggleItemMagnetPacket(payload)
    }
  }

  override fun onServerReceive(payload: CustomPayload, ctx: ServerPlayNetworking.Context) {
    super.onServerReceive(payload, ctx)
    ItemMagnetState.onPlayerToggle(ctx.player())
  }
}
