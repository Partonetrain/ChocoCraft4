package net.chococraft.forge.client;

import net.chococraft.ChococraftClient;
import net.chococraft.client.gui.ChocoboInventoryScreen;
import net.chococraft.client.models.armor.ChocoDisguiseModel;
import net.chococraft.client.models.entities.AdultChocoboModel;
import net.chococraft.client.models.entities.ChicoboModel;
import net.chococraft.client.renderer.entities.ChocoboRenderer;
import net.chococraft.common.inventory.SaddleBagMenu;
import net.chococraft.registry.ModEntities;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterMenuScreensEvent;
import net.chococraft.registry.ModMenus;

public class ForgeClientHandler {
	public static void registerEntityRenders(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(ModEntities.CHOCOBO.get(), ChocoboRenderer::new);
	}

	public static void registerMenuScreen(RegisterMenuScreensEvent event) {
		event.register(ModMenus.CHOCOBO.get(), ChocoboInventoryScreen::new);
	}

	public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(ChococraftClient.CHOCOBO, () -> AdultChocoboModel.createBodyLayer());
		event.registerLayerDefinition(ChococraftClient.CHICOBO, () -> ChicoboModel.createBodyLayer());
		event.registerLayerDefinition(ChococraftClient.CHOCO_DISGUISE, () -> ChocoDisguiseModel.createArmorDefinition());
	}
}
