package net.kayn.fallen_gems_affixes.adventure.entity.affix.special;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kayn.fallen_gems_affixes.adventure.entity.affix.EntityAffix;
import net.kayn.fallen_gems_affixes.adventure.entity.affix.ScaledValue;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public class ArcaneShieldAffix extends EntityAffix {

    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "arcane_shield");
    private static final String SHIELD_KEY = "fga.arcane_shield_hp";
    private static final String CD_KEY     = "fga.arcane_shield_next";

    public static final Codec<ArcaneShieldAffix> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ScaledValue.CODEC.fieldOf("absorb_percent").forGetter(a -> a.absorbPercent),
            ScaledValue.CODEC.fieldOf("shield_max_hp").forGetter(a -> a.shieldMaxHp),
            Codec.INT.optionalFieldOf("recharge_ticks", 300).forGetter(a -> a.rechargeTicks)
    ).apply(inst, ArcaneShieldAffix::new));

    private final ScaledValue absorbPercent;
    private final ScaledValue shieldMaxHp;
    private final int rechargeTicks;

    public ArcaneShieldAffix(ScaledValue absorbPercent, ScaledValue shieldMaxHp, int rechargeTicks) {
        this.absorbPercent = absorbPercent;
        this.shieldMaxHp   = shieldMaxHp;
        this.rechargeTicks = rechargeTicks;
    }

    @Override
    public void onTick(LivingEntity entity, float level) {
        long now = entity.level().getGameTime();
        if (entity.getPersistentData().contains(SHIELD_KEY)) return;
        if (entity.getPersistentData().getLong(CD_KEY) > now) return;

        // Recharge: restore the shield to max
        entity.getPersistentData().putFloat(SHIELD_KEY, shieldMaxHp.get(level) * entity.getMaxHealth());
    }

    @Override
    public float onHurt(LivingEntity entity, DamageSource src, float amount, float level) {
        float shield = entity.getPersistentData().getFloat(SHIELD_KEY);
        if (shield <= 0f) return amount;

        float absorbed = amount * absorbPercent.get(level);
        float remaining = shield - absorbed;

        if (remaining <= 0f) {
            entity.getPersistentData().remove(SHIELD_KEY);
            entity.getPersistentData().putLong(CD_KEY, entity.level().getGameTime() + rechargeTicks);
        } else {
            entity.getPersistentData().putFloat(SHIELD_KEY, remaining);
        }

        return amount - absorbed;
    }

    @Override public int getTickInterval()                   { return 20; }
    @Override public ResourceLocation getType()              { return TYPE; }
    @Override public Codec<? extends EntityAffix> getCodec() { return CODEC; }
}
