package io.sc3.goodies.itemmagnet

import io.netty.buffer.ByteBuf
import io.sc3.goodies.ScGoodies
import io.sc3.library.networking.ScLibraryPacket
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload

class ToggleItemMagnetPacket : ScLibraryPacket() {
  override fun getId(): CustomPayload.Id<ToggleItemMagnetPacket> {
    return ToggleItemMagnetPacket.id
  }

  override fun onClientReceive(ctx: ClientPlayNetworking.Context) {}

  override fun onServerReceive(ctx: ServerPlayNetworking.Context) {
    ItemMagnetState.onPlayerToggle(ctx.player())
  }

  companion object {
    val INSTANCE = ToggleItemMagnetPacket()
    val CODEC: PacketCodec<ByteBuf, ToggleItemMagnetPacket> = PacketCodec.unit(INSTANCE)
    val id: CustomPayload.Id<ToggleItemMagnetPacket> = CustomPayload.Id(ScGoodies.ModId("toggle_item_magnet"))
  }
}
