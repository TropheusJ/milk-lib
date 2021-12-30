package io.github.tropheusj.milk;

import static io.github.tropheusj.milk.Milk.CAULDRON_ENABLED;
import static io.github.tropheusj.milk.Milk.FLOWING_MILK;
import static io.github.tropheusj.milk.Milk.FLUID_ENABLED;
import static io.github.tropheusj.milk.Milk.MILK_CAULDRON;
import static io.github.tropheusj.milk.Milk.MILK_FLUID_BLOCK;
import static io.github.tropheusj.milk.Milk.STILL_MILK;
import static net.minecraft.item.Items.MILK_BUCKET;

import java.util.Optional;

import net.minecraft.block.Blocks;

import org.jetbrains.annotations.Nullable;

import io.github.tropheusj.dripstone_fluid_lib.DripstoneInteractingFluid;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.WorldView;

public abstract class MilkFluid extends FlowableFluid implements DripstoneInteractingFluid {

	@Override
	public Fluid getStill() {
		return STILL_MILK;
	}

	@Override
	public Fluid getFlowing() {
		return FLOWING_MILK;
	}

	@Override
	public Item getBucketItem() {
		return MILK_BUCKET;
	}

	@Override
	protected int getFlowSpeed(WorldView worldView) {
		return 2;
	}

	@Override
	protected float getBlastResistance() {
		return 100.0F;
	}

	@Override
	protected boolean canBeReplacedWith(FluidState fluidState, BlockView blockView, BlockPos blockPos, Fluid fluid, Direction direction) {
		return false;
	}

	@Override
	public int getTickRate(WorldView worldView) {
		return 5;
	}

	@Override
	protected int getLevelDecreasePerBlock(WorldView worldView) {
		return 1;
	}

	@Override
	protected void beforeBreakingBlock(WorldAccess world, BlockPos pos, BlockState state) {
		final BlockEntity blockEntity = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;
		Block.dropStacks(state, world, pos, blockEntity);
	}

	@Override
	protected boolean isInfinite() {
		return true;
	}

	@Override
	public boolean matchesType(Fluid fluid) {
		return fluid == getStill() || fluid == getFlowing();
	}

	@Override
	protected BlockState toBlockState(FluidState fluidState) {
		if (FLUID_ENABLED) return MILK_FLUID_BLOCK.getDefaultState().with(Properties.LEVEL_15, getBlockStateLevel(fluidState));
		return Blocks.AIR.getDefaultState();
	}

	@Override
	public Optional<SoundEvent> getBucketFillSound() {
		return Optional.of(SoundEvents.ITEM_BUCKET_FILL);
	}

	@Override
	public int getParticleColor(World world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
		return 0xFFFFFF;
	}

	@Override
	public boolean growsDripstone(BlockState state) {
		return true;
	}

	@Override
	public int getFluidDripWorldEvent(BlockState state, World world, BlockPos cauldronPos) {
		return WorldEvents.POINTED_DRIPSTONE_DRIPS_WATER_INTO_CAULDRON;
	}

	@Override
	public @Nullable BlockState getCauldronBlockState(BlockState state, World world, BlockPos cauldronPos) {
		return CAULDRON_ENABLED ? MILK_CAULDRON.getDefaultState() : null;
	}

	@Override
	public float getFluidDripChance(BlockState state, World world, BlockPos pos) {
		return WATER_DRIP_CHANCE;
	}

	public static class Flowing extends MilkFluid {
		@Override
		protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
			super.appendProperties(builder);
			builder.add(LEVEL);
		}

		@Override
		public int getLevel(FluidState fluidState) {
			return fluidState.get(LEVEL);
		}

		@Override
		public boolean isStill(FluidState fluidState) {
			return false;
		}
	}

	public static class Still extends MilkFluid {
		@Override
		public int getLevel(FluidState fluidState) {
			return 8;
		}

		@Override
		public boolean isStill(FluidState fluidState) {
			return true;
		}
	}
}
