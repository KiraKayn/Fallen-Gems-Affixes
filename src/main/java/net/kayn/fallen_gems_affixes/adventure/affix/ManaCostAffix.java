package net.kayn.fallen_gems_affixes.adventure.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixType;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Set;

public class ManaCostAffix extends Affix {

    public static final Codec<ManaCostAffix> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                    LootRarity.mapCodec(StepFunction.CODEC).fieldOf("values").forGetter(a -> a.values),
                    LootCategory.SET_CODEC.fieldOf("types").forGetter(a -> a.types)
            ).apply(inst, ManaCostAffix::new));

    protected final Map<LootRarity, StepFunction> values;
    protected final Set<LootCategory> types;

    public ManaCostAffix(Map<LootRarity, StepFunction> values, Set<LootCategory> types) {
        super(AffixType.POTION);
        this.values = values;
        this.types  = types;
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        if (!this.values.containsKey(rarity)) return false;
        return this.types.isEmpty() || this.types.contains(cat);
    }

    @Override
    public MutableComponent getDescription(ItemStack stack, LootRarity rarity, float level) {
        float pct = this.getReductionPercent(rarity, level);
        return Component.translatable(
                "affix.fallen_gems_affixes.mana_cost.desc",
                fmt(pct * 100));
    }

    @Override
    public Component getAugmentingText(ItemStack stack, LootRarity rarity, float level) {
        MutableComponent comp = this.getDescription(stack, rarity, level);
        float min = this.getReductionPercent(rarity, 0);
        float max = this.getReductionPercent(rarity, 1);
        return comp.append(valueBounds(
                Component.literal(fmt(min * 100) + "%"),
                Component.literal(fmt(max * 100) + "%")));
    }

    public float getReductionPercent(LootRarity rarity, float level) {
        StepFunction func = this.values.get(rarity);
        return func != null ? func.get(level) : 0f;
    }
}