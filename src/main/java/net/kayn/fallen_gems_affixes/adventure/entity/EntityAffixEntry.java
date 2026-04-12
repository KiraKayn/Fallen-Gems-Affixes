package net.kayn.fallen_gems_affixes.adventure.entity;

import net.minecraft.resources.ResourceLocation;

/**
 * Represents a single affix entry stored in the universal_boss JSON.
 * Level is clamped to [0, 1].
 */
public record EntityAffixEntry(ResourceLocation affixId, float level, float chance) {
    public EntityAffixEntry {
        level = Math.max(0f, Math.min(1f, level));
        chance = Math.max(0f, Math.min(1f, chance));
    }
}