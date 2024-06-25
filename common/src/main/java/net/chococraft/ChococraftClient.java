package net.chococraft;

import dev.architectury.registry.menu.MenuRegistry;
import net.chococraft.client.gui.ChocoboInventoryScreen;
import net.chococraft.registry.ModMenus;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class ChococraftClient {
	public static final ModelLayerLocation CHOCOBO = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Chococraft.MOD_ID, "main"), "chocobo");
	public static final ModelLayerLocation CHICOBO = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Chococraft.MOD_ID, "main"), "chicobo");
	public static final ModelLayerLocation CHOCO_DISGUISE = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Chococraft.MOD_ID, "main"), "choco_disguise");

	public static void init() {
		Chococraft.LOGGER.info("Registering chocobo screen");
		MenuRegistry.registerScreenFactory(ModMenus.CHOCOBO.get(), ChocoboInventoryScreen::new);
	}
}
