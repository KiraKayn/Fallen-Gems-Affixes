package net.kayn.fallen_gems_affixes.adventure.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixDefinition;
import dev.shadowsoffire.apotheosis.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

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
                    affixDef(),
                    LootRarity.mapCodec(StepFunction.CODEC).fieldOf("values").forGetter(a -> a.values),
                    LootCategory.SET_CODEC.fieldOf("types").forGetter(a -> a.types),
                    Codec.STRING.listOf()
                            .optionalFieldOf("bypass_iframes_rarities", Collections.emptyList())
                            .forGetter(a -> List.copyOf(a.bypassIframesRarityIds))
            ).apply(inst, (def, values, types, bypassList) ->
                    new MultiShotAffix(def, values, types, new HashSet<>(bypassList))));

    protected final Map<LootRarity, StepFunction> values;
    protected final Set<LootCategory> types;
    protected final Set<String> bypassIframesRarityIds;

    public MultiShotAffix(AffixDefinition def,
                          Map<LootRarity, StepFunction> values,
                          Set<LootCategory> types,
                          Set<String> bypassIframesRarityIds) {
        super(def);
        this.values                 = values;
        this.types                  = types;
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
    public MutableComponent getDescription(AffixInstance inst, AttributeTooltipContext ctx) {
        int shots = getExtraShots(inst.getRarity(), inst.level());
        if (isBypassIframes(inst.getRarity())) {
            return Component.translatable("affix.fallen_gems_affixes.multi_shot.desc.bypass", shots);
        }
        return Component.translatable("affix.fallen_gems_affixes.multi_shot.desc", shots);
    }

    @Override
    public Component getAugmentingText(AffixInstance inst, AttributeTooltipContext ctx) {
        MutableComponent comp = this.getDescription(inst, ctx);
        int min = getExtraShots(inst.getRarity(), 0);
        int max = getExtraShots(inst.getRarity(), 1);
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