package net.kayn.fallen_gems_affixes.attachment.rarity;

import com.google.common.base.Predicates;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraft.resources.ResourceLocation;
import net.rtxyd.fallen.lib.runtime.forgemod.addon.apotheosis.SimpleRarityRegistry;
import net.rtxyd.fallen.lib.runtime.forgemod.network.AbstractPacketBoundRegistry;
import net.rtxyd.fallen.lib.runtime.forgemod.util.GameLifecycleHelper;
import net.rtxyd.fallen.lib.runtime.forgemod.util.ILocalRarity;
import org.jetbrains.annotations.NotNull;

import java.util.*;

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
        for (DynamicHolder<LootRarity> ra : RarityRegistry.INSTANCE.getOrderedRarities()) {
            if (ra.get() instanceof ILocalRarity rarity) {
                rarityRegistry.register(new FallenRarity(rarity.fallen_lib$getId(), rarity));
            }
        }
        for (Map.Entry<ResourceLocation, FallenRarity> en : this.registry.entrySet()) {
            FallenRarity rarity = en.getValue();
            rarityRegistry.register(rarity);
        }
        GameLifecycleHelper.submitContextCall(Fallen.ContextKeys.FALLEN_RARITIES, () -> this.registry.values());
        GameLifecycleHelper.callAndRemoveIfPresent(Fallen.ContextKeys.DELAYED_RARITY_REGISTER, GameLifecycleHelper.EMPTY_EX_CONSUMER);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull Iterator<Map.Entry<ResourceLocation, FallenRarity>> iterator() {
        return (Iterator<Map.Entry<ResourceLocation, FallenRarity>>)(Object)rarityRegistry.getRarityMapView().entrySet().iterator();
    }
}
