package net.kayn.fallen_gems_affixes.mixin;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.kayn.fallen_gems_affixes.adventure.affix.ConcentrationAffix;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(value = AbstractSpell.class, remap = false)
public class AbstractSpellMixin {

    @Inject(method = "canBeInterrupted", at = @At("RETURN"), cancellable = true)
    private void onCanBeInterrupted(@Nullable Player player, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && player != null) {
            if (ConcentrationAffix.hasConcentration(player)) {
                cir.setReturnValue(false);
            }
        }
    }
}