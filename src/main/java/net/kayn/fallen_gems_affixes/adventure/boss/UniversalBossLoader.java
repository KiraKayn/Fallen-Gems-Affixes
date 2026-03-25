package net.kayn.fallen_gems_affixes.adventure.boss;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import dev.shadowsoffire.apotheosis.adventure.boss.BossStats;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UniversalBossLoader extends SimpleJsonResourceReloadListener {

    public static final UniversalBossLoader INSTANCE = new UniversalBossLoader();

    private static final Map<String, Float> RAW_TIER_CHANCES = new LinkedHashMap<>();
    private static final Map<String, BossStats> RAW_STATS = new LinkedHashMap<>();
    private static final List<ResourceLocation> BLACKLIST = new ArrayList<>();
    private static final List<TagKey<EntityType<?>>> BLACKLIST_TAGS = new ArrayList<>();

    @Nullable
    private static UniversalBossConfig resolvedConfig = null;

    public UniversalBossLoader() {
        super(new Gson(), "universal_boss");
    }

    @Nullable
    public static UniversalBossConfig getConfig() {
        if (resolvedConfig == null) resolvedConfig = tryResolve();
        return resolvedConfig;
    }

    private static UniversalBossConfig tryResolve() {
        if (RAW_TIER_CHANCES.isEmpty()) return null;

        Map<LootRarity, Float> tierChances = new LinkedHashMap<>();
        for (Map.Entry<String, Float> entry : RAW_TIER_CHANCES.entrySet()) {
            LootRarity rarity = resolveRarity(entry.getKey());
            if (rarity != null) tierChances.put(rarity, entry.getValue());
            else FallenGemsAffixes.LOGGER.warn("universal_boss: still cannot resolve rarity '{}'", entry.getKey());
        }

        Map<LootRarity, BossStats> statsMap = new LinkedHashMap<>();
        for (Map.Entry<String, BossStats> entry : RAW_STATS.entrySet()) {
            LootRarity rarity = resolveRarity(entry.getKey());
            if (rarity != null) statsMap.put(rarity, entry.getValue());
        }

        if (tierChances.isEmpty()) return null;
        return new UniversalBossConfig(tierChances, statsMap, BLACKLIST, BLACKLIST_TAGS);
    }

    @Nullable
    private static LootRarity resolveRarity(String name) {
        try {
            DynamicHolder<LootRarity> holder = RarityRegistry.byLegacyId(name);
            return holder.isBound() ? holder.get() : null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager manager, ProfilerFiller profiler) {
        RAW_TIER_CHANCES.clear();
        RAW_STATS.clear();
        BLACKLIST.clear();
        BLACKLIST_TAGS.clear();
        resolvedConfig = null;

        JsonObject json = null;
        for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
            json = entry.getValue().getAsJsonObject();
            break;
        }

        if (json == null) {
            FallenGemsAffixes.LOGGER.warn("No universal_boss JSON found — universal boss system disabled.");
            return;
        }

        try {
            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject("tier_chances").entrySet()) {
                RAW_TIER_CHANCES.put(entry.getKey(), entry.getValue().getAsFloat());
            }

            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject("stats").entrySet()) {
                BossStats stats = BossStats.CODEC
                        .parse(JsonOps.INSTANCE, entry.getValue())
                        .resultOrPartial(err -> FallenGemsAffixes.LOGGER.error("Failed to parse BossStats for '{}': {}", entry.getKey(), err))
                        .orElse(null);
                if (stats != null) RAW_STATS.put(entry.getKey(), stats);
            }

            if (json.has("blacklist")) {
                json.getAsJsonArray("blacklist").forEach(e -> {
                    String s = e.getAsString();
                    if (s.startsWith("#")) {
                        ResourceLocation tagId = new ResourceLocation(s.substring(1));
                        BLACKLIST_TAGS.add(TagKey.create(Registries.ENTITY_TYPE, tagId));
                    } else {
                        BLACKLIST.add(new ResourceLocation(s));
                    }
                });
            }

            FallenGemsAffixes.LOGGER.info("universal_boss: loaded {} raw tiers (resolution deferred until first use).", RAW_TIER_CHANCES.size());

        } catch (Exception e) {
            FallenGemsAffixes.LOGGER.error("Failed to load universal_boss JSON", e);
        }
    }
}