package net.kayn.fallen_gems_affixes.mixin.accessor;

import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(value = RarityRegistry.class, remap = false)
public interface RarityRegistryAccessor {
    @Accessor("sorted")
    List<LootRarity> getSorted();

    @Accessor("sorted")
    void setSorted(List<LootRarity> sorted);
}