package net.kayn.fallen_gems_affixes.adventure.socket.gem.storage;

import dev.shadowsoffire.apotheosis.adventure.Adventure;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.cutting.GemCuttingMenu;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Map;

public record GemUpgradeMatch(
        DynamicHolder<LootRarity> fromRarity,
        int dustSlot,
        int matSlot,
        int dustCost,
        int matCost
) {

    public void execute(Container matInv, Map<DynamicHolder<LootRarity>, Integer> map) {
        int fromCount = map.getOrDefault(fromRarity, 0);
        map.put(fromRarity, fromCount - 2);
        DynamicHolder<LootRarity> toRarity = RarityRegistry.next(fromRarity);
        map.merge(toRarity, 1, Integer::sum);
        matInv.removeItem(dustSlot, dustCost);
        matInv.removeItem(matSlot, matCost);
        matInv.setChanged();
    }

    @Nullable
    public static GemUpgradeMatch findMatch(DynamicHolder<LootRarity> fromRarity, Container matInv) {
        if (fromRarity == RarityRegistry.getMaxRarity()) return null;

        int dustCost = GemCuttingMenu.getDustCost(fromRarity.get());

        int dustSlot = -1;
        for (int i = 0; i < matInv.getContainerSize(); i++) {
            ItemStack s = matInv.getItem(i);
            if (s.getItem() == Adventure.Items.GEM_DUST.get() && s.getCount() >= dustCost) {
                dustSlot = i;
                break;
            }
        }
        if (dustSlot < 0) return null;

        for (int i = 0; i < matInv.getContainerSize(); i++) {
            if (i == dustSlot) continue;
            ItemStack s = matInv.getItem(i);
            if (!RarityRegistry.isMaterial(s.getItem())) continue;
            DynamicHolder<LootRarity> matRarity = RarityRegistry.getMaterialRarity(s.getItem());
            if (!matRarity.isBound()) continue;

            int cost;
            if (matRarity == fromRarity) {
                cost = GemCuttingMenu.STD_MAT_COST;
            } else if (matRarity == RarityRegistry.next(fromRarity)) {
                cost = GemCuttingMenu.NEXT_MAT_COST;
            } else if (fromRarity != RarityRegistry.getMinRarity() && matRarity == RarityRegistry.prev(fromRarity)) {
                cost = GemCuttingMenu.PREV_MAT_COST;
            } else {
                continue;
            }

            if (s.getCount() < cost) continue;
            return new GemUpgradeMatch(fromRarity, dustSlot, i, dustCost, cost);
        }
        return null;
    }
}
