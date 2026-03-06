package net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus;

import com.google.common.base.Preconditions;
import com.ibm.icu.text.DecimalFormat;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public class WetResistanceBonus extends GemBonus {

    public static final Codec<WetResistanceBonus> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            gemClass(),
            VALUES_CODEC.fieldOf("values").forGetter(b -> b.values)
    ).apply(inst, WetResistanceBonus::new));

    public final Map<LootRarity, StepFunction> values;

    public WetResistanceBonus(GemClass gemClass, Map<LootRarity, StepFunction> values) {
        super(new ResourceLocation(FallenGemsAffixes.MOD_ID, "wet_resistance"), gemClass);
        this.values = values;
    }

    @Override
    public Component getSocketBonusTooltip(ItemStack gem, LootRarity rarity) {
        DecimalFormat df = new DecimalFormat("#.##");
        double pct = values.get(rarity).get(0) * 100;
        return Component.translatable("bonus.fallen_gems_affixes.wet_resistance.desc", df.format(pct))
                .withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public boolean supports(LootRarity rarity) {
        return values.containsKey(rarity);
    }

    @Override
    public WetResistanceBonus validate() {
        Preconditions.checkNotNull(this.values, "WetResistanceBonus missing values");
        return this;
    }

    @Override
    public int getNumberOfUUIDs() {
        return 0;
    }

    @Override
    public Codec<? extends GemBonus> getCodec() {
        return CODEC;
    }
}