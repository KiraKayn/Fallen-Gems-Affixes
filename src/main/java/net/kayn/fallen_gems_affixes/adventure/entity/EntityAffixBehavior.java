package net.kayn.fallen_gems_affixes.adventure.entity;

import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import net.minecraft.world.entity.LivingEntity;

/**
 * Optional interface for {@link dev.shadowsoffire.apotheosis.adventure.affix.Affix} subclasses
 * that need per-tick logic when applied as entity affixes (e.g. speed/armor buffs
 * based on entity state).
 *
 * <p>Standard Affix methods ({@code onHurt}, {@code doPostAttack}, {@code doPostHurt},
 * {@code getDamageBonus}) are called automatically by {@link EntityAffixEventHandler}
 * for all entity affixes — this interface is only needed for tick-level effects.</p>
 */
public interface EntityAffixBehavior {

    /**
     * Called every {@link #tickInterval()} ticks for each entity that has this
     * affix applied as an entity affix.
     *
     * @param entity the entity bearing the affix
     * @param rarity the resolved rarity of this instance
     * @param level  the level of this instance, in [0, 1]
     */
    void tickEntityAffix(LivingEntity entity, LootRarity rarity, float level);

    /**
     * Tick interval between calls. Defaults to every 5 ticks (4×/second).
     */
    default int tickInterval() {
        return 5;
    }
}