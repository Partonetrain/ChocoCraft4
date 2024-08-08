package net.chococraft.fabric;

import dev.architectury.registry.client.rendering.RenderTypeRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import net.chococraft.ChococraftClient;
import net.chococraft.client.gui.ChocoboInventoryScreen;
import net.chococraft.client.models.armor.ChocoDisguiseModel;
import net.chococraft.client.models.entities.AdultChocoboModel;
import net.chococraft.client.models.entities.ChicoboModel;
import net.chococraft.client.renderer.entities.ChocoboRenderer;
import net.chococraft.common.entity.AbstractChocobo;
import net.chococraft.fabric.common.packets.OpenChocoboScreenPayload;
import net.chococraft.registry.ModEntities;
import net.chococraft.registry.ModMenus;
import net.chococraft.registry.ModRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

public class ChococraftClientFabric implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		EntityModelLayerRegistry.registerModelLayer(ChococraftClient.CHOCOBO, AdultChocoboModel::createBodyLayer);
		EntityModelLayerRegistry.registerModelLayer(ChococraftClient.CHICOBO, ChicoboModel::createBodyLayer);
		EntityModelLayerRegistry.registerModelLayer(ChococraftClient.CHOCO_DISGUISE, ChocoDisguiseModel::createArmorDefinition);

		EntityRendererRegistry.register(ModEntities.CHOCOBO.get(), ChocoboRenderer::new);

		ChococraftClient.init();

		ClientPlayNetworking.registerGlobalReceiver(OpenChocoboScreenPayload.ID, (payload, context) -> {
			Entity entity = context.client().level.getEntity(payload.entityId());
			if (entity instanceof AbstractHorse) {
				AbstractChocobo abstractChocobo = (AbstractChocobo) entity;
				ChocoboInventoryScreen.openInventory(payload.containerId(), abstractChocobo);
			}
		});
		ClientLifecycleEvents.CLIENT_STARTED.register(client ->
				MenuRegistry.registerScreenFactory(ModMenus.CHOCOBO.get(), ChocoboInventoryScreen::new));

		RenderTypeRegistry.register(RenderType.cutout(), ModRegistry.GYSAHL_GREEN.get());
	}
}
