package io.sc3.goodies

import io.sc3.goodies.ScGoodies.ModId

class Tags {
    val CONCRETE: net.minecraft.registry.tag.TagKey<net.minecraft.item.Item> = register("concrete")

    val ELYTRA: net.minecraft.registry.tag.TagKey<net.minecraft.item.Item> = register("elytra")
    val SHARK: net.minecraft.registry.tag.TagKey<net.minecraft.item.Item> = register("shark")

    val ANY_IRON_SHULKER_BOX: net.minecraft.registry.tag.TagKey<net.minecraft.item.Item> = register("iron_shulker")
    val IRON_SHULKER_BOX: net.minecraft.registry.tag.TagKey<net.minecraft.item.Item> = register("iron_shulker/iron")
    val GOLD_SHULKER_BOX: net.minecraft.registry.tag.TagKey<net.minecraft.item.Item> = register("iron_shulker/gold")
    val DIAMOND_SHULKER_BOX: net.minecraft.registry.tag.TagKey<net.minecraft.item.Item> =
        register("iron_shulker/diamond")

    val ANY_UPGRADABLE_STORAGE: net.minecraft.registry.tag.TagKey<net.minecraft.item.Item> =
        register("upgradable_storage")
    val ANY_IRON_STORAGE: net.minecraft.registry.tag.TagKey<net.minecraft.item.Item> = register("iron_storage")

    private fun register(id: String): net.minecraft.registry.tag.TagKey<net.minecraft.item.Item> {
        return net.minecraft.registry.tag.TagKey.of<T>(RegistryKeys.ITEM, ModId(id))
    }
}
