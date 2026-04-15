package net.kayn.fallen_gems_affixes.adventure.entity.affix.special;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kayn.fallen_gems_affixes.adventure.entity.affix.EntityAffix;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class FearAuraAffix extends EntityAffix {

    public static final ResourceLocation TYPE = new ResourceLocation("fallen_gems_affixes", "fear_aura");

    public static final Codec<FearAuraAffix> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.FLOAT.optionalFieldOf("radius", 8.0f).forGetter(a -> a.radius),
            Codec.INT.optionalFieldOf("slowness_amplifier", 1).forGetter(a -> a.slownessAmp),
            Codec.INT.optionalFieldOf("weakness_amplifier", 0).forGetter(a -> a.weaknessAmp),
            Codec.INT.optionalFieldOf("effect_duration", 80).forGetter(a -> a.effectDuration),
            Codec.INT.optionalFieldOf("interval", 40).forGetter(a -> a.interval)
    ).apply(inst, FearAuraAffix::new));

    private final float radius;
    private final int slownessAmp;
    private final int weaknessAmp;
    private final int effectDuration;
    private final int interval;

    public FearAuraAffix(float radius, int slownessAmp, int weaknessAmp, int effectDuration, int interval) {
        this.radius        = radius;
        this.slownessAmp   = slownessAmp;
        this.weaknessAmp   = weaknessAmp;
        this.effectDuration = effectDuration;
        this.interval      = interval;
    }

    @Override
    public void onTick(LivingEntity entity, float level) {
        entity.level().getEntitiesOfClass(Player.class,
                        entity.getBoundingBox().inflate(radius),
                        p -> !p.isCreative() && !p.isSpectator())
                .forEach(player -> {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, effectDuration, slownessAmp, false, true));
                    player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,          effectDuration, weaknessAmp, false, true));
                });
    }

    @Override public int getTickInterval()                   { return interval; }
    @Override public ResourceLocation getType()              { return TYPE; }
    @Override public Codec<? extends EntityAffix> getCodec() { return CODEC; }
}
