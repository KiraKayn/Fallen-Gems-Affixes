package net.kayn.fallen_gems_affixes.adventure.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixType;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.placebo.util.StepFunction;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Set;

public class ManaDamageAffix extends Affix {

    public static final Codec<ManaDamageAffix> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                    LootRarity.mapCodec(StepFunction.CODEC).fieldOf("values").forGetter(a -> a.values),
                    LootCategory.SET_CODEC.fieldOf("types").forGetter(a -> a.types)
            ).apply(inst, ManaDamageAffix::new));

    protected final Map<LootRarity, StepFunction> values;
    protected final Set<LootCategory> types;

    public ManaDamageAffix(Map<LootRarity, StepFunction> values, Set<LootCategory> types) {
        super(AffixType.POTION);
        this.values = values;
        this.types  = types;
    }

    @Override
    public Codec<? extends Affix> getCodec() { return CODEC; }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        if (!this.values.containsKey(rarity)) return false;
        return this.types.isEmpty() || this.types.contains(cat);
    }

    @Override
    public MutableComponent getDescription(ItemStack stack, LootRarity rarity, float level) {
        float scaling = this.getScalingFactor(rarity, level);
        return Component.translatable(
                "affix.fallen_gems_affixes.mana_damage.desc",
                fmt(scaling * 100));
    }

    @Override
    public Component getAugmentingText(ItemStack stack, LootRarity rarity, float level) {
        MutableComponent comp = this.getDescription(stack, rarity, level);
        float min = this.getScalingFactor(rarity, 0);
        float max = this.getScalingFactor(rarity, 1);
        return comp.append(valueBounds(
                Component.literal(fmt(min * 100) + "%"),
                Component.literal(fmt(max * 100) + "%")));
    }

    public float getScalingFactor(LootRarity rarity, float level) {
        StepFunction f = this.values.get(rarity);
        return f != null ? f.get(level) : 0f;
    }

    public float getDamageMultiplier(Player caster, LootRarity rarity, float level) {
        float maxMana = (float) caster.getAttributeValue(AttributeRegistry.MAX_MANA.get());
        if (maxMana <= 0f) return 1f;

        float currentMana = io.redspace.ironsspellbooks.api.magic.MagicData
                .getPlayerMagicData(caster).getMana();
        float manaRatio     = Math.min(currentMana / maxMana, 1f);
        float scalingFactor = this.getScalingFactor(rarity, level);

        return 1f + (scalingFactor * manaRatio);
    }
}