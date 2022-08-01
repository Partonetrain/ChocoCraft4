package net.chococraft.client.renderer.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.chococraft.Chococraft;
import net.chococraft.client.models.entities.AdultChocoboModel;
import net.chococraft.common.entities.Chocobo;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

public class LayerPlumage extends RenderLayer<Chocobo, AdultChocoboModel<Chocobo>> {

	private ResourceLocation PLUMAGE = new ResourceLocation(Chococraft.MODID, "textures/entities/chocobos/plumage.png");

	public LayerPlumage(RenderLayerParent<Chocobo, AdultChocoboModel<Chocobo>> rendererIn) {
		super(rendererIn);
	}

	@Override
	public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, Chocobo chocoboEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		if (!chocoboEntity.isInvisible() && chocoboEntity.isMale()) {
			renderColoredCutoutModel(this.getParentModel(), PLUMAGE, matrixStackIn, bufferIn, packedLightIn, chocoboEntity, 1.0F, 1.0F, 1.0F);
		}
	}
}
