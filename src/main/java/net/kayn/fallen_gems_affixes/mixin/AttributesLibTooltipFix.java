package net.kayn.fallen_gems_affixes.mixin;

import dev.shadowsoffire.attributeslib.client.AttributesLibClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AttributesLibClient.class)
public class AttributesLibTooltipFix {
    private static boolean shouldNeg = false;
    @ModifyArg(method = "applyTextFor",
            at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/chat/TextColor;fromRgb(I)Lnet/minecraft/network/chat/TextColor;"
    ))
    private static int checkColor(int pColor){
        if (pColor == 0xF93131) {
            shouldNeg = true;
        }
        return pColor;
    }

    @ModifyArg(
            method = "applyTextFor",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ai/attributes/AttributeModifier;<init>(Ljava/util/UUID;Ljava/util/function/Supplier;DLnet/minecraft/world/entity/ai/attributes/AttributeModifier$Operation;)V"
            ),
            index = 2
    )
    private static double fixNeg(double value) {
        double value1 = shouldNeg ? -value : value;
        shouldNeg = false;
        return value1;
    }
}
