package io.sc3.goodies.mixin.fix;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mojang.serialization.Dynamic;

import com.mojang.serialization.OptionalDynamic;
import io.sc3.goodies.ScGoodies;
import io.sc3.goodies.ironstorage.IronStorageVariant;
import net.minecraft.datafixer.fix.ItemStackComponentizationFix;
import net.minecraft.util.DyeColor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStackComponentizationFix.class)
public class ItemStackComponentizationFixMixin {

  @Inject(at = @At("RETURN"), method = "fixBlockEntityData", cancellable = true)
  private static <T> void fixBlockEntityData(ItemStackComponentizationFix.StackData data, Dynamic<T> dynamic,
                                             String blockEntityId, CallbackInfoReturnable<Dynamic<T>> cir) {
    Set<String> itemIds = new HashSet<>();
    for (IronStorageVariant variant : IronStorageVariant.getEntries()) {
      // Chests
      itemIds.add(String.format("%s:%s", ScGoodies.modId, variant.getChestId()));
      // Barrels
      itemIds.add(String.format("%s:%s", ScGoodies.modId, variant.getBarrelId()));
      // Shulkers (undyed)
      itemIds.add(String.format("%s:%s", ScGoodies.modId, variant.getShulkerId()));
      // Shulkers (dyed)
      for (DyeColor color : DyeColor.values()) {
        itemIds.add(String.format("%s:%s_%s", ScGoodies.modId, variant.getShulkerId(), color.getName()));
      }
    }

    if (data.itemMatches(itemIds)) {
      List<Dynamic<T>> list = dynamic.get("Items")
        .asList(itemsDynamic -> itemsDynamic.emptyMap()
          .set("slot", itemsDynamic.createInt(itemsDynamic.get("Slot").asByte((byte) 0) & 255))
          .set("item", itemsDynamic.remove("Slot")));
      if (!list.isEmpty()) {
        data.setComponent("minecraft:container", dynamic.createList(list.stream()));
      }
      cir.setReturnValue(dynamic.remove("Items"));
    }
  }

  @Inject(at = @At("RETURN"), method = "fixStack")
  private static <T> void fixStack(ItemStackComponentizationFix.StackData data, Dynamic<T> dynamic,
                                   CallbackInfo ci) {
    if (data.itemMatches(Set.of(ScGoodies.modId + "glass_item_frame"))) {
      data.getAndRemove("EntityTag").result().ifPresent(entityTag ->
        data.setComponent("minecraft:entity_data",entityTag.set("id", dynamic.createString(ScGoodies.modId + ":glass_item_frame"))
        )
      );
    }
    if (data.itemMatches(Set.of(ScGoodies.modId + "glow_glass_item_frame"))) {
      data.getAndRemove("EntityTag").result().ifPresent(entityTag ->
        data.setComponent("minecraft:entity_data",entityTag.set("id", dynamic.createString(ScGoodies.modId + ":glow_glass_item_frame"))
        )
      );
    }
    if (data.itemMatches(Set.of(ScGoodies.modId + ":ancient_tome"))) {
      // Convert StoredEnchantments NBT tag to minecraft:stored_enchantments component.
      // This mirrors what vanilla does for minecraft:enchanted_book in fixStack.
      // Old format: StoredEnchantments: [{id: "minecraft:sharpness", lvl: 5s}]
      // New format: minecraft:stored_enchantments: {levels: {"minecraft:sharpness": 5}, show_in_tooltip: false}
      data.getAndRemove("StoredEnchantments").result().ifPresent(storedEnchantments -> {
        List<? extends Dynamic<?>> enchantList = storedEnchantments.asList(d -> d);
        if (!enchantList.isEmpty()) {
          Dynamic<T> levels = dynamic.emptyMap();
          for (Dynamic<?> ench : enchantList) {
            String id = ench.get("id").asString("");
            int lvl = ench.get("lvl").asInt(0);
            if (!id.isEmpty() && lvl > 0) {
              levels = levels.set(id, dynamic.createInt(lvl));
            }
          }
          data.setComponent("minecraft:stored_enchantments",
            dynamic.emptyMap()
              .set("levels", levels)
              .set("show_in_tooltip", dynamic.createBoolean(false)));
        }
      });
    }
  }
}
