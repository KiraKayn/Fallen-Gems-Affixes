package net.kayn.fallen_gems_affixes.adventure.boss;

import dev.shadowsoffire.apotheosis.adventure.boss.BossStats;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import net.kayn.fallen_gems_affixes.adventure.entity.EntityAffixEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;

import javax.annotation.Nullable;
import java.util.*;

public record UniversalBossConfig(
        Map<LootRarity, Float> tierChances,
        Map<LootRarity, BossStats> stats,
        List<ResourceLocation> blacklist,
        List<TagKey<EntityType<?>>> blacklistTags,
        Map<ResourceLocation, List<LootRarity>> dimensionRarities,
        Map<String, List<EntityAffixEntry>> affixes,
        Map<String, Float> statChances,
        Map<String, List<EntityAffixEntry>> mobAffixes,
        Map<LootRarity, Float> gearBonus
) {

    @Nullable
    public List<LootRarity> getRaritiesForDimension(ResourceLocation dimensionId) {
        if (dimensionRarities.isEmpty()) return null;
        return dimensionRarities.getOrDefault(dimensionId, List.of());
    }

    @Nullable
    public LootRarity rollRarity(RandomSource rand) {
        return rollRarity(rand, null, Collections.emptyMap());
    }

    @Nullable
    public LootRarity rollRarity(RandomSource rand, @Nullable Set<LootRarity> allowed) {
        return rollRarity(rand, allowed, Collections.emptyMap());
    }

    @Nullable
    public LootRarity rollRarity(RandomSource rand, @Nullable Set<LootRarity> allowed, Map<LootRarity, Float> bonuses) {
        Map<LootRarity, Float> pool = new LinkedHashMap<>();
        for (Map.Entry<LootRarity, Float> entry : tierChances.entrySet()) {
            if (allowed != null && !allowed.contains(entry.getKey())) continue;
            float adjusted = entry.getValue() + bonuses.getOrDefault(entry.getKey(), 0f);
            if (adjusted > 0f) pool.put(entry.getKey(), adjusted);
        }
        float totalWeight = pool.values().stream().reduce(0f, Float::sum);
        if (totalWeight <= 0f) return null;
        if (rand.nextFloat() >= totalWeight) return null;
        float roll = rand.nextFloat() * totalWeight;
        float cumulative = 0f;
        for (Map.Entry<LootRarity, Float> entry : pool.entrySet()) {
            cumulative += entry.getValue();
            if (roll < cumulative) return entry.getKey();
        }
        return null;
    }

    public boolean isBlacklisted(EntityType<?> type, @Nullable ResourceLocation id) {
        if (id != null && blacklist.contains(id)) return true;
        for (TagKey<EntityType<?>> tag : blacklistTags) {
            if (type.builtInRegistryHolder().is(tag)) return true;
        }
        return false;
    }

    public List<EntityAffixEntry> getAffixesForRarity(LootRarity rarity) {
        return affixes.getOrDefault(getRarityKey(rarity), Collections.emptyList());
    }

    public List<EntityAffixEntry> getMobAffixesForRarity(LootRarity rarity) {
        return mobAffixes.getOrDefault(getRarityKey(rarity), Collections.emptyList());
    }

    public float getStatChance(LootRarity rarity) {
        return statChances.getOrDefault(getRarityKey(rarity), 1.0f);
    }

    public String getRarityKey(LootRarity rarity) {
        ResourceLocation loc = RarityRegistry.INSTANCE.getKey(rarity);
        return loc != null ? loc.getPath() : "unknown";
    }
}