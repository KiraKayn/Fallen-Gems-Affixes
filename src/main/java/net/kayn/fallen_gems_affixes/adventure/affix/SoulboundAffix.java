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

public class SoulboundAffix extends Affix {

    public static final Codec<SoulboundAffix> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                    LootRarity.mapCodec(SoulboundData.CODEC).fieldOf("values").forGetter(a -> a.values),
                    LootCategory.SET_CODEC.optionalFieldOf("types", Set.of()).forGetter(a -> a.types))
            .apply(inst, SoulboundAffix::new));

    protected final Map<LootRarity, SoulboundData> values;
    protected final Set<LootCategory> types;

    public SoulboundAffix(Map<LootRarity, SoulboundData> values, Set<LootCategory> types) {
        super(AffixType.ABILITY);
        this.values = values;
        this.types = types;
    }

    @Override
    public MutableComponent getDescription(ItemStack stack, LootRarity rarity, float level) {
        return Component.translatable("affix.fallen_gems_affixes.soulbound.desc")
                .withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public Component getAugmentingText(ItemStack stack, LootRarity rarity, float level) {
        return Component.translatable("affix.fallen_gems_affixes.soulbound.desc")
                .withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public boolean canApplyTo(ItemStack itemStack, LootCategory lootCategory, LootRarity lootRarity) {
        return (this.types.isEmpty() || this.types.contains(lootCategory)) && this.values.containsKey(lootRarity);
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }

    public record SoulboundData() {
        public static final SoulboundData INSTANCE = new SoulboundData();
        private static final Codec<SoulboundData> CODEC = Codec.unit(INSTANCE);
    }
}