package net.kayn.fallen_gems_affixes.event;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

public class HurtEventHandler {
    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;

        ItemStack weapon = attacker.getMainHandItem();
        DamageSource damageSource = event.getSource();
        Map<IDamageOrResistanceBonus.BonusName, Float> damageBonuses = new HashMap<>();
        if (!weapon.isEmpty() && LivingEntity.getEquipmentSlotForItem(weapon) == EquipmentSlot.MAINHAND) {
            LootCategory weaponCat = LootCategory.forItem(weapon);
            SocketHelper.getGems(weapon).forEach(gemInstance -> {
                if (!gemInstance.isValid()) return;
                if (!gemInstance.rarity().isBound()) return;
                LootRarity rarity = gemInstance.rarity().get();
                gemInstance.gem().get().getBonus(weaponCat, rarity).ifPresent(bonus -> {
                    if (bonus instanceof IDamageOrResistanceBonus bonus1
                            && bonus1.getBonusType() != IDamageOrResistanceBonus.BonusType.RESISTANCE
                            && bonus1.checkCondition(attacker, victim, damageSource, IDamageOrResistanceBonus.BonusType.DAMAGE)) {
                        IDamageOrResistanceBonus.BonusName name = bonus1.getBonusName();
                        damageBonuses.compute(name, (k, value) -> {
                            float bonusValue = bonus1.getValue(IDamageOrResistanceBonus.BonusType.DAMAGE, rarity, 0f);
                            if (value == null) {
                                return bonusValue;
                            } else {
                                return value + bonusValue;
                            }
                        });
                    }
                });
            });
        }
        Map<IDamageOrResistanceBonus.BonusName, Float> damageReductions = new HashMap<>();

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) continue;
            ItemStack armor = victim.getItemBySlot(slot);
            if (armor.isEmpty()) continue;
            LootCategory armorCat = LootCategory.forItem(armor);

            SocketHelper.getGems(armor).forEach(gemInstance -> {
                if (!gemInstance.isValid()) return;
                if (!gemInstance.rarity().isBound()) return;
                LootRarity rarity = gemInstance.rarity().get();
                gemInstance.gem().get().getBonus(armorCat, rarity).ifPresent(bonus -> {
                    if (bonus instanceof IDamageOrResistanceBonus bonus1) {
                        IDamageOrResistanceBonus.BonusName name = bonus1.getBonusName();
                        switch (bonus1.getBonusType()) {
                            case RESISTANCE -> {
                                if (bonus1.checkCondition(attacker, victim, damageSource, IDamageOrResistanceBonus.BonusType.RESISTANCE)) {
                                    damageReductions.compute(name, (k, value) -> {
                                        float reduction = bonus1.getValue(IDamageOrResistanceBonus.BonusType.RESISTANCE, rarity, 0f);
                                        if (value == null) {
                                            return reduction;
                                        } else {
                                            return value + reduction;
                                        }
                                    });
                                }
                            }
                            case BOTH -> {
                                if (bonus1.checkCondition(attacker, victim, damageSource, IDamageOrResistanceBonus.BonusType.DAMAGE)) {
                                    damageBonuses.compute(name, (k, value) -> {
                                        float bonusValue = bonus1.getValue(IDamageOrResistanceBonus.BonusType.DAMAGE, rarity, 0f);
                                        if (value == null) {
                                            return bonusValue;
                                        } else {
                                            return value + bonusValue;
                                        }
                                    });
                                }
                                if (bonus1.checkCondition(attacker, victim, damageSource, IDamageOrResistanceBonus.BonusType.RESISTANCE)) {
                                    damageReductions.compute(name, (k, value) -> {
                                        float reduction = bonus1.getValue(IDamageOrResistanceBonus.BonusType.RESISTANCE, rarity, 0f);
                                        if (value == null) {
                                            return reduction;
                                        } else {
                                            return value + reduction;
                                        }
                                    });
                                }
                            }
                        }
                    }
                });
            });
        }
        for (Float bonus : damageBonuses.values()) {
            if (bonus != null && bonus > 0.01f) {
                event.setAmount(event.getAmount() * (1.0f + bonus));
            }
        }

        for (Float reduction : damageReductions.values()) {
            if (reduction != null && reduction > 0.01f) {
                event.setAmount(event.getAmount() * (1.0f - Math.min(reduction, 0.8f)));
            }
        }
    }
}
