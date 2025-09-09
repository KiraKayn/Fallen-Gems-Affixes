package net.kayn.fallen_gems_affixes.attachment;

import com.google.gson.JsonObject;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AugmentRecipeSerializer implements RecipeSerializer<AugmentRecipe> {
    @Override
    public @NotNull AugmentRecipe fromJson(ResourceLocation id, JsonObject json) {
        Ingredient base = Ingredient.fromJson(json.get("base"));
        Ingredient addition = Ingredient.fromJson(json.get("addition"));
        return new AugmentRecipe(id, base, addition);
    }

    @Override
    public AugmentRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
        Ingredient base = Ingredient.fromNetwork(buf);
        Ingredient addition = Ingredient.fromNetwork(buf);
        return new AugmentRecipe(id, base, addition);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, AugmentRecipe recipe) {
        recipe.base.toNetwork(buf);
        recipe.addition.toNetwork(buf);
    }
}