package io.github.tropheusj.milk.potion.bottle;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.LingeringPotionItem;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class LingeringMilkBottle extends LingeringPotionItem {
	public LingeringMilkBottle(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		world.playSound(null, user.getX(), user.getY(), user.getZ(),
				SoundEvents.ENTITY_LINGERING_POTION_THROW, SoundCategory.NEUTRAL,
				0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));

		ItemStack itemStack = user.getStackInHand(hand);
		if (!world.isClient) {
			PotionEntity potionEntity = new PotionEntity(world, user);
			potionEntity.setItem(itemStack);
			potionEntity.setProperties(user, user.getPitch(), user.getYaw(), -20.0F, 0.5F, 1.0F);
			((PotionItemEntityExtensions) potionEntity).setMilk(true);
			world.spawnEntity(potionEntity);
		}

		user.incrementStat(Stats.USED.getOrCreateStat(this));
		if (!user.getAbilities().creativeMode) {
			itemStack.decrement(1);
		}

		return TypedActionResult.success(itemStack, world.isClient());
	}

	@Override
	public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
		if (this.isIn(group)) {
			stacks.add(new ItemStack(this));
		}
	}

	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
	}

	@Override
	public String getTranslationKey(ItemStack stack) {
		return getTranslationKey();
	}
}
