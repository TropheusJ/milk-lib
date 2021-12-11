package io.github.tropheusj.milk;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

import net.minecraft.block.AbstractCauldronBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BannerItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class MilkCauldron extends LeveledCauldronBlock {
	public static final Map<Item, CauldronBehavior> MILK_CAULDRON_BEHAVIOR = CauldronBehavior.createMap();
	public static final CauldronBehavior FILL_FROM_BUCKET = (state, world, pos, player, hand, stack) -> CauldronBehavior.fillCauldron(world, pos, player, hand, stack, Milk.MILK_CAULDRON.getDefaultState().with(LEVEL, 3), SoundEvents.ITEM_BUCKET_EMPTY);
	public static final CauldronBehavior FILL_FROM_BOTTLE = (state, world, pos, player, hand, stack) -> {
		Block block = state.getBlock();
		if ((block == Blocks.CAULDRON || block == Milk.MILK_CAULDRON) && (!state.contains(LEVEL) || state.get(LEVEL) != 3) && stack.getItem() == Milk.MILK_BOTTLE) {
			if (!world.isClient) {
				player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
				player.incrementStat(Stats.USE_CAULDRON);
				player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
				if (block == Blocks.CAULDRON) {
					world.setBlockState(pos, Milk.MILK_CAULDRON.getDefaultState().with(LEVEL, 1));
				} else {
					world.setBlockState(pos, state.with(LEVEL, state.get(LEVEL) + 1));
				}
				world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
				world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
			}
			return ActionResult.success(world.isClient);
		}
		return ActionResult.PASS;
	};
	public static final CauldronBehavior EMPTY_TO_BUCKET = (state, world, pos, player, hand, stack) -> CauldronBehavior.emptyCauldron(state, world, pos, player, hand, stack, new ItemStack(Items.MILK_BUCKET), statex -> statex.get(LEVEL) == 3, SoundEvents.ITEM_BUCKET_FILL);
	public static final CauldronBehavior EMPTY_TO_BOTTLE = (state, world, pos, player, hand, stack) -> {
		if (!world.isClient) {
			Item item = stack.getItem();
			player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Milk.MILK_BOTTLE)));
			player.incrementStat(Stats.USE_CAULDRON);
			player.incrementStat(Stats.USED.getOrCreateStat(item));
			LeveledCauldronBlock.decrementFluidLevel(state, world, pos);
			world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
			world.emitGameEvent(null, GameEvent.FLUID_PICKUP, pos);
		}
		return ActionResult.success(world.isClient);
	};
	public static final CauldronBehavior MILKIFY_DYEABLE_ITEM = (state, world, pos, player, hand, stack) -> {
		Item item = stack.getItem();
		if ((item instanceof DyeableItem dyeableItem)) {
			if (!world.isClient) {
				dyeableItem.setColor(stack, 0xFFFFFF);
				player.incrementStat(Stats.CLEAN_ARMOR);
				LeveledCauldronBlock.decrementFluidLevel(state, world, pos);
			}
			return ActionResult.success(world.isClient);
		}
		return ActionResult.PASS;
	};
	public static final CauldronBehavior MILKIFY_SHULKER_BOX = (state, world, pos, player, hand, stack) -> {
		Block block = Block.getBlockFromItem(stack.getItem());
		if ((block instanceof ShulkerBoxBlock)) {
			if (!world.isClient) {
				ItemStack itemStack = new ItemStack(Blocks.WHITE_SHULKER_BOX);
				if (stack.hasNbt()) {
					itemStack.setNbt(stack.getNbt().copy());
				}

				player.setStackInHand(hand, itemStack);
				player.incrementStat(Stats.CLEAN_SHULKER_BOX);
				LeveledCauldronBlock.decrementFluidLevel(state, world, pos);
			}
			return ActionResult.success(world.isClient);
		}
		return ActionResult.PASS;
	};
	public static final CauldronBehavior MILKIFY_BANNER = (state, world, pos, player, hand, stack) -> {
		if (!world.isClient()) {
			ItemStack itemStack = new ItemStack(Items.WHITE_BANNER);
			if (!player.getAbilities().creativeMode) {
				stack.decrement(1);
			}

			if (stack.isEmpty()) {
				player.setStackInHand(hand, itemStack);
			} else if (player.getInventory().insertStack(itemStack)) {
				player.playerScreenHandler.syncState();
			} else {
				player.dropItem(itemStack, false);
			}

			player.incrementStat(Stats.CLEAN_BANNER);
			LeveledCauldronBlock.decrementFluidLevel(state, world, pos);
		}
		return ActionResult.success(world.isClient());
	};

	public MilkCauldron(Settings settings) {
		super(settings, precipitation -> false, getMilkCauldronBehaviors());
	}

	public static Map<Item, CauldronBehavior> getMilkCauldronBehaviors() {
		// dyeables
		for (Field field : Items.class.getDeclaredFields()) {
			try {
				if (Modifier.isStatic(field.getModifiers())) {
					Object obj = field.get(null);
					if (obj instanceof Item item) {
						if (item instanceof DyeableItem) {
							MILK_CAULDRON_BEHAVIOR.put(item, MilkCauldron.MILKIFY_DYEABLE_ITEM);
						} else if (item instanceof BannerItem) {
							MILK_CAULDRON_BEHAVIOR.put(item, MilkCauldron.MILKIFY_BANNER);
						} else if (item instanceof BlockItem blockItem) {
							if (blockItem.getBlock() instanceof ShulkerBoxBlock) {
								MILK_CAULDRON_BEHAVIOR.put(item, MilkCauldron.MILKIFY_SHULKER_BOX);
							}
						}
					}
				}
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

		MILK_CAULDRON_BEHAVIOR.put(Items.MILK_BUCKET, FILL_FROM_BUCKET);
		MILK_CAULDRON_BEHAVIOR.put(Items.BUCKET, EMPTY_TO_BUCKET);

		return MILK_CAULDRON_BEHAVIOR;
	}

	@Override
	protected boolean canBeFilledByDripstone(Fluid fluid) {
		return fluid instanceof MilkFluid;
	}

	@Override
	public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
		if (!world.isClient && isEntityTouchingFluid(state, pos, entity)) {
			boolean shouldDrain = false;
			if (entity.isOnFire()) {
				entity.extinguish();
				shouldDrain = true;
			}

			if (entity instanceof LivingEntity livingEntity) {
				shouldDrain = Milk.tryRemoveRandomEffect(livingEntity);
			}

			if (shouldDrain && entity.canModifyAt(world, pos)) {
				decrementFluidLevel(state, world, pos);
			}
		}
	}

	@Override
	public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
		return Items.CAULDRON.getDefaultStack();
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		return super.onUse(state, world, pos, player, hand, hit);
	}
}
