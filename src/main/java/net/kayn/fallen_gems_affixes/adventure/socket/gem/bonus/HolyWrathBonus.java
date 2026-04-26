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

public class HolyWrathBonus extends GemBonus {

    public static final Codec<HolyWrathBonus> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            gemClass(),
            VALUES_CODEC.fieldOf("cooldown").forGetter(b -> b.cooldown),
            VALUES_CODEC.fieldOf("radius").forGetter(b -> b.radius),
            VALUES_CODEC.fieldOf("spell_level").forGetter(b -> b.spellLevel),
            VALUES_CODEC.fieldOf("glow_duration").forGetter(b -> b.glowDuration)
    ).apply(inst, HolyWrathBonus::new));

    public final Map<LootRarity, StepFunction> cooldown;
    public final Map<LootRarity, StepFunction> radius;
    public final Map<LootRarity, StepFunction> spellLevel;
    public final Map<LootRarity, StepFunction> glowDuration;

    public HolyWrathBonus(GemClass gemClass,
                          Map<LootRarity, StepFunction> cooldown,
                          Map<LootRarity, StepFunction> radius,
                          Map<LootRarity, StepFunction> spellLevel,
                          Map<LootRarity, StepFunction> glowDuration) {
        super(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "holy_wrath"), gemClass);
        this.cooldown = cooldown;
        this.radius = radius;
        this.spellLevel = spellLevel;
        this.glowDuration = glowDuration;
    }

    public float getCooldown(LootRarity rarity) {
        StepFunction fn = cooldown.get(rarity);
        return fn != null ? fn.get(0) : 20f;
    }

    public float getRadius(LootRarity rarity) {
        StepFunction fn = radius.get(rarity);
        return fn != null ? fn.get(0) : 10f;
    }

    public int getSpellLevel(LootRarity rarity) {
        StepFunction fn = spellLevel.get(rarity);
        return fn != null ? (int) fn.get(0) : 1;
    }

    public int getGlowDurationTicks(LootRarity rarity) {
        StepFunction fn = glowDuration.get(rarity);
        return fn != null ? (int) (fn.get(0) * 20) : 100;
    }

    @Override
    public Component getSocketBonusTooltip(ItemStack gem, LootRarity rarity) {
        return Component.translatable("bonus.fallen_gems_affixes.holy_wrath.desc",
                Component.literal(String.format(Locale.ROOT, "%.1f", getRadius(rarity))).withStyle(ChatFormatting.YELLOW),
                Component.literal(formatSeconds(getCooldown(rarity)) + "s").withStyle(ChatFormatting.YELLOW)
        ).withStyle(ChatFormatting.YELLOW);
    }

    private static String formatSeconds(float v) {
        return v == (int) v ? String.valueOf((int) v) : String.format(Locale.ROOT, "%.1f", v);
    }

    @Override
    public boolean supports(LootRarity rarity) {
        return cooldown.containsKey(rarity) && radius.containsKey(rarity)
                && spellLevel.containsKey(rarity) && glowDuration.containsKey(rarity);
    }

    @Override
    public HolyWrathBonus validate() {
        Preconditions.checkNotNull(this.cooldown, "HolyWrathBonus missing cooldown");
        Preconditions.checkNotNull(this.radius, "HolyWrathBonus missing radius");
        Preconditions.checkNotNull(this.spellLevel, "HolyWrathBonus missing spell_level");
        Preconditions.checkNotNull(this.glowDuration, "HolyWrathBonus missing glow_duration");
        return this;
    }

    @Override
    public int getNumberOfUUIDs() { return 0; }

    @Override
    public Codec<? extends GemBonus> getCodec() { return CODEC; }
}