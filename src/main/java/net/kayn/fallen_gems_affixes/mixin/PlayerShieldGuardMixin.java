package net.kayn.fallen_gems_affixes.mixin;

import net.kayn.fallen_gems_affixes.adventure.affix.ShieldGuardAffix;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerShieldGuardMixin {

    @Inject(method = "disableShield", at = @At("HEAD"), cancellable = true)
    private void fallen_gems_affixes$cancelShieldDisable(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        ItemStack shield = player.getUseItem();
        if (ShieldGuardAffix.isImmune(shield)) {
            ci.cancel();
        }
    }

    @ModifyConstant(method = "disableShield", constant = @Constant(intValue = 100))
    private int fallen_gems_affixes$reduceShieldDisableTime(int original) {
        Player player = (Player) (Object) this;
        ItemStack shield = player.getUseItem();
        float reduction = ShieldGuardAffix.getReduction(shield);
        if (reduction <= 0f) return original;
        return Math.max(Math.round(original * (1f - reduction)), 0);
    }
}
