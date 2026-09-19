package net.kayn.fallen_gems_affixes.adventure.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixDefinition;
import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

import java.util.Map;
import java.util.Set;

public class ShieldGuardAffix extends Affix {

    public static final Codec<ShieldGuardAffix> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                    affixDef(),
                    LootRarity.mapCodec(Codec.FLOAT).fieldOf("reduction").forGetter(a -> a.reduction),
                    LootCategory.SET_CODEC.fieldOf("types").forGetter(a -> a.types))
            .apply(inst, ShieldGuardAffix::new));

    protected final Map<LootRarity, Float> reduction;
    protected final Set<LootCategory> types;

    public ShieldGuardAffix(AffixDefinition def, Map<LootRarity, Float> reduction, Set<LootCategory> types) {
        super(def);
        this.reduction = reduction;
        this.types     = types;
    }

    public float getReduction(LootRarity rarity) {
        return reduction.getOrDefault(rarity, 0f);
    }

    public boolean isImmune(LootRarity rarity) {
        return getReduction(rarity) >= 1f;
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        if (cat.isNone()) return false;
        return (types.isEmpty() || types.contains(cat)) && reduction.containsKey(rarity);
    }

    @Override
    public MutableComponent getDescription(AffixInstance inst, AttributeTooltipContext ctx) {
        float red = reduction.getOrDefault(inst.getRarity(), 0f);
        if (red >= 1f) {
            return Component.translatable("affix.fallen_gems_affixes.shield_guard.immune")
                    .withStyle(ChatFormatting.YELLOW);
        }
        return Component.translatable("affix.fallen_gems_affixes.shield_guard.desc", Affix.fmt(red * 100f))
                .withStyle(ChatFormatting.YELLOW);
    }

    public static float getReduction(ItemStack stack) {
        if (stack.isEmpty()) return 0f;
        Map<DynamicHolder<Affix>, AffixInstance> affixes = AffixHelper.getAffixes(stack);
        for (Map.Entry<DynamicHolder<Affix>, AffixInstance> entry : affixes.entrySet()) {
            if (entry.getKey().isBound() && entry.getKey().get() instanceof ShieldGuardAffix guard) {
                return guard.getReduction(entry.getValue().getRarity());
            }
        }
        return 0f;
    }

    public static boolean isImmune(ItemStack stack) {
        return getReduction(stack) >= 1f;
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }
}