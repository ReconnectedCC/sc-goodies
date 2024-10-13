package io.sc3.goodies.nature

import net.minecraft.block.SpreadableBlock
import net.minecraft.block.SnowyBlock
import com.mojang.serialization.MapCodec

class ScGrass(settings: Settings) : SpreadableBlock(settings) {
  companion object {
    val CODEC: MapCodec<ScGrass> = createCodec(::ScGrass);
  }

  override fun getCodec(): MapCodec<ScGrass> {
    return CODEC
  }
}
