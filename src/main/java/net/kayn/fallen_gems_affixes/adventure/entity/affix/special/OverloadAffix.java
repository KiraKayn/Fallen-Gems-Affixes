package net.kayn.fallen_gems_affixes.adventure.entity.affix.special;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kayn.fallen_gems_affixes.adventure.entity.affix.EntityAffix;
import net.kayn.fallen_gems_affixes.adventure.entity.affix.ScaledValue;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

public class OverloadAffix extends EntityAffix {

    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "overload");
    private static final String HIT_KEY = "fga.overload_hits";

    public static final Codec<OverloadAffix> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.optionalFieldOf("hits_required", 5).forGetter(a -> a.hitsRequired),
            ScaledValue.CODEC.fieldOf("aoe_damage").forGetter(a -> a.aoeDamage),
            Codec.FLOAT.optionalFieldOf("radius", 4.0f).forGetter(a -> a.radius)
    ).apply(inst, OverloadAffix::new));

    private final int hitsRequired;
    private final ScaledValue aoeDamage;
    private final float radius;

    public OverloadAffix(int hitsRequired, ScaledValue aoeDamage, float radius) {
        this.hitsRequired = hitsRequired;
        this.aoeDamage    = aoeDamage;
        this.radius       = radius;
    }

    @Override
    public void doPostAttack(LivingEntity entity, Entity target, float level) {
        int hits = entity.getPersistentData().getInt(HIT_KEY) + 1;
        entity.getPersistentData().putInt(HIT_KEY, hits);
        if (hits < hitsRequired) return;
        entity.getPersistentData().putInt(HIT_KEY, 0);
        if (target == null) return;

        DamageSource src = entity.damageSources().mobAttack(entity);
        float damage = aoeDamage.get(level);
        AABB box = target.getBoundingBox().inflate(radius);
        target.level().getEntitiesOfClass(LivingEntity.class, box, e -> e != entity)
                .forEach(e -> e.hurt(src, damage));
    }

    @Override public ResourceLocation getType()              { return TYPE; }
    @Override public Codec<? extends EntityAffix> getCodec() { return CODEC; }
}