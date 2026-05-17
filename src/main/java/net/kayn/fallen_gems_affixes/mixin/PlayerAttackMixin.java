package net.kayn.fallen_gems_affixes.mixin;

import net.kayn.fallen_gems_affixes.event.PlayerCriticalHitEvent;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerAttackMixin {
    @Inject(method = "attack", at = @At("HEAD"))
    private void detectCriticalHit(Entity target, CallbackInfo ci) {
        Player player = (Player)(Object)this;
        if (!(target instanceof LivingEntity living)) return;
        if (player.level().isClientSide) return;

        boolean isCrit = player.fallDistance > 0.0F
                && !player.onGround()
                && !player.onClimbable()
                && !player.isInWater()
                && !player.hasEffect(MobEffects.BLINDNESS)
                && !player.isPassenger()
                && player.getAttackStrengthScale(0.5F) > 0.9F;

        if (isCrit) {
            MinecraftForge.EVENT_BUS.post(new PlayerCriticalHitEvent(player, living));
        }
    }
}