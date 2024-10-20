package io.sc3.goodies

import io.sc3.goodies.seats.Seats
import net.fabricmc.api.ModInitializer
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory

object ScGoodies : ModInitializer {
  @JvmField
  internal val log = LoggerFactory.getLogger("ScGoodies")!!

  internal const val modId = "sc-goodies"
  internal fun ModId(value: String) = Identifier(modId, value)

  override fun onInitialize() {
    log.info("sc-goodies initializing")

    Registration.init()
    Seats.init()
  }


  fun <T : BlockEntity?, P : BlockEntity?> checkTypeForTicker(
    placedType: BlockEntityType<P>,
    tickerType: BlockEntityType<T>,
    ticker: BlockEntityTicker<in T>?
  ): BlockEntityTicker<P>? {
    return if (tickerType === placedType) ticker as BlockEntityTicker<P> else null
  }
}
