package net.kayn.fallen_gems_affixes.mixin;

import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import net.minecraft.resources.ResourceLocation;
import net.rtxyd.fallen.lib.runtime.forgemod.util.ILocalRarity;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = LootRarity.class, remap = false)
@Implements(@Interface(iface = ILocalRarity.class, prefix = "fga$"))
public abstract class LootRarityMixin {

    public ResourceLocation fga$fallen_lib$getId() {
        LootRarity self = (LootRarity) (Object) this;
        return RarityRegistry.INSTANCE.getKey(self);
    }
}