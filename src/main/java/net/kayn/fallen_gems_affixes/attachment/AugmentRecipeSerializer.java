package net.kayn.fallen_gems_affixes.attachment;

import com.google.gson.JsonObject;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AugmentRecipeSerializer implements RecipeSerializer<AugmentRecipe> {

    @Override
    public AugmentRecipe fromJson(ResourceLocation id, JsonObject json) {
        ResourceLocation augmentId = new ResourceLocation(json.get("id").getAsString());

        JsonObject addition = json.getAsJsonObject("addition");
        ResourceLocation itemId = new ResourceLocation(addition.get("item").getAsString());
        ItemStack augmentStack = new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(itemId)));

        Set<LootCategory> categories = new HashSet<>();
        if (json.has("categories")) {
            json.getAsJsonArray("categories").forEach(el -> {
                String name = el.getAsString();
                LootCategory cat = LootCategory.BY_ID.getOrDefault(name, LootCategory.BY_ID.get("apotheosis:" + name));
                if (cat != null) categories.add(cat);
            });
        }

        return new AugmentRecipe(augmentId, augmentStack, categories);
    }

    @Override
    public AugmentRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
        ResourceLocation augmentId = buf.readResourceLocation();
        ItemStack stack = buf.readItem();

        int count = buf.readVarInt();
        Set<LootCategory> categories = new HashSet<>();
        for (int i = 0; i < count; i++) {
            String cat = buf.readUtf();
            LootCategory c = LootCategory.BY_ID.get(cat);
            if (c != null) categories.add(c);
        }

        return new AugmentRecipe(augmentId, stack, categories);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, AugmentRecipe recipe) {
        buf.writeResourceLocation(recipe.getAugmentId());
        buf.writeItem(recipe.getAddition());

        buf.writeVarInt(recipe.getValidCategories().size());
        for (LootCategory cat : recipe.getValidCategories()) {
            buf.writeUtf(cat.toString());
        }
    }
}