package net.kayn.fallen_gems_affixes.adventure.entity.affix.special;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kayn.fallen_gems_affixes.adventure.entity.affix.EntityAffix;
import net.kayn.fallen_gems_affixes.adventure.entity.affix.ScaledValue;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public class ColossusAffix extends EntityAffix {

    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "colossus");

    public static final Codec<ColossusAffix> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ScaledValue.CODEC.fieldOf("max_damage_per_hit").forGetter(a -> a.maxDamagePerHit)
    ).apply(inst, ColossusAffix::new));

    private final ScaledValue maxDamagePerHit;

    public ColossusAffix(ScaledValue maxDamagePerHit) {
        this.maxDamagePerHit = maxDamagePerHit;
    }

    @Override
    public float onHurt(LivingEntity entity, DamageSource src, float amount, float level) {
        return Math.min(amount, maxDamagePerHit.get(level));
    }

    @Override public ResourceLocation getType()              { return TYPE; }
    @Override public Codec<? extends EntityAffix> getCodec() { return CODEC; }
}