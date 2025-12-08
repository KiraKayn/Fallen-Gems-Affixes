package net.kayn.fallen_gems_affixes.attachment;

import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.item.augments.AugmentItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AugmentModel implements BakedModel {

    private final BakedModel original;
    private final ItemOverrides overrides;

    public AugmentModel(BakedModel original) {
        this.original = original;
        this.overrides = new ItemOverrides() {
            @Override
            public BakedModel resolve(@NotNull BakedModel original, @NotNull ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int seed) {
                BakedModel specific = AugmentModel.this.resolve(stack, original);
                return specific == original ? specific : specific.getOverrides().resolve(specific, stack, world, entity, seed);
            }
        };
    }

    private BakedModel resolve(ItemStack stack, BakedModel original) {
        AugmentItem.AugmentData data = AugmentItem.getAugmentData(stack);
        if (data != null) {
            ResourceLocation modelId = new ResourceLocation(FallenGemsAffixes.MOD_ID, "items/augments/" + data.getAugmentId().getPath());
            return Minecraft.getInstance().getModelManager().getModel(modelId);
        }
        return original;
    }

    @Override
    public @NotNull ItemOverrides getOverrides() {
        return overrides;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType) {
        return original.getQuads(state, side, rand, extraData, renderType);
    }

    @Override
    @Deprecated
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand) {
        return original.getQuads(state, side, rand, ModelData.EMPTY, null);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return original.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return original.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return original.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return original.isCustomRenderer();
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon() {
        return original.getParticleIcon();
    }

    @Override
    public @NotNull ItemTransforms getTransforms() {
        return original.getTransforms();
    }
}