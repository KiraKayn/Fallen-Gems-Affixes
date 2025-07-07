package net.kayn.fallen_gems_affixes.mixin.permanent_effect;

import net.kayn.fallen_gems_affixes.util.ProtectedMobEffectMap;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow
    @Final
    @Mutable
    private Map<MobEffect, MobEffectInstance> activeEffects;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void replaceEffectMap(EntityType<? extends LivingEntity> pEntityType, Level pLevel, CallbackInfo ci) {
        if (!(((Object) this) instanceof Player player)) return;
        try {
            ProtectedMobEffectMap<?> wrapped = new ProtectedMobEffectMap<>(player);
            wrapped.putAll(this.activeEffects);
            this.activeEffects = wrapped;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * When an effect is to be removed, mob effect with lose its attribute modifiers when this method returns.
     */
    @Inject(method = "onEffectRemoved", at = @At("HEAD"), cancellable = true)
    private void onEffectRemovedPrefix(MobEffectInstance effect, CallbackInfo ci) {
        if ((Object) this instanceof Player player) {
            if (player.getActiveEffectsMap() instanceof ProtectedMobEffectMap<?> map && map.isExternalRemover() && map.containsPermanent(effect.getEffect())) {
                ci.cancel();
            }
        }
    }
}