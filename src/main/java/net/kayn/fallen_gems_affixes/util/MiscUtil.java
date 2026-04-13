package net.kayn.fallen_gems_affixes.util;

import com.google.gson.JsonObject;
import net.kayn.fallen_gems_affixes.recipe.ErasureRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class MiscUtil {
    public static boolean isOnCooldown(ResourceLocation id, float cooldown, LivingEntity entity) {
        long lastApplied = entity.getPersistentData().getLong("fga._cooldown." + id.toString());
        return lastApplied != 0 && lastApplied + cooldown >= entity.level().getGameTime();
    }

    public static void startCooldown(ResourceLocation id, LivingEntity entity) {
        entity.getPersistentData().putLong("fga._cooldown." + id.toString(), entity.level().getGameTime());
    }

    public static <T extends Recipe<?>> Supplier<RecipeSerializer<T>> simpleRecipeSerializer(Supplier<T> supplier) {
        return () -> new RecipeSerializer<>() {
            @Override
            public @NotNull T fromJson(ResourceLocation id, JsonObject json) {
                return supplier.get();
            }

            @Override
            public T fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
                return supplier.get();
            }

            @Override
            public void toNetwork(FriendlyByteBuf pBuffer, T pRecipe) {

            }
        };
    }
}
