package net.kayn.fallen_gems_affixes.adventure.entity.affix.special;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kayn.fallen_gems_affixes.adventure.entity.affix.EntityAffix;
import net.kayn.fallen_gems_affixes.adventure.entity.affix.ScaledValue;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class RegenerationAffix extends EntityAffix {

    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "mob_regeneration");

    public static final Codec<RegenerationAffix> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ScaledValue.CODEC.fieldOf("heal_percent").forGetter(a -> a.healPercent),
            Codec.INT.optionalFieldOf("interval", 40).forGetter(a -> a.interval)
    ).apply(inst, RegenerationAffix::new));

    private final ScaledValue healPercent;
    private final int interval;

    public RegenerationAffix(ScaledValue healPercent, int interval) {
        this.healPercent = healPercent;
        this.interval    = interval;
    }

    @Override
    public void onTick(LivingEntity entity, float level) {
        if (entity.getHealth() >= entity.getMaxHealth()) return;
        entity.heal(entity.getMaxHealth() * healPercent.get(level));
    }

    @Override public int getTickInterval()                   { return interval; }
    @Override public ResourceLocation getType()              { return TYPE; }
    @Override public Codec<? extends EntityAffix> getCodec() { return CODEC; }
}
