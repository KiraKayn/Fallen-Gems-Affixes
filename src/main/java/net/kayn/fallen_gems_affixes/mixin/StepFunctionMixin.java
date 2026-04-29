package net.kayn.fallen_gems_affixes.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.shadowsoffire.placebo.util.StepFunction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = StepFunction.class, remap = false)
public class StepFunctionMixin {
    @Shadow @Final private float min;

    @Shadow @Final private float step;

    @ModifyReturnValue(method = "get", at = @At("RETURN"))
    private float modifyGet(float result, float level) {
        if (step == 0) return result;
        if (level > 0 && level < 1) {
            return result + this.min * (level - 1);
        }
        return result;
    }
}
