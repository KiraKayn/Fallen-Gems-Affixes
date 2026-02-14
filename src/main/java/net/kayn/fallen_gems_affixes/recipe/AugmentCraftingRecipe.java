package net.kayn.fallen_gems_affixes.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.item.augments.AugmentItem;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

import java.util.Map;

public class AugmentCraftingRecipe extends ShapedRecipe {

    private final ResourceLocation augmentId;
    private final int recipeWidth;
    private final int recipeHeight;

    public AugmentCraftingRecipe(ResourceLocation id, String group, CraftingBookCategory category,
                                 int width, int height, NonNullList<Ingredient> ingredients,
                                 ItemStack result, ResourceLocation augmentId) {
        super(id, group, category, width, height, ingredients, result);
        this.augmentId = augmentId;
        this.recipeWidth = width;
        this.recipeHeight = height;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess access) {
        return AugmentItem.createAugment(this.augmentId);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Fallen.RecipeSerializers.AUGMENT_CRAFTING.get();
    }

    public static class Serializer implements RecipeSerializer<AugmentCraftingRecipe> {

        @Override
        public AugmentCraftingRecipe fromJson(ResourceLocation id, JsonObject json) {
            String group = GsonHelper.getAsString(json, "group", "");
            CraftingBookCategory category = CraftingBookCategory.CODEC.byName(
                    GsonHelper.getAsString(json, "category", null), CraftingBookCategory.MISC
            );

            ResourceLocation augmentId = new ResourceLocation(GsonHelper.getAsString(json, "augment"));

            Map<String, Ingredient> key = keyFromJson(GsonHelper.getAsJsonObject(json, "key"));
            String[] pattern = patternFromJson(GsonHelper.getAsJsonArray(json, "pattern"));
            int width = pattern[0].length();
            int height = pattern.length;
            NonNullList<Ingredient> ingredients = dissolvePattern(pattern, key, width, height);

            ItemStack result = AugmentItem.createAugment(augmentId);

            return new AugmentCraftingRecipe(id, group, category, width, height, ingredients, result, augmentId);
        }

        private static Map<String, Ingredient> keyFromJson(JsonObject json) {
            Map<String, Ingredient> map = new java.util.HashMap<>();
            for (Map.Entry<String, com.google.gson.JsonElement> entry : json.entrySet()) {
                if (entry.getKey().length() != 1) {
                    throw new JsonSyntaxException("Invalid key entry: '" + entry.getKey() + "' is an invalid symbol (must be 1 character only).");
                }
                if (" ".equals(entry.getKey())) {
                    throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");
                }
                map.put(entry.getKey(), Ingredient.fromJson(entry.getValue()));
            }
            map.put(" ", Ingredient.EMPTY);
            return map;
        }

        private static String[] patternFromJson(JsonArray jsonArray) {
            String[] pattern = new String[jsonArray.size()];
            for (int i = 0; i < pattern.length; i++) {
                String line = GsonHelper.convertToString(jsonArray.get(i), "pattern[" + i + "]");
                if (i > 0 && pattern[0].length() != line.length()) {
                    throw new JsonSyntaxException("Invalid pattern: each row must be the same width");
                }
                pattern[i] = line;
            }
            return pattern;
        }

        private static NonNullList<Ingredient> dissolvePattern(String[] pattern, Map<String, Ingredient> key, int width, int height) {
            NonNullList<Ingredient> ingredients = NonNullList.withSize(width * height, Ingredient.EMPTY);

            for (int i = 0; i < pattern.length; i++) {
                String row = pattern[i];
                for (int j = 0; j < row.length(); j++) {
                    String symbol = String.valueOf(row.charAt(j));
                    Ingredient ingredient = key.get(symbol);
                    if (ingredient == null) {
                        throw new JsonSyntaxException("Pattern references symbol '" + symbol + "' but it's not defined in the key");
                    }
                    ingredients.set(i * width + j, ingredient);
                }
            }

            return ingredients;
        }

        @Override
        public AugmentCraftingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            String group = buffer.readUtf();
            CraftingBookCategory category = buffer.readEnum(CraftingBookCategory.class);
            int width = buffer.readVarInt();
            int height = buffer.readVarInt();

            NonNullList<Ingredient> ingredients = NonNullList.withSize(width * height, Ingredient.EMPTY);
            for (int i = 0; i < ingredients.size(); i++) {
                ingredients.set(i, Ingredient.fromNetwork(buffer));
            }

            ResourceLocation augmentId = buffer.readResourceLocation();
            ItemStack result = AugmentItem.createAugment(augmentId);

            return new AugmentCraftingRecipe(id, group, category, width, height, ingredients, result, augmentId);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, AugmentCraftingRecipe recipe) {
            buffer.writeUtf(recipe.getGroup());
            buffer.writeEnum(recipe.category());
            buffer.writeVarInt(recipe.recipeWidth);
            buffer.writeVarInt(recipe.recipeHeight);

            for (Ingredient ingredient : recipe.getIngredients()) {
                ingredient.toNetwork(buffer);
            }

            buffer.writeResourceLocation(recipe.augmentId);
        }
    }
}