package net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus;

import com.google.common.base.Preconditions;
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

import java.util.Locale;
import java.util.Map;

public class EchoingStrikeBonus extends GemBonus {

    public static final Codec<EchoingStrikeBonus> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            gemClass(),
            VALUES_CODEC.fieldOf("damage").forGetter(b -> b.damage),
            VALUES_CODEC.fieldOf("delay_ticks").forGetter(b -> b.delayTicks)
    ).apply(inst, EchoingStrikeBonus::new));

    private final Map<LootRarity, StepFunction> damage;
    private final Map<LootRarity, StepFunction> delayTicks;

    public EchoingStrikeBonus(GemClass gemClass, Map<LootRarity, StepFunction> damage, Map<LootRarity, StepFunction> delayTicks) {
        super(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "echoing_strike"), gemClass);
        this.damage = damage;
        this.delayTicks = delayTicks;
    }

    public float getDamageMultiplier(LootRarity rarity) {
        StepFunction fn = damage.get(rarity);
        return fn != null ? fn.get(0) : 0.5f;
    }

    public int getDelayTicks(LootRarity rarity) {
        StepFunction fn = delayTicks.get(rarity);
        return fn != null ? (int) fn.get(0) : 20;
    }

    @Override
    public Component getSocketBonusTooltip(ItemStack gem, LootRarity rarity) {
        int percent = Math.round(getDamageMultiplier(rarity) * 100f);
        int ticks = getDelayTicks(rarity);
        float seconds = ticks / 20f;

        String formatted = String.format(Locale.ROOT, "%.2f", seconds)
                .replaceAll("0+$", "")
                .replaceAll("\\.$", "") + "s";

        Component percentComp = Component.literal(percent + "%").withStyle(ChatFormatting.YELLOW);
        Component timeComp = Component.literal(formatted).withStyle(ChatFormatting.YELLOW);

        return Component.translatable("bonus.fallen_gems_affixes.echoing_strike.desc", percentComp, timeComp)
                .withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public boolean supports(LootRarity rarity) {
        return damage.containsKey(rarity) && delayTicks.containsKey(rarity);
    }

    @Override
    public EchoingStrikeBonus validate() {
        Preconditions.checkNotNull(this.damage, "EchoingStrikeBonus missing 'damage' field");
        Preconditions.checkNotNull(this.delayTicks, "EchoingStrikeBonus missing 'delay_ticks' field");
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