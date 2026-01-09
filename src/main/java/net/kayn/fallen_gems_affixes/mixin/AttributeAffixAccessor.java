package net.kayn.fallen_gems_affixes.mixin;

import dev.shadowsoffire.apotheosis.adventure.affix.AttributeAffix;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(AttributeAffix.class)
public interface AttributeAffixAccessor {
    @Accessor
    Map<LootRarity, AttributeAffix.ModifierInst> getModifiers();
}
