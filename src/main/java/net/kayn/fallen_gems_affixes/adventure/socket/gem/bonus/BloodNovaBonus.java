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

public class BloodNovaBonus extends GemBonus {

    public static final Codec<BloodNovaBonus> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            gemClass(),
            VALUES_CODEC.fieldOf("cooldown").forGetter(b -> b.cooldown),
            VALUES_CODEC.fieldOf("radius").forGetter(b -> b.radius),
            VALUES_CODEC.fieldOf("damage_percent").forGetter(b -> b.damagePercent),
            VALUES_CODEC.fieldOf("heal_percent").forGetter(b -> b.healPercent),
            VALUES_CODEC.fieldOf("bleed_duration").forGetter(b -> b.bleedDuration)
    ).apply(inst, BloodNovaBonus::new));

    public final Map<LootRarity, StepFunction> cooldown;
    public final Map<LootRarity, StepFunction> radius;
    public final Map<LootRarity, StepFunction> damagePercent;
    public final Map<LootRarity, StepFunction> healPercent;
    public final Map<LootRarity, StepFunction> bleedDuration;

    public BloodNovaBonus(GemClass gemClass,
                          Map<LootRarity, StepFunction> cooldown,
                          Map<LootRarity, StepFunction> radius,
                          Map<LootRarity, StepFunction> damagePercent,
                          Map<LootRarity, StepFunction> healPercent,
                          Map<LootRarity, StepFunction> bleedDuration) {
        super(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "blood_nova"), gemClass);
        this.cooldown = cooldown;
        this.radius = radius;
        this.damagePercent = damagePercent;
        this.healPercent = healPercent;
        this.bleedDuration = bleedDuration;
    }

    public float getCooldown(LootRarity rarity) {
        StepFunction fn = cooldown.get(rarity);
        return fn != null ? fn.get(0) : 12f;
    }

    public float getRadius(LootRarity rarity) {
        StepFunction fn = radius.get(rarity);
        return fn != null ? fn.get(0) : 4f;
    }

    public float getDamagePercent(LootRarity rarity) {
        StepFunction fn = damagePercent.get(rarity);
        return fn != null ? fn.get(0) : 0.5f;
    }

    public float getHealPercent(LootRarity rarity) {
        StepFunction fn = healPercent.get(rarity);
        return fn != null ? fn.get(0) : 0.10f;
    }

    public int getBleedDurationTicks(LootRarity rarity) {
        StepFunction fn = bleedDuration.get(rarity);
        return fn != null ? (int) (fn.get(0) * 20) : 60;
    }

    @Override
    public Component getSocketBonusTooltip(ItemStack gem, LootRarity rarity) {
        float damagePct = getDamagePercent(rarity) * 100;
        float healPct   = getHealPercent(rarity) * 100;
        float radius    = getRadius(rarity);
        float bleedSec  = getBleedDurationTicks(rarity) / 20f;
        float cooldown  = getCooldown(rarity);

        return Component.translatable("bonus.fallen_gems_affixes.blood_nova.desc",
                Component.literal(String.format(Locale.ROOT, "%.0f%%", damagePct)).withStyle(ChatFormatting.YELLOW),
                Component.literal(String.format(Locale.ROOT, "%.1f", radius)).withStyle(ChatFormatting.YELLOW),
                Component.literal(String.format(Locale.ROOT, "%.1f", bleedSec)).withStyle(ChatFormatting.YELLOW),
                Component.literal(String.format(Locale.ROOT, "%.0f%%", healPct)).withStyle(ChatFormatting.YELLOW),
                Component.literal(formatSeconds(cooldown) + "s").withStyle(ChatFormatting.YELLOW)
        ).withStyle(ChatFormatting.YELLOW);
    }

    private static String formatSeconds(float v) {
        return v == (int) v ? String.valueOf((int) v) : String.format(Locale.ROOT, "%.1f", v);
    }

    @Override
    public boolean supports(LootRarity rarity) {
        return cooldown.containsKey(rarity) && radius.containsKey(rarity)
                && damagePercent.containsKey(rarity) && healPercent.containsKey(rarity)
                && bleedDuration.containsKey(rarity);
    }

    @Override
    public BloodNovaBonus validate() {
        Preconditions.checkNotNull(this.cooldown, "BloodNovaBonus missing cooldown");
        Preconditions.checkNotNull(this.radius, "BloodNovaBonus missing radius");
        Preconditions.checkNotNull(this.damagePercent, "BloodNovaBonus missing damage_percent");
        Preconditions.checkNotNull(this.healPercent, "BloodNovaBonus missing heal_percent");
        Preconditions.checkNotNull(this.bleedDuration, "BloodNovaBonus missing bleed_duration");
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