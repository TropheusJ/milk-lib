package io.github.tropheusj.milk;

import static net.minecraft.item.Items.BUCKET;
import static net.minecraft.item.Items.DRAGON_BREATH;
import static net.minecraft.item.Items.GLASS_BOTTLE;
import static net.minecraft.item.Items.GUNPOWDER;
import static net.minecraft.item.Items.MILK_BUCKET;
import static net.minecraft.item.Items.SPLASH_POTION;

import io.github.tropheusj.milk.mixin.BrewingRecipeRegistryAccessor;
import io.github.tropheusj.milk.potion.MilkAreaEffectCloudEntity;
import io.github.tropheusj.milk.potion.MilkPotionDispenserBehavior;
import io.github.tropheusj.milk.potion.bottle.LingeringMilkBottle;
import io.github.tropheusj.milk.potion.bottle.MilkBottle;
import io.github.tropheusj.milk.potion.bottle.PotionItemEntityExtensions;
import io.github.tropheusj.milk.potion.bottle.SplashMilkBottle;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.CauldronFluidContent;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.EmptyItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.FullItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ProjectileDispenserBehavior;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.Position;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

@SuppressWarnings("UnstableApiUsage")
public class Milk {
	public static final String MOD_ID = "milk";
	// fluids - if any are non-null, all are non-null.
	public static FlowableFluid STILL_MILK = null;
	public static FlowableFluid FLOWING_MILK = null;
	public static Block MILK_FLUID_BLOCK = null;

	// bottles - may be enabled individually.
	public static Item MILK_BOTTLE = null;
	public static Item SPLASH_MILK_BOTTLE = null;
	public static Item LINGERING_MILK_BOTTLE = null;

	// cauldron.
	public static Block MILK_CAULDRON = null;

	// if true, milk can be placed from buckets.
	public static boolean MILK_PLACING_ENABLED = false;

	public static EntityType<MilkAreaEffectCloudEntity> MILK_EFFECT_CLOUD_ENTITY_TYPE = null;

	// tags.
	// all milk fluids.
	public static final TagKey<Fluid> MILK_FLUID_TAG = TagKey.of(Registry.FLUID_KEY, new Identifier("c", "milk"));
	// all milk bottles.
	public static final TagKey<Item> MILK_BOTTLE_TAG = TagKey.of(Registry.ITEM_KEY, new Identifier("c", "milk_bottles"));
	// all milk buckets.
	public static final TagKey<Item> MILK_BUCKET_TAG = TagKey.of(Registry.ITEM_KEY, new Identifier("c", "milk_buckets"));

	public static void enableMilkPlacing() {
		if (STILL_MILK == null) {
			throw new RuntimeException("to enable milk placing, you need to enable milk with enableMilkFluid()!");
		}
		MILK_PLACING_ENABLED = true;
	}

	public static void enableMilkFluid() {
		if (STILL_MILK == null) {
			// register
			STILL_MILK = Registry.register(
					Registry.FLUID,
					id("still_milk"),
					new MilkFluid.Still()
			);
			FLOWING_MILK = Registry.register(
					Registry.FLUID,
					id("flowing_milk"),
					new MilkFluid.Flowing()
			);
			MILK_FLUID_BLOCK = Registry.register(
					Registry.BLOCK,
					id("milk_fluid_block"),
					new MilkFluidBlock(STILL_MILK, FabricBlockSettings.copyOf(Blocks.WATER).mapColor(MapColor.WHITE))
			);

			// transfer
			FluidStorage.combinedItemApiProvider(MILK_BUCKET).register(context ->
					new FullItemFluidStorage(context, bucket -> ItemVariant.of(BUCKET), FluidVariant.of(STILL_MILK), FluidConstants.BUCKET)
			);
			FluidStorage.combinedItemApiProvider(BUCKET).register(context ->
					new EmptyItemFluidStorage(context, bucket -> ItemVariant.of(MILK_BUCKET), STILL_MILK, FluidConstants.BUCKET)
			);

			// extras
			if (MILK_CAULDRON != null) {
				CauldronFluidContent.registerCauldron(MILK_CAULDRON, STILL_MILK, FluidConstants.BOTTLE, LeveledCauldronBlock.LEVEL);
			}
			if (MILK_BOTTLE != null) {
				FluidStorage.combinedItemApiProvider(MILK_BOTTLE).register(context ->
						new FullItemFluidStorage(context, bottle -> ItemVariant.of(GLASS_BOTTLE), FluidVariant.of(STILL_MILK), FluidConstants.BOTTLE)
				);
				FluidStorage.combinedItemApiProvider(GLASS_BOTTLE).register(context ->
						new EmptyItemFluidStorage(context, bucket -> ItemVariant.of(MILK_BOTTLE), STILL_MILK, FluidConstants.BOTTLE)
				);
			}
		}
	}

