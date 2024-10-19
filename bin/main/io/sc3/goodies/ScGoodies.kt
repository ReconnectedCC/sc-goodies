package io.sc3.goodies

import io.sc3.goodies.seats.Seats
import net.fabricmc.api.ModInitializer
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
}
