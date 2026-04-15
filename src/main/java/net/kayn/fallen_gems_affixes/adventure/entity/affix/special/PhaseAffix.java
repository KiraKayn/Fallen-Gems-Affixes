package net.kayn.fallen_gems_affixes.adventure.entity.affix.special;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kayn.fallen_gems_affixes.adventure.entity.affix.EntityAffix;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class PhaseAffix extends EntityAffix {

    public static final ResourceLocation TYPE = new ResourceLocation("fallen_gems_affixes", "phase");
    private static final String CD_KEY = "fga.phase_next";

    public static final Codec<PhaseAffix> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.optionalFieldOf("invuln_ticks", 20).forGetter(a -> a.invulnTicks),
            Codec.INT.optionalFieldOf("cooldown_ticks", 200).forGetter(a -> a.cooldownTicks)
    ).apply(inst, PhaseAffix::new));

    private final int invulnTicks;
    private final int cooldownTicks;

    public PhaseAffix(int invulnTicks, int cooldownTicks) {
        this.invulnTicks   = invulnTicks;
        this.cooldownTicks = cooldownTicks;
    }

    @Override
    public void onTick(LivingEntity entity, float level) {
        long gameTime = entity.level().getGameTime();
        long nextPhase = entity.getPersistentData().getLong(CD_KEY);

        if (gameTime < nextPhase) return;

        entity.invulnerableTime = invulnTicks;
        entity.getPersistentData().putLong(CD_KEY, gameTime + cooldownTicks);
    }

    @Override public int getTickInterval()                   { return 5; }
    @Override public ResourceLocation getType()              { return TYPE; }
    @Override public Codec<? extends EntityAffix> getCodec() { return CODEC; }
}
