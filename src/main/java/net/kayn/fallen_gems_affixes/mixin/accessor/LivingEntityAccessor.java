package net.kayn.fallen_gems_affixes.mixin.accessor;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(value = LivingEntity.class, remap = false)
public interface LivingEntityAccessor {

    @Accessor("effectsDirty")
    void setEffectsDirty(boolean value);

    @Accessor("activeEffects")
    Map<Holder<MobEffect>, MobEffectInstance> getActiveEffects();

    @Invoker("onEffectUpdated")
    void invokeOnEffectUpdated(MobEffectInstance instance, boolean reapply, @Nullable Entity source);

    @Invoker("onEffectRemoved")
    void invokeOnEffectRemoved(MobEffectInstance instance);

    @Invoker("onEffectAdded")
    void invokeOnEffectAdded(MobEffectInstance instance, @Nullable Entity entity);
}
