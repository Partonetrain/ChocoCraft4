package net.chococraft.client.renderer.entities;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import net.chococraft.Chococraft;
import net.chococraft.ChococraftClient;
import net.chococraft.client.models.entities.AdultChocoboModel;
import net.chococraft.client.models.entities.ChicoboModel;
import net.chococraft.client.renderer.layers.LayerCollar;
import net.chococraft.client.renderer.layers.LayerPlumage;
import net.chococraft.client.renderer.layers.LayerSaddle;
import net.chococraft.common.entity.AbstractChocobo;
import net.chococraft.common.entity.properties.ChocoboColor;
import net.minecraft.Util;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class ChocoboRenderer extends MobRenderer<AbstractChocobo, EntityModel<AbstractChocobo>> {
	private static final Map<ChocoboColor, ResourceLocation> CHOCOBO_PER_COLOR = Util.make(Maps.newHashMap(), (map) -> {
		map.put(ChocoboColor.YELLOW, ResourceLocation.fromNamespaceAndPath(Chococraft.MOD_ID, "textures/entities/chocobos/yellowchocobo.png"));
		map.put(ChocoboColor.GREEN, ResourceLocation.fromNamespaceAndPath(Chococraft.MOD_ID, "textures/entities/chocobos/greenchocobo.png"));
		map.put(ChocoboColor.BLUE, ResourceLocation.fromNamespaceAndPath(Chococraft.MOD_ID, "textures/entities/chocobos/bluechocobo.png"));
		map.put(ChocoboColor.WHITE, ResourceLocation.fromNamespaceAndPath(Chococraft.MOD_ID, "textures/entities/chocobos/whitechocobo.png"));
		map.put(ChocoboColor.BLACK, ResourceLocation.fromNamespaceAndPath(Chococraft.MOD_ID, "textures/entities/chocobos/blackchocobo.png"));
		map.put(ChocoboColor.GOLD, ResourceLocation.fromNamespaceAndPath(Chococraft.MOD_ID, "textures/entities/chocobos/goldchocobo.png"));
		map.put(ChocoboColor.PINK, ResourceLocation.fromNamespaceAndPath(Chococraft.MOD_ID, "textures/entities/chocobos/pinkchocobo.png"));
		map.put(ChocoboColor.RED, ResourceLocation.fromNamespaceAndPath(Chococraft.MOD_ID, "textures/entities/chocobos/redchocobo.png"));
		map.put(ChocoboColor.PURPLE, ResourceLocation.fromNamespaceAndPath(Chococraft.MOD_ID, "textures/entities/chocobos/purplechocobo.png"));
		map.put(ChocoboColor.FLAME, ResourceLocation.fromNamespaceAndPath(Chococraft.MOD_ID, "textures/entities/chocobos/flamechocobo.png"));
	});
	private static final Map<ChocoboColor, ResourceLocation> CHICOBO_PER_COLOR = Util.make(Maps.newHashMap(), (map) -> {
		map.put(ChocoboColor.YELLOW, ResourceLocation.fromNamespaceAndPath(Chococraft.MOD_ID, "textures/entities/chicobos/yellowchocobo.png"));
		map.put(ChocoboColor.GREEN, ResourceLocation.fromNamespaceAndPath(Chococraft.MOD_ID, "textures/entities/chicobos/greenchocobo.png"));
		map.put(ChocoboColor.BLUE, ResourceLocation.fromNamespaceAndPath(Chococraft.MOD_ID, "textures/entities/chicobos/bluechocobo.png"));
		map.put(ChocoboColor.WHITE, ResourceLocation.fromNamespaceAndPath(Chococraft.MOD_ID, "textures/entities/chicobos/whitechocobo.png"));
		map.put(ChocoboColor.BLACK, ResourceLocation.fromNamespaceAndPath(Chococraft.MOD_ID, "textures/entities/chicobos/blackchocobo.png"));
		map.put(ChocoboColor.GOLD, ResourceLocation.fromNamespaceAndPath(Chococraft.MOD_ID, "textures/entities/chicobos/goldchocobo.png"));
		map.put(ChocoboColor.PINK, ResourceLocation.fromNamespaceAndPath(Chococraft.MOD_ID, "textures/entities/chicobos/pinkchocobo.png"));
		map.put(ChocoboColor.RED, ResourceLocation.fromNamespaceAndPath(Chococraft.MOD_ID, "textures/entities/chicobos/redchocobo.png"));
		map.put(ChocoboColor.PURPLE, ResourceLocation.fromNamespaceAndPath(Chococraft.MOD_ID, "textures/entities/chicobos/purplechocobo.png"));
		map.put(ChocoboColor.FLAME, ResourceLocation.fromNamespaceAndPath(Chococraft.MOD_ID, "textures/entities/chicobos/flamechocobo.png"));
	});

	private final EntityModel<AbstractChocobo> chicoboModel;
	private final EntityModel<AbstractChocobo> chocoboModel = this.getModel();

	public ChocoboRenderer(EntityRendererProvider.Context context) {
		super(context, new AdultChocoboModel<>(context.bakeLayer(ChococraftClient.CHOCOBO)), 1.0f);
		this.chicoboModel = new ChicoboModel<>(context.bakeLayer(ChococraftClient.CHICOBO));

		this.addLayer(new LayerCollar(this));
		this.addLayer(new LayerPlumage(this));
		this.addLayer(new LayerSaddle(this));
	}

	@Override
	public void render(AbstractChocobo chocobo, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		this.model = chocobo.isBaby() ? chicoboModel : chocoboModel;
		super.render(chocobo, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
	}

	@Override
	public ResourceLocation getTextureLocation(AbstractChocobo chocobo) {
		ChocoboColor color = chocobo.getChocoboColor();
		return chocobo.isBaby() ? CHICOBO_PER_COLOR.get(color) : CHOCOBO_PER_COLOR.get(color);
	}
}
