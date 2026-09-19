package net.kayn.fallen_gems_affixes.mixin.celestisynth;

import net.kayn.fallen_gems_affixes.adventure.affix.HomingAffix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.thecelestialworkshop.celestisynth.common.entity.projectile.RainfallArrow;

@Mixin(value = RainfallArrow.class, remap = false)
public class RainfallArrowMixin {

    @ModifyConstant(
            method = "hitEffect",
            constant = @Constant(floatValue = 2.0F),
            remap = false
    )
    private float fga$useBaseDamage(float original) {
        RainfallArrow arrow = (RainfallArrow) (Object) this;
        return (float) arrow.getBaseDamage();
    }

    @ModifyConstant(
            method = "tick",
            constant = @Constant(intValue = 5),
            remap = false
    )
    private int fga$extendLifeForHoming(int original) {
        RainfallArrow arrow = (RainfallArrow) (Object) this;
        if (arrow.getPersistentData().contains(HomingAffix.KEY_TURN_RATE)) {
            return 200;
        }
        return original;
    }
}