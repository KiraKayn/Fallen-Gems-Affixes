package net.kayn.fallen_gems_affixes.adventure.entity.affix;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;

import javax.annotation.Nullable;

public abstract class EntityAffix {

    @Nullable
    private ResourceLocation id;

    /** The class-level type ID used for codec dispatch (e.g. "fallen_gems_affixes:enrage"). */
    public abstract ResourceLocation getType();
    public abstract Codec<? extends EntityAffix> getCodec();

    public void setId(ResourceLocation id) { this.id = id; }

    @Nullable
    public ResourceLocation getId() { return id; }

    /** Called every {@link #getTickInterval()} server ticks. */
    public void onTick(LivingEntity entity, float level) {}

    /** Return modified damage amount. Return ≤ 0 to cancel the event. */
    public float onHurt(LivingEntity entity, DamageSource src, float amount, float level) { return amount; }

    /** Called after the entity successfully attacks a target. */
    public void doPostAttack(LivingEntity entity, Entity target, float level) {}

    /** Called after the entity takes damage. */
    public void doPostHurt(LivingEntity entity, @Nullable Entity attacker, float level) {}

    /** Bonus flat damage added per attack. */
    public float getDamageBonus(LivingEntity entity, MobType targetType, float level) { return 0f; }

    /** Called on entity death. */
    public void onDeath(LivingEntity entity, DamageSource src, float level) {}

    public int getTickInterval() { return 20; }
}