package net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus;

import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public interface IDamageOrResistanceBonus {
    @NotNull
    BonusType getBonusType();

    @NotNull
    BonusName getBonusName();

    float getValue(BonusType type, LootRarity rarity, float level);

    boolean checkCondition(LivingEntity attacker, LivingEntity target, DamageSource damageSource, BonusType type);

    boolean supports(LootRarity rarity);

    enum BonusType {
        DAMAGE,
        RESISTANCE,
        BOTH;
    }
    enum BonusName {
        BOSS,
        HYDRA,
        WET,
        DRAGON
    }
}
