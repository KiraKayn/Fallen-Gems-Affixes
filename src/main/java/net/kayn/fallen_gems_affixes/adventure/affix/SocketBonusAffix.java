package net.kayn.fallen_gems_affixes.adventure.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixType;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Set;

public class SocketBonusAffix extends Affix {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "socket_bonus");
    public static final Codec<SocketBonusAffix> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                    LootRarity.mapCodec(SocketData.CODEC).fieldOf("values").forGetter(a -> a.values),
                    LootCategory.SET_CODEC.fieldOf("types").forGetter(a -> a.types)
            ).apply(inst, SocketBonusAffix::new));

    protected final Map<LootRarity, SocketData> values;
    protected final Set<LootCategory> types;

    public SocketBonusAffix(Map<LootRarity, SocketData> values, Set<LootCategory> types) {
        super(AffixType.ABILITY);
        this.values = values;
        this.types = types;
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }

    @Override
    public MutableComponent getDescription(ItemStack stack, LootRarity rarity, float level) {
        SocketData data = this.values.get(rarity);
        if (data == null) return Component.empty();

        int bonus = data.level().getInt(level);

        if (bonus >= 1) {
            return Component.translatable("affix.fallen_gems_affixes.socket_bonus.desc_multiple",
                    bonus);
        }

        return Component.translatable("affix.fallen_gems_affixes.socket_bonus.desc",
                bonus);
    }

    @Override
    public Component getAugmentingText(ItemStack stack, LootRarity rarity, float level) {
        SocketData data = this.values.get(rarity);
        if (data == null) return Component.empty();

        int currentBonus = data.level().getInt(level);
        int minBonus = data.level().getInt(0);
        int maxBonus = data.level().getInt(1);

        MutableComponent comp = Component.translatable("affix.fallen_gems_affixes.socket_bonus.desc",
                currentBonus);

        // Add min/max bounds if they differ
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
        if (cat == null || cat.isNone()) {
            return false;
        }
        return (this.types.isEmpty() || this.types.contains(cat)) && this.values.containsKey(rarity);
    }

    public record SocketData(StepFunction level) {
        private static final Codec<SocketData> CODEC = RecordCodecBuilder.create(inst -> inst
                .group(
                        StepFunction.CODEC.optionalFieldOf("sockets", StepFunction.constant(1)).forGetter(SocketData::level)
                ).apply(inst, SocketData::new));
    }
}