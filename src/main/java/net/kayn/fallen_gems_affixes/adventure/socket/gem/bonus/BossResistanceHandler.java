package net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import net.kayn.fallen_gems_affixes.util.BossUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = "fallen_gems_affixes")
public class BossResistanceHandler {

    private static final TagKey<EntityType<?>> BOSS_TAG = TagKey.create(
            ForgeRegistries.ENTITY_TYPES.getRegistryKey(),
            new ResourceLocation("fallen_gems_affixes", "boss_slayer")
    );

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();

        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;
        if (!BossUtil.isBoss(attacker, BOSS_TAG)) return;

        final float[] totalReduction = {0.0F};

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
                    if (bonus instanceof BossResistanceBonus resistance && resistance.supports(rarity)) {
                        double reduction = resistance.values.get(rarity).get(0);
                        totalReduction[0] += (float) reduction;
                    }
                });
            });
        }
        // Max 80%
        if (totalReduction[0] > 0) {
            totalReduction[0] = Math.min(totalReduction[0], 0.8F);
            event.setAmount(event.getAmount() * (1.0F - totalReduction[0]));
        }
    }
}