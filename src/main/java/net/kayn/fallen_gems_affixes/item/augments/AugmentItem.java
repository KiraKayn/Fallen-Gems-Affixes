package net.kayn.fallen_gems_affixes.item.augments;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.augment.AugmentRegistry;
import net.kayn.fallen_gems_affixes.types.augment.IAugment;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
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

        /*
         * Write all config values into the AUGMENT_DATA inner_data block so that the
         * augment item always carries its own configuration.
         *
         * For Genesis, this block later gets overwritten with dynamic state by
         * GenesisAugment.apply(). For GemPower/Supremacy the "power" key is the only
         * field that actually matters, but the extras do no harm.
         */

        AugmentData data = AUGMENT_DATA.get(augmentId);
        if (data != null) {
            try {
                CompoundTag augmentDataCompound = new CompoundTag();
                ListTag list = new ListTag();

                CompoundTag entry = new CompoundTag();
                entry.putString(Fallen.AugmentMisc.TYPE, augmentId.toString());

                CompoundTag inner = new CompoundTag();
                inner.putFloat("power", data.getPower());
                inner.putFloat("defaultPower",    data.getDefaultPower());
                inner.putFloat("affixPowerBoost", data.getAffixPowerBoost());
                inner.putFloat("gemPowerBoost",   data.getGemPowerBoost());
                // Duality fields
                inner.putFloat("critChanceMultiplier", data.getCritChanceMultiplier());
                inner.putFloat("critDamageReduction", data.getCritDamageReduction());
                inner.putFloat("physicalRatio", data.getPhysicalRatio());
                inner.putFloat("magicRatio", data.getMagicRatio());

                entry.put(Fallen.AugmentMisc.INNER_DATA, inner);
                list.add(entry);

                augmentDataCompound.put(Fallen.AugmentMisc.AUGMENTS, list);
                tag.put(Fallen.AugmentMisc.AUGMENT_DATA, augmentDataCompound);
            } catch (Exception e) {
                FallenGemsAffixes.LOGGER.error("Error writing augment default inner data for {}", augmentId, e);
            }
        }

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

            // When categories is empty it means "applies to all items".
            // Show every known LootCategory
            List<LootCategory> displayCategories;
            if (categories.isEmpty()) {
                displayCategories = new ArrayList<>(LootCategory.BY_ID.values());
                displayCategories.sort(Comparator.comparing(LootCategory::getName));
            } else {
                displayCategories = new ArrayList<>(categories);
            }

            tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable("tooltip.fallen_gems_affixes.augment.categories")
                    .withStyle(ChatFormatting.GREEN));

            for (int i = 0; i < displayCategories.size(); i += 3) {
                int endIndex = Math.min(i + 3, displayCategories.size());
                List<String> lineCategories = new ArrayList<>();

                for (int j = i; j < endIndex; j++) {
                    lineCategories.add(Component.translatable(
                            displayCategories.get(j).getDescIdPlural()).getString());
                }

                tooltip.add(Component.literal("  • " + String.join(", ", lineCategories))
                        .withStyle(ChatFormatting.GREEN));
            }

            tooltip.add(Component.literal(""));
        }

        ResourceLocation id = getAugmentId(stack);
        if (id != null) {
            IAugment augment = AugmentRegistry.get(id);
            if (augment != null) {
                augment.appendItemTooltip(stack, level, tooltip, flag);

                tooltip.add(Component.literal(""));
                tooltip.add(Component.literal("Fabled").withStyle(ChatFormatting.DARK_RED));
            }
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        ResourceLocation augmentId = getAugmentId(stack);
        if (augmentId != null) {
            return Component
                    .translatable("item.fallen_gems_affixes.augment." + augmentId.getPath())
                    .withStyle(ChatFormatting.DARK_RED);
        }
        return super.getName(stack);
    }


    public static class Loader extends SimpleJsonResourceReloadListener {

        public Loader() {
            super(GSON, "augments");
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> objects,
                             ResourceManager resourceManager, ProfilerFiller profiler) {
            AUGMENT_DATA.clear();

            for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
                try {
                    ResourceLocation id = entry.getKey();
                    JsonObject json = entry.getValue().getAsJsonObject();

                    ResourceLocation augmentId = new ResourceLocation(json.get("augment").getAsString());
                    ResourceLocation texture   = new ResourceLocation(json.get("texture").getAsString());

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

                    float power = 1.0f;
                    if (json.has("power")) {
                        try { power = json.get("power").getAsFloat(); }
                        catch (Exception ex) {
                            FallenGemsAffixes.LOGGER.warn(
                                    "Invalid power value for augment {}: {}", id, json.get("power"));
                        }
                    }

                    float defaultPower    = 0.5f;
                    float affixPowerBoost = 0.1f;
                    float gemPowerBoost   = 0.1f;

                    if (json.has("default_power")) {
                        try { defaultPower = json.get("default_power").getAsFloat(); }
                        catch (Exception ex) {
                            FallenGemsAffixes.LOGGER.warn("Invalid default_power for augment {}", id);
                        }
                    }
                    if (json.has("affix_power_boost")) {
                        try { affixPowerBoost = json.get("affix_power_boost").getAsFloat(); }
                        catch (Exception ex) {
                            FallenGemsAffixes.LOGGER.warn("Invalid affix_power_boost for augment {}", id);
                        }
                    }
                    if (json.has("gem_power_boost")) {
                        try { gemPowerBoost = json.get("gem_power_boost").getAsFloat(); }
                        catch (Exception ex) {
                            FallenGemsAffixes.LOGGER.warn("Invalid gem_power_boost for augment {}", id);
                        }
                    }
                    // duality fields
                    float critChanceMultiplier = 2.0f;
                    float critDamageReduction = 0.3f;
                    float physicalRatio = 0.5f;
                    float magicRatio = 0.5f;

                    if (json.has("crit_chance_multiplier")) {
                        try { critChanceMultiplier = json.get("crit_chance_multiplier").getAsFloat(); }
                        catch (Exception ex) {
                            FallenGemsAffixes.LOGGER.warn(
                                    "Invalid crit_chance_multiplier for augment {}", id);
                        }
                    }
                    if (json.has("crit_damage_reduction")) {
                        try { critDamageReduction = json.get("crit_damage_reduction").getAsFloat(); }
                        catch (Exception ex) {
                            FallenGemsAffixes.LOGGER.warn(
                                    "Invalid crit_damage_reduction for augment {}", id);
                        }
                    }
                    if (json.has("physical_ratio")) {
                        try { physicalRatio = json.get("physical_ratio").getAsFloat(); }
                        catch (Exception ex) {
                            FallenGemsAffixes.LOGGER.warn(
                                    "Invalid physical_ratio for augment {}", id);
                        }
                    }
                    if (json.has("magic_ratio")) {
                        try { magicRatio = json.get("magic_ratio").getAsFloat(); }
                        catch (Exception ex) {
                            FallenGemsAffixes.LOGGER.warn(
                                    "Invalid magic_ratio for augment {}", id);
                        }
                    }

                    AUGMENT_DATA.put(augmentId, new AugmentData(
                            augmentId, texture, categories,
                            power, defaultPower, affixPowerBoost, gemPowerBoost,
                            critChanceMultiplier, critDamageReduction, physicalRatio, magicRatio));

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
        private final float power;
        private final float defaultPower;
        private final float affixPowerBoost;
        private final float gemPowerBoost;

        /**
         * Duality
         */
        private final float critChanceMultiplier;
        private final float critDamageReduction;
        private final float physicalRatio;
        private final float magicRatio;

        public AugmentData(ResourceLocation augmentId, ResourceLocation texture,
                           Set<LootCategory> categories, float power, float critChanceMultiplier, float critDamageReduction, float physicalRatio, float magicRatio) {
            this(augmentId, texture, categories, power, 0.5f, 0.1f, 0.1f, magicRatio, physicalRatio, critDamageReduction, critChanceMultiplier);
        }

        public AugmentData(ResourceLocation augmentId, ResourceLocation texture,
                           Set<LootCategory> categories, float power,
                           float defaultPower, float affixPowerBoost, float gemPowerBoost, float critChanceMultiplier, float critDamageReduction, float physicalRatio, float magicRatio) {
            this.augmentId       = augmentId;
            this.texture         = texture;
            this.categories      = categories;
            this.power           = power;
            this.defaultPower    = defaultPower;
            this.affixPowerBoost = affixPowerBoost;
            this.gemPowerBoost   = gemPowerBoost;
            this.critChanceMultiplier = critChanceMultiplier;
            this.critDamageReduction = critDamageReduction;
            this.physicalRatio = physicalRatio;
            this.magicRatio = magicRatio;
        }

        public ResourceLocation  getAugmentId()      { return augmentId; }
        public ResourceLocation  getTexture()         { return texture; }
        public Set<LootCategory> getCategories()      { return categories; }
        public float             getPower()           { return power; }
        public float             getDefaultPower()    { return defaultPower; }
        public float             getAffixPowerBoost() { return affixPowerBoost; }
        public float             getGemPowerBoost()   { return gemPowerBoost; }

        public boolean canApplyTo(LootCategory category) {
            return categories.isEmpty() || categories.contains(category);
        }

        public float getCritChanceMultiplier() {
            return critChanceMultiplier;
        }

        public float getCritDamageReduction() {
            return critDamageReduction;
        }

        public float getPhysicalRatio() {
            return physicalRatio;
        }

        public float getMagicRatio() {
            return magicRatio;
        }
    }
}