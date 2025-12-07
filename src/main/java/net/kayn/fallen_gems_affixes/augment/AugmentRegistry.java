package net.kayn.fallen_gems_affixes.augment;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import net.kayn.fallen_gems_affixes.item.augments.AugmentItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.FileReader;
import java.nio.file.Path;
import java.util.*;

/**
 * Registry for augments loaded from JSON files.
 */
public class AugmentRegistry {

    private static final Map<ResourceLocation, AugmentData> REGISTRY = new HashMap<>();
    private static final Gson GSON = new Gson();

    /**
     * Holds the data for a single augment.
     */
    public static class AugmentData {
        public final ResourceLocation id;
        public final ItemStack itemStack;
        public final Set<LootCategory> categories;
        public final boolean unique;
        public final String displayName;

        public AugmentData(ResourceLocation id, ItemStack itemStack, Set<LootCategory> categories, boolean unique, String displayName) {
            this.id = id;
            this.itemStack = itemStack;
            this.categories = categories;
            this.unique = unique;
            this.displayName = displayName;
        }
    }

    /**
     * Load all JSON files in config or data folder.
     */
    public static void loadAll() {
        REGISTRY.clear();
        try {
            Path dir = FMLPaths.GAMEDIR.get().resolve("config/fallen_gems_affixes/augments/");
            if (!dir.toFile().exists()) return;

            for (var file : Objects.requireNonNull(dir.toFile().listFiles((d, n) -> n.endsWith(".json")))) {
                JsonObject json = GSON.fromJson(new FileReader(file), JsonObject.class);

                ResourceLocation id = new ResourceLocation(json.get("id").getAsString());
                String displayName = json.has("displayName") ? json.get("displayName").getAsString() : "";

                boolean unique = json.has("unique") && json.get("unique").getAsBoolean();

                // Load ItemStack
                ResourceLocation itemId = new ResourceLocation(json.get("item").getAsString());
                Item item = ForgeRegistries.ITEMS.getValue(itemId);
                if (item == null) continue;
                ItemStack stack = AugmentItem.createStack(item, id.toString());

                // Load loot categories
                Set<LootCategory> categories = new HashSet<>();
                if (json.has("categories")) {
                    json.getAsJsonArray("categories").forEach(el -> {
                        String name = el.getAsString();
                        LootCategory cat = LootCategory.BY_ID.getOrDefault(name, LootCategory.BY_ID.get("apotheosis:" + name));
                        if (cat != null) categories.add(cat);
                    });
                }

                REGISTRY.put(id, new AugmentData(id, stack, categories, unique, displayName));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get augment metadata by ID.
     */
    public static AugmentData get(ResourceLocation id) {
        return REGISTRY.get(id);
    }

    /**
     * Get all registered augments.
     */
    public static Collection<AugmentData> getAll() {
        return REGISTRY.values();
    }

    /**
     * Convenience: get augment ID from an AugmentItem stack.
     */
    public static AugmentData get(ItemStack stack) {
        if (!(stack.getItem() instanceof AugmentItem)) return null;
        String idStr = AugmentItem.getAugmentId(stack);
        if (idStr.isEmpty()) return null;
        return get(new ResourceLocation(idStr));
    }
}