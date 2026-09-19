package net.kayn.fallen_gems_affixes.attachment.rarity;

import com.google.common.base.Predicates;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.mixin.accessor.DynamicHolderAccessor;
import net.kayn.fallen_gems_affixes.mixin.accessor.DynamicRegistryAccessor;
import net.kayn.fallen_gems_affixes.mixin.accessor.RarityRegistryAccessor;
import net.minecraft.resources.ResourceLocation;
import net.rtxyd.fallen.lib.runtime.forgemod.addon.apotheosis.SimpleRarityRegistry;
import net.rtxyd.fallen.lib.runtime.forgemod.network.AbstractPacketBoundRegistry;
import net.rtxyd.fallen.lib.runtime.forgemod.util.ILocalRarity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class FallenRarityRegistry extends AbstractPacketBoundRegistry<FallenRarity, ClientLikeSyncFallenRarityPacket.Begin, ClientLikeSyncFallenRarityPacket, ClientLikeSyncFallenRarityPacket.End> implements Iterable<Map.Entry<ResourceLocation, FallenRarity>> {
    public final SimpleRarityRegistry<ResourceLocation, ILocalRarity> rarityRegistry = new SimpleRarityRegistry<>();

    public FallenRarityRegistry() {
        super(FallenGemsAffixes.LOGGER, "fallen_rarities", "type", Predicates.alwaysTrue(), true, false);
    }

    @Override
    protected void registerBuiltinCodecs() {
        this.registerCodec(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "fallen_rarity"), FallenRarity.CODEC);
    }

    @Override
    public void beginReload() {
        super.beginReload();
        rarityRegistry.reset();
    }

    @Override
    public void onReload() {
        super.onReload();
        FallenGemsAffixes.LOGGER.info("Loading rarities...");
        for (LootRarity ra : RarityRegistry.getSortedRarities()) {
            ILocalRarity rarity = (ILocalRarity) (Object) ra;
            rarityRegistry.register(new FallenRarity(rarity.fallen_lib$getId(), rarity));
        }
        for (Map.Entry<ResourceLocation, FallenRarity> en : this.registry.entrySet()) {
            FallenRarity rarity = en.getValue();
            rarityRegistry.register(rarity);
        }
        FallenGemsAffixes.LOGGER.info("Finalize loading...");
        processApothRarities();
        FallenGemsAffixes.LOGGER.info("Loading complete with {} entries", rarityRegistry.getRarityMapView().size());
    }

    private void processApothRarities() {
        var regAccessor = (DynamicRegistryAccessor) RarityRegistry.INSTANCE;
        var ordAccessor = (RarityRegistryAccessor) RarityRegistry.INSTANCE;

        BiMap<ResourceLocation, LootRarity> map = HashBiMap.create(regAccessor.getRegistry());

        var ordered = new ArrayList<>(ordAccessor.getSorted());
        ordered.removeIf(rarity -> {
            ResourceLocation key = RarityRegistry.INSTANCE.getKey(rarity);
            return key != null && Fallen.Common.FALLEN_RARITIES.contains(key);
        });

        var fallenRarities = registry.values();
        for (FallenRarity fallenRarity : fallenRarities) {
            ResourceLocation location = fallenRarity.getClassifier();
            LootRarity rarity = (LootRarity) (Object) fallenRarity.getRarity();
            if (rarity == null) continue;
            regAccessor.getHolders().remove(location);
            RarityRegistry.INSTANCE.holder(location);
            map.put(location, rarity);
        }

        ordAccessor.setSorted(ImmutableList.copyOf(ordered));
        regAccessor.setRegistry(ImmutableBiMap.copyOf(map));
        regAccessor.getHolders().values().forEach(h -> ((DynamicHolderAccessor) h).invokeBind());
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull Iterator<Map.Entry<ResourceLocation, FallenRarity>> iterator() {
        return (Iterator<Map.Entry<ResourceLocation, FallenRarity>>) (Object) rarityRegistry.getRarityMapView().entrySet().iterator();
    }
}