package net.kayn.fallen_gems_affixes.adventure.entity.affix.special;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kayn.fallen_gems_affixes.adventure.entity.affix.EntityAffix;
import net.kayn.fallen_gems_affixes.adventure.entity.affix.ScaledValue;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class CursedAffix extends EntityAffix {

    public static final ResourceLocation TYPE = new ResourceLocation("fallen_gems_affixes", "cursed");

    private static final List<MobEffect> POOL = List.of(
            MobEffects.MOVEMENT_SLOWDOWN,
            MobEffects.WEAKNESS,
            MobEffects.BLINDNESS,
            MobEffects.HUNGER,
            MobEffects.POISON,
            MobEffects.CONFUSION,
            MobEffects.WITHER
    );

    public static final Codec<CursedAffix> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ScaledValue.CODEC.fieldOf("chance").forGetter(a -> a.chance),
            Codec.INT.optionalFieldOf("duration_ticks", 80).forGetter(a -> a.duration),
            Codec.INT.optionalFieldOf("amplifier", 0).forGetter(a -> a.amplifier)
    ).apply(inst, CursedAffix::new));

    private final ScaledValue chance;
    private final int duration;
    private final int amplifier;

    public CursedAffix(ScaledValue chance, int duration, int amplifier) {
        this.chance    = chance;
        this.duration  = duration;
        this.amplifier = amplifier;
    }

    @Override
    public void doPostAttack(LivingEntity entity, Entity target, float level) {
        if (!(target instanceof LivingEntity living)) return;
        if (entity.getRandom().nextFloat() >= chance.get(level)) return;

        MobEffect effect = POOL.get(entity.getRandom().nextInt(POOL.size()));
        living.addEffect(new MobEffectInstance(effect, duration, amplifier));
    }

    @Override public ResourceLocation getType()              { return TYPE; }
    @Override public Codec<? extends EntityAffix> getCodec() { return CODEC; }
}