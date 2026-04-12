package net.kayn.fallen_gems_affixes.adventure.entity;

import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.ItemStack;

/**
 * Runtime wrapper for an entity-level affix instance.
 * Uses {@link ItemStack#EMPTY} as the proxy stack since entity affixes
 * have no associated item.
 */

public record EntityAffixInstance(
        DynamicHolder<? extends Affix> affix,
        DynamicHolder<LootRarity> rarity,
        float level
) {
    public boolean isValid() {
        return affix.isBound() && rarity.isBound();
    }

    private Affix afx()      { return affix.get(); }
    private LootRarity rty() { return rarity.get(); }

    public MutableComponent getName() {
        return Component.translatable(affix.getId().toString());
    }

    public float getDamageBonus(MobType mobType) {
        return afx().getDamageBonus(ItemStack.EMPTY, rty(), level, mobType);
    }

    public void doPostAttack(LivingEntity user, Entity target) {
        afx().doPostAttack(ItemStack.EMPTY, rty(), level, user, target);
    }

    public void doPostHurt(LivingEntity user, Entity attacker) {
        afx().doPostHurt(ItemStack.EMPTY, rty(), level, user, attacker);
    }

    public float onHurt(DamageSource src, LivingEntity ent, float amount) {
        return afx().onHurt(ItemStack.EMPTY, rty(), level, src, ent, amount);
    }

    public int getDamageProtection(DamageSource source) {
        return afx().getDamageProtection(ItemStack.EMPTY, rty(), level, source);
    }
}