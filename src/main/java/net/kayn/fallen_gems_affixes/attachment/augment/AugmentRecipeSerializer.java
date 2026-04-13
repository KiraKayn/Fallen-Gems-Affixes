package net.kayn.fallen_gems_affixes.attachment.augment;

import com.google.gson.JsonObject;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AugmentRecipeSerializer implements RecipeSerializer<AugmentRecipe> {
    @Override
    public @NotNull AugmentRecipe fromJson(ResourceLocation id, JsonObject json) {
        FallenGemsAffixes.LOGGER.info("create augment recipe.");
        return new AugmentRecipe();
    }

    @Override
    public AugmentRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
        return new AugmentRecipe();
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, AugmentRecipe recipe) {
    }
}