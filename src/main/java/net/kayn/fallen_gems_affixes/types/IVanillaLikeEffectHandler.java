package net.kayn.fallen_gems_affixes.types;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;

import javax.annotation.Nullable;

public interface IVanillaLikeEffectHandler extends IEffectHandler{

    boolean removeEffect(Holder<MobEffect> effect);

    boolean addEffect(MobEffectInstance effectInstance, LivingEntity source);

    void refreshDirtyAttributes();

    void onAttributeUpdated(Holder<Attribute> attribute);

    void onEffectAdded(MobEffectInstance effectInstance, LivingEntity source);

    void onEffectUpdated(MobEffectInstance effectInstance, boolean forced, @Nullable Entity source);

    void onEffectRemoved(MobEffectInstance effectInstance);
}
