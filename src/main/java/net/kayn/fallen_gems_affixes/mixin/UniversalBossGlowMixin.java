package net.kayn.fallen_gems_affixes.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class UniversalBossGlowMixin {

    @Inject(method = "isCurrentlyGlowing", at = @At("RETURN"), cancellable = true)
    private void hoverGlow(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) return;

        Entity self = (Entity) (Object) this;
        if (!self.level().isClientSide()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.crosshairPickEntity == self) {
            if (hasBossColor(self)) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    private void rarityGlowColor(CallbackInfoReturnable<Integer> cir) {
        Entity self = (Entity) (Object) this;
        if (!self.level().isClientSide()) return;

        Integer color = getBossColor(self);
        if (color != null) {
            cir.setReturnValue(color);
        }
    }
    private boolean hasBossColor(Entity entity) {
        return entity.getCustomName() != null && entity.getCustomName().getStyle().getColor() != null;
    }
    private Integer getBossColor(Entity entity) {
        if (entity.getCustomName() != null) {
            var color = entity.getCustomName().getStyle().getColor();
            if (color != null) return color.getValue();
        }
        return null;
    }
}