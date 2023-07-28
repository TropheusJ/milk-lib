package io.github.tropheusj.milk.mixin.pathfinding;

import io.github.tropheusj.milk.Milk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.pathing.MobNavigation;

@Mixin(MobNavigation.class)
public class MobNavigationMixin {
	@ModifyVariable(
			method = "getPathfindingY",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"
			)
	)
	private BlockState treatMilkAsWater(BlockState state) {
		if (Milk.isMilk(state)) {
			return Blocks.WATER.getDefaultState();
		}
		return state;
	}
}
