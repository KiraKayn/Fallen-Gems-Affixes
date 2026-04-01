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

public class ArrowTeleSlashBonus extends GemBonus {

    public static final Codec<ArrowTeleSlashBonus> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            gemClass(),
            VALUES_CODEC.fieldOf("cooldown").forGetter(b -> b.cooldown),
            VALUES_CODEC.fieldOf("radius").forGetter(b -> b.radius)
    ).apply(inst, ArrowTeleSlashBonus::new));

    public final Map<LootRarity, StepFunction> cooldown;
    public final Map<LootRarity, StepFunction> radius;

    public ArrowTeleSlashBonus(GemClass gemClass,
                               Map<LootRarity, StepFunction> cooldown,
                               Map<LootRarity, StepFunction> radius) {
        super(new ResourceLocation(FallenGemsAffixes.MOD_ID, "arrow_tele_slash"), gemClass);
        this.cooldown = cooldown;
        this.radius = radius;
    }

    public float getCooldown(LootRarity rarity) {
        StepFunction fn = cooldown.get(rarity);
        return fn != null ? fn.get(0) : 8f;
    }

    public float getRadius(LootRarity rarity) {
        StepFunction fn = radius.get(rarity);
        return fn != null ? fn.get(0) : 3f;
    }

    @Override
    public Component getSocketBonusTooltip(ItemStack gem, LootRarity rarity) {
        return Component.translatable("bonus.fallen_gems_affixes.arrow_teleport.desc",
                Component.literal(String.format(Locale.ROOT, "%.1f", getRadius(rarity))).withStyle(ChatFormatting.YELLOW),
                Component.literal(formatSeconds(getCooldown(rarity)) + "s").withStyle(ChatFormatting.YELLOW)
        ).withStyle(ChatFormatting.YELLOW);
    }

    private static String formatSeconds(float v) {
        return v == (int) v ? String.valueOf((int) v) : String.format(Locale.ROOT, "%.1f", v);
    }

    @Override
    public boolean supports(LootRarity rarity) {
        return cooldown.containsKey(rarity) && radius.containsKey(rarity);
    }

    @Override
    public ArrowTeleSlashBonus validate() {
        Preconditions.checkNotNull(this.cooldown, "ArrowTeleSlashBonus missing cooldown");
        Preconditions.checkNotNull(this.radius,   "ArrowTeleSlashBonus missing radius");
        return this;
    }

    @Override
    public int getNumberOfUUIDs() { return 0; }

    @Override
    public Codec<? extends GemBonus> getCodec() { return CODEC; }
}