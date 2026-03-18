package net.kayn.fallen_gems_affixes.adventure.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixType;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Set;

public class ProspectorAffix extends Affix {

    public static final Codec<ProspectorAffix> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                    LootRarity.mapCodec(Codec.INT).fieldOf("range").forGetter(a -> a.range),
                    LootCategory.SET_CODEC.fieldOf("types").forGetter(a -> a.types))
            .apply(inst, ProspectorAffix::new));

    protected final Map<LootRarity, Integer> range;
    protected final Set<LootCategory> types;

    public ProspectorAffix(Map<LootRarity, Integer> range, Set<LootCategory> types) {
        super(AffixType.ABILITY);
        this.range = range;
        this.types = types;
    }

    public int getRange(LootRarity rarity) {
        return range.getOrDefault(rarity, 8);
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        if (cat.isNone()) return false;
        return (types.isEmpty() || types.contains(cat)) && range.containsKey(rarity);
    }

    @Override
    public MutableComponent getDescription(ItemStack stack, LootRarity rarity, float level) {
        return Component.translatable(
                "affix.fallen_gems_affixes.prospector.desc",
                getRange(rarity)
        ).withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }
}