package pw.switchcraft.goodies

import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.fabricmc.fabric.api.registry.FlattenableBlockRegistry
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.block.*
import net.minecraft.block.AbstractBlock.ContextPredicate
import net.minecraft.block.Material.SOLID_ORGANIC
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.*
import net.minecraft.registry.Registerable
import net.minecraft.registry.Registries.*
import net.minecraft.registry.Registry.register
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys.CONFIGURED_FEATURE
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.sound.BlockSoundGroup.GRASS
import net.minecraft.util.DyeColor
import net.minecraft.util.Rarity.EPIC
import net.minecraft.util.Rarity.UNCOMMON
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.intprovider.ConstantIntProvider
import net.minecraft.world.gen.feature.ConfiguredFeature
import net.minecraft.world.gen.feature.Feature
import net.minecraft.world.gen.feature.TreeFeatureConfig
import net.minecraft.world.gen.feature.size.TwoLayersFeatureSize
import net.minecraft.world.gen.foliage.LargeOakFoliagePlacer
import net.minecraft.world.gen.stateprovider.BlockStateProvider
import net.minecraft.world.gen.trunk.LargeOakTrunkPlacer
import pw.switchcraft.goodies.Registration.ModBlockEntities.rBlockEntity
import pw.switchcraft.goodies.Registration.ModBlocks.autumnGrass
import pw.switchcraft.goodies.Registration.ModBlocks.blueGrass
import pw.switchcraft.goodies.Registration.ModBlocks.blueSapling
import pw.switchcraft.goodies.Registration.ModBlocks.chestSettings
import pw.switchcraft.goodies.Registration.ModBlocks.mapleSapling
import pw.switchcraft.goodies.Registration.ModBlocks.rBlock
import pw.switchcraft.goodies.Registration.ModBlocks.sakuraSapling
import pw.switchcraft.goodies.Registration.ModBlocks.shulkerSettings
import pw.switchcraft.goodies.Registration.ModItems.elytraSettings
import pw.switchcraft.goodies.Registration.ModItems.itemSettings
import pw.switchcraft.goodies.Registration.ModItems.rItem
import pw.switchcraft.goodies.ScGoodies.ModId
import pw.switchcraft.goodies.datagen.recipes.handlers.RECIPE_HANDLERS
import pw.switchcraft.goodies.elytra.DyedElytraItem
import pw.switchcraft.goodies.elytra.SpecialElytraItem
import pw.switchcraft.goodies.elytra.SpecialElytraType
import pw.switchcraft.goodies.enderstorage.*
import pw.switchcraft.goodies.hoverboots.HoverBootsItem
import pw.switchcraft.goodies.ironchest.*
import pw.switchcraft.goodies.ironshulker.IronShulkerBlock
import pw.switchcraft.goodies.ironshulker.IronShulkerBlockEntity
import pw.switchcraft.goodies.ironshulker.IronShulkerCauldronBehavior
import pw.switchcraft.goodies.ironshulker.IronShulkerItem
import pw.switchcraft.goodies.itemmagnet.ItemMagnetItem
import pw.switchcraft.goodies.itemmagnet.MAGNET_MAX_DAMAGE
import pw.switchcraft.goodies.itemmagnet.ToggleItemMagnetPacket
import pw.switchcraft.goodies.misc.ConcreteExtras
import pw.switchcraft.goodies.misc.EndermitesFormShulkers
import pw.switchcraft.goodies.misc.PopcornItem
import pw.switchcraft.goodies.nature.ScGrass
import pw.switchcraft.goodies.nature.ScSaplingGenerator
import pw.switchcraft.goodies.nature.ScTree
import pw.switchcraft.goodies.tomes.AncientTomeItem
import pw.switchcraft.goodies.tomes.TomeEnchantments
import pw.switchcraft.goodies.util.BaseItem
import pw.switchcraft.library.networking.registerServerReceiver
import pw.switchcraft.library.recipe.RecipeHandler
import java.util.*

object Registration {
  private val items = mutableListOf<Item>()