	public static void enableCauldron() {
		if (MILK_CAULDRON == null) {
			// register
			MILK_CAULDRON = Registry.register(
					Registry.BLOCK,
					id("milk_cauldron"),
					new MilkCauldron(FabricBlockSettings.copyOf(Blocks.CAULDRON))
			);
			CauldronBehavior.EMPTY_CAULDRON_BEHAVIOR.put(MILK_BUCKET, MilkCauldron.FILL_FROM_BUCKET);
			// transfer
			if (STILL_MILK != null) {
				CauldronFluidContent.registerCauldron(MILK_CAULDRON, STILL_MILK, FluidConstants.BOTTLE, LeveledCauldronBlock.LEVEL);
			}
			// cauldron interactions
			if (MILK_BOTTLE != null && !CauldronBehavior.EMPTY_CAULDRON_BEHAVIOR.containsKey(MILK_BOTTLE)) {
				CauldronBehavior fillFromMilkBottle = MilkCauldron.addInputToCauldronExchange(
						Milk.MILK_BOTTLE.getDefaultStack(), Items.GLASS_BOTTLE.getDefaultStack(), true);
				CauldronBehavior.EMPTY_CAULDRON_BEHAVIOR.put(MILK_BOTTLE, fillFromMilkBottle);
				MilkCauldron.MILK_CAULDRON_BEHAVIOR.put(Milk.MILK_BOTTLE, fillFromMilkBottle);

				CauldronBehavior emptyToBottle = MilkCauldron.addOutputToItemExchange(
						Items.GLASS_BOTTLE.getDefaultStack(), Milk.MILK_BOTTLE.getDefaultStack(), true);
				MilkCauldron.MILK_CAULDRON_BEHAVIOR.put(Items.GLASS_BOTTLE, emptyToBottle);
			}
		}
	}

	public static void enableMilkBottle() {
		if (MILK_BOTTLE == null) {
			// register
			MILK_BOTTLE = Registry.register(
					Registry.ITEM,
					id("milk_bottle"),
					new MilkBottle(new FabricItemSettings().recipeRemainder(Items.GLASS_BOTTLE).maxCount(1).group(ItemGroup.BREWING))
			);
			// potions
			BrewingRecipeRegistryAccessor.milk$registerPotionType(MILK_BOTTLE);
			// transfer
			if (STILL_MILK != null) {
				FluidStorage.combinedItemApiProvider(MILK_BOTTLE).register(context ->
						new FullItemFluidStorage(context, bottle -> ItemVariant.of(GLASS_BOTTLE), FluidVariant.of(STILL_MILK), FluidConstants.BOTTLE)
				);
				FluidStorage.combinedItemApiProvider(GLASS_BOTTLE).register(context ->
						new EmptyItemFluidStorage(context, bucket -> ItemVariant.of(MILK_BOTTLE), STILL_MILK, FluidConstants.BOTTLE)
				);
			}
			// cauldron interactions
			if (MILK_CAULDRON != null && !CauldronBehavior.EMPTY_CAULDRON_BEHAVIOR.containsKey(MILK_BOTTLE)) {
				CauldronBehavior fillFromMilkBottle = MilkCauldron.addInputToCauldronExchange(
						Milk.MILK_BOTTLE.getDefaultStack(), Items.GLASS_BOTTLE.getDefaultStack(), true);
				CauldronBehavior.EMPTY_CAULDRON_BEHAVIOR.put(MILK_BOTTLE, fillFromMilkBottle);
				MilkCauldron.MILK_CAULDRON_BEHAVIOR.put(Milk.MILK_BOTTLE, fillFromMilkBottle);

				CauldronBehavior emptyToBottle = MilkCauldron.addOutputToItemExchange(
						Items.GLASS_BOTTLE.getDefaultStack(), Milk.MILK_BOTTLE.getDefaultStack(), true);
				MilkCauldron.MILK_CAULDRON_BEHAVIOR.put(Items.GLASS_BOTTLE, emptyToBottle);
			}
			// dispenser interactions
			DispenserBlock.registerBehavior(MILK_BOTTLE, MilkPotionDispenserBehavior.INSTANCE);
		}
	}

	public static void enableSplashMilkBottle() {
		if (SPLASH_MILK_BOTTLE == null) {
			// register
			SPLASH_MILK_BOTTLE = Registry.register(
					Registry.ITEM,
					id("splash_milk_bottle"),
					new SplashMilkBottle(new FabricItemSettings().maxCount(1).group(ItemGroup.BREWING))
			);
			// potions
			BrewingRecipeRegistryAccessor.milk$registerPotionType(SPLASH_MILK_BOTTLE);
			if (MILK_BOTTLE != null) {
				BrewingRecipeRegistryAccessor.milk$registerItemRecipe(MILK_BOTTLE, GUNPOWDER, SPLASH_MILK_BOTTLE);
			}
			// transfer
			if (STILL_MILK != null) {
				FluidStorage.combinedItemApiProvider(SPLASH_MILK_BOTTLE).register(context ->
						new FullItemFluidStorage(context, bottle -> ItemVariant.of(GLASS_BOTTLE), FluidVariant.of(STILL_MILK), FluidConstants.BOTTLE)
				);
			}
			// dispenser interactions
			DispenserBlock.registerBehavior(SPLASH_MILK_BOTTLE, MilkPotionDispenserBehavior.INSTANCE);
		}
	}

	public static void enableLingeringMilkBottle() {
		if (LINGERING_MILK_BOTTLE == null) {
			// register
			LINGERING_MILK_BOTTLE = Registry.register(
					Registry.ITEM,
					id("lingering_milk_bottle"),
					new LingeringMilkBottle(new FabricItemSettings().maxCount(1).group(ItemGroup.BREWING))
			);
			// potions
			BrewingRecipeRegistryAccessor.milk$registerPotionType(LINGERING_MILK_BOTTLE);
			// lingering effect
			MILK_EFFECT_CLOUD_ENTITY_TYPE = Registry.register(
					Registry.ENTITY_TYPE,
					id("milk_area_effect_cloud"),
					FabricEntityTypeBuilder.<MilkAreaEffectCloudEntity>create()
							.fireImmune()
							.dimensions(EntityDimensions.fixed(6.0F, 0.5F))
							.trackRangeChunks(10)
							.trackedUpdateRate(Integer.MAX_VALUE)
							.build()
			);
			// potions
			if (SPLASH_MILK_BOTTLE != null) {
				BrewingRecipeRegistryAccessor.milk$registerItemRecipe(SPLASH_MILK_BOTTLE, DRAGON_BREATH, LINGERING_MILK_BOTTLE);
			}
			// transfer
			if (STILL_MILK != null) {
				FluidStorage.combinedItemApiProvider(LINGERING_MILK_BOTTLE).register(context ->
						new FullItemFluidStorage(context, bottle -> ItemVariant.of(GLASS_BOTTLE), FluidVariant.of(STILL_MILK), FluidConstants.BOTTLE)
				);
			}
			// dispenser interactions
			DispenserBlock.registerBehavior(LINGERING_MILK_BOTTLE, MilkPotionDispenserBehavior.INSTANCE);
		}
	}

	public static void enableAllMilkBottles() {
		enableMilkBottle();
		enableSplashMilkBottle();
		enableLingeringMilkBottle();
	}

	public static boolean isMilk(FluidState state) {
		return state.isOf(Milk.STILL_MILK) || state.isOf(Milk.FLOWING_MILK);
	}

	public static boolean isMilkBottle(Item item) {
		return item == Milk.MILK_BOTTLE || item == Milk.SPLASH_MILK_BOTTLE || item == Milk.LINGERING_MILK_BOTTLE;
	}

	public static boolean tryRemoveRandomEffect(LivingEntity user) {
		if (user.getStatusEffects().size() > 0) {
			int indexOfEffectToRemove = user.world.random.nextInt(user.getStatusEffects().size());
			StatusEffectInstance effectToRemove = (StatusEffectInstance) user.getStatusEffects().toArray()[indexOfEffectToRemove];
			user.removeStatusEffect(effectToRemove.getEffectType());
			return true;
		}
		return false;
	}

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}
}
