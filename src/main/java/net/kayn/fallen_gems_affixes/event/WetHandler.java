package net.kayn.fallen_gems_affixes.event;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus.WetDamageBonus;
import net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus.WetResistanceBonus;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "fallen_gems_affixes")
public class WetHandler {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;

        if (attacker.isInWaterOrRain()) {
            ItemStack weapon = attacker.getMainHandItem();
            LootCategory weaponCat = LootCategory.forItem(weapon);
            final float[] damageBonus = {0f};

            SocketHelper.getGems(weapon).forEach(gemInstance -> {
                if (!gemInstance.isValid()) return;
                if (!gemInstance.rarity().isBound()) return;
                LootRarity rarity = gemInstance.rarity().get();
                gemInstance.gem().get().getBonus(weaponCat, rarity).ifPresent(bonus -> {
                    if (bonus instanceof WetDamageBonus wet && wet.supports(rarity)) {
                        damageBonus[0] += (float) wet.values.get(rarity).get(0);
                    }
                });
            });

            if (damageBonus[0] > 0) {
                event.setAmount(event.getAmount() * (1.0f + damageBonus[0]));
            }
        }
        if (victim.isInWaterOrRain()) {
            final float[] totalReduction = {0f};

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
                        if (bonus instanceof WetResistanceBonus wet && wet.supports(rarity)) {
                            totalReduction[0] += (float) wet.values.get(rarity).get(0);
                        }
                    });
                });
            }

            if (totalReduction[0] > 0) {
                totalReduction[0] = Math.min(totalReduction[0], 0.8f);
                event.setAmount(event.getAmount() * (1.0f - totalReduction[0]));
            }
        }
    }
}