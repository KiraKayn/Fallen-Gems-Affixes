package net.kayn.fallen_gems_affixes.mixin.accessor;

import com.google.common.collect.BiMap;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import dev.shadowsoffire.placebo.reload.DynamicRegistry;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = DynamicRegistry.class, remap = false)
public interface DynamicRegistryAccessor {
    @Accessor("registry")
    BiMap<ResourceLocation, LootRarity> getRegistry();

    @Accessor("registry")
    void setRegistry(BiMap<ResourceLocation, LootRarity> registry);

    @Accessor("holders")
    Map<ResourceLocation, DynamicHolder<?>> getHolders();
}