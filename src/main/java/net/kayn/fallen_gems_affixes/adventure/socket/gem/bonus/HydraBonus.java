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

public class HydraBonus extends GemBonus {

    public static final Codec<HydraBonus> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            gemClass(),
            VALUES_CODEC.fieldOf("reduction").forGetter(b -> b.reduction),
            VALUES_CODEC.fieldOf("fire_penalty").forGetter(b -> b.firePenalty)
    ).apply(inst, HydraBonus::new));

    // Damage reduction when NOT on fire
    public final Map<LootRarity, StepFunction> reduction;
    // Extra damage taken when on fire
    public final Map<LootRarity, StepFunction> firePenalty;

    public HydraBonus(GemClass gemClass, Map<LootRarity, StepFunction> reduction, Map<LootRarity, StepFunction> firePenalty) {
        super(new ResourceLocation(FallenGemsAffixes.MOD_ID, "hydra"), gemClass);
        this.reduction = reduction;
        this.firePenalty = firePenalty;
    }

    @Override
    public Component getSocketBonusTooltip(ItemStack gem, LootRarity rarity) {
        DecimalFormat df = new DecimalFormat("#.##");
        double reductionPct = reduction.get(rarity).get(0) * 100;
        double penaltyPct = firePenalty.get(rarity).get(0) * 100;
        return Component.translatable("bonus.fallen_gems_affixes.hydra.desc",
                df.format(reductionPct),
                df.format(penaltyPct)
        ).withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public boolean supports(LootRarity rarity) {
        return reduction.containsKey(rarity) && firePenalty.containsKey(rarity);
    }

    @Override
    public HydraBonus validate() {
        Preconditions.checkNotNull(this.reduction, "HydraBonus missing reduction values");
        Preconditions.checkNotNull(this.firePenalty, "HydraBonus missing fire_penalty values");
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