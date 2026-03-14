package net.kayn.fallen_gems_affixes.event;

import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.config.ModConfig;
import net.kayn.fallen_gems_affixes.item.AffixScrollItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class AffixScrollDropHandler {

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Monster mob)) return;

        var data = mob.getPersistentData();
        if (!data.getBoolean("fga.universal_boss")) return;

        String rarityKey = data.getString("fga.universal_boss.rarity");
        if (rarityKey.isEmpty()) return;

        LootRarity rarity;
        try {
            rarity = RarityRegistry.byLegacyId(rarityKey).get();
        } catch (Exception e) {
            return;
        }

        if (mob.getRandom().nextDouble() >= ModConfig.AFFIX_SCROLL_DROP_CHANCE.get()) return;

        Affix affix = rollRandomAffix(rarity, mob.getRandom());
        if (affix == null) return;

        ResourceLocation affixId = AffixRegistry.INSTANCE.getKey(affix);
        if (affixId == null) return;

        float level = mob.getRandom().nextFloat();
        ItemStack scroll = AffixScrollItem.createScroll(affixId, rarity, level);

        ItemEntity itemEntity = new ItemEntity(
                mob.level(),
                mob.getX(), mob.getY() + 0.5, mob.getZ(),
                scroll
        );
        itemEntity.setDefaultPickUpDelay();
        event.getDrops().add(itemEntity);
    }

    private static Affix rollRandomAffix(LootRarity rarity, net.minecraft.util.RandomSource rand) {
        List<Affix> candidates = new ArrayList<>();
        for (Affix affix : AffixRegistry.INSTANCE.getValues()) {
            if (affix.getType().needsValidation()) {
                for (LootCategory cat : LootCategory.VALUES) {
                    if (!cat.isNone() && affix.canApplyTo(ItemStack.EMPTY, cat, rarity)) {
                        candidates.add(affix);
                        break;
                    }
                }
            }
        }

        if (candidates.isEmpty()) {
            FallenGemsAffixes.LOGGER.warn("No affix candidates found for rarity {} when generating scroll", rarityKey(rarity));
            return null;
        }

        return candidates.get(rand.nextInt(candidates.size()));
    }

    private static String rarityKey(LootRarity rarity) {
        ResourceLocation key = RarityRegistry.INSTANCE.getKey(rarity);
        return key != null ? key.toString() : "unknown";
    }
}