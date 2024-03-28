package io.sc3.goodies.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import io.sc3.goodies.ScGoodies;
import io.sc3.goodies.seats.SeatEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
  @Unique private Entity mountingEntity;
  
  @Inject(
    method = "onEntityPassengersSet",
    at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/entity/Entity;startRiding(Lnet/minecraft/entity/Entity;Z)Z"
    ),
    locals = LocalCapture.CAPTURE_FAILHARD
  )
  private void onEntityPassengersSetInject(
    EntityPassengersSetS2CPacket packet,
    CallbackInfo ci,
    @Local(ordinal = 0) Entity mountingEntity
  ) {
    // Keep track of the entity being mounted (the one we received passengers for)
    this.mountingEntity = mountingEntity;
  }
  
  @ModifyArg(
    method = "onEntityPassengersSet",
    at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/text/Text;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/text/MutableText;"
    ),
    index = 0
  )
  private String onEntityPassengersSetModifyArg(String key) {
    // If we're mounting an sc-goodies Seat entity, change the language key
    if (mountingEntity != null && mountingEntity instanceof SeatEntity) {
      return "block." + ScGoodies.modId + ".seat.mount";
    } else {
      return key;
    }
  }
}
