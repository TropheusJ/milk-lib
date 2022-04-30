package io.github.tropheusj.milk.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.tropheusj.milk.Milk;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.BrewingStandScreenHandler;

@Mixin(BrewingStandScreenHandler.PotionSlot.class)
public abstract class BrewingStandScreenHandlerPotionSlotMixin {
	@Inject(method = "matches", at = @At("HEAD"), cancellable = true)
	private static void milk$matches(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (stack.isOf(Milk.SPLASH_MILK_BOTTLE)  || stack.isOf(Milk.LINGERING_MILK_BOTTLE) || stack.isOf(Milk.MILK_BOTTLE)) {
			cir.setReturnValue(true);
		}
	}
}
