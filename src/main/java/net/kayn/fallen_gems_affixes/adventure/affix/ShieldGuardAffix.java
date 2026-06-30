package net.kayn.fallen_gems_affixes.adventure.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixType;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Set;

public class ShieldGuardAffix extends Affix {

    public static final Codec<ShieldGuardAffix> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                    LootRarity.mapCodec(Codec.FLOAT).fieldOf("reduction").forGetter(a -> a.reduction),
                    LootCategory.SET_CODEC.fieldOf("types").forGetter(a -> a.types))
            .apply(inst, ShieldGuardAffix::new));

    protected final Map<LootRarity, Float> reduction;
    protected final Set<LootCategory> types;

    public ShieldGuardAffix(Map<LootRarity, Float> reduction, Set<LootCategory> types) {
        super(AffixType.ABILITY);
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
    public MutableComponent getDescription(ItemStack stack, LootRarity rarity, float level) {
        float red = reduction.getOrDefault(rarity, 0f);
        if (red >= 1f) {
            return Component.translatable("affix.fallen_gems_affixes.shield_guard.immune")
                    .withStyle(ChatFormatting.YELLOW);
        }
        return Component.translatable("affix.fallen_gems_affixes.shield_guard.desc", Affix.fmt(red * 100f))
                .withStyle(ChatFormatting.YELLOW);
    }

    public static float getReduction(ItemStack stack) {
        if (stack.isEmpty()) return 0f;
        Map<DynamicHolder<? extends Affix>, AffixInstance> affixes = AffixHelper.getAffixes(stack);
        for (Map.Entry<DynamicHolder<? extends Affix>, AffixInstance> entry : affixes.entrySet()) {
            if (entry.getKey().isBound() && entry.getKey().get() instanceof ShieldGuardAffix guard) {
                return guard.getReduction(entry.getValue().rarity().get());
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