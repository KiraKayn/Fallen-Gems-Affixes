package net.kayn.fallen_gems_affixes.adventure.socket.gem.storage;

import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemItem;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemRegistry;
import dev.shadowsoffire.placebo.block_entity.TickingBlockEntity;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public abstract class GemCaseTile extends BlockEntity implements TickingBlockEntity {

    public final Object2ObjectMap<DynamicHolder<Gem>, Map<DynamicHolder<LootRarity>, Integer>> gems = new Object2ObjectLinkedOpenHashMap<>();

    protected final Set<GemCaseMenu> activeContainers = new HashSet<>();
    protected final int maxCount;

    public final ItemStack[] upgradeMatItems = new ItemStack[GemCaseMenu.UPGRADE_MAT_COUNT];

    private GemCaseAnimationState animationState;
    private final LazyOptional<IItemHandler> itemHandlerOpt = LazyOptional.of(GemCaseItemHandler::new);

    public GemCaseTile(BlockEntityType<?> type, BlockPos pos, BlockState state, int maxCount) {
        super(type, pos, state);
        this.maxCount = maxCount;
        Arrays.fill(upgradeMatItems, ItemStack.EMPTY);
    }

    public void depositGem(ItemStack stack) {
        DynamicHolder<Gem> gemHolder = GemItem.getGem(stack);
        if (!gemHolder.isBound()) return;
        DynamicHolder<LootRarity> rarityHolder = AffixHelper.getRarity(stack);
        if (!rarityHolder.isBound()) return;
        Map<DynamicHolder<LootRarity>, Integer> map = getGems(gemHolder);
        int current = map.getOrDefault(rarityHolder, 0);
        map.put(rarityHolder, Math.min(maxCount, current + stack.getCount()));
        dispatchToNearbyPlayers();
        setChanged();
    }

    public ItemStack extractGem(DynamicHolder<Gem> gem, DynamicHolder<LootRarity> rarity, int count) {
        Map<DynamicHolder<LootRarity>, Integer> map = getGems(gem);
        int stored = map.getOrDefault(rarity, 0);
        if (stored == 0 || !gem.isBound() || !rarity.isBound()) return ItemStack.EMPTY;
        count = Math.min(count, stored);
        if (count <= 0) return ItemStack.EMPTY;
        int remaining = stored - count;
        if (remaining <= 0) map.remove(rarity);
        else map.put(rarity, remaining);
        if (map.isEmpty()) gems.remove(gem);
        ItemStack result = GemRegistry.createGemStack(gem.get(), rarity.get());
        result.setCount(count);
        dispatchToNearbyPlayers();
        setChanged();
        return result;
    }

    public boolean upgradeGem(DynamicHolder<Gem> gem, DynamicHolder<LootRarity> fromRarity, Container matInv) {
        GemUpgradeMatch match = getUpgradeMatch(gem, fromRarity, matInv);
        if (match != null) {
            match.execute(matInv, getGems(gem));
            dispatchToNearbyPlayers();
            setChanged();
            return true;
        }
        return false;
    }

    @Nullable
    public GemUpgradeMatch getUpgradeMatch(DynamicHolder<Gem> gem, DynamicHolder<LootRarity> fromRarity, Container matInv) {
        Map<DynamicHolder<LootRarity>, Integer> map = getGems(gem);
        if (map.getOrDefault(fromRarity, 0) < 2) return null;
        return GemUpgradeMatch.findMatch(fromRarity, matInv);
    }

    public int getCount(DynamicHolder<Gem> gem, DynamicHolder<LootRarity> rarity) {
        return getGems(gem).getOrDefault(rarity, 0);
    }

    public int getCount(Gem gem, DynamicHolder<LootRarity> rarity) {
        ResourceLocation id = GemRegistry.INSTANCE.getKey(gem);
        if (id == null) return 0;
        return getCount(GemRegistry.INSTANCE.holder(id), rarity);
    }

    public final Map<DynamicHolder<LootRarity>, Integer> getGems(DynamicHolder<Gem> gem) {
        return gems.computeIfAbsent(gem, g -> new HashMap<>());
    }

    public GemCaseAnimationState getAnimationState() {
        if (animationState == null) animationState = new GemCaseAnimationState(level.getRandom());
        return animationState;
    }


    @Override
    public void clientTick(Level level, BlockPos pos, BlockState state) {
        GemCaseAnimationState animState = getAnimationState();
        int uniqueGems = 0;
        for (Map<DynamicHolder<LootRarity>, Integer> rarityMap : gems.values()) {
            if (rarityMap.values().stream().mapToInt(Integer::intValue).sum() > 0) uniqueGems++;
        }
        Player player = level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4, false);
        animState.tick(Math.min(uniqueGems, 16), player != null);
    }

    public void saveGemData(CompoundTag tag) {
        CompoundTag gemsTag = new CompoundTag();
        for (Map.Entry<DynamicHolder<Gem>, Map<DynamicHolder<LootRarity>, Integer>> entry : gems.entrySet()) {
            CompoundTag rarityTag = new CompoundTag();
            for (Map.Entry<DynamicHolder<LootRarity>, Integer> rEntry : entry.getValue().entrySet()) {
                if (rEntry.getValue() > 0) rarityTag.putInt(rEntry.getKey().getId().toString(), rEntry.getValue());
            }
            if (!rarityTag.isEmpty()) gemsTag.put(entry.getKey().getId().toString(), rarityTag);
        }
        tag.put("gems", gemsTag);

        CompoundTag matsTag = new CompoundTag();
        for (int i = 0; i < upgradeMatItems.length; i++) {
            if (!upgradeMatItems[i].isEmpty())
                matsTag.put(String.valueOf(i), upgradeMatItems[i].save(new CompoundTag()));
        }
        if (!matsTag.isEmpty()) tag.put("upgrade_mats", matsTag);
    }

    public void loadGemData(CompoundTag tag) {
        gems.clear();
        CompoundTag gemsTag = tag.getCompound("gems");
        for (String gemKey : gemsTag.getAllKeys()) {
            DynamicHolder<Gem> gemHolder = GemRegistry.INSTANCE.holder(new ResourceLocation(gemKey));
            if (!gemHolder.isBound()) continue;
            CompoundTag rarityTag = gemsTag.getCompound(gemKey);
            Map<DynamicHolder<LootRarity>, Integer> map = new HashMap<>();
            for (String rarityKey : rarityTag.getAllKeys()) {
                DynamicHolder<LootRarity> rarityHolder = RarityRegistry.INSTANCE.holder(new ResourceLocation(rarityKey));
                if (!rarityHolder.isBound()) continue;
                int count = rarityTag.getInt(rarityKey);
                if (count > 0) map.put(rarityHolder, count);
            }
            if (!map.isEmpty()) gems.put(gemHolder, map);
        }
        Arrays.fill(upgradeMatItems, ItemStack.EMPTY);
        if (tag.contains("upgrade_mats")) {
            CompoundTag matsTag = tag.getCompound("upgrade_mats");
            for (int i = 0; i < upgradeMatItems.length; i++) {
                if (matsTag.contains(String.valueOf(i)))
                    upgradeMatItems[i] = ItemStack.of(matsTag.getCompound(String.valueOf(i)));
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        saveGemData(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        loadGemData(tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveGemData(tag);
        return tag;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        if (pkt.getTag() != null) {
            loadGemData(pkt.getTag());
            activeContainers.forEach(GemCaseMenu::onChanged);
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void addListener(GemCaseMenu ctr) {
        activeContainers.add(ctr);
    }

    public void removeListener(GemCaseMenu ctr) {
        activeContainers.remove(ctr);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return itemHandlerOpt.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandlerOpt.invalidate();
    }

    private void dispatchToNearbyPlayers() {
        if (level == null || level.isClientSide()) return;
        ClientboundBlockEntityDataPacket pkt = getUpdatePacket();
        ((ServerLevel) level).getChunkSource().chunkMap.getPlayers(new ChunkPos(getBlockPos()), false).forEach(p -> p.connection.send(pkt));
    }

    private class GemCaseItemHandler implements IItemHandler {

        private List<DynamicHolder<LootRarity>> getRarities() {
            return RarityRegistry.INSTANCE.getOrderedRarities();
        }

        @Override
        public int getSlots() {
            return 1 + gems.size() * getRarities().size();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            if (slot == 0) return ItemStack.EMPTY;
            GemRarityPair pair = getGemRarityForSlot(slot - 1);
            if (pair == null) return ItemStack.EMPTY;
            int count = getCount(pair.gem(), pair.rarity());
            if (count <= 0) return ItemStack.EMPTY;
            ItemStack s = GemRegistry.createGemStack(pair.gem().get(), pair.rarity().get());
            s.setCount(Math.min(count, s.getMaxStackSize()));
            return s;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            DynamicHolder<Gem> g = GemItem.getGem(stack);
            if (!g.isBound()) return stack;
            DynamicHolder<LootRarity> r = AffixHelper.getRarity(stack);
            if (!r.isBound()) return stack;
            if (!simulate) depositGem(stack);
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot == 0 || amount <= 0) return ItemStack.EMPTY;
            GemRarityPair pair = getGemRarityForSlot(slot - 1);
            if (pair == null) return ItemStack.EMPTY;
            if (simulate) {
                int count = Math.min(getCount(pair.gem(), pair.rarity()), amount);
                if (count <= 0) return ItemStack.EMPTY;
                ItemStack s = GemRegistry.createGemStack(pair.gem().get(), pair.rarity().get());
                s.setCount(count);
                return s;
            }
            return extractGem(pair.gem(), pair.rarity(), amount);
        }

        @Override
        public int getSlotLimit(int slot) {
            return maxCount;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return GemItem.getGem(stack).isBound();
        }

        @Nullable
        private GemRarityPair getGemRarityForSlot(int index) {
            List<DynamicHolder<LootRarity>> rarities = getRarities();
            int i = 0;
            for (DynamicHolder<Gem> gem : gems.keySet()) {
                for (DynamicHolder<LootRarity> rarity : rarities) {
                    if (i == index) return new GemRarityPair(gem, rarity);
                    i++;
                }
            }
            return null;
        }
    }

    public record GemRarityPair(DynamicHolder<Gem> gem, DynamicHolder<LootRarity> rarity) {
    }

    public static class BasicGemCaseTile extends GemCaseTile {
        public BasicGemCaseTile(BlockPos pos, BlockState state) {
            super(GemCaseRegistry.GEM_CASE_TILE.get(), pos, state, Short.MAX_VALUE);
        }
    }

    public static class EnderGemCaseTile extends GemCaseTile {
        public EnderGemCaseTile(BlockPos pos, BlockState state) {
            super(GemCaseRegistry.ENDER_GEM_CASE_TILE.get(), pos, state, Integer.MAX_VALUE);
        }
    }
}