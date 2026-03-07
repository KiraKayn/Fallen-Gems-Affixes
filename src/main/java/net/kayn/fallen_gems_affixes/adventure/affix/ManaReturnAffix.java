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

public class ManaReturnAffix extends Affix {

    public static final Codec<ManaReturnAffix> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                    LootRarity.mapCodec(StepFunction.CODEC).fieldOf("chance").forGetter(a -> a.chance),
                    LootRarity.mapCodec(StepFunction.CODEC).fieldOf("return_percent").forGetter(a -> a.returnPercent),
                    LootCategory.SET_CODEC.fieldOf("types").forGetter(a -> a.types)
            ).apply(inst, ManaReturnAffix::new));

    protected final Map<LootRarity, StepFunction> chance;
    protected final Map<LootRarity, StepFunction> returnPercent;
    protected final Set<LootCategory> types;

    public ManaReturnAffix(
            Map<LootRarity, StepFunction> chance,
            Map<LootRarity, StepFunction> returnPercent,
            Set<LootCategory> types) {
        super(AffixType.POTION);
        this.chance        = chance;
        this.returnPercent = returnPercent;
        this.types         = types;
    }

    @Override
    public Codec<? extends Affix> getCodec() { return CODEC; }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        if (!this.chance.containsKey(rarity)) return false;
        if (!this.returnPercent.containsKey(rarity)) return false;
        return this.types.isEmpty() || this.types.contains(cat);
    }

    @Override
    public MutableComponent getDescription(ItemStack stack, LootRarity rarity, float level) {
        float pctChance = this.getChance(rarity, level);
        float pctReturn = this.getReturnPercent(rarity, level);
        return Component.translatable(
                "affix.fallen_gems_affixes.mana_return.desc",
                fmt(pctChance * 100),
                fmt(pctReturn * 100));
    }

    @Override
    public Component getAugmentingText(ItemStack stack, LootRarity rarity, float level) {
        MutableComponent comp = this.getDescription(stack, rarity, level);
        float minChance  = this.getChance(rarity, 0);
        float maxChance  = this.getChance(rarity, 1);
        float minReturn  = this.getReturnPercent(rarity, 0);
        float maxReturn  = this.getReturnPercent(rarity, 1);
        return comp.append(valueBounds(
                Component.literal(fmt(minChance * 100) + "% / " + fmt(minReturn * 100) + "%"),
                Component.literal(fmt(maxChance * 100) + "% / " + fmt(maxReturn * 100) + "%")));
    }

    public float getChance(LootRarity rarity, float level) {
        StepFunction f = this.chance.get(rarity);
        return f != null ? f.get(level) : 0f;
    }

    public float getReturnPercent(LootRarity rarity, float level) {
        StepFunction f = this.returnPercent.get(rarity);
        return f != null ? f.get(level) : 0f;
    }
}