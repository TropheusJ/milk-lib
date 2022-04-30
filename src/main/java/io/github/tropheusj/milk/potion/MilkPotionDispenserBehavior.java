package io.github.tropheusj.milk.potion;

import io.github.tropheusj.milk.potion.bottle.PotionItemEntityExtensions;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ProjectileDispenserBehavior;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;

public enum MilkPotionDispenserBehavior implements DispenserBehavior {
	INSTANCE;

	@Override
	public ItemStack dispense(BlockPointer blockPointer, ItemStack itemStack) {
		return (new ProjectileDispenserBehavior() {
			@Override
			protected ProjectileEntity createProjectile(World world, Position position, ItemStack stack) {
				return Util.make(new PotionEntity(world, position.getX(), position.getY(), position.getZ()), entity -> {
					entity.setItem(stack);
					((PotionItemEntityExtensions) entity).setMilk(true);
				});
			}

			@Override
			protected float getVariation() {
				return super.getVariation() * 0.5F;
			}

			@Override
			protected float getForce() {
				return super.getForce() * 1.25F;
			}
		}).dispense(blockPointer, itemStack);
	}
}
