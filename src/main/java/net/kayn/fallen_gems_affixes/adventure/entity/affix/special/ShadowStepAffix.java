package net.kayn.fallen_gems_affixes.adventure.entity.affix.special;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kayn.fallen_gems_affixes.adventure.entity.affix.EntityAffix;
import net.kayn.fallen_gems_affixes.adventure.entity.affix.ScaledValue;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class ShadowStepAffix extends EntityAffix {

    public static final ResourceLocation TYPE = new ResourceLocation("fallen_gems_affixes", "shadow_step");
    private static final String CD_KEY = "fga.shadow_step_next";

    public static final Codec<ShadowStepAffix> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ScaledValue.CODEC.fieldOf("chance").forGetter(a -> a.chance),
            Codec.INT.optionalFieldOf("cooldown_ticks", 100).forGetter(a -> a.cooldown)
    ).apply(inst, ShadowStepAffix::new));

    private final ScaledValue chance;
    private final int cooldown;

    public ShadowStepAffix(ScaledValue chance, int cooldown) {
        this.chance   = chance;
        this.cooldown = cooldown;
    }

    @Override
    public void doPostHurt(LivingEntity entity, Entity attacker, float level) {
        if (attacker == null) return;
        long now = entity.level().getGameTime();
        if (entity.getPersistentData().getLong(CD_KEY) > now) return;
        if (entity.getRandom().nextFloat() >= chance.get(level)) return;

        // Teleport to directly behind the attacker relative to their facing
        Vec3 look    = attacker.getLookAngle();
        Vec3 behind  = attacker.position().subtract(look.scale(1.5));
        entity.teleportTo(behind.x, attacker.getY(), behind.z);
        entity.getPersistentData().putLong(CD_KEY, now + cooldown);
    }

    @Override public ResourceLocation getType()              { return TYPE; }
    @Override public Codec<? extends EntityAffix> getCodec() { return CODEC; }
}