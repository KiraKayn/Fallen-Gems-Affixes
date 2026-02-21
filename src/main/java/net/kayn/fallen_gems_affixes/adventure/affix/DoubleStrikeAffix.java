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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Set;


public class DoubleStrikeAffix extends Affix {

    public static final Codec<DoubleStrikeAffix> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                    LootRarity.mapCodec(StepFunction.CODEC).fieldOf("values").forGetter(a -> a.values),
                    LootCategory.SET_CODEC.fieldOf("types").forGetter(a -> a.types)
            )
            .apply(inst, DoubleStrikeAffix::new));

    private static final Logger LOGGER = LogManager.getLogger();

    protected final Map<LootRarity, StepFunction> values;
    protected final Set<LootCategory> types;

    public DoubleStrikeAffix(Map<LootRarity, StepFunction> values, Set<LootCategory> types) {
        super(AffixType.ABILITY);
        this.values = values;
        this.types = types;
    }

    public float getTrueLevel(LootRarity rarity, float level) {
        StepFunction f = this.values.get(rarity);
        if (f == null) return 0f;
        return (float) f.get(level);
    }

    public float calculateBonusDamage(
            LivingEntity attacker,
            LivingEntity target,
            DamageSource source,
            float originalDamage,
            LootRarity rarity,
            float level
    ) {
        float percent = getTrueLevel(rarity, level);

        LOGGER.info(
                "DoubleStrike → rarity={}, level={}, percent={}, originalDamage={}",
                rarity, level, percent, originalDamage
        );

        return originalDamage * percent;
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        return cat != null && !cat.isNone() && (this.types.isEmpty() || this.types.contains(cat)) && this.values.containsKey(rarity);
    }

    @Override
    public MutableComponent getDescription(ItemStack stack, LootRarity rarity, float affixLevel) {
        StepFunction f = this.values.get(rarity);
        if (f == null) return Component.empty();

        float percent = getTrueLevel(rarity, affixLevel) * 100f;
        String formatted = ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(percent);
        return Component.translatable("affix.fallen_gems_affixes.double_strike.desc", formatted);
    }

    @Override
    public Component getAugmentingText(ItemStack stack, LootRarity rarity, float affixLevel) {
        StepFunction f = this.values.get(rarity);
        if (f == null) return Component.empty();

        float current = getTrueLevel(rarity, affixLevel) * 100f;
        float min = (float) f.get(0f) * 100f;
        float max = (float) f.get(1f) * 100f;

        MutableComponent comp = Component.translatable("affix.fallen_gems_affixes.double_strike.desc",
                ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(current));

        if (min != max) {
            Component minComp = Component.literal(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(min) + "%");
            Component maxComp = Component.literal(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(max) + "%");
            comp.append(Affix.valueBounds(minComp, maxComp));
        }

        return comp;
    }
}