  internal fun init() {
    // Force static initializers to run
    listOf(ModBlocks, ModItems, ModBlockEntities, ModScreens)

    // Iron Chests and Shulkers
    IronChestVariant.values().forEach { variant ->
      registerIronChest(variant)

      registerIronShulker(variant) // Undyed shulker
      DyeColor.values().forEach { registerIronShulker(variant, it) }

      // Shulker block entities, done in bulk for each dyed variant + undyed
      registerIronShulkerBlockEntities(variant)
    }

    IronChestUpgrade.values().forEach { upgrade ->
      rItem(upgrade.itemName + "_chest_upgrade", IronChestUpgradeItem(upgrade, false, itemSettings()))
      rItem(upgrade.itemName + "_shulker_upgrade", IronChestUpgradeItem(upgrade, true, itemSettings()))
    }

    IronShulkerCauldronBehavior.registerBehavior()

    // Ender Storage
    EnderStorageBlockEntity.initEvents()
    EnderStorageCommands.register()
    if (FabricLoader.getInstance().isModLoaded("computercraft")) {
      EnderStorageMethods.register()
    }

    // Item Magnets
    registerServerReceiver(ToggleItemMagnetPacket.id, ToggleItemMagnetPacket::fromBytes)

    // Dyed + Special Elytra
    DyeColor.values()
      .forEach { rItem("elytra_${it.getName()}", DyedElytraItem(it, elytraSettings())) }
    SpecialElytraType.values()
      .forEach { rItem("elytra_${it.type}", SpecialElytraItem(it, elytraSettings())) }

    // Concrete Slabs and Stairs
    ConcreteExtras.colors.values.forEach {
      val settings = AbstractBlock.Settings.copy(it.baseBlock)

      val slabBlock = rBlock(it.slabBlockId.path, SlabBlock(settings))
      rItem(slabBlock, ::BlockItem)

      val stairsBlock = rBlock(it.stairsBlockId.path, StairsBlock(it.baseBlock.defaultState, settings))
      rItem(stairsBlock, ::BlockItem)
    }

    TomeEnchantments.init()
    EndermitesFormShulkers.init()

    // Nature
    FlattenableBlockRegistry.register(ModBlocks.pinkGrass, Blocks.DIRT_PATH.defaultState)
    FlattenableBlockRegistry.register(autumnGrass, Blocks.DIRT_PATH.defaultState)
    FlattenableBlockRegistry.register(blueGrass, Blocks.DIRT_PATH.defaultState)

    RECIPE_HANDLERS.forEach(RecipeHandler::registerSerializers)
  }

  internal fun bootstrapFeatures(featureRegisterable: Registerable<ConfiguredFeature<*, *>>) {
    featureRegisterable.register(
      sakuraSapling.treeFeature,
      ConfiguredFeature(
        Feature.TREE,
        TreeFeatureConfig.Builder(
          BlockStateProvider.of(Blocks.SPRUCE_LOG),
          LargeOakTrunkPlacer(3, 11, 0),
          BlockStateProvider.of(sakuraSapling.leaves),
          LargeOakFoliagePlacer(ConstantIntProvider.create(2), ConstantIntProvider.create(4), 4),
          TwoLayersFeatureSize(0, 0, 0, OptionalInt.of(4))
        ).ignoreVines().build()
      )
    )

    featureRegisterable.register(
      mapleSapling.treeFeature,
      ConfiguredFeature(
        Feature.TREE,
        TreeFeatureConfig.Builder(
          BlockStateProvider.of(Blocks.OAK_LOG),
          LargeOakTrunkPlacer(3, 9, 0),
          BlockStateProvider.of(mapleSapling.leaves),
          LargeOakFoliagePlacer(ConstantIntProvider.create(2), ConstantIntProvider.create(4), 4),
          TwoLayersFeatureSize(0, 0, 0, OptionalInt.of(4))
        ).ignoreVines().build()
      )
    )

    featureRegisterable.register(
      blueSapling.treeFeature,
      ConfiguredFeature(
        Feature.TREE,
        TreeFeatureConfig.Builder(
          BlockStateProvider.of(Blocks.BIRCH_LOG),
          LargeOakTrunkPlacer(3, 8, 0),
          BlockStateProvider.of(blueSapling.leaves),
          LargeOakFoliagePlacer(ConstantIntProvider.create(2), ConstantIntProvider.create(4), 4),
          TwoLayersFeatureSize(0, 0, 0, OptionalInt.of(4))
        ).ignoreVines().build()
      )
    )
  }

