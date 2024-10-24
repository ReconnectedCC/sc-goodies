package io.sc3.goodies.enderstorage

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.JsonSerializer
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.sc3.goodies.Registration
import io.sc3.goodies.Registration.ModBlocks
import io.sc3.goodies.ScGoodies.modId
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.server.MinecraftServer
import net.minecraft.text.Text
import net.minecraft.text.Text.translatable
import net.minecraft.util.DyeColor
import net.minecraft.util.Formatting
import net.minecraft.util.Uuids
import net.minecraft.util.math.BlockPos
import java.util.*
import kotlin.jvm.optionals.getOrNull


data class Frequency(
  val owner: Optional<UUID> = Optional.empty(),

  val ownerName: Optional<String> = Optional.empty(),

  val left  : DyeColor = DyeColor.WHITE,
  val middle: DyeColor = DyeColor.WHITE,
  val right : DyeColor = DyeColor.WHITE
) {
  val personal
    get() = !owner.isEmpty

  @delegate:Transient
  val ownerText: Text by lazy {
    val key = ModBlocks.enderStorage.translationKey
    if (personal) {
      translatable("$key.owner_name", ownerName.getOrNull())
    } else {
      translatable("$key.public")
    }
  }

  fun toNbt(): NbtCompound {
    val nbt = NbtCompound()
    if (!owner.isEmpty) {
      nbt.putUuid("owner", owner.get())
      ownerName.getOrNull()?.let { nbt.putString("ownerName", it) }
    }
    nbt.putByte("left", left.id.toByte())
    nbt.putByte("middle", middle.id.toByte())
    nbt.putByte("right", right.id.toByte())
    return nbt
  }

  fun toPacket(buf: PacketByteBuf) {
    buf.writeNullable(owner.get(), PacketByteBuf::writeUuid)
    buf.writeNullable(ownerName.get(), PacketByteBuf::writeString)
    buf.writeEnumConstant(left)
    buf.writeEnumConstant(middle)
    buf.writeEnumConstant(right)
  }

  fun toTextParts(vararg formatting: Formatting): Array<Text> {
    val key = "block.$modId.ender_storage.frequency"
    return arrayOf(
      translatable("$key.${left.getName()}").formatted(*formatting),
      translatable("$key.${middle.getName()}").formatted(*formatting),
      translatable("$key.${right.getName()}").formatted(*formatting),
    )
  }

  fun toText(): Text = translatable(
    "block.$modId.ender_storage.frequency",
    *toTextParts()
  )

  fun dyeColor(index: Int): DyeColor = when (index) {
    0 -> left
    1 -> middle
    2 -> right
    else -> throw IllegalArgumentException("Invalid index $index")
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Frequency

    if (owner != other.owner) return false
    if (left != other.left) return false
    if (middle != other.middle) return false
    if (right != other.right) return false

    return true
  }

  override fun hashCode(): Int {
    var result = owner.getOrNull().hashCode()
    result = 31 * result + left.hashCode()
    result = 31 * result + middle.hashCode()
    result = 31 * result + right.hashCode()
    return result
  }

  fun getCodec(): MapCodec<Frequency> {
    return CODEC
  }
  fun getPacketCodec(): PacketCodec<RegistryByteBuf, Frequency> {
    return PACKET_CODEC
  }
  fun toJson(): String? {
    val result = CODEC.codec().encodeStart(JsonOps.INSTANCE, this).result();
    if(result.isEmpty) {
      return null;
    }

    return Gson().toJson(result.get())
  }

  companion object {
    fun fromJson(str: String): Frequency? {
      val result = CODEC.codec().decode(JsonOps.INSTANCE, JsonParser().parse(str)).result();
      if(result.isEmpty) {
        return null;
      }

      return result.get().first
    }
    val CODEC: MapCodec<Frequency> = RecordCodecBuilder.mapCodec { i ->
      i.group(
        Uuids.CODEC.optionalFieldOf("owner").forGetter(Frequency::owner),
        Codec.STRING.optionalFieldOf("ownerName").forGetter(Frequency::ownerName),
        DyeColor.CODEC.fieldOf("left").forGetter(Frequency::left),
        DyeColor.CODEC.fieldOf("middle").forGetter(Frequency::middle),
        DyeColor.CODEC.fieldOf("right").forGetter(Frequency::right)
      ).apply(i, ::Frequency)
    }

    val PACKET_CODEC: PacketCodec<RegistryByteBuf, Frequency> =
      PacketCodec.tuple(
        PacketCodecs.optional(Uuids.PACKET_CODEC), Frequency::owner,
        PacketCodecs.optional(PacketCodecs.STRING), Frequency::ownerName,

        DyeColor.PACKET_CODEC, Frequency::left,
        DyeColor.PACKET_CODEC, Frequency::middle,
        DyeColor.PACKET_CODEC, Frequency::right,
        ::Frequency
      )
    fun fromNbt(nbt: NbtCompound, server: MinecraftServer? = null): Frequency {
      val owner = if (nbt.containsUuid("owner")) nbt.getUuid("owner") else null

      val ownerName = if (nbt.contains("ownerName")) {
        nbt.getString("ownerName")
      } else if (owner != null && server != null) {
        server.userCache?.getByUuid(owner)?.orElse(null)?.name
      } else {
        null
      }

      return Frequency(
        Optional.ofNullable(owner),
        Optional.ofNullable(ownerName),
        DyeColor.byId(nbt.getByte("left").toInt()),
        DyeColor.byId(nbt.getByte("middle").toInt()),
        DyeColor.byId(nbt.getByte("right").toInt())
      )
    }

    fun fromPacket(buf: PacketByteBuf) = Frequency(
      owner     = Optional.ofNullable(buf.readNullable(PacketByteBuf::readUuid)),
      ownerName = Optional.ofNullable(buf.readNullable(PacketByteBuf::readString)),
      left      = buf.readEnumConstant(DyeColor::class.java),
      middle    = buf.readEnumConstant(DyeColor::class.java),
      right     = buf.readEnumConstant(DyeColor::class.java)
    )


    fun fromStack(stack: ItemStack): Frequency? {
      return stack.get(Registration.ModComponents.FREQUENCY)
    }
  }
}
