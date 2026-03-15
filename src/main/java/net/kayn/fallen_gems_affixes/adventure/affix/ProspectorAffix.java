package net.kayn.fallen_gems_affixes.adventure.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixType;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

public class ProspectorAffix extends Affix {

    public static final Codec<ProspectorAffix> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                    PlaceboCodecs.nullableField(Codec.INT, "range", 24).forGetter(a -> a.range),
                    LootCategory.SET_CODEC.fieldOf("types").forGetter(a -> a.types))
            .apply(inst, ProspectorAffix::new));

    protected final int range;
    protected final Set<LootCategory> types;

    public ProspectorAffix(int range, Set<LootCategory> types) {
        super(AffixType.ABILITY);
        this.range = range;
        this.types = types;
    }

    public int getRange() {
        return range;
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        if (cat.isNone()) return false;
        return types.isEmpty() || types.contains(cat);
    }

    @Override
    public MutableComponent getDescription(ItemStack stack, LootRarity rarity, float level) {
        return Component.translatable(
                "affix.fallen_gems_affixes.prospector.desc",
                range
        ).withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }
}