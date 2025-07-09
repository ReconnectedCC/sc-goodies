package io.sc3.goodies.itemmagnet

import io.sc3.goodies.ScGoodies.modId
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW


object ItemMagnetHotkey {
  lateinit var toggleBinding: KeyBinding

  internal fun initEvents() {
    toggleBinding = KeyBindingHelper.registerKeyBinding(KeyBinding(
      "key.$modId.toggle_item_magnet",
      InputUtil.Type.KEYSYM,
      GLFW.GLFW_KEY_M,
      "category.$modId"
    ))

    ClientTickEvents.END_CLIENT_TICK.register { _ ->
      while (toggleBinding.wasPressed()) {
        ClientPlayNetworking.send(ToggleItemMagnetPacket.INSTANCE)
      }
    }
  }
}
