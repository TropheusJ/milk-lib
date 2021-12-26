package io.github.tropheusj.milk.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.tropheusj.milk.Milk;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.tag.Tag;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@Inject(method = "updateMovementInFluid", at = @At("HEAD"))
	public void updateMovementInFluid(Tag<Fluid> tag, double d, CallbackInfoReturnable<Boolean> cir) {
		if ((Object) this instanceof PlayerEntity player && Milk.FLUID_ENABLED) {
			FluidState fluidState = player.world.getFluidState(player.getBlockPos());
			if (fluidState.getFluid() == Milk.STILL_MILK || fluidState.getFluid() == Milk.FLOWING_MILK) {
				if (player.getStatusEffects().size() > 0) {
					player.clearStatusEffects();
				}
			}
		}
	}
}
