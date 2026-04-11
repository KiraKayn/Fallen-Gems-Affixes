package net.kayn.fallen_gems_affixes.adventure.socket.gem.storage;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemItem;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemRegistry;
import dev.shadowsoffire.placebo.menu.BlockEntityMenu;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public class GemCaseMenu extends BlockEntityMenu<GemCaseTile> {

    public static final int INPUT_SLOT = 0;
    public static final int FILTER_SLOT = 1;
    public static final int FIRST_RARITY_SLOT = 2;
    public static final int UPGRADE_MAT_COUNT = 6;

    public final SimpleContainer ioInv = new SimpleContainer(2);
    public final SimpleContainer upgradeMatInv = new SimpleContainer(UPGRADE_MAT_COUNT) {
        @Override
        public void setChanged() {
            super.setChanged();
            GemCaseMenu.this.onChanged();
        }
    };

    @Nullable
    public Gem selectedGem = null;

    private final List<DynamicHolder<LootRarity>> orderedRarities;
    private Runnable notifier = null;

    public GemCaseMenu(int id, Inventory inv, BlockPos pos) {
        super(GemCaseRegistry.GEM_CASE_MENU.get(), id, inv, pos);
        this.orderedRarities = RarityRegistry.INSTANCE.getOrderedRarities();

        if (this.tile != null) {
            this.tile.addListener(this);
            for (int i = 0; i < UPGRADE_MAT_COUNT; i++) {
                upgradeMatInv.setItem(i, tile.upgradeMatItems[i].copy());
            }
        }

        initSlots(inv);
    }

    public int getFirstUpgradeMatSlot() {
        return FIRST_RARITY_SLOT + orderedRarities.size();
    }

    public int getMaxCount() {
        return tile != null ? tile.maxCount : Short.MAX_VALUE;
    }

    private void initSlots(Inventory inv) {
        addSlot(new Slot(ioInv, 0, 142, 99) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return GemItem.getGem(stack).isBound();
            }

            @Override
            public int getMaxStackSize() {
                return 64;
            }

            @Override
            public void setChanged() {
                super.setChanged();
                if (!GemCaseMenu.this.level.isClientSide() && !getItem().isEmpty()) {
                    tile.depositGem(getItem());
                }
                if (!getItem().isEmpty() && GemCaseMenu.this.level.isClientSide()) {
                    inv.player.level().playSound(inv.player, pos, SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.NEUTRAL, 0.5F, 0.7F);
                }
                ioInv.setItem(0, ItemStack.EMPTY);
            }
        });

        addSlot(new Slot(ioInv, 1, 142, 18) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return !LootCategory.forItem(stack).isNone();
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public void setChanged() {
                GemCaseMenu.this.onChanged();
            }
        });

        for (int i = 0; i < orderedRarities.size(); i++) {
            addSlot(new GemCaseSlot(this, orderedRarities.get(i), 21 + i * 18, 91));
        }

        for (int i = 0; i < UPGRADE_MAT_COUNT; i++) {
            addSlot(new Slot(upgradeMatInv, i, -45 + 18 * (i % 2), 37 + 18 * (i / 2)) {
                @Override
                public int getMaxStackSize() {
                    return 64;
                }

                @Override
                public void setChanged() {
                    super.setChanged();
                    GemCaseMenu.this.onChanged();
                }
            });
        }

        addPlayerSlots(inv, 8, 148);

        mover.registerRule((stack, slot) -> slot == FILTER_SLOT, this.playerInvStart, slots.size());
        mover.registerRule((stack, slot) -> isRaritySlot(slot), this.playerInvStart, slots.size());
        mover.registerRule((stack, slot) -> isUpgradeMatSlot(slot), this.playerInvStart, slots.size());
        mover.registerRule((stack, slot) -> slot >= this.playerInvStart && GemItem.getGem(stack).isBound(), INPUT_SLOT, INPUT_SLOT + 1);
        mover.registerRule((stack, slot) -> slot >= this.playerInvStart && !LootCategory.forItem(stack).isNone(), FILTER_SLOT, FILTER_SLOT + 1);
        registerInvShuffleRules();
    }

    private boolean isRaritySlot(int slot) {
        return slot >= FIRST_RARITY_SLOT && slot < FIRST_RARITY_SLOT + orderedRarities.size();
    }

    private boolean isUpgradeMatSlot(int slot) {
        int base = getFirstUpgradeMatSlot();
        return slot >= base && slot < base + UPGRADE_MAT_COUNT;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        boolean shift = (id & 0x1000) != 0;
        int fromOrdinal = id & 0xFFF;
        if (selectedGem == null) return false;
        List<DynamicHolder<LootRarity>> rarities = RarityRegistry.INSTANCE.getOrderedRarities();
        if (fromOrdinal < 0 || fromOrdinal >= rarities.size() - 1) return false;

        DynamicHolder<LootRarity> fromRarity = RarityRegistry.byOrdinal(fromOrdinal);
        DynamicHolder<Gem> holder = GemRegistry.INSTANCE.holder(GemRegistry.INSTANCE.getKey(selectedGem));

        int tries = shift ? 64 : 1;
        boolean any = false;
        while (tries-- > 0) {
            if (!tile.upgradeGem(holder, fromRarity, upgradeMatInv)) break;
            any = true;
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.BLOCKS, 1F, 1.5F + 0.35F * (1 - 2 * level.random.nextFloat()));
        }
        return any;
    }

    public void setSelectedGem(DynamicHolder<Gem> holder) {
        selectedGem = holder.isBound() ? holder.get() : null;
        onChanged();
    }

    public int getGemCount(Gem gem) {
        int sum = 0;
        for (DynamicHolder<LootRarity> r : orderedRarities) sum += tile.getCount(gem, r);
        return sum;
    }

    public int getGemCount(Gem gem, DynamicHolder<LootRarity> rarity) {
        return tile.getCount(gem, rarity);
    }

    public ItemStack extractGem(DynamicHolder<LootRarity> rarity, int count) {
        if (selectedGem == null) return ItemStack.EMPTY;
        DynamicHolder<Gem> holder = GemRegistry.INSTANCE.holder(GemRegistry.INSTANCE.getKey(selectedGem));
        return tile.extractGem(holder, rarity, count);
    }

    @Nullable
    public GemUpgradeMatch getUpgradeMatch(DynamicHolder<LootRarity> fromRarity) {
        if (selectedGem == null) return null;
        DynamicHolder<Gem> holder = GemRegistry.INSTANCE.holder(GemRegistry.INSTANCE.getKey(selectedGem));
        return tile.getUpgradeMatch(holder, fromRarity, upgradeMatInv);
    }

    public List<DynamicHolder<LootRarity>> getOrderedRarities() {
        return orderedRarities;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < 16 * 16 && tile != null && !tile.isRemoved();
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!level.isClientSide() && tile != null) {
            tile.removeListener(this);
            for (int i = 0; i < UPGRADE_MAT_COUNT; i++) {
                tile.upgradeMatItems[i] = upgradeMatInv.getItem(i).copy();
            }
            tile.setChanged();
        }
        clearContainer(player, ioInv);
    }

    public void setNotifier(Runnable r) {
        notifier = r;
    }

    public void onChanged() {
        if (notifier != null) notifier.run();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (getSlot(index) instanceof GemCaseSlot) {
            mover.quickMoveStack(this, player, index);
            return ItemStack.EMPTY;
        }
        return mover.quickMoveStack(this, player, index);
    }

    @Override
    public void onQuickMove(ItemStack original, ItemStack remaining, Slot slot) {
        if (slot instanceof GemCaseSlot gss && selectedGem != null) {
            int amount = original.getCount() - remaining.getCount();
            DynamicHolder<Gem> holder = GemRegistry.INSTANCE.holder(GemRegistry.INSTANCE.getKey(selectedGem));
            tile.extractGem(holder, gss.rarity, amount);
        }
        slot.setChanged();
    }
}