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
import net.kayn.fallen_gems_affixes.adventure.entity.EntityAffixEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;

import javax.annotation.Nullable;
import java.util.*;

public class UniversalBossLoader extends SimpleJsonResourceReloadListener {

    public static final UniversalBossLoader INSTANCE = new UniversalBossLoader();

    private static final Map<String, Float>                  RAW_TIER_CHANCES       = new LinkedHashMap<>();
    private static final Map<String, BossStats>              RAW_STATS              = new LinkedHashMap<>();
    private static final Map<String, Float>                  RAW_STAT_CHANCES       = new LinkedHashMap<>();
    private static final List<ResourceLocation>              BLACKLIST              = new ArrayList<>();
    private static final List<TagKey<EntityType<?>>>         BLACKLIST_TAGS         = new ArrayList<>();
    private static final Map<String, List<String>>           RAW_DIMENSION_RARITIES = new LinkedHashMap<>();
    private static final Map<String, List<EntityAffixEntry>> RAW_AFFIXES            = new LinkedHashMap<>();
    private static final Map<String, List<EntityAffixEntry>> RAW_MOB_AFFIXES        = new LinkedHashMap<>();

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

    @Nullable
    private static UniversalBossConfig tryResolve() {
        if (RAW_TIER_CHANCES.isEmpty()) return null;

        Map<LootRarity, Float> tierChances = new LinkedHashMap<>();
        for (Map.Entry<String, Float> e : RAW_TIER_CHANCES.entrySet()) {
            LootRarity r = resolveRarity(e.getKey());
            if (r != null) tierChances.put(r, e.getValue());
            else FallenGemsAffixes.LOGGER.warn("[FGA] Cannot resolve rarity '{}'", e.getKey());
        }

        Map<LootRarity, BossStats> statsMap = new LinkedHashMap<>();
        for (Map.Entry<String, BossStats> e : RAW_STATS.entrySet()) {
            LootRarity r = resolveRarity(e.getKey());
            if (r != null) statsMap.put(r, e.getValue());
        }

        Map<ResourceLocation, List<LootRarity>> dimensionRarities = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> e : RAW_DIMENSION_RARITIES.entrySet()) {
            ResourceLocation dimId = new ResourceLocation(e.getKey());
            List<LootRarity> rarities = new ArrayList<>();
            for (String name : e.getValue()) {
                LootRarity r = resolveRarity(name);
                if (r != null) rarities.add(r);
            }
            if (!rarities.isEmpty()) dimensionRarities.put(dimId, rarities);
        }

        if (tierChances.isEmpty()) return null;

        return new UniversalBossConfig(
                tierChances, statsMap, BLACKLIST, BLACKLIST_TAGS,
                dimensionRarities,
                new LinkedHashMap<>(RAW_AFFIXES),
                new LinkedHashMap<>(RAW_STAT_CHANCES),
                new LinkedHashMap<>(RAW_MOB_AFFIXES));
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
        RAW_STAT_CHANCES.clear();
        BLACKLIST.clear();
        BLACKLIST_TAGS.clear();
        RAW_DIMENSION_RARITIES.clear();
        RAW_AFFIXES.clear();
        RAW_MOB_AFFIXES.clear();
        resolvedConfig = null;

        if (objects.isEmpty()) {
            FallenGemsAffixes.LOGGER.warn("[FGA] No universal_boss JSON found — system disabled.");
            return;
        }

