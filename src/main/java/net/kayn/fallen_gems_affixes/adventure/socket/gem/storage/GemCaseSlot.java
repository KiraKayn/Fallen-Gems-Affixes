package net.kayn.fallen_gems_affixes.adventure.socket.gem.storage;

import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemItem;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class GemCaseSlot extends Slot {

    private static final Container EMPTY_CONTAINER = new SimpleContainer(0);

    private final GemCaseMenu menu;
    final DynamicHolder<LootRarity> rarity;

    public GemCaseSlot(GemCaseMenu menu, DynamicHolder<LootRarity> rarity, int x, int y) {
        super(EMPTY_CONTAINER, -1, x, y);
        this.menu = menu;
        this.rarity = rarity;
    }

    @Override
    public ItemStack getItem() {
        Gem gem = menu.selectedGem;
        if (gem == null) return ItemStack.EMPTY;
        int count = menu.getGemCount(gem, rarity);
        if (count <= 0) return ItemStack.EMPTY;
        ItemStack s = GemRegistry.createGemStack(gem, rarity.get());
        s.setCount(Math.min(count, s.getMaxStackSize()));
        return s;
    }

    @Override
    public boolean hasItem() {
        Gem gem = menu.selectedGem;
        return gem != null && menu.getGemCount(gem, rarity) > 0;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public boolean mayPickup(Player player) {
        return hasItem();
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        if (!stack.isEmpty()) {
            DynamicHolder<Gem> gem = GemItem.getGem(stack);
            DynamicHolder<LootRarity> rarity = AffixHelper.getRarity(stack);
            if (!gem.isBound() || gem.get() != menu.selectedGem || !rarity.get().equals(this.rarity.get())) {
                FallenGemsAffixes.LOGGER.warn("Player {} tried to take a gem that doesn't match! (gem={}, rarity={})", player.getName().getString(), gem.getId(), rarity.getId());
                return;
            }
            menu.extractGem(this.rarity, stack.getCount());
        }
        setChanged();
    }

    @Override
    public ItemStack remove(int amount) {
        Gem gem = menu.selectedGem;
        if (gem == null) return ItemStack.EMPTY;
        int count = menu.getGemCount(gem, rarity);
        int toExtract = Math.min(count, amount);
        if (toExtract <= 0) return ItemStack.EMPTY;
        ItemStack s = GemRegistry.createGemStack(gem, rarity.get());
        s.setCount(toExtract);
        return s;
    }

    @Override
    public void set(ItemStack stack) {
    }

    @Override
    public void setByPlayer(ItemStack stack) {
    }

    @Override
    public void setChanged() {
        container.setChanged();
    }

    @Override
    public int getMaxStackSize() {
        return menu.getMaxCount();
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return Math.min(getMaxStackSize(), stack.getMaxStackSize());
    }

    @Override
    public boolean isHighlightable() {
        return true;
    }

    @Override
    public boolean isSameInventory(Slot other) {
        return false;
    }

    @Override
    public boolean allowModification(Player player) {
        return false;
    }

    public boolean isActive() {
        Gem gem = menu.selectedGem;
        if (gem == null) return false;
        boolean inRange = rarity.get().isAtLeast(gem.getMinRarity()) && rarity.get().isAtMost(gem.getMaxRarity());
        boolean hasItems = menu.getGemCount(gem, rarity) > 0;
        return inRange || hasItems;
    }
}