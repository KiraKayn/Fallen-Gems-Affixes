package net.kayn.fallen_gems_affixes.attachment;

import com.google.gson.JsonObject;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AugmentRecipeSerializer implements RecipeSerializer<AugmentRecipe> {
    @Override
    public @NotNull AugmentRecipe fromJson(ResourceLocation id, JsonObject json) {
        // Read augment ID
        ResourceLocation augmentId = new ResourceLocation(json.get("augment").getAsString());

        // Read augment item
        ResourceLocation itemId = new ResourceLocation(json.get("item").getAsString());
        ItemStack augmentItem = new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(itemId)));

        // Read categories
        Set<LootCategory> categories = new HashSet<>();
        if (json.has("categories")) {
            json.getAsJsonArray("categories").forEach(element -> {
                String categoryName = element.getAsString();
                LootCategory category = LootCategory.BY_ID.get(categoryName);
                if (category == null) {
                    category = LootCategory.BY_ID.get("apotheosis:" + categoryName);
                }
                if (category != null) {
                    categories.add(category);
                }
            });
        }

        return new AugmentRecipe(id, augmentId, augmentItem, categories);
    }

    @Override
    public AugmentRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
        ResourceLocation augmentId = buf.readResourceLocation();
        ItemStack augmentItem = buf.readItem();

        int categoryCount = buf.readVarInt();
        Set<LootCategory> categories = new HashSet<>();
        for (int i = 0; i < categoryCount; i++) {
            String categoryId = buf.readUtf();
            LootCategory category = LootCategory.BY_ID.get(categoryId);
            if (category != null) {
                categories.add(category);
            }
        }

        return new AugmentRecipe(id, augmentId, augmentItem, categories);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, AugmentRecipe recipe) {
        buf.writeResourceLocation(recipe.getAugmentId());
        buf.writeItem(recipe.augmentItem);

        buf.writeVarInt(recipe.getValidCategories().size());
        for (LootCategory category : recipe.getValidCategories()) {
            buf.writeUtf(category.toString());
        }
    }
}