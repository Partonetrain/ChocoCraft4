package net.chococraft.neoforge.common.config;

import net.chococraft.Chococraft;
import net.chococraft.common.config.ChocoConfig;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class NeoForgeChocoConfig {
	public static class Common {

		public final IntValue chocoboSpawnWeight;
		public final IntValue chocoboPackSizeMin;
		public final IntValue chocoboPackSizeMax;

		public final DoubleValue tameChance;

		public final ModConfigSpec.BooleanValue nameTamedChocobos;
		public final ModConfigSpec.BooleanValue canChocobosFly;
		public final IntValue kwehIntervalLimit;
		public final ModConfigSpec.BooleanValue requireEmptyHand;
		public final ModConfigSpec.BooleanValue genderless;
		public final ModConfigSpec.ConfigValue<List<? extends String>> maleNames;
		public final ModConfigSpec.ConfigValue<List<? extends String>> femaleNames;

		Common(ModConfigSpec.Builder builder) {
			builder.comment("Spawning configuration")
					.push("spawning");

			chocoboSpawnWeight = builder
					.comment("Controls Chocobo Spawn Weight [Default: 10]")
					.defineInRange("chocoboSpawnWeight", 10, 0, Integer.MAX_VALUE);

			chocoboPackSizeMin = builder
					.comment("Controls Chocobo Pack Size Min [Default: 1]")
					.defineInRange("chocoboPackSizeMin", 1, 0, Integer.MAX_VALUE);

			chocoboPackSizeMax = builder
					.comment("Controls Chocobo Pack Size Max [Default: 3]")
					.defineInRange("chocoboPackSizeMax", 3, 0, Integer.MAX_VALUE);

			builder.pop();

			builder.comment("Chocobo configuration")
					.push("Chocobo");

			tameChance = builder
					.comment("This multiplier controls the tame chance per gysahl used, so .15 results in 15% chance to tame [Default: 0.15]")
					.defineInRange("tameChance", 0.15, 0, 1);

			canChocobosFly = builder
					.comment("If certain chocobos are allowed to fly [Default: true]")
					.define("canChocobosFly", true);

			kwehIntervalLimit = builder
					.comment("Determines the maximum interval duration for the Chocobo's ambient sound [Default: 100]")
					.defineInRange("kwehIntervalLimit", 100, 1, Integer.MAX_VALUE);

			requireEmptyHand = builder
					.comment("If mounting a tamed chocobo requires an empty hand [Default: true]")
					.define("requireEmptyHand", true);

			genderless = builder
					.comment("If chocobo gender is purely cosmetic [Default: false]")
					.define("genderless", false);

			builder.pop();
			builder.comment("Naming configuration")
					.push("Naming");

			nameTamedChocobos = builder
					.comment("If taming a chocobo will provide them with a name (unless already named) [Default: true]")
					.define("nameTamedChocobos", true);

			maleNames = builder
					.comment("The list of male names it can choose from if 'nameTamedChocobos' is enabled")
					.defineList("maleNames", List.of(ChocoConfig.maleNames), o -> (o instanceof String));

			femaleNames = builder
					.comment("The list of female names it can choose from if 'nameTamedChocobos' is enabled")
					.defineList("femaleNames", List.of(ChocoConfig.femaleNames), o -> (o instanceof String));

			builder.pop();
		}
	}

	public static final ModConfigSpec commonSpec;
	public static final Common COMMON;

	static {
		final Pair<Common, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(Common::new);
		commonSpec = specPair.getRight();
		COMMON = specPair.getLeft();
	}

	@SubscribeEvent
	public static void onLoad(final ModConfigEvent.Loading configEvent) {
		Chococraft.LOGGER.debug("Loaded Chococraft's config file {}", configEvent.getConfig().getFileName());
	}

	@SubscribeEvent
	public static void onFileChange(final ModConfigEvent.Reloading configEvent) {
		Chococraft.LOGGER.debug("Chococraft's config just got changed on the file system!");
		if (configEvent.getConfig().getModId().equals(Chococraft.MOD_ID)) {
			if (COMMON.chocoboPackSizeMin.get() > COMMON.chocoboPackSizeMax.get()) {
				Chococraft.LOGGER.error("Chocobo Pack Size Min {} is greater than Chocobo Pack Size Max {}. This is not allowed!",
						COMMON.chocoboPackSizeMin.get(), COMMON.chocoboPackSizeMax.get());
			}
		}
	}
}