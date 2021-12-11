package io.github.tropheusj.milk.mixin;

import java.util.List;

import io.github.tropheusj.milk.Milk;

import io.github.tropheusj.milk.potion.MilkAreaEffectCloudEntity;
import io.github.tropheusj.milk.potion.bottle.LingeringMilkBottle;
import io.github.tropheusj.milk.potion.bottle.PotionItemEntityExtensions;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.Potion;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

@Mixin(PotionEntity.class)
public abstract class PotionEntityMixin extends ThrownItemEntity implements FlyingItemEntity, PotionItemEntityExtensions {
	@Shadow
	protected abstract void damageEntitiesHurtByWater();

	@Shadow
	protected abstract void applySplashPotion(List<StatusEffectInstance> statusEffects, @Nullable Entity entity);

	@Shadow protected abstract void applyLingeringPotion(ItemStack stack, Potion potion);

	@Shadow protected abstract void extinguishFire(BlockPos pos);

	@Unique
	private boolean milk = false;

	public PotionEntityMixin(EntityType<? extends ThrownItemEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(method = "onBlockHit", at = @At(value = "HEAD"))
	protected void onBlockHit(BlockHitResult blockHitResult, CallbackInfo ci) {
		if (isMilk()) {
			Direction side = blockHitResult.getSide();
			BlockPos pos = blockHitResult.getBlockPos().offset(side);
			this.extinguishFire(pos);
			this.extinguishFire(pos.offset(side.getOpposite()));

			for(Direction direction2 : Direction.Type.HORIZONTAL) {
				this.extinguishFire(pos.offset(direction2));
			}
		}
	}

	@Inject(method = "onCollision", at = @At(value = "HEAD"), cancellable = true)
	protected void onCollision(HitResult hitResult, CallbackInfo ci) {
		if (isMilk()) {
			super.onCollision(hitResult);
			if (!this.world.isClient) {
				damageEntitiesHurtByWater();
				if (getItem().getItem() instanceof LingeringMilkBottle) {
					applyLingeringPotion(null, null);
				} else {
					applySplashPotion(null, hitResult.getType() == HitResult.Type.ENTITY ? ((EntityHitResult) hitResult).getEntity() : null);
				}

				this.world.syncWorldEvent(WorldEvents.INSTANT_SPLASH_POTION_SPLASHED, this.getBlockPos(), 0xFFFFFF);
				this.discard();
			}
			ci.cancel();
		}
	}

	@Inject(method = "applySplashPotion", at = @At("HEAD"), cancellable = true)
	private void applySplashPotion(List<StatusEffectInstance> statusEffects, Entity entity, CallbackInfo ci) {
		if (isMilk()) {
			Box box = this.getBoundingBox().expand(4.0, 2.0, 4.0);
			List<LivingEntity> list = this.world.getNonSpectatingEntities(LivingEntity.class, box);
			if (!list.isEmpty()) {
				for (LivingEntity livingEntity : list) {
					if (livingEntity.isAffectedBySplashPotions()) {
						double d = this.squaredDistanceTo(livingEntity);
						if (d < 16.0) {
							Milk.tryRemoveRandomEffect(livingEntity);
						}
					}
				}
			}
			ci.cancel();
		}
	}

	@Inject(method = "applyLingeringPotion", at = @At("HEAD"), cancellable = true)
	private void applyLingeringPotion(ItemStack stack, Potion potion, CallbackInfo ci) {
		if (isMilk()) {
			MilkAreaEffectCloudEntity areaEffectCloudEntity = new MilkAreaEffectCloudEntity(this.world, this.getX(), this.getY(), this.getZ());
			Entity entity = this.getOwner();
			if (entity instanceof LivingEntity) {
				areaEffectCloudEntity.setOwner((LivingEntity) entity);
			}

			areaEffectCloudEntity.setRadius(3.0F);
			areaEffectCloudEntity.setRadiusOnUse(-0.5F);
			areaEffectCloudEntity.setWaitTime(10);
			areaEffectCloudEntity.setRadiusGrowth(-areaEffectCloudEntity.getRadius() / (float) areaEffectCloudEntity.getDuration());

			this.world.spawnEntity(areaEffectCloudEntity);
			ci.cancel();
		}
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		nbt.putBoolean("Milk", milk);
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		setMilk(nbt.getBoolean("Milk"));
	}

	@Override
	public boolean isMilk() {
		return milk;
	}

	@Override
	public void setMilk(boolean value) {
		milk = value;
	}
}
