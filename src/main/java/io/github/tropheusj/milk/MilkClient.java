package io.github.tropheusj.milk;

import static io.github.tropheusj.milk.Milk.FLOWING_MILK;
import static io.github.tropheusj.milk.Milk.STILL_MILK;
import static io.github.tropheusj.milk.Milk.id;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.Identifier;

public class MilkClient implements ClientModInitializer {
	public static void setupFluidRendering(final Fluid still, final Fluid flowing, final Identifier textureBase) {
		final Identifier stillTexture = new Identifier(textureBase.getNamespace(), "block/" + textureBase.getPath() + "_still");
		final Identifier flowingTexture = new Identifier(textureBase.getNamespace(), "block/" + textureBase.getPath() + "_flow");

		FluidRenderHandler handler = new SimpleFluidRenderHandler(stillTexture, flowingTexture);
		FluidRenderHandlerRegistry.INSTANCE.register(still, handler);
		FluidRenderHandlerRegistry.INSTANCE.register(flowing, handler);
	}

	@Override
	public void onInitializeClient() {
		if (STILL_MILK != null) {
			setupFluidRendering(STILL_MILK, FLOWING_MILK, id("milk"));
			BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(), STILL_MILK, FLOWING_MILK);
		}
	}
}
