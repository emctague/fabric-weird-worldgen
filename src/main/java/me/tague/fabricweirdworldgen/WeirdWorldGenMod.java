package me.tague.fabricweirdworldgen;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.decorator.ChanceDecoratorConfig;
import net.minecraft.world.gen.decorator.ConfiguredDecorator;
import net.minecraft.world.gen.decorator.Decorator;
import net.minecraft.world.gen.feature.*;

/**
 * Weird Worldgen Mod
 *
 * Configures every single biome to generate most of the structures the game has to offer.
 * Generally results in empty space being spammed with mesa mineshafts.
 */
public class WeirdWorldGenMod implements ModInitializer {

	/** Incremented each time a biome is set up - used to stagger various feature configuration options depending on the
	    active biome, while still maintaining some consistency across versions. */
	private static int biomeIndex = 0;

	@Override
	public void onInitialize() {
		// Set up for all existing biomes.
		for (Biome biome : Registry.BIOME) setupBiome(biome);

		// Set up new biomes when they're registered.
		RegistryEntryAddedCallback.event(Registry.BIOME).register((i, identifier, biome) -> setupBiome(biome));
	}

	/** Set up the given biome with an absurd number of structures. */
	private void setupBiome(Biome biome) {
		addStructureFeature(biome, Feature.END_CITY.configure(new DefaultFeatureConfig()));
		addFeature(biome, Feature.END_ISLAND.configure(new DefaultFeatureConfig()));
		addFeature(biome, Feature.CHORUS_PLANT.configure(new DefaultFeatureConfig()));

		// Select a village type in a round-robin.
		// There cannot be multiple village types in one biome - the latest VillageFeatureConfig to be added overwrites previous ones.
		String[] potentialVillageTypes = {"plains", "taiga", "snowy", "savanna", "desert"};
		String villageType = potentialVillageTypes[biomeIndex % potentialVillageTypes.length];

		addStructureFeature(biome, Feature.VILLAGE.configure(new VillageFeatureConfig("village/" + villageType + "/town_centers", 800)));

		addStructureFeature(biome, Feature.PILLAGER_OUTPOST.configure(new DefaultFeatureConfig()));
		addStructureFeature(biome, Feature.WOODLAND_MANSION.configure(new DefaultFeatureConfig()));

		// Only one type can exist per biome, and Mesa makes for a more... "interesting" landscape, so we make it more common.
		MineshaftFeature.Type mineshaftType = biomeIndex % 3 == 1 ? MineshaftFeature.Type.NORMAL : MineshaftFeature.Type.MESA;
		addStructureFeature(biome, Feature.MINESHAFT.configure(new MineshaftFeatureConfig(70.0f, mineshaftType)));

		addStructureFeature(biome, Feature.STRONGHOLD.configure(new DefaultFeatureConfig()));
		addFeature(biome, Feature.MONSTER_ROOM.configure(new DefaultFeatureConfig()));

		addFeature(biome, Feature.GLOWSTONE_BLOB.configure(new DefaultFeatureConfig()));

		addStructureFeature(biome, Feature.JUNGLE_TEMPLE.configure(new DefaultFeatureConfig()));
		addFeature(biome, Feature.DESERT_WELL.configure(new DefaultFeatureConfig()));
		addStructureFeature(biome, Feature.DESERT_PYRAMID.configure(new DefaultFeatureConfig()));
		addStructureFeature(biome, Feature.NETHER_BRIDGE.configure(new DefaultFeatureConfig()));

		addStructureFeature(biome, Feature.SHIPWRECK.configure(new ShipwreckFeatureConfig(true)));
		addStructureFeature(biome, Feature.SHIPWRECK.configure(new ShipwreckFeatureConfig(false)));
		addStructureFeature(biome, Feature.OCEAN_MONUMENT.configure(new DefaultFeatureConfig()));
		addStructureFeature(biome, Feature.OCEAN_RUIN.configure(new OceanRuinFeatureConfig(OceanRuinFeature.BiomeType.WARM, 50.0f, 50.0f)));

		addStructureFeature(biome, Feature.SWAMP_HUT.configure(new DefaultFeatureConfig()));

		biomeIndex++;
	}

	/** Add a configured Structure feature to the given biome. Most added Features are Structure features. */
	private <C extends FeatureConfig, X extends StructureFeature<C>> void addStructureFeature (Biome biome, ConfiguredFeature<C, X> fc) {
		if (biome.getCategory() != Biome.Category.THEEND) {
			biome.addStructureFeature(fc);
			addFeature(biome, fc);
		}
	}

	/** Add a configured feature to the given biome, giving it a (supposedly, but doesn't seem to work) 100% chance of
	    generating, and making it attempt to generate the feature at various stages of generation. */
	private <C extends FeatureConfig, X extends Feature<C>> void addFeature (Biome biome, ConfiguredFeature<C, X> fc) {
		if (biome.getCategory() != Biome.Category.THEEND) {
			ConfiguredFeature<?, ?> decoratedFeature = fc.createDecoratedFeature(new ConfiguredDecorator<ChanceDecoratorConfig>(Decorator.CHANCE_PASSTHROUGH, new ChanceDecoratorConfig(2)));
			biome.addFeature(GenerationStep.Feature.SURFACE_STRUCTURES, decoratedFeature);
			biome.addFeature(GenerationStep.Feature.UNDERGROUND_STRUCTURES, decoratedFeature);
			biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, decoratedFeature);
			biome.addFeature(GenerationStep.Feature.LOCAL_MODIFICATIONS, decoratedFeature);
		}
	}

}
