package io.sc3.goodies.itemmagnet

import com.mojang.serialization.Codec
import io.netty.buffer.ByteBuf
import io.sc3.library.networking.ScLibraryPacket
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload

class ToggleItemMagnetPacket(override val payload: CustomPayload) : ScLibraryPacket() {
  override val id: CustomPayload.Id<CustomPayload> = ToggleItemMagnetPacket.id

  companion object {
    val INSTANCE = ToggleItemMagnetPacket { id };
    val CODEC: PacketCodec<ByteBuf, CustomPayload> = PacketCodecs.codec(Codec.unit(INSTANCE.payload));
    val id: CustomPayload.Id<CustomPayload> = CustomPayload.id("toggle_item_magnet")

    fun fromBytes(payload: CustomPayload): ToggleItemMagnetPacket {
      return ToggleItemMagnetPacket(payload)
    }
  }

  override fun onServerReceive(payload: CustomPayload, ctx: ServerPlayNetworking.Context) {
    super.onServerReceive(payload, ctx)
    ItemMagnetState.onPlayerToggle(ctx.player())
  }
}
