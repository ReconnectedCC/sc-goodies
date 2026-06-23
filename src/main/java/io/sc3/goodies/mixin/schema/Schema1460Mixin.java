package io.sc3.goodies.mixin.schema;

import java.util.Map;
import java.util.function.Supplier;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;

import io.sc3.goodies.ScGoodies;
import io.sc3.goodies.ironstorage.IronStorageVariant;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.Schema1460;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Schema1460.class)
public class Schema1460Mixin {
  @Inject(at = @At("RETURN"), method = "registerBlockEntities")
  private static void registerBlockEntities(Schema schema, CallbackInfoReturnable<Map<String, Supplier<TypeTemplate>>> cir) {
    Map<String, Supplier<TypeTemplate>> map = cir.getReturnValue();

    for (IronStorageVariant ironStorageVariant : IronStorageVariant.getEntries()) {
      schema.register(map, String.format("%s:%s", ScGoodies.modId, ironStorageVariant.getChestId()), () ->
        DSL.optionalFields("Items", DSL.list(TypeReferences.ITEM_STACK.in(schema)))
      );
      schema.register(map, String.format("%s:%s", ScGoodies.modId, ironStorageVariant.getShulkerId()), () ->
        DSL.optionalFields("Items", DSL.list(TypeReferences.ITEM_STACK.in(schema)))
      );
      schema.register(map, String.format("%s:%s", ScGoodies.modId, ironStorageVariant.getBarrelId()), () ->
        DSL.optionalFields("Items", DSL.list(TypeReferences.ITEM_STACK.in(schema)))
      );
    }

  }
  @Inject(at = @At("RETURN"), method = "registerEntities")
  private static void registerEntities(Schema schema, CallbackInfoReturnable<Map<String, Supplier<TypeTemplate>>> cir) {
    Map<String, Supplier<TypeTemplate>> map = cir.getReturnValue();
    schema.register(map, String.format("%s:%s", ScGoodies.modId, "glass_item_frame"), (name) -> DSL.optionalFields("Item", TypeReferences.ITEM_STACK.in(schema)));
  }
}
