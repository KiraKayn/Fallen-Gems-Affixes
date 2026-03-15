package net.kayn.fallen_gems_affixes.adventure.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixType;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AfflictedAffix extends Affix {

    public static final UUID SPEED_MODIFIER_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    public static final String SPEED_MODIFIER_NAME = "fga:afflicted_speed";

    public static final Codec<AfflictedAffix> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                    GemBonus.VALUES_CODEC.fieldOf("damage_per_effect").forGetter(a -> a.damagePerEffect),
                    GemBonus.VALUES_CODEC.fieldOf("speed_per_effect").forGetter(a -> a.speedPerEffect),
                    LootCategory.SET_CODEC.fieldOf("types").forGetter(a -> a.types))
            .apply(inst, AfflictedAffix::new));

    protected final Map<LootRarity, StepFunction> damagePerEffect;
    protected final Map<LootRarity, StepFunction> speedPerEffect;
    protected final Set<LootCategory> types;

    public AfflictedAffix(Map<LootRarity, StepFunction> damagePerEffect,
                          Map<LootRarity, StepFunction> speedPerEffect,
                          Set<LootCategory> types) {
        super(AffixType.ABILITY);
        this.damagePerEffect = damagePerEffect;
        this.speedPerEffect  = speedPerEffect;
        this.types           = types;
    }

    public float getDamageBonus(LivingEntity entity, LootRarity rarity, float level) {
        int count = countNegativeEffects(entity);
        if (count == 0) return 0f;
        return damagePerEffect.get(rarity).get(level) * count;
    }

    public float getSpeedBonus(LivingEntity entity, LootRarity rarity, float level) {
        int count = countNegativeEffects(entity);
        if (count == 0) return 0f;
        return speedPerEffect.get(rarity).get(level) * count;
    }

    public static int countNegativeEffects(LivingEntity entity) {
        return (int) entity.getActiveEffects().stream()
                .filter(e -> e.getEffect().getCategory() == MobEffectCategory.HARMFUL)
                .count();
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        if (cat.isNone()) return false;
        return (types.isEmpty() || types.contains(cat)) && damagePerEffect.containsKey(rarity);
    }

    @Override
    public MutableComponent getDescription(ItemStack stack, LootRarity rarity, float level) {
        float dmg   = damagePerEffect.get(rarity).get(level);
        float speed = speedPerEffect.get(rarity).get(level);
        return Component.translatable(
                "affix.fallen_gems_affixes.afflicted.desc",
                Affix.fmt(dmg * 100f),
                Affix.fmt(speed * 100f)
        ).withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }
}