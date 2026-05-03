package net.kayn.fallen_gems_affixes.adventure.entity.affix.special;

import com.mojang.serialization.Codec;
import net.kayn.fallen_gems_affixes.adventure.entity.affix.EntityAffix;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public class ColossusAffix extends EntityAffix {

    public static final ResourceLocation TYPE =
            ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "colossus");

    public static final Codec<ColossusAffix> CODEC = Codec.unit(ColossusAffix::new);

    @Override
    public float onHurt(LivingEntity entity, DamageSource src, float amount, float level) {
        return amount;
    }

    @Override public ResourceLocation getType()               { return TYPE; }
    @Override public Codec<? extends EntityAffix> getCodec()  { return CODEC; }
}