package net.kayn.fallen_gems_affixes.item.augments;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AugmentItem extends Item {
    private static final Gson GSON = new Gson();
    private static final String AUGMENT_ID_TAG = "AugmentId";
    private static final Map<ResourceLocation, AugmentData> AUGMENT_DATA = new HashMap<>();

    public AugmentItem(Properties properties) {
        super(properties);
    }

    public static ItemStack createAugment(ResourceLocation augmentId) {
        ItemStack stack = new ItemStack(net.kayn.fallen_gems_affixes.registry.ModItems.AUGMENT_ITEM.get());
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(AUGMENT_ID_TAG, augmentId.toString());
        return stack;
    }

    public static ResourceLocation getAugmentId(ItemStack stack) {
        if (stack.hasTag()) {
            assert stack.getTag() != null;
            if (stack.getTag().contains(AUGMENT_ID_TAG)) {
                return new ResourceLocation(stack.getTag().getString(AUGMENT_ID_TAG));
            }
        }
        return null;
    }

    public static AugmentData getAugmentData(ItemStack stack) {
        ResourceLocation augmentId = getAugmentId(stack);
        if (augmentId != null) {
            return AUGMENT_DATA.get(augmentId);
        }
        return null;
    }

    public static AugmentData getAugmentData(ResourceLocation augmentId) {
        return AUGMENT_DATA.get(augmentId);
    }

    public static Collection<AugmentData> getAllAugmentData() {
        return AUGMENT_DATA.values();
    }

    public static boolean canApplyTo(ItemStack augmentStack, LootCategory category) {
        AugmentData data = getAugmentData(augmentStack);
        if (data == null) return false;
        return data.canApplyTo(category);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        AugmentData data = getAugmentData(stack);
        if (data != null) {
            Set<LootCategory> categories = data.getCategories();
            if (!categories.isEmpty()) {
                tooltip.add(Component.translatable("tooltip.fallen_gems_affixes.augment.categories"));
                for (LootCategory category : categories) {
                    tooltip.add(Component.literal("  • " + category.toString()).withStyle(style -> style.withColor(0xAABBCC)));
                }
            }
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        ResourceLocation augmentId = getAugmentId(stack);
        if (augmentId != null) {
            String translationKey = "item.fallen_gems_affixes.augment." + augmentId.getPath();
            return Component.translatable(translationKey);
        }
        return super.getName(stack);
    }

    public static class Loader extends SimpleJsonResourceReloadListener {

        public Loader() {
            super(GSON, "augments");
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
            AUGMENT_DATA.clear();

            for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
                try {
                    ResourceLocation id = entry.getKey();
                    JsonObject json = entry.getValue().getAsJsonObject();

                    ResourceLocation augmentId = new ResourceLocation(json.get("augment").getAsString());
                    ResourceLocation texture = new ResourceLocation(json.get("texture").getAsString());

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

                    AUGMENT_DATA.put(augmentId, new AugmentData(augmentId, texture, categories));
                } catch (Exception e) {
                    FallenGemsAffixes.LOGGER.error("Error loading augment data: {}", entry.getKey(), e);
                }
            }

            FallenGemsAffixes.LOGGER.info("Loaded {} augment data entries", AUGMENT_DATA.size());
        }
    }

    public static class AugmentData {
        private final ResourceLocation augmentId;
        private final ResourceLocation texture;
        private final Set<LootCategory> categories;

        public AugmentData(ResourceLocation augmentId, ResourceLocation texture, Set<LootCategory> categories) {
            this.augmentId = augmentId;
            this.texture = texture;
            this.categories = categories;
        }

        public ResourceLocation getAugmentId() {
            return augmentId;
        }

        public ResourceLocation getTexture() {
            return texture;
        }

        public Set<LootCategory> getCategories() {
            return categories;
        }

        public boolean canApplyTo(LootCategory category) {
            return categories.isEmpty() || categories.contains(category);
        }
    }
}