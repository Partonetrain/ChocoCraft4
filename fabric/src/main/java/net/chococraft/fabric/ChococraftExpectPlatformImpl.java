package net.chococraft.fabric;

import com.google.gson.internal.LinkedTreeMap;
import net.chococraft.common.entity.AbstractChocobo;
import net.chococraft.common.entity.properties.ChocoboColor;
import net.chococraft.common.entity.properties.MovementType;
import net.chococraft.common.inventory.SaddleBagMenu;
import net.chococraft.common.items.armor.AbstractChocoDisguiseItem;
import net.chococraft.fabric.common.entity.FabricChocobo;
import net.chococraft.fabric.common.inventory.FabricSaddleBagMenu;
import net.chococraft.fabric.common.items.FabricChocoDisguiseItem;
import net.chococraft.fabric.registry.ModDataSerializers;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;

import java.nio.file.Path;
import java.util.List;

public class ChococraftExpectPlatformImpl {
	public static Path getConfigDirectory() {
		return FabricLoader.getInstance().getConfigDir();
	}

	public static boolean isModLoaded(String modID) {
		return FabricLoader.getInstance().isModLoaded(modID);
	}

	public static EntityType.Builder<? extends AbstractChocobo> constructChocoboEntityType() {
		return EntityType.Builder.of(FabricChocobo::new, MobCategory.CREATURE).sized(1.2f, 2.8f).clientTrackingRange(64);
	}

	public static SaddleBagMenu constructMenu(int i, Inventory inventory, FriendlyByteBuf friendlyByteBuf) {
		return FabricSaddleBagMenu.create(i, inventory, friendlyByteBuf);
	}

	public static SaddleBagMenu constructMenu(int i, Inventory inventory, AbstractChocobo abstractChocobo) {
		return new FabricSaddleBagMenu(i, inventory, (FabricChocobo) abstractChocobo);
	}

	public static AbstractChocoDisguiseItem constructChocoDisguise(Holder<ArmorMaterial> material, ArmorItem.Type type, Item.Properties properties) {
		return new FabricChocoDisguiseItem(material, type, properties);
	}

	public static LinkedTreeMap<String, LinkedTreeMap<String, List<LinkedTreeMap<String, String>>>> getBreedingInfoMap() {
		return ChococraftFabric.breedingConfig.get().breedingInfo;
	}

	public static float getTameChance() {
		return (float) ChococraftFabric.config.get().chocobo.tameChance;
	}

	public static boolean canChocobosFly() {
		return ChococraftFabric.config.get().chocobo.canChocobosFly;
	}

	public static int kwehIntervalLimit() {
		return ChococraftFabric.config.get().chocobo.kwehIntervalLimit;
	}

	public static boolean requireEmptyHand() {
		return ChococraftFabric.config.get().chocobo.requireEmptyHand;
	}

	public static boolean genderless() {
		return ChococraftFabric.config.get().chocobo.genderless;
	}

	public static boolean nameTamedChocobos() {
		return ChococraftFabric.config.get().naming.nameTamedChocobos;
	}

	public static List<? extends String> getConfiguredMaleNames() {
		return ChococraftFabric.config.get().naming.maleNames;
	}

	public static List<? extends String> getConfiguredFemaleNames() {
		return ChococraftFabric.config.get().naming.femaleNames;
	}

	public static EntityDataSerializer<ChocoboColor> getColorSerializer() {
		return ModDataSerializers.CHOCOBO_COLOR;
	}

	public static EntityDataSerializer<MovementType> getMovementSerializer() {
		return ModDataSerializers.MOVEMENT_TYPE;
	}
}
