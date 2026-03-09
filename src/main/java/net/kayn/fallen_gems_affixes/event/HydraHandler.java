/*
package net.kayn.fallen_gems_affixes.event;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus.HydraBonus;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "fallen_gems_affixes")
public class HydraHandler {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();

        boolean onFire = victim.isOnFire();

        final float[] totalReduction = {0f};
        final float[] totalPenalty = {0f};

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) continue;

            ItemStack armor = victim.getItemBySlot(slot);
            if (armor.isEmpty()) continue;

            LootCategory category = LootCategory.forItem(armor);

            SocketHelper.getGems(armor).forEach(gemInstance -> {
                if (!gemInstance.isValid()) return;
                if (!gemInstance.rarity().isBound()) return;

                LootRarity rarity = gemInstance.rarity().get();
                gemInstance.gem().get().getBonus(category, rarity).ifPresent(bonus -> {
                    if (bonus instanceof HydraBonus hydra && hydra.supports(rarity)) {
                        totalReduction[0] += hydra.reduction.get(rarity).get(0);
                        totalPenalty[0]   += hydra.firePenalty.get(rarity).get(0);
                    }
                });
            });
        }

        boolean isFireDamage = event.getSource().is(DamageTypeTags.IS_FIRE);
        boolean hasFireResistance = victim.hasEffect(MobEffects.FIRE_RESISTANCE);

        if (isFireDamage) {
            if (totalPenalty[0] > 0) {
                event.setAmount(event.getAmount() * (1.0f + totalPenalty[0]));
            }
        } else if (!hasFireResistance) {
            if (totalReduction[0] > 0) {
                totalReduction[0] = Math.min(totalReduction[0], 0.8f);
                event.setAmount(event.getAmount() * (1.0f - totalReduction[0]));
            }
        }
    }
}*/
