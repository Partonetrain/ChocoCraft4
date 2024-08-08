package net.chococraft;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class ChococraftClient {
	public static final ModelLayerLocation CHOCOBO = new ModelLayerLocation(new ResourceLocation(Chococraft.MOD_ID, "main"), "chocobo");
	public static final ModelLayerLocation CHICOBO = new ModelLayerLocation(new ResourceLocation(Chococraft.MOD_ID, "main"), "chicobo");
	public static final ModelLayerLocation CHOCO_DISGUISE = new ModelLayerLocation(new ResourceLocation(Chococraft.MOD_ID, "main"), "choco_disguise");

	public static void init() {
	}
}
