package net.kayn.fallen_gems_affixes.adventure.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixType;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MultiShotAffix extends Affix {

    public static final float SPREAD_DEGREES = 10f;
    public static final String KEY_BYPASS_IFRAMES = "fga:multishot_bypass_iframes";

    public static final Codec<MultiShotAffix> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                    LootRarity.mapCodec(StepFunction.CODEC).fieldOf("values")
                            .forGetter(a -> a.values),
                    LootCategory.SET_CODEC.fieldOf("types")
                            .forGetter(a -> a.types),
                    PlaceboCodecs.nullableField(Codec.list(Codec.STRING), "bypass_iframes_rarities", Collections.emptyList())
                            .forGetter(a -> List.copyOf(a.bypassIframesRarityIds))
            ).apply(inst, (values, types, bypassList) ->
                    new MultiShotAffix(values, types, new HashSet<>(bypassList))));

    protected final Map<LootRarity, StepFunction> values;
    protected final Set<LootCategory> types;
    protected final Set<String> bypassIframesRarityIds;

    public MultiShotAffix(Map<LootRarity, StepFunction> values, Set<LootCategory> types,
                          Set<String> bypassIframesRarityIds) {
        super(AffixType.ABILITY);
        this.values                = values;
        this.types                 = types;
        this.bypassIframesRarityIds = bypassIframesRarityIds;
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
        int shots = getExtraShots(rarity, level);
        if (isBypassIframes(rarity)) {
            return Component.translatable("affix.fallen_gems_affixes.multi_shot.desc.bypass", shots);
        }
        return Component.translatable("affix.fallen_gems_affixes.multi_shot.desc", shots);
    }

    @Override
    public Component getAugmentingText(ItemStack stack, LootRarity rarity, float level) {
        MutableComponent comp = this.getDescription(stack, rarity, level);
        int min = getExtraShots(rarity, 0);
        int max = getExtraShots(rarity, 1);
        return comp.append(valueBounds(
                Component.literal(String.valueOf(min)),
                Component.literal(String.valueOf(max))));
    }

    public int getExtraShots(LootRarity rarity, float level) {
        StepFunction f = this.values.get(rarity);
        return f != null ? (int) f.get(level) : 0;
    }
    public boolean isBypassIframes(LootRarity rarity) {
        if (bypassIframesRarityIds.isEmpty()) return false;
        var key = RarityRegistry.INSTANCE.getKey(rarity);
        return key != null && bypassIframesRarityIds.contains(key.getPath());
    }
}