package io.github.tropheusj.modid;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class ExampleMod implements ModInitializer {
	public static final String MOD_ID = "modid";
	public static String VERSION;

	@Override
	public void onInitialize() {
		VERSION = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow().getMetadata().getVersion().getFriendlyString();
	}
}
