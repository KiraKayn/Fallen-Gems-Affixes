package net.kayn.fallen_gems_affixes.adventure.affix;

import dev.shadowsoffire.apotheosis.adventure.affix.AttributeAffix;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.Map;
import java.util.Set;

public class AdaptiveSpellPowerAffix extends AttributeAffix {

    public AdaptiveSpellPowerAffix(Attribute attr, AttributeModifier.Operation op, Map<LootRarity, StepFunction> values, Set<LootCategory> types) {
        super(attr, op, values, types);
    }
}