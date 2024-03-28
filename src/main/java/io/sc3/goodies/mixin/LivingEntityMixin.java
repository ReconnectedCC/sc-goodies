package io.sc3.goodies.mixin;

import io.sc3.goodies.hoverboots.HoverBootsItem;
import io.sc3.goodies.seats.SeatEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("ConstantConditions")
@Mixin(LivingEntity.class)
public class LivingEntityMixin {
  @Unique private Entity dismountingEntity;
  
  @Inject(method = "jump", at = @At("TAIL"))
  private void jump(CallbackInfo ci) {
    HoverBootsItem.onLivingJump((LivingEntity) (Object) this);
  }

  @ModifyVariable(method = "handleFallDamage", at = @At("HEAD"), ordinal = 0, argsOnly = true)
  private float handleFallDamage(float fallDistance) {
    return HoverBootsItem.modifyFallDistance((LivingEntity) (Object) this, fallDistance);
  }
  
  @Inject(method = "onDismounted", at = @At("HEAD"))
  private void onDismounted(Entity entity, CallbackInfo ci) {
    dismountingEntity = entity;
  }
  
  @Redirect(
    method = "onDismounted",
    at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/entity/LivingEntity;requestTeleportAndDismount(DDD)V"
    )
  )
  private void adjustDismountPosition(LivingEntity livingEntity, double x, double y, double z) {
    if (dismountingEntity instanceof SeatEntity) {
      // Add a small offset to prevent the player from getting stuck in the seat
      livingEntity.requestTeleportAndDismount(x, y + 0.25, z);
    } else {
      livingEntity.requestTeleportAndDismount(x, y, z);
    }
  }
}
