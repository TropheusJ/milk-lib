package io.github.tropheusj.milk.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.tropheusj.milk.Milk;
import net.minecraft.item.Item;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.recipe.Ingredient;

@Mixin(BrewingRecipeRegistry.class)
public abstract class BrewingRecipeRegistryMixin {
	@Shadow
	@Final
	private static List<Ingredient> POTION_TYPES;

	@Shadow
	@Final
	private static List<BrewingRecipeRegistry.Recipe<Item>> ITEM_RECIPES;

	@Inject(at = @At("HEAD"), method = "registerPotionType", cancellable = true)
	private static void registerPotionType(Item item, CallbackInfo ci) {
		if (isMilkBottle(item)) {
			POTION_TYPES.add(Ingredient.ofItems(item));
			ci.cancel();
		}
	}

	@Inject(at = @At("HEAD"), method = "registerItemRecipe", cancellable = true)
	private static void registerItemRecipe(Item input, Item ingredient, Item output, CallbackInfo ci) {
		if (isMilkBottle(input) && isMilkBottle(output)) {
			ITEM_RECIPES.add(new BrewingRecipeRegistry.Recipe<>(input, Ingredient.ofItems(ingredient), output));
			ci.cancel();
		}
	}

	private static boolean isMilkBottle(Item item) {
		return item == Milk.MILK_BOTTLE || item == Milk.SPLASH_MILK_BOTTLE || item == Milk.LINGERING_MILK_BOTTLE;
	}
}
