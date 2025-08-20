package net.kayn.fallen_gems_affixes.types;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public interface IEffectHandler {

    MobEffectInstance addEffectRet(MobEffectInstance effectInstance);

    void addEffectSilent(MobEffectInstance effectInstance);

    MobEffectInstance removeEffectRet(Holder<MobEffect> effect);
}
