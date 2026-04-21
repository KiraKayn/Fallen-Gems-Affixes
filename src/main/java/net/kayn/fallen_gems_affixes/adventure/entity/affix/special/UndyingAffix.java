package net.kayn.fallen_gems_affixes.adventure.entity.affix.special;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kayn.fallen_gems_affixes.adventure.entity.affix.EntityAffix;
import net.kayn.fallen_gems_affixes.adventure.entity.affix.ScaledValue;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public class UndyingAffix extends EntityAffix {

    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "undying");
    private static final String TRIGGERED_KEY = "fga.undying_triggered";

    public static final Codec<UndyingAffix> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ScaledValue.CODEC.fieldOf("heal_on_trigger").forGetter(a -> a.healOnTrigger)
    ).apply(inst, UndyingAffix::new));

    private final ScaledValue healOnTrigger;

    public UndyingAffix(ScaledValue healOnTrigger) { this.healOnTrigger = healOnTrigger; }

    @Override
    public float onHurt(LivingEntity entity, DamageSource src, float amount, float level) {
        if (entity.getHealth() - amount <= 0f && !entity.getPersistentData().getBoolean(TRIGGERED_KEY)) {
            entity.getPersistentData().putBoolean(TRIGGERED_KEY, true);
            entity.setHealth(entity.getMaxHealth() * healOnTrigger.get(level));
            return 0f;
        }
        return amount;
    }

    @Override public ResourceLocation getType()              { return TYPE; }
    @Override public Codec<? extends EntityAffix> getCodec() { return CODEC; }
}