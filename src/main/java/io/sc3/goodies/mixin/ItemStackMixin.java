package io.sc3.goodies.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.item.TooltipType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import io.sc3.goodies.Registration.ModItems;
import io.sc3.goodies.ScGoodies;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.text.Text.translatable;

@SuppressWarnings("ConstantConditions")
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
  @Redirect(
    method = "getTooltip",
    at = @At(value="INVOKE", target="Lnet/minecraft/item/ItemStack;isDamaged()Z")
  )
  private boolean isDamaged(ItemStack stack) {
    return !stack.isOf(ModItems.INSTANCE.getItemMagnet()) && stack.isDamaged();
  }

  @ModifyExpressionValue(
    method = "getTooltip",
    at = @At(
      value="INVOKE",
      target="Lcom/google/common/collect/Lists;newArrayList()Ljava/util/ArrayList;"
    )
  )
  private ArrayList<Text> shareList(
    ArrayList<Text> original, @Share("list") LocalRef<ArrayList<Text>> list
  ) {
    list.set(original);
    return original;
  }

  @Inject(
    method="getTooltip",
    at=@At(value="INVOKE",target="Lnet/minecraft/item/ItemStack;isDamaged()Z")
  )
  private void addMagnetChargeTooltip(
    Item.TooltipContext context,
    PlayerEntity player,
    TooltipType type,
    CallbackInfoReturnable<List<Text>> cir,
    @Share("list") LocalRef<ArrayList<Text>> list) {
    ItemStack stack = (ItemStack) (Object) this;
    if (stack.isOf(ModItems.INSTANCE.getItemMagnet())) {
      int charge = stack.getMaxDamage() - stack.getDamage();
      int max = stack.getMaxDamage();
      list.get().add(translatable("item." + ScGoodies.modId + ".item_magnet.charge", charge, max));
    }
  }
}
