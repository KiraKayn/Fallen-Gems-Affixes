package net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.placebo.util.StepFunction;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;
import java.util.Map;

public class VoidHunterBonus extends GemBonus {

    private static final ResourceLocation ECHOING_STRIKES_ID = ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "echoing_strikes");

    public static final Codec<VoidHunterBonus> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            gemClass(),
            VALUES_CODEC.fieldOf("cooldown").forGetter(b -> b.cooldown),
            VALUES_CODEC.fieldOf("duration").forGetter(b -> b.duration),
            VALUES_CODEC.fieldOf("amplifier").forGetter(b -> b.amplifier)
    ).apply(inst, VoidHunterBonus::new));

    public final Map<LootRarity, StepFunction> cooldown;
    public final Map<LootRarity, StepFunction> duration;
    public final Map<LootRarity, StepFunction> amplifier;

    public VoidHunterBonus(GemClass gemClass, Map<LootRarity, StepFunction> cooldown,
                           Map<LootRarity, StepFunction> duration,
                           Map<LootRarity, StepFunction> amplifier) {
        super(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "void_hunter"), gemClass);
        this.cooldown = cooldown;
        this.duration = duration;
        this.amplifier = amplifier;
    }

    public int getAmplifier(LootRarity rarity) {
        StepFunction fn = amplifier.get(rarity);
        return fn != null ? (int) fn.get(0) : 0;
    }


    @Override
    public Component getSocketBonusTooltip(ItemStack gem, LootRarity rarity) {
        float cd = cooldown.get(rarity).get(0);
        float dur = duration.get(rarity).get(0);
        int amp = getAmplifier(rarity);

        Component effectName = getEchoingStrikesName();
        Component effectWithLevel = effectName == null
                ? Component.literal("Echoing Strikes")
                : ((net.minecraft.network.chat.MutableComponent) effectName)
                .append(Component.literal(" "))
                .append(Component.translatable("enchantment.level." + (amp + 1)));

        Component durationText = Component.literal(formatSeconds(dur) + "s").withStyle(ChatFormatting.YELLOW);
        Component cooldownText = Component.literal(formatSeconds(cd) + "s").withStyle(ChatFormatting.YELLOW);

        return Component.translatable("bonus.fallen_gems_affixes.void_hunter.desc",
                effectWithLevel,
                durationText,
                cooldownText
        ).withStyle(ChatFormatting.YELLOW);
    }

    private static String formatSeconds(float value) {
        return value == (int) value ? String.valueOf((int) value) : String.format(Locale.ROOT, "%.1f", value);
    }

    private static Component getEchoingStrikesName() {
        AbstractSpell spell = SpellRegistry.REGISTRY.get().getValue(ECHOING_STRIKES_ID);
        if (spell != null) {
            return Component.literal("Echoing Strikes")
                    .withStyle(spell.getSchoolType().getDisplayName().getStyle());
        }
        return null;
    }

    @Override
    public boolean supports(LootRarity rarity) {
        return cooldown.containsKey(rarity) && duration.containsKey(rarity) && amplifier.containsKey(rarity);
    }

    @Override
    public VoidHunterBonus validate() {
        Preconditions.checkNotNull(this.cooldown, "VoidHunterBonus missing cooldown values");
        Preconditions.checkNotNull(this.duration, "VoidHunterBonus missing duration values");
        Preconditions.checkNotNull(this.amplifier, "VoidHunterBonus missing amplifier values");
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

    public float getCooldown(LootRarity rarity) {
        return cooldown.get(rarity).get(0);
    }

    public float getDuration(LootRarity rarity) {
        return duration.get(rarity).get(0);
    }
}