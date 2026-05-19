package net.kayn.fallen_gems_affixes.mixin;

import dev.shadowsoffire.attributeslib.impl.AttributeEvents;
import net.kayn.fallen_gems_affixes.event.PlayerCriticalHitEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AttributeEvents.class, remap = false)
public class ApotheosisCritMixin {

    @Inject(
            method = "apothCriticalStrike",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/shadowsoffire/placebo/network/PacketDistro;sendToTracking"
            )
    )
    private void onApotheosisCritRegistered(LivingHurtEvent e, CallbackInfo ci) {
        if (e.getSource().getEntity() instanceof Player player) {
            float finalCritDamage = e.getAmount();
            MinecraftForge.EVENT_BUS.post(new PlayerCriticalHitEvent(player, e.getEntity(), finalCritDamage));
        }
    }
}