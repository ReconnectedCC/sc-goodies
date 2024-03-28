package io.sc3.goodies.mixin;

import io.sc3.goodies.seats.StairsSeat;
import io.sc3.library.SeatBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(StairsBlock.class)
public class StairsBlockMixin implements SeatBlock {
  @NotNull
  @Override
  public ActionResult canSitOn(
    @NotNull World world,
    @NotNull BlockPos blockPos,
    @NotNull BlockState blockState,
    @Nullable BlockHitResult blockHitResult,
    @Nullable PlayerEntity playerEntity
  ) {
    return StairsSeat.INSTANCE.canSitOn(world, blockPos, blockState, blockHitResult, playerEntity);
  }
  
  @NotNull
  @Override
  public Vec3d getSeatPos(@NotNull World world, @NotNull BlockPos blockPos, @NotNull BlockState blockState) {
    return StairsSeat.INSTANCE.getSeatPos(world, blockPos, blockState);
  }
}
