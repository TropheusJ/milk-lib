package io.github.tropheusj.milk;

import io.github.tropheusj.milk.potion.MilkAreaEffectCloudEntity;
import io.github.tropheusj.milk.potion.bottle.LingeringMilkBottle;
import io.github.tropheusj.milk.potion.bottle.MilkBottle;
import io.github.tropheusj.milk.potion.bottle.SplashMilkBottle;
import io.github.tropheusj.milk.mixin.BrewingRecipeRegistryAccessor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.CauldronFluidContent;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.FullItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import static net.minecraft.item.Items.BUCKET;
import static net.minecraft.item.Items.DRAGON_BREATH;
import static net.minecraft.item.Items.GLASS_BOTTLE;
import static net.minecraft.item.Items.GUNPOWDER;
import static net.minecraft.item.Items.MILK_BUCKET;

@SuppressWarnings("UnstableApiUsage")
public class Milk {
	public static final String MOD_ID = "milk";
	public static final FlowableFluid STILL_MILK = new MilkFluid.Still();
	public static final FlowableFluid FLOWING_MILK = new MilkFluid.Flowing();
	public static final Block MILK_FLUID_BLOCK = new MilkFluidBlock(STILL_MILK, FabricBlockSettings.copyOf(Blocks.WATER).mapColor(MapColor.WHITE));

	public static Block MILK_CAULDRON = new MilkCauldron(FabricBlockSettings.copyOf(Blocks.CAULDRON));
	public static boolean CAULDRON_ENABLED = false;

	public static Item MILK_BOTTLE = new MilkBottle(new FabricItemSettings().recipeRemainder(Items.GLASS_BOTTLE).maxCount(1).group(ItemGroup.BREWING));
	public static boolean MILK_BOTTLE_ENABLED = false;

	public static Item SPLASH_MILK_BOTTLE = new SplashMilkBottle(new FabricItemSettings().maxCount(1).group(ItemGroup.BREWING));
	public static boolean SPLASH_MILK_BOTTLE_ENABLED = false;

	public static Item LINGERING_MILK_BOTTLE = new LingeringMilkBottle(new FabricItemSettings().maxCount(1).group(ItemGroup.BREWING));
	public static boolean LINGERING_MILK_BOTTLE_ENABLED = false;

	public static boolean MILK_BOTTLE_CAULDRON_BEHAVIOR = false;

	public static boolean FLUID_ENABLED = false;

	public static EntityType<MilkAreaEffectCloudEntity> MILK_EFFECT_CLOUD_ENTITY_TYPE = FabricEntityTypeBuilder.<MilkAreaEffectCloudEntity>create()
			.fireImmune()
			.dimensions(EntityDimensions.fixed(6.0F, 0.5F))
			.trackRangeChunks(10)
			.trackedUpdateRate(Integer.MAX_VALUE)
			.build();

	public static void enableMilkFluids() {
		if (!FLUID_ENABLED) {
			Registry.register(Registry.FLUID, id("still_milk"), STILL_MILK);
			Registry.register(Registry.FLUID, id("flowing_milk"), FLOWING_MILK);
			Registry.register(Registry.BLOCK, id("milk_fluid_block"), MILK_FLUID_BLOCK);
			FluidStorage.combinedItemApiProvider(MILK_BUCKET).register(context ->
					new FullItemFluidStorage(context, bucket -> ItemVariant.of(BUCKET), FluidVariant.of(STILL_MILK), FluidConstants.BUCKET)
			);
			if (CAULDRON_ENABLED) {
				CauldronFluidContent.registerCauldron(MILK_CAULDRON, STILL_MILK, FluidConstants.BOTTLE, LeveledCauldronBlock.LEVEL);
			}
			if (MILK_BOTTLE_ENABLED) {
				FluidStorage.combinedItemApiProvider(MILK_BOTTLE).register(context ->
						new FullItemFluidStorage(context, bottle -> ItemVariant.of(GLASS_BOTTLE), FluidVariant.of(STILL_MILK), FluidConstants.BOTTLE)
				);
			}
			FLUID_ENABLED = true;
		}
	}

