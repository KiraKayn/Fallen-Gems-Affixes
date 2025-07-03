package net.kayn.fallen_gems_affixes.mixin;

import net.kayn.fallen_gems_affixes.util.ProtectedMobEffectMap;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.Map;

import static net.kayn.fallen_gems_affixes.event.PermanentEffectHandler.checkGemBonus;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow
    @Final
    @Mutable
    private Map<MobEffect, MobEffectInstance> activeEffects;
    private static final Logger LOGGER = LogManager.getLogger();

    @Shadow public abstract Map<MobEffect, MobEffectInstance> getActiveEffectsMap();

    public LivingEntityMixin(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void replaceEffectMap(EntityType<? extends LivingEntity> pEntityType, Level pLevel, CallbackInfo ci) {
        if (!(((Object) this) instanceof Player)) return;

        try {
            ProtectedMobEffectMap<? extends Entity> wrapped = new ProtectedMobEffectMap<>(this);
            wrapped.putAll(this.activeEffects);
            this.activeEffects = (ProtectedMobEffectMap<? extends Entity>) wrapped;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Inject(method = "onEffectRemoved", at = @At("HEAD"), cancellable = true)
    private void onPermanentEffectRemoved(MobEffectInstance effect, CallbackInfo ci) {
        if ((Object) this instanceof Player player) {
            if (player.getActiveEffectsMap() instanceof ProtectedMobEffectMap<?> map && map.isExternalRemover() && map.containsPermanent(effect.getEffect())) {
                ci.cancel();
            }
        }
    }
}