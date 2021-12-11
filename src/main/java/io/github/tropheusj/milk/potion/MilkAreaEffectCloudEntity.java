package io.github.tropheusj.milk.potion;

import io.github.tropheusj.milk.Milk;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class MilkAreaEffectCloudEntity extends AreaEffectCloudEntity {
	public MilkAreaEffectCloudEntity(World world, double d, double e, double f) {
		super(world, d, e, f);
		setColor(0xFFFFFF);
	}

	@Override
	public void tick() {
		boolean bl = this.isWaiting();
		float f = this.getRadius();
		if (this.world.isClient) {
			if (bl && this.random.nextBoolean()) {
				return;
			}

			ParticleEffect particleEffect = this.getParticleType();
			int i;
			float g;
			if (bl) {
				i = 2;
				g = 0.2F;
			} else {
				i = MathHelper.ceil((float) Math.PI * f * f);
				g = f;
			}

			for(int k = 0; k < i; ++k) {
				float l = this.random.nextFloat() * (float) (Math.PI * 2);
				float m = MathHelper.sqrt(this.random.nextFloat()) * g;
				double d = this.getX() + (double)(MathHelper.cos(l) * m);
				double e = this.getY();
				double n = this.getZ() + (double)(MathHelper.sin(l) * m);
				double s;
				double t;
				double u;
				if (particleEffect.getType() != ParticleTypes.ENTITY_EFFECT) {
					if (bl) {
						s = 0.0;
						t = 0.0;
						u = 0.0;
					} else {
						s = (0.5 - this.random.nextDouble()) * 0.15;
						t = 0.01F;
						u = (0.5 - this.random.nextDouble()) * 0.15;
					}
				} else {
					int o = bl && this.random.nextBoolean() ? 16777215 : this.getColor();
					s = ((float)(o >> 16 & 0xFF) / 255.0F);
					t = ((float)(o >> 8 & 0xFF) / 255.0F);
					u = ((float)(o & 0xFF) / 255.0F);
				}

				this.world.addImportantParticle(particleEffect, d, e, n, s, t, u);
			}
		} else {
			if (this.age >= getWaitTime() + getDuration()) {
				this.discard();
				return;
			}

			boolean bl2 = this.age < getWaitTime();
			if (bl != bl2) {
				this.setWaiting(bl2);
			}

			if (bl2) {
				return;
			}

			if (getRadiusGrowth() != 0.0F) {
				f += getRadiusGrowth();
				if (f < 0.5F) {
					this.discard();
					return;
				}

				this.setRadius(f);
			}

			if (this.age % 5 == 0) {
				world.getOtherEntities(this, getBoundingBox().expand(2)).forEach(entity -> {
					if (entity instanceof LivingEntity livingEntity) {
						Milk.tryRemoveRandomEffect(livingEntity);
					}
				});
			}
		}
	}
}
