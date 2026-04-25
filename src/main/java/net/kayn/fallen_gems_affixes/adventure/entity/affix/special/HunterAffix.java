package net.kayn.fallen_gems_affixes.adventure.entity.affix.special;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kayn.fallen_gems_affixes.adventure.entity.affix.EntityAffix;
import net.kayn.fallen_gems_affixes.adventure.entity.affix.ScaledValue;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public class HunterAffix extends EntityAffix {

    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "hunter");

    private static final String MARK_UNTIL_KEY = "fga.hunter_mark_until";
    private static final String NEXT_MARK_KEY  = "fga.hunter_next_mark";

    public static final Codec<HunterAffix> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.optionalFieldOf("mark_interval_ticks", 100).forGetter(a -> a.markInterval),
            Codec.INT.optionalFieldOf("mark_duration_ticks", 100).forGetter(a -> a.markDuration),
            ScaledValue.CODEC.fieldOf("damage_bonus").forGetter(a -> a.damageBonus)
    ).apply(inst, HunterAffix::new));

    private final int markInterval;
    private final int markDuration;
    private final ScaledValue damageBonus;

    public HunterAffix(int markInterval, int markDuration, ScaledValue damageBonus) {
        this.markInterval = markInterval;
        this.markDuration = markDuration;
        this.damageBonus  = damageBonus;
    }

    @Override
    public void onTick(LivingEntity entity, float level) {
        if (!(entity instanceof Mob mob)) return;
        if (!(mob.getTarget() instanceof LivingEntity)) return;
        LivingEntity target = mob.getTarget();

        long now = entity.level().getGameTime();
        long nextMark = entity.getPersistentData().getLong(NEXT_MARK_KEY);
        if (now < nextMark) return;

        target.getPersistentData().putLong(MARK_UNTIL_KEY, now + markDuration);
        entity.getPersistentData().putLong(NEXT_MARK_KEY, now + markInterval);
    }

    @Override
    public int getTickInterval() { return 10; }

    @Override
    public void doPostAttack(LivingEntity entity, Entity target, float level) {
        if (!(target instanceof LivingEntity living)) return;
        long markUntil = living.getPersistentData().getLong(MARK_UNTIL_KEY);
        if (entity.level().getGameTime() > markUntil) return;
        DamageSource src = entity.damageSources().mobAttack(entity);
        living.hurt(src, damageBonus.get(level));
    }

    @Override public ResourceLocation getType()              { return TYPE; }
    @Override public Codec<? extends EntityAffix> getCodec() { return CODEC; }
}