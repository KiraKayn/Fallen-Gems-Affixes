package net.kayn.fallen_gems_affixes.mixin;

import net.kayn.fallen_gems_affixes.adventure.affix.TrueShotAffix;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class TrueShotMixin {

    @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
    private void fga$trueShotBypassImmunity(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (!(source.getDirectEntity() instanceof AbstractArrow arrow)) return;

        if (arrow.getPersistentData().getBoolean(TrueShotAffix.KEY_TRUE_SHOT)) {
            cir.setReturnValue(false);
        }
    }
}