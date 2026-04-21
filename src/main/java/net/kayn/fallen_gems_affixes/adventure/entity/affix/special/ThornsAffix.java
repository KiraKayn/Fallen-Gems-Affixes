package net.kayn.fallen_gems_affixes.adventure.entity.affix.special;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kayn.fallen_gems_affixes.adventure.entity.affix.EntityAffix;
import net.kayn.fallen_gems_affixes.adventure.entity.affix.ScaledValue;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class ThornsAffix extends EntityAffix {

    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "mob_thorns");

    public static final Codec<ThornsAffix> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ScaledValue.CODEC.fieldOf("reflect").forGetter(a -> a.reflect)
    ).apply(inst, ThornsAffix::new));

    private final ScaledValue reflect;

    public ThornsAffix(ScaledValue reflect) { this.reflect = reflect; }

    @Override
    public void doPostHurt(LivingEntity entity, Entity attacker, float level) {
        if (!(attacker instanceof LivingEntity living)) return;
        living.hurt(entity.level().damageSources().thorns(entity), reflect.get(level));
    }

    @Override public ResourceLocation getType()              { return TYPE; }
    @Override public Codec<? extends EntityAffix> getCodec() { return CODEC; }
}
