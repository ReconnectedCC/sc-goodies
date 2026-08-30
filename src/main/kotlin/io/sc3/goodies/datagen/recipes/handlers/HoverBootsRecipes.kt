package io.sc3.goodies.datagen.recipes.handlers

import io.sc3.goodies.Registration.ModItems
import io.sc3.goodies.ScGoodies.ModId
import io.sc3.goodies.datagen.ScGoodiesDatagen
import io.sc3.library.recipe.IngredientBrew
import io.sc3.library.recipe.IngredientEnchanted
import io.sc3.library.recipe.RecipeHandler
import net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients
import net.minecraft.component.ComponentChanges
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.ItemEnchantmentsComponent
import net.minecraft.component.type.PotionContentsComponent
import net.minecraft.data.server.recipe.RecipeExporter
import net.minecraft.data.server.recipe.RecipeProvider.conditionsFromItem
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.item.DyeItem
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.potion.Potion
import net.minecraft.potion.Potions
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.Ingredient.ofItems
import net.minecraft.recipe.book.RecipeCategory
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.registry.tag.ItemTags
import net.minecraft.util.DyeColor
import java.util.Optional

object HoverBootsRecipes : RecipeHandler {
  private val log by ScGoodiesDatagen::log

  override fun generateRecipes(exporter: RecipeExporter, wrapper: RegistryWrapper.WrapperLookup) {
    ShapedRecipeJsonBuilder
      .create(RecipeCategory.TOOLS, ModItems.hoverBoots[DyeColor.WHITE]!!)
      .pattern("IDI")
      .pattern("fFj")
      .pattern("IFI")
      .input('I', Items.IRON_BLOCK)
      .input('D', Items.DIAMOND_BLOCK)
      .input('F', Items.FEATHER)
      .input('f', featherFallingBookIngredient(wrapper))
      .input('j', leapingPotionIngredient())
      .criterion("has_diamond_boots", conditionsFromItem(Items.DIAMOND_BOOTS))
      .offerTo(exporter)

    val hoverBootsIngredient = ofItems(*ModItems.hoverBoots.values.toTypedArray())

    DyeColor.entries.forEach { color ->
      log.info("Generating recipe for ${color.name} hover boots")
      ShapelessRecipeJsonBuilder
        .create(RecipeCategory.TOOLS, ModItems.hoverBoots[color]!!, 1)
        .group(ModId("dyed_hover_boots").toString())
        .input(hoverBootsIngredient)
        .input(DyeItem.byColor(color))
        .criterion("has_diamond_boots", conditionsFromItem(Items.DIAMOND_BOOTS))
        .offerTo(exporter, ModId("hover_boots_dyed_${color.getName()}"))
    }
  }
  fun leapingPotionIngredient(): Ingredient {
    fun potionIngredient(item: Item, potion: RegistryEntry<Potion>) = DefaultCustomIngredients.components(
      ofItems(item),
      ComponentChanges.builder()
        .add(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent(
          Optional.of(potion),
          Optional.empty(),
          listOf()
        ))
        .build()
    )

    val items = listOf(Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION)
    val potions = listOf(Potions.LEAPING, Potions.LONG_LEAPING, Potions.STRONG_LEAPING)

    return DefaultCustomIngredients.any(
      *items.flatMap { item ->
        potions.map { potion -> potionIngredient(item, potion) }
      }.toTypedArray()
    )
  }
  fun featherFallingBookIngredient(registries: RegistryWrapper.WrapperLookup): Ingredient {
    val enchantmentRegistry = registries.getWrapperOrThrow(RegistryKeys.ENCHANTMENT)

    fun enchantedBookIngredient(enchantmentKey: RegistryKey<Enchantment>, level: Int): Ingredient {
      val enchantment = enchantmentRegistry.getOrThrow(enchantmentKey)
      return DefaultCustomIngredients.components(
        ofItems(Items.ENCHANTED_BOOK),
        ComponentChanges.builder()
          .add(DataComponentTypes.STORED_ENCHANTMENTS, ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT)
            .apply { add(enchantment, level) }
            .build()
          )
          .build()
      )
    }
    fun enchantedItemIngredient(enchantmentKey: RegistryKey<Enchantment>, level: Int): Ingredient {
      val enchantment = enchantmentRegistry.getOrThrow(enchantmentKey)
      return DefaultCustomIngredients.components(
        Ingredient.fromTag(ItemTags.FOOT_ARMOR_ENCHANTABLE),
        ComponentChanges.builder()
          .add(DataComponentTypes.STORED_ENCHANTMENTS, ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT)
            .apply { add(enchantment, level) }
            .build()
          )
          .build()
      )
    }


    return DefaultCustomIngredients.any(
      enchantedBookIngredient(Enchantments.FEATHER_FALLING, 1),
      enchantedBookIngredient(Enchantments.FEATHER_FALLING, 2),
      enchantedBookIngredient(Enchantments.FEATHER_FALLING, 3),
      enchantedBookIngredient(Enchantments.FEATHER_FALLING, 4),
      enchantedItemIngredient(Enchantments.FEATHER_FALLING, 1),
      enchantedItemIngredient(Enchantments.FEATHER_FALLING, 2),
      enchantedItemIngredient(Enchantments.FEATHER_FALLING, 3),
      enchantedItemIngredient(Enchantments.FEATHER_FALLING, 4)
    )
  }
}
