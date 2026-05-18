package net.kayn.fallen_gems_affixes.adventure.entity.affix.special;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kayn.fallen_gems_affixes.adventure.entity.affix.EntityAffix;
import net.kayn.fallen_gems_affixes.adventure.entity.affix.ScaledValue;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class PredatorAffix extends EntityAffix {

    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "predator");

    public static final Codec<PredatorAffix> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ScaledValue.CODEC.fieldOf("bonus_damage").forGetter(a -> a.bonusDamage)
    ).apply(inst, PredatorAffix::new));

    private final ScaledValue bonusDamage;

    public PredatorAffix(ScaledValue bonusDamage) {
        this.bonusDamage = bonusDamage;
    }

    @Override
    public void doPostAttack(LivingEntity entity, Entity target, float level) {
        if (!(target instanceof LivingEntity living)) return;
        Vec3 targetLook = living.getLookAngle();
        Vec3 toAttacker = entity.position().subtract(living.position()).normalize();
        if (targetLook.dot(toAttacker) >= 0) return;
        DamageSource src = entity.damageSources().mobAttack(entity);
        living.hurt(src, bonusDamage.get(level));
    }

    @Override public ResourceLocation getType()              { return TYPE; }
    @Override public Codec<? extends EntityAffix> getCodec() { return CODEC; }
}