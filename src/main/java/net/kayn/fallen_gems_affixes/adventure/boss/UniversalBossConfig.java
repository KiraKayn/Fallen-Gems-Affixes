package net.kayn.fallen_gems_affixes.adventure.boss;

import dev.shadowsoffire.apotheosis.adventure.boss.BossStats;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public record UniversalBossConfig(Map<LootRarity, Float> tierChances, Map<LootRarity, BossStats> stats,
                                  List<ResourceLocation> blacklist, List<TagKey<EntityType<?>>> blacklistTags) {

    @Nullable
    public LootRarity rollRarity(RandomSource rand) {
        float totalWeight = tierChances.values().stream().reduce(0f, Float::sum);
        if (totalWeight <= 0f) return null;

        if (rand.nextFloat() >= totalWeight) return null;

        float roll = rand.nextFloat() * totalWeight;
        float cumulative = 0f;

        for (Map.Entry<LootRarity, Float> entry : tierChances.entrySet()) {
            cumulative += entry.getValue();
            if (roll < cumulative) return entry.getKey();
        }

        return null;
    }

    public boolean isBlacklisted(EntityType<?> type, @Nullable ResourceLocation id) {
        if (id != null && blacklist.contains(id)) return true;
        for (TagKey<EntityType<?>> tag : blacklistTags) {
            if (type.builtInRegistryHolder().is(tag)) {
                return true;
            }
        }

        return false;
    }

    public String getRarityKey(LootRarity rarity) {
        ResourceLocation key = RarityRegistry.INSTANCE.getKey(rarity);
        return key != null ? key.getPath() : "unknown";


    }
}