  private fun registerIronChest(variant: IronChestVariant) {
    with (variant) {
      // Register the block and item
      val chestBlock = rBlock(chestId, IronChestBlock(chestSettings(), this))
      rItem(chestBlock, ::BlockItem)

      // Register the block entity and screen handler
      rBlockEntity(chestId, chestBlock) { pos, state -> IronChestBlockEntity(this, pos, state) }
      register(SCREEN_HANDLER, ModId(chestId), chestScreenHandlerType)
    }
  }

  private fun registerIronShulker(variant: IronChestVariant, color: DyeColor? = null) {
    with (variant) {
      val id = if (color != null) "${shulkerId}_${color.getName()}" else shulkerId

      // Register the block and item
      val shulkerBlock = rBlock(id, IronShulkerBlock(shulkerSettings(color), this, color))
      rItem(shulkerBlock, ::IronShulkerItem, itemSettings().maxCount(1))
    }
  }

  private fun registerIronShulkerBlockEntities(variant: IronChestVariant) {
    with (variant) {
      val blocks = mutableSetOf(shulkerBlock)
      blocks.addAll(dyedShulkerBlocks.values)

      // Register the block entity
      rBlockEntity(shulkerId, *blocks.toTypedArray())
        { pos, state -> IronShulkerBlockEntity(this, pos, state) }

      register(SCREEN_HANDLER, ModId(shulkerId), shulkerScreenHandlerType)
    }
  }

  object ModBlocks {
    val enderStorage = rBlock("ender_storage", EnderStorageBlock(AbstractBlock.Settings
      .of(Material.STONE).requiresTool().strength(22.5f, 600.0f)))

    val pinkGrass = rBlock("pink_grass", ScGrass(grassSettings(MapColor.PINK)))
    val autumnGrass = rBlock("autumn_grass", ScGrass(grassSettings(MapColor.ORANGE)))
    val blueGrass = rBlock("blue_grass", ScGrass(grassSettings(MapColor.LIGHT_BLUE)))

    val sakuraSapling = registerSapling("sakura")
    val mapleSapling = registerSapling("maple")
    val blueSapling = registerSapling("blue")

    fun <T : Block> rBlock(name: String, value: T): T =
      register(BLOCK, ModId(name), value)

    fun blockSettings(): AbstractBlock.Settings = AbstractBlock.Settings
      .of(Material.STONE)
      .strength(2.0f)
      .nonOpaque()

    fun chestSettings(): AbstractBlock.Settings = AbstractBlock.Settings
      .of(Material.METAL)
      .strength(3.0f)
      .nonOpaque()

    fun shulkerSettings(color: DyeColor?): AbstractBlock.Settings {
      val predicate = ContextPredicate { _, world, pos ->
        val be = world.getBlockEntity(pos) as? IronShulkerBlockEntity
        be?.suffocates() ?: true
      }

      return AbstractBlock.Settings
        .of(Material.SHULKER_BOX)
        .mapColor(color?.mapColor ?: MapColor.PURPLE)
        .strength(2.0f)
        .dynamicBounds()
        .nonOpaque()
        .suffocates(predicate)
        .blockVision(predicate)
    }

    private fun leavesSettings(): AbstractBlock.Settings = AbstractBlock.Settings
      .of(Material.LEAVES)
      .strength(0.2f)
      .ticksRandomly()
      .sounds(GRASS)
      .nonOpaque()
      .allowsSpawning { _, _, _, _ -> false }
      .suffocates { _, _, _ -> false }
      .blockVision { _, _, _ -> false }

    private fun saplingSettings(): AbstractBlock.Settings = AbstractBlock.Settings
      .of(Material.PLANT)
      .noCollision()
      .ticksRandomly()
      .breakInstantly()
      .sounds(GRASS)

    private fun potSettings(): AbstractBlock.Settings = AbstractBlock.Settings.copy(Blocks.POTTED_OAK_SAPLING)

