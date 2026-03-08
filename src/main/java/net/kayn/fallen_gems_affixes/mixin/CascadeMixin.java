package net.kayn.fallen_gems_affixes.mixin;

import dev.shadowsoffire.attributeslib.impl.AttributeEvents;
import net.kayn.fallen_gems_affixes.augment.CascadeAugment;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AttributeEvents.class, remap = false)
public class CascadeMixin {

    private static final ThreadLocal<Integer> CRIT_ITERATION =
            ThreadLocal.withInitial(() -> 0);

    @Inject(method = "apothCriticalStrike", at = @At("HEAD"))
    private void resetCounter(LivingHurtEvent e, CallbackInfo ci) {
        CRIT_ITERATION.set(0);
    }

    @ModifyVariable(
            method = "apothCriticalStrike",
            at = @At(value = "CONSTANT", args = "floatValue=0.85"),
            name = "critMult"
    )
    private float applyCascade(float critMult, LivingHurtEvent e) {
        int iteration = CRIT_ITERATION.get();
        CRIT_ITERATION.set(iteration + 1);

        // Only fires on overcrit (iteration 1+)
        if (iteration == 0) return critMult;

        LivingEntity attacker = e.getSource().getEntity() instanceof LivingEntity le ? le : null;
        if (attacker == null) return critMult;

        CascadeAugment.CascadeData data = CascadeAugment.getCascadeData(attacker);
        if (data == null) return critMult;

        if (attacker.getRandom().nextFloat() < data.chance) {
            critMult *= (1.0f + data.damageBonus);
        }

        return critMult;
    }
}