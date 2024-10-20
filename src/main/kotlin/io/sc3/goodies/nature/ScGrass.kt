package io.sc3.goodies.nature

import com.mojang.serialization.MapCodec
import net.minecraft.block.SpreadableBlock

class ScGrass(settings: Settings) : SpreadableBlock(settings) {
  val CODEC: MapCodec<ScGrass> = createCodec(::ScGrass)

  override fun getCodec(): MapCodec<out SpreadableBlock> {
    return CODEC
  }
}