    private fun grassSettings(color: MapColor): AbstractBlock.Settings = AbstractBlock.Settings
      .of(SOLID_ORGANIC, color)
      .ticksRandomly()
      .strength(0.6f)
      .sounds(GRASS)

    private fun registerSapling(name: String): ScTree {
      val feature = RegistryKey.of(CONFIGURED_FEATURE, ModId("${name}_tree"))
      val sapling = rBlock("${name}_sapling", SaplingBlock(ScSaplingGenerator(feature), saplingSettings()))
      val leaves = rBlock("${name}_leaves", LeavesBlock(leavesSettings()))
      val potted = rBlock("potted_${name}_sapling", FlowerPotBlock(sapling, potSettings()))
      val saplingItem = rItem(sapling, ::BlockItem, itemSettings())
      val leavesItem = rItem(leaves, ::BlockItem, itemSettings())

      val tree = ScTree(
        sapling,
        leaves,
        feature,
        potted,
        saplingItem
      )
      tree.registerTreeLoot()
      return tree
    }
  }

  object ModItems {
    val itemGroup: ItemGroup = FabricItemGroup.builder(ModId("main"))
      .icon { ItemStack(Items.AXOLOTL_BUCKET) }
      .entries { _, entries, _ ->
        items.forEach(entries::add)
        entries.addAll(AncientTomeItem.getTomeStacks())
      }
      .build()

    val enderStorage = rItem(ModBlocks.enderStorage, ::BlockItem, itemSettings())

    val hoverBoots = DyeColor.values().associateWith {
      rItem("hover_boots_${it.getName()}", HoverBootsItem(it, itemSettings().maxCount(1)))
    }

    val itemMagnet = rItem("item_magnet", ItemMagnetItem(itemSettings()
      .maxDamage(MAGNET_MAX_DAMAGE)))
    val dragonScale = rItem("dragon_scale", BaseItem(itemSettings()
      .maxCount(16)
      .rarity(EPIC)))
    val popcorn = rItem("popcorn", PopcornItem(itemSettings()
      .food(PopcornItem.foodComponent)
      .maxCount(1)))
    val ancientTome = rItem("ancient_tome", AncientTomeItem(itemSettings()
      .maxCount(1)
      .rarity(UNCOMMON)))

    // TODO: Clean up
    val pinkGrass = rItem(ModBlocks.pinkGrass, ::BlockItem, itemSettings())
    val autumnGrass = rItem(ModBlocks.autumnGrass, ::BlockItem, itemSettings())
    val blueGrass = rItem(ModBlocks.blueGrass, ::BlockItem, itemSettings())

    fun <T : Item> rItem(name: String, value: T): T =
      register(ITEM, ModId(name), value).also { items.add(it) }

    fun <B : Block, I : Item> rItem(parent: B, supplier: (B, Item.Settings) -> I,
                                    settings: Item.Settings = itemSettings()): I {
      val item = register(ITEM, BLOCK.getId(parent), supplier(parent, settings))
      Item.BLOCK_ITEMS[parent] = item
      items.add(item)
      return item
    }

    fun itemSettings(): FabricItemSettings = FabricItemSettings()

    fun elytraSettings(): FabricItemSettings = itemSettings()
      .maxDamage(432)
      .rarity(UNCOMMON)
      .equipmentSlot { EquipmentSlot.CHEST }
  }

  object ModBlockEntities {
    val enderStorage = rBlockEntity("ender_storage", ModBlocks.enderStorage, factory = ::EnderStorageBlockEntity)

    fun <T : BlockEntity> rBlockEntity(name: String, vararg block: Block,
                                       factory: (BlockPos, BlockState) -> T): BlockEntityType<T> {
      val blockEntityType = FabricBlockEntityTypeBuilder.create(factory, *block).build()
      return register(BLOCK_ENTITY_TYPE, ModId(name), blockEntityType)
    }
  }

  object ModScreens {
    val enderStorage: ScreenHandlerType<EnderStorageScreenHandler> =
      register(SCREEN_HANDLER, ModId("ender_storage"), ExtendedScreenHandlerType(::EnderStorageScreenHandler))
  }
}