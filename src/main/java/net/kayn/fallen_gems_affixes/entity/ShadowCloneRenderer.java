package net.kayn.fallen_gems_affixes.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.adventure.set.trickster.TricksterEntities;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ShadowCloneRenderer extends MobRenderer<ShadowCloneEntity, HumanoidModel<ShadowCloneEntity>> {
    public static final ResourceLocation TEXTURE = FallenGemsAffixes.id("textures/entity/shadow_clone.png");

    public ShadowCloneRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new HumanoidModel<>(ctx.bakeLayer(TricksterEntities.SHADOW_CLONE_LAYER)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(ShadowCloneEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(ShadowCloneEntity entity, float yaw, float partials, PoseStack pose,
                       MultiBufferSource buffers, int light) {
        super.render(entity, yaw, partials, pose, buffers, light);
    }

    @Override
    protected RenderType getRenderType(ShadowCloneEntity entity, boolean visible, boolean invisible, boolean glowing) {
        return RenderType.entityTranslucent(TEXTURE);
    }
}