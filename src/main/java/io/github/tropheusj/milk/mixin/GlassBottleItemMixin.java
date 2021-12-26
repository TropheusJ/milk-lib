package io.github.tropheusj.milk.mixin;

import io.github.tropheusj.milk.Milk;

import net.minecraft.fluid.FluidState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.GlassBottleItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

@Mixin(GlassBottleItem.class)
public abstract class GlassBottleItemMixin extends Item {
	private GlassBottleItemMixin(Settings settings) {
		super(settings);
	}

	@Shadow
	protected abstract ItemStack fill(ItemStack itemStack, PlayerEntity playerEntity, ItemStack itemStack2);

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V",
			ordinal = 1, shift = At.Shift.AFTER), method = "use", cancellable = true)
	public void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
		BlockHitResult hitResult = Item.raycast(world, user, RaycastContext.FluidHandling.SOURCE_ONLY);
		BlockPos blockPos = hitResult.getBlockPos();
		FluidState state = world.getFluidState(blockPos);
		if ((Milk.MILK_BOTTLE_ENABLED && Milk.FLUID_ENABLED) && (state.getFluid() == Milk.STILL_MILK || state.getFluid() == Milk.FLOWING_MILK)) {
			cir.setReturnValue(TypedActionResult.success(fill(user.getStackInHand(hand), user, new ItemStack(Milk.MILK_BOTTLE))));
		}
	}
}
