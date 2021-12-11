package io.github.tropheusj.milk.mixin;

import io.github.tropheusj.milk.Milk;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidDrainable;
import net.minecraft.block.FluidFillable;
import net.minecraft.block.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.FluidModificationItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.MilkBucketItem;

import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import static net.minecraft.item.BucketItem.getEmptiedStack;

@Mixin(MilkBucketItem.class)
public abstract class MilkBucketItemMixin extends Item implements FluidModificationItem {
	public MilkBucketItemMixin(Settings settings) {
		super(settings);
	}

	/**
	 * @author Tropheus Jay
	 * @reason Add bucket functionality to milk bucket. Overwrite to fail-fast in conflicts.
	 */
	@Overwrite
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack itemStack = user.getStackInHand(hand);
		BlockHitResult blockHitResult = raycast(world, user, RaycastContext.FluidHandling.NONE);
		if (blockHitResult.getType() == HitResult.Type.MISS || user.isSneaking()) {
			return ItemUsage.consumeHeldItem(world, user, hand);
		} else if (blockHitResult.getType() != HitResult.Type.BLOCK) {
			return TypedActionResult.pass(itemStack);
		}

		BlockPos hit = blockHitResult.getBlockPos();
		Direction direction = blockHitResult.getSide();
		BlockPos offset = hit.offset(direction);
		if (world.canPlayerModifyAt(user, hit) && user.canPlaceOn(offset, direction, itemStack)) {
			if (this.placeFluid(user, world, offset, blockHitResult)) {
				this.onEmptied(user, world, itemStack, offset);
				if (user instanceof ServerPlayerEntity server) {
					Criteria.PLACED_BLOCK.trigger(server, offset, itemStack);
				}

				user.incrementStat(Stats.USED.getOrCreateStat(this));
				return TypedActionResult.success(getEmptiedStack(itemStack, user), world.isClient());
			} else {
				return TypedActionResult.fail(itemStack);
			}

		}
		return TypedActionResult.fail(itemStack);
	}

	@Override
	public boolean placeFluid(@Nullable PlayerEntity player, World world, BlockPos pos, @Nullable BlockHitResult hitResult) {
		BlockState blockState = world.getBlockState(pos);
		Block block = blockState.getBlock();
		Material material = blockState.getMaterial();
		boolean canPlace = blockState.canBucketPlace(Milk.STILL_MILK);
		boolean bl2 = blockState.isAir() ||
				canPlace ||
				block instanceof FluidFillable fillable && fillable.canFillWithFluid(world, pos, blockState, Milk.STILL_MILK);
		if (!bl2) {
			return hitResult != null && this.placeFluid(player, world, hitResult.getBlockPos().offset(hitResult.getSide()), null);
		} else if (world.getDimension().isUltrawarm()) {
			int i = pos.getX();
			int j = pos.getY();
			int k = pos.getZ();
			world.playSound(
					player,
					pos,
					SoundEvents.BLOCK_FIRE_EXTINGUISH,
					SoundCategory.BLOCKS,
					0.5F,
					2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F
			);

			for(int l = 0; l < 8; ++l) {
				world.addParticle(
						ParticleTypes.LARGE_SMOKE, (double)i + Math.random(), (double)j + Math.random(), (double)k + Math.random(), 0.0, 0.0, 0.0
				);
			}

			return true;
		} else if (block instanceof FluidFillable fillable) {
			fillable.tryFillWithFluid(world, pos, blockState, Milk.STILL_MILK.getStill(false));
			this.playEmptyingSound(player, world, pos);
			return true;
		} else {
			if (!world.isClient && canPlace && !material.isLiquid()) {
				world.breakBlock(pos, true);
			}

			if (!world.setBlockState(pos, Milk.STILL_MILK.getDefaultState().getBlockState(), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD)
					&& !blockState.getFluidState().isStill()) {
				return false;
			} else {
				this.playEmptyingSound(player, world, pos);
				return true;
			}
		}
	}

	protected void playEmptyingSound(@Nullable PlayerEntity player, WorldAccess world, BlockPos pos) {
		world.playSound(player, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
		world.emitGameEvent(player, GameEvent.FLUID_PLACE, pos);
	}
}
