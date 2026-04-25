package net.kayn.fallen_gems_affixes.adventure.entity.affix.special;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kayn.fallen_gems_affixes.adventure.entity.affix.EntityAffix;
import net.kayn.fallen_gems_affixes.adventure.entity.affix.ScaledValue;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class ExecutionerAffix extends EntityAffix {

    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "executioner");

    public static final Codec<ExecutionerAffix> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.FLOAT.optionalFieldOf("threshold", 0.30f).forGetter(a -> a.threshold),
            ScaledValue.CODEC.fieldOf("bonus_damage").forGetter(a -> a.bonusDamage)
    ).apply(inst, ExecutionerAffix::new));

    private final float threshold;
    private final ScaledValue bonusDamage;

    public ExecutionerAffix(float threshold, ScaledValue bonusDamage) {
        this.threshold   = threshold;
        this.bonusDamage = bonusDamage;
    }

    @Override
    public void doPostAttack(LivingEntity entity, Entity target, float level) {
        if (!(target instanceof LivingEntity living)) return;
        if ((living.getHealth() / living.getMaxHealth()) > threshold) return;
        DamageSource src = entity.damageSources().mobAttack(entity);
        living.hurt(src, bonusDamage.get(level));
    }

    @Override public ResourceLocation getType()              { return TYPE; }
    @Override public Codec<? extends EntityAffix> getCodec() { return CODEC; }
}