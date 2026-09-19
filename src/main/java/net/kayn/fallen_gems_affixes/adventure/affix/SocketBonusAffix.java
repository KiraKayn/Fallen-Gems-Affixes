package net.kayn.fallen_gems_affixes.adventure.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixDefinition;
import dev.shadowsoffire.apotheosis.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

import java.util.Map;
import java.util.Set;

public class SocketBonusAffix extends Affix {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "socket_bonus");

    public static final Codec<SocketBonusAffix> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                    affixDef(),
                    LootRarity.mapCodec(SocketData.CODEC).fieldOf("values").forGetter(a -> a.values),
                    LootCategory.SET_CODEC.fieldOf("types").forGetter(a -> a.types)
            ).apply(inst, SocketBonusAffix::new));

    protected final Map<LootRarity, SocketData> values;
    protected final Set<LootCategory> types;

    public SocketBonusAffix(AffixDefinition def, Map<LootRarity, SocketData> values, Set<LootCategory> types) {
        super(def);
        this.values = values;
        this.types = types;
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }

    @Override
    public MutableComponent getDescription(AffixInstance inst, AttributeTooltipContext ctx) {
        SocketData data = this.values.get(inst.getRarity());
        if (data == null) return Component.empty();

        int bonus = data.level().getInt(inst.level());

        if (bonus >= 1) {
            return Component.translatable("affix.fallen_gems_affixes.socket_bonus.desc_multiple", bonus);
        }

        return Component.translatable("affix.fallen_gems_affixes.socket_bonus.desc", bonus);
    }

    @Override
    public Component getAugmentingText(AffixInstance inst, AttributeTooltipContext ctx) {
        SocketData data = this.values.get(inst.getRarity());
        if (data == null) return Component.empty();

        int currentBonus = data.level().getInt(inst.level());
        int minBonus = data.level().getInt(0);
        int maxBonus = data.level().getInt(1);

        MutableComponent comp = Component.translatable("affix.fallen_gems_affixes.socket_bonus.desc", currentBonus);

        if (minBonus != maxBonus) {
            Component minComp = Component.literal(String.valueOf(minBonus));
            Component maxComp = Component.literal(String.valueOf(maxBonus));
            comp.append(Affix.valueBounds(minComp, maxComp));
        }

        return comp;
    }

    public int getBonusSockets(LootRarity rarity, float level) {
        SocketData data = this.values.get(rarity);
        return data != null ? data.level().getInt(level) : 0;
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        if (cat == null || cat.isNone()) return false;
        return (this.types.isEmpty() || this.types.contains(cat)) && this.values.containsKey(rarity);
    }

    public record SocketData(StepFunction level) {
        private static final Codec<SocketData> CODEC = RecordCodecBuilder.create(inst -> inst
                .group(
                        StepFunction.CODEC.optionalFieldOf("sockets", StepFunction.constant(1)).forGetter(SocketData::level)
                ).apply(inst, SocketData::new));
    }
}