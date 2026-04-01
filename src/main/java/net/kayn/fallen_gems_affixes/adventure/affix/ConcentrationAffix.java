package net.kayn.fallen_gems_affixes.adventure.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixType;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Set;

public class ConcentrationAffix extends Affix {

    public static final Codec<ConcentrationAffix> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                    LootRarity.mapCodec(Codec.BOOL).fieldOf("values").forGetter(a -> a.values),
                    LootCategory.SET_CODEC.fieldOf("types").forGetter(a -> a.types)
            ).apply(inst, ConcentrationAffix::new));

    protected final Map<LootRarity, Boolean> values;
    protected final Set<LootCategory> types;

    public ConcentrationAffix(Map<LootRarity, Boolean> values, Set<LootCategory> types) {
        super(AffixType.ABILITY);
        this.values = values;
        this.types = types;
    }

    public static boolean hasConcentration(Player player) {
        for (ItemStack stack : player.getAllSlots()) {
            if (stack.isEmpty()) continue;
            var affixes = AffixHelper.getAffixes(stack);
            for (var inst : affixes.values()) {
                if (!inst.isValid()) continue;
                if (inst.affix().get() instanceof ConcentrationAffix) return true;
            }
        }
        return false;
    }

    @Override
    public Codec<? extends Affix> getCodec() { return CODEC; }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        if (!Boolean.TRUE.equals(this.values.get(rarity))) return false;
        return this.types.isEmpty() || this.types.contains(cat);
    }

    @Override
    public MutableComponent getDescription(ItemStack stack, LootRarity rarity, float level) {
        return Component.translatable("affix.fallen_gems_affixes.concentration.desc");
    }

    @Override
    public Component getAugmentingText(ItemStack stack, LootRarity rarity, float level) {
        return getDescription(stack, rarity, level);
    }
}