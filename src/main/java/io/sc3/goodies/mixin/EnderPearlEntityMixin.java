package io.sc3.goodies.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.sc3.goodies.glue.EndermiteEntityOwner;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.EndermiteEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(EnderPearlEntity.class)
public class EnderPearlEntityMixin {
  @Inject(
    method = "onCollision",
    at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/server/world/ServerWorld;spawnEntity(Lnet/minecraft/entity/Entity;)Z"
    )
  )
  public void assignOwnerOnEndermiteSpawn(
    HitResult hitResult,
    CallbackInfo ci,
    @Local ServerPlayerEntity player,
    @Local EndermiteEntity endermite
  ) {
    if (player != null) {
      ((EndermiteEntityOwner) endermite).sc_goodies$setOwner(player.getUuid());
    }
  }
}
