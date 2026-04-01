package net.kayn.fallen_gems_affixes.adventure.boss;

import dev.shadowsoffire.apotheosis.adventure.boss.BossStats;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record UniversalBossConfig(
        Map<LootRarity, Float> tierChances,
        Map<LootRarity, BossStats> stats,
        List<ResourceLocation> blacklist,
        List<TagKey<EntityType<?>>> blacklistTags,
        Map<ResourceLocation, List<LootRarity>> dimensionRarities
) {


    @Nullable
    public List<LootRarity> getRaritiesForDimension(ResourceLocation dimensionId) {
        if (dimensionRarities.isEmpty()) return null;
        return dimensionRarities.getOrDefault(dimensionId, List.of());
    }

    @Nullable
    public LootRarity rollRarity(RandomSource rand) {
        return rollRarity(rand, null);
    }

    @Nullable
    public LootRarity rollRarity(RandomSource rand, @Nullable Set<LootRarity> allowed) {
        Map<LootRarity, Float> pool = tierChances;
        if (allowed != null) {
            pool = new LinkedHashMap<>();
            for (Map.Entry<LootRarity, Float> entry : tierChances.entrySet()) {
                if (allowed.contains(entry.getKey())) pool.put(entry.getKey(), entry.getValue());
            }
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

    public String getRarityKey(LootRarity rarity) {
        ResourceLocation key = RarityRegistry.INSTANCE.getKey(rarity);
        return key != null ? key.getPath() : "unknown";
    }
}