	public static void enableCauldron() {
		if (!CAULDRON_ENABLED) {
			Registry.register(Registry.BLOCK, id("milk_cauldron"), MILK_CAULDRON);
			CauldronBehavior.EMPTY_CAULDRON_BEHAVIOR.put(MILK_BUCKET, MilkCauldron.FILL_FROM_BUCKET);
			if (FLUID_ENABLED) {
				CauldronFluidContent.registerCauldron(MILK_CAULDRON, STILL_MILK, FluidConstants.BOTTLE, LeveledCauldronBlock.LEVEL);
			}
			if (MILK_BOTTLE_ENABLED && !MILK_BOTTLE_CAULDRON_BEHAVIOR) {
				CauldronBehavior.EMPTY_CAULDRON_BEHAVIOR.put(MILK_BOTTLE, MilkCauldron.FILL_FROM_BOTTLE);
				MilkCauldron.MILK_CAULDRON_BEHAVIOR.put(Milk.MILK_BOTTLE, MilkCauldron.FILL_FROM_BOTTLE);
				MilkCauldron.MILK_CAULDRON_BEHAVIOR.put(Items.GLASS_BOTTLE, MilkCauldron.EMPTY_TO_BOTTLE);
				MILK_BOTTLE_CAULDRON_BEHAVIOR = true;
			}
		}
		CAULDRON_ENABLED = true;
	}

	public static void enableMilkBottle() {
		if (!MILK_BOTTLE_ENABLED) {
			Registry.register(Registry.ITEM, id("milk_bottle"), MILK_BOTTLE);
			BrewingRecipeRegistryAccessor.invokeRegisterPotionType(MILK_BOTTLE);
			if (FLUID_ENABLED) {
				FluidStorage.combinedItemApiProvider(MILK_BOTTLE).register(context ->
						new FullItemFluidStorage(context, bottle -> ItemVariant.of(GLASS_BOTTLE), FluidVariant.of(STILL_MILK), FluidConstants.BOTTLE)
				);
			}
			if (CAULDRON_ENABLED && !MILK_BOTTLE_CAULDRON_BEHAVIOR) {
				CauldronBehavior.EMPTY_CAULDRON_BEHAVIOR.put(MILK_BOTTLE, MilkCauldron.FILL_FROM_BOTTLE);
				MilkCauldron.MILK_CAULDRON_BEHAVIOR.put(Milk.MILK_BOTTLE, MilkCauldron.FILL_FROM_BOTTLE);
				MilkCauldron.MILK_CAULDRON_BEHAVIOR.put(Items.GLASS_BOTTLE, MilkCauldron.EMPTY_TO_BOTTLE);
				MILK_BOTTLE_CAULDRON_BEHAVIOR = true;
			}
			MILK_BOTTLE_ENABLED = true;
		}
	}

	public static void enableSplashMilkBottle() {
		if (!SPLASH_MILK_BOTTLE_ENABLED) {
			Registry.register(Registry.ITEM, id("splash_milk_bottle"), SPLASH_MILK_BOTTLE);
			BrewingRecipeRegistryAccessor.invokeRegisterPotionType(SPLASH_MILK_BOTTLE);
			if (MILK_BOTTLE_ENABLED) {
				BrewingRecipeRegistryAccessor.invokeRegisterItemRecipe(MILK_BOTTLE, GUNPOWDER, SPLASH_MILK_BOTTLE);
			}
			SPLASH_MILK_BOTTLE_ENABLED = true;
		}
	}

	public static void enableLingeringMilkBottle() {
		if (!LINGERING_MILK_BOTTLE_ENABLED) {
			Registry.register(Registry.ITEM, id("lingering_milk_bottle"), LINGERING_MILK_BOTTLE);
			BrewingRecipeRegistryAccessor.invokeRegisterPotionType(LINGERING_MILK_BOTTLE);
			Registry.register(Registry.ENTITY_TYPE, id("milk_area_effect_cloud"), MILK_EFFECT_CLOUD_ENTITY_TYPE);
			if (SPLASH_MILK_BOTTLE_ENABLED) {
				BrewingRecipeRegistryAccessor.invokeRegisterItemRecipe(SPLASH_MILK_BOTTLE, DRAGON_BREATH, LINGERING_MILK_BOTTLE);
			}
			LINGERING_MILK_BOTTLE_ENABLED = true;
		}
	}

	public static void enableAllMilkBottles() {
		enableMilkBottle();
		enableSplashMilkBottle();
		enableLingeringMilkBottle();
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
