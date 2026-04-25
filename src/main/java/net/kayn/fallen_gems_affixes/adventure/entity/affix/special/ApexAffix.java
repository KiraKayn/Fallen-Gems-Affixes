package net.kayn.fallen_gems_affixes.adventure.entity.affix.special;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kayn.fallen_gems_affixes.adventure.entity.affix.EntityAffix;
import net.kayn.fallen_gems_affixes.adventure.entity.affix.ScaledValue;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;

public class ApexAffix extends EntityAffix {

    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "apex");

    public static final Codec<ApexAffix> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ScaledValue.CODEC.fieldOf("bonus_damage").forGetter(a -> a.bonusDamage),
            Codec.FLOAT.optionalFieldOf("check_radius", 16.0f).forGetter(a -> a.checkRadius)
    ).apply(inst, ApexAffix::new));

    private final ScaledValue bonusDamage;
    private final float checkRadius;

    public ApexAffix(ScaledValue bonusDamage, float checkRadius) {
        this.bonusDamage = bonusDamage;
        this.checkRadius = checkRadius;
    }

    @Override
    public void doPostAttack(LivingEntity entity, Entity target, float level) {
        if (!(target instanceof LivingEntity living)) return;
        AABB box = entity.getBoundingBox().inflate(checkRadius);
        boolean alone = entity.level().getEntitiesOfClass(Monster.class, box, e -> e != entity).isEmpty();
        if (!alone) return;
        DamageSource src = entity.damageSources().mobAttack(entity);
        living.hurt(src, bonusDamage.get(level));
    }

    @Override public ResourceLocation getType()              { return TYPE; }
    @Override public Codec<? extends EntityAffix> getCodec() { return CODEC; }
}