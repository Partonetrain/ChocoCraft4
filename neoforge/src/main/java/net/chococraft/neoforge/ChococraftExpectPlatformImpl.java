package net.chococraft.neoforge;

import com.google.gson.internal.LinkedTreeMap;
import net.chococraft.common.entity.AbstractChocobo;
import net.chococraft.common.entity.properties.ChocoboColor;
import net.chococraft.common.entity.properties.MovementType;
import net.chococraft.common.inventory.SaddleBagMenu;
import net.chococraft.common.items.armor.AbstractChocoDisguiseItem;
import net.chococraft.neoforge.common.config.BreedingConfig;
import net.chococraft.neoforge.common.config.NeoForgeChocoConfig;
import net.chococraft.neoforge.common.entity.NeoForgeChocobo;
import net.chococraft.neoforge.common.inventory.NeoForgeSaddleBagMenu;
import net.chococraft.neoforge.common.items.NeoForgeChocoDisguiseItem;
import net.chococraft.neoforge.registry.ModDataSerializers;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.List;

public class ChococraftExpectPlatformImpl {
	public static Path getConfigDirectory() {
		return FMLPaths.CONFIGDIR.get();
	}

	public static boolean isModLoaded(String modID) {
		return ModList.get().isLoaded(modID);
	}

	public static EntityType.Builder<? extends AbstractChocobo> constructChocoboEntityType() {
		return EntityType.Builder.of(NeoForgeChocobo::new, MobCategory.CREATURE).sized(1.2f, 2.8f).clientTrackingRange(64);
	}

	public static SaddleBagMenu constructMenu(int i, Inventory inventory, FriendlyByteBuf friendlyByteBuf) {
		return NeoForgeSaddleBagMenu.create(i, inventory, friendlyByteBuf);
	}

	public static SaddleBagMenu constructMenu(int i, Inventory inventory, AbstractChocobo abstractChocobo) {
		return new NeoForgeSaddleBagMenu(i, inventory, (NeoForgeChocobo) abstractChocobo);
	}

	public static AbstractChocoDisguiseItem constructChocoDisguise(Holder<ArmorMaterial> material, ArmorItem.Type type, Item.Properties properties) {
		return new NeoForgeChocoDisguiseItem(material, type, properties);
	}

	public static LinkedTreeMap<String, LinkedTreeMap<String, List<LinkedTreeMap<String, String>>>> getBreedingInfoMap() {
		return BreedingConfig.breedingInfoHashmap;
	}

	public static float getTameChance() {
		return NeoForgeChocoConfig.COMMON.tameChance.get().floatValue();
	}

	public static boolean canChocobosFly() {
		return NeoForgeChocoConfig.COMMON.canChocobosFly.get();
	}

	public static int kwehIntervalLimit() {
		return NeoForgeChocoConfig.COMMON.kwehIntervalLimit.get();
	}

	public static boolean requireEmptyHand() {
		return NeoForgeChocoConfig.COMMON.requireEmptyHand.get();
	}

	public static boolean genderless() {
		return NeoForgeChocoConfig.COMMON.genderless.get();
	}

	public static boolean nameTamedChocobos() {
		return NeoForgeChocoConfig.COMMON.nameTamedChocobos.get();
	}

	public static List<? extends String> getConfiguredMaleNames() {
		return NeoForgeChocoConfig.COMMON.maleNames.get();
	}

	public static List<? extends String> getConfiguredFemaleNames() {
		return NeoForgeChocoConfig.COMMON.femaleNames.get();
	}

	public static EntityDataSerializer<ChocoboColor> getColorSerializer() {
		return ModDataSerializers.CHOCOBO_COLOR.get();
	}

	public static EntityDataSerializer<MovementType> getMovementSerializer() {
		return ModDataSerializers.MOVEMENT_TYPE.get();
	}
}