        for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
            JsonObject json = entry.getValue().getAsJsonObject();
            try {
                parseTierChances(json);
                parseStats(json);
                parseBlacklist(json);
                parseDimensionRarities(json);
                parseAffixes(json);
                parseMobAffixes(json);
            } catch (Exception e) {
                FallenGemsAffixes.LOGGER.error("[FGA] Failed to load universal_boss from {}", entry.getKey(), e);
            }
        }

        FallenGemsAffixes.LOGGER.info("[FGA] universal_boss loaded: {} tiers, {} affix groups, {} mob affix groups.",
                RAW_TIER_CHANCES.size(), RAW_AFFIXES.size(), RAW_MOB_AFFIXES.size());
    }

    private static void parseTierChances(JsonObject json) {
        if (!json.has("tier_chances")) return;
        for (Map.Entry<String, JsonElement> e : json.getAsJsonObject("tier_chances").entrySet())
            RAW_TIER_CHANCES.put(e.getKey(), e.getValue().getAsFloat());
    }

    private static void parseStats(JsonObject json) {
        if (!json.has("stats")) return;
        for (Map.Entry<String, JsonElement> e : json.getAsJsonObject("stats").entrySet()) {
            JsonObject statObj = e.getValue().getAsJsonObject().deepCopy();
            float statChance = 1.0f;
            if (statObj.has("stat_chance")) {
                statChance = Math.max(0f, Math.min(1f, statObj.get("stat_chance").getAsFloat()));
                statObj.remove("stat_chance");
            }
            RAW_STAT_CHANCES.put(e.getKey(), statChance);
            BossStats stats = BossStats.CODEC
                    .parse(JsonOps.INSTANCE, statObj)
                    .resultOrPartial(err -> FallenGemsAffixes.LOGGER.error("[FGA] Bad BossStats '{}': {}", e.getKey(), err))
                    .orElse(null);
            if (stats != null) RAW_STATS.put(e.getKey(), stats);
        }
    }

    private static void parseBlacklist(JsonObject json) {
        if (!json.has("blacklist")) return;
        json.getAsJsonArray("blacklist").forEach(e -> {
            String s = e.getAsString();
            if (s.startsWith("#"))
                BLACKLIST_TAGS.add(TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(s.substring(1))));
            else
                BLACKLIST.add(new ResourceLocation(s));
        });
    }

    private static void parseDimensionRarities(JsonObject json) {
        if (!json.has("dimension_rarities")) return;
        for (Map.Entry<String, JsonElement> e : json.getAsJsonObject("dimension_rarities").entrySet()) {
            List<String> names = new ArrayList<>();
            e.getValue().getAsJsonArray().forEach(el -> names.add(el.getAsString()));
            RAW_DIMENSION_RARITIES.put(e.getKey(), names);
        }
    }

    private static void parseAffixes(JsonObject json) {
        if (!json.has("affixes")) return;
        for (Map.Entry<String, JsonElement> e : json.getAsJsonObject("affixes").entrySet()) {
            String rarityKey = e.getKey();
            List<EntityAffixEntry> entries = new ArrayList<>();
            for (JsonElement el : e.getValue().getAsJsonArray()) {
                JsonObject obj = el.getAsJsonObject();
                if (!obj.has("affix")) continue;
                ResourceLocation affixId = new ResourceLocation(obj.get("affix").getAsString());
                float level  = obj.has("level")  ? Math.max(0f, Math.min(1f, obj.get("level").getAsFloat()))  : 0.5f;
                float chance = obj.has("chance") ? Math.max(0f, Math.min(1f, obj.get("chance").getAsFloat())) : 1.0f;
                entries.add(new EntityAffixEntry(affixId, level, chance));
            }
            if (!entries.isEmpty()) RAW_AFFIXES.put(rarityKey, entries);
        }
    }

    private static void parseMobAffixes(JsonObject json) {
        if (!json.has("mob_affixes")) return;
        for (Map.Entry<String, JsonElement> e : json.getAsJsonObject("mob_affixes").entrySet()) {
            String rarityKey = e.getKey();
            List<EntityAffixEntry> entries = new ArrayList<>();
            for (JsonElement el : e.getValue().getAsJsonArray()) {
                JsonObject obj = el.getAsJsonObject();
                if (!obj.has("affix")) continue;
                ResourceLocation affixId = new ResourceLocation(obj.get("affix").getAsString());
                float level  = obj.has("level")  ? Math.max(0f, Math.min(1f, obj.get("level").getAsFloat()))  : 0.5f;
                float chance = obj.has("chance") ? Math.max(0f, Math.min(1f, obj.get("chance").getAsFloat())) : 1.0f;
                entries.add(new EntityAffixEntry(affixId, level, chance));
            }
            if (!entries.isEmpty()) RAW_MOB_AFFIXES.put(rarityKey, entries);
        }
    }
}