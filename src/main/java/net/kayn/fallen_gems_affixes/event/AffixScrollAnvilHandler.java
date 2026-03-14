package net.kayn.fallen_gems_affixes.event;

import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.kayn.fallen_gems_affixes.config.ModConfig;
import net.kayn.fallen_gems_affixes.item.AffixScrollItem;
import net.kayn.fallen_gems_affixes.recipe.ErasureRecipe;
import net.kayn.fallen_gems_affixes.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

public class AffixScrollAnvilHandler {


    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();

        if (!right.is(ModItems.AFFIX_SCROLL.get())) return;
        if (!AffixScrollItem.hasAffix(right)) return;
        if (left.isEmpty()) return;

        var itemRarityHolder = AffixHelper.getRarity(left);
        if (!itemRarityHolder.isBound()) return;
        LootRarity itemRarity = itemRarityHolder.get();

        LootCategory category = LootCategory.forItem(left);
        if (category.isNone()) return;

        int slotsUsed = ErasureRecipe.getScrollSlotsUsed(left);
        if (slotsUsed >= ModConfig.MAX_SCROLL_SLOTS.get()) {
            event.setOutput(ItemStack.EMPTY);
            return;
        }

        ResourceLocation affixId = AffixScrollItem.getAffixId(right);
        LootRarity rarity = AffixScrollItem.getAffixRarity(right);
        float level = AffixScrollItem.getAffixLevel(right);

        if (affixId == null || rarity == null) return;

        Affix affix = AffixRegistry.INSTANCE.getValue(affixId);
        if (affix == null) return;

        if (!affix.canApplyTo(left, category, rarity)) {
            return;
        }

        if (!itemRarity.equals(rarity)) return;

        var currentAffixes = AffixHelper.getAffixes(left);
        if (currentAffixes != null && currentAffixes.containsKey(AffixRegistry.INSTANCE.holder(affixId))) {
            return;
        }

        ItemStack output = left.copy();

        Map<DynamicHolder<? extends Affix>, AffixInstance> newAffixes = new HashMap<>();
        if (currentAffixes != null) newAffixes.putAll(currentAffixes);

        DynamicHolder<Affix> holder = AffixRegistry.INSTANCE.holder(affixId);
        DynamicHolder<LootRarity> rarityHolder = dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry.INSTANCE.holder(rarity);
        newAffixes.put(holder, new AffixInstance(holder, output, rarityHolder, level));

        AffixHelper.setAffixes(output, newAffixes);
        ErasureRecipe.markScrollAffix(output, affixId);

        event.setOutput(output);
        event.setCost(ModConfig.AFFIX_SCROLL_XP_COST.get());
        event.setMaterialCost(1);
    }
}