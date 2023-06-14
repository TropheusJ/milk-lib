package io.github.tropheusj.milk.mixin;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import io.github.tropheusj.milk.Milk;

import org.spongepowered.asm.mixin.injection.Slice;

import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@ModifyVariable(
			method = "updateMovementInFluid",
			slice = @Slice(
					from = @At(
							value = "INVOKE",
							target = "Lnet/minecraft/entity/Entity;getWorld()Lnet/minecraft/world/World;",
							ordinal = 0
					)
			),
			at = @At(value = "STORE"
			)
	)
	public FluidState milk$clearEffectsInMilk(FluidState state) {
		if ((Object) this instanceof LivingEntity entity && Milk.STILL_MILK != null) {
			if (Milk.isMilk(state)) {
				if (entity.getStatusEffects().size() > 0) {
					entity.clearStatusEffects();
				}
			}
		}
		return state;
	}
}
