package net.kayn.fallen_gems_affixes.mixin.permanent_effect;

import net.kayn.fallen_gems_affixes.event.PermanentEffectHandler;
import net.kayn.fallen_gems_affixes.util.IProtectedEffectMapAccessor;
import net.kayn.fallen_gems_affixes.util.ProtectedMobEffectMap;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements IProtectedEffectMapAccessor {
    @Shadow
    @Final
    @Mutable
    private Map<MobEffect, MobEffectInstance> activeEffects;
//    @Unique
//    private Map<MobEffect, Integer> protected_Effects;

    public LivingEntityMixin(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

//    @Unique
//    public Map<MobEffect, Integer> getProtected_EffectMap() {
//        return protected_Effects;
//    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void replaceEffectMap(EntityType<? extends LivingEntity> pEntityType, Level pLevel, CallbackInfo ci) {
        if (PermanentEffectHandler.isUseTickEvent()) return;
        if (!(((Object) this) instanceof Player)) return;

        try {
            ProtectedMobEffectMap<?> wrapped = new ProtectedMobEffectMap<>(this);
            wrapped.putAll(this.activeEffects);
            this.activeEffects = wrapped;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Inject(method = "onEffectRemoved", at = @At("HEAD"), cancellable = true)
    private void onEffectRemovedPrefix(MobEffectInstance effect, CallbackInfo ci) {
        if (PermanentEffectHandler.isUseTickEvent()) return;
        if ((Object) this instanceof Player player) {
            if (player.getActiveEffectsMap() instanceof ProtectedMobEffectMap<?> map && map.isExternalRemover() && map.containsPermanent(effect.getEffect())) {
                ci.cancel();
            }
        }
    }

//    @Inject(method = "getActiveEffectsMap", at = @At("HEAD"), cancellable = true)
//    private void getActiveEffectsMapAlt(CallbackInfoReturnable<Map<MobEffect, MobEffectInstance>> cir) {
//        if (!PermanentEffectHandler.isUseTickEvent() && !((Object) this instanceof Player)) return;
//        cir.setReturnValue(protectedMobEffects);
//    }
}