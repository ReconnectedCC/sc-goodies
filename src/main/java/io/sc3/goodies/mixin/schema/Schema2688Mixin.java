package io.sc3.goodies.mixin.schema;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import io.sc3.goodies.ScGoodies;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.Schema2688;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.function.Supplier;
@Mixin(Schema2688.class)
public class Schema2688Mixin {
  @Inject(at = @At("RETURN"), method = "registerEntities")
  private static void registerEntities(Schema schema, CallbackInfoReturnable<Map<String, Supplier<TypeTemplate>>> cir) {
    Map<String, Supplier<TypeTemplate>> map = cir.getReturnValue();
    schema.register(map, String.format("%s:%s", ScGoodies.modId, "glow_glass_item_frame"), (name -> DSL.optionalFields("Item", TypeReferences.ITEM_STACK.in(schema))));
  }
}
