package net.kayn.fallen_gems_affixes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import net.kayn.fallen_gems_affixes.attachment.augment.AugmentHelper;
import net.kayn.fallen_gems_affixes.attachment.augment.AugmentInstance;
import net.kayn.fallen_gems_affixes.augment.*;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentInnerData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static net.kayn.fallen_gems_affixes.Fallen.AugmentMisc.*;

@Mixin(AffixHelper.class)
public class AffixHelperMixin {

    @SuppressWarnings({"ConstantConditions"})
    @WrapOperation(method = "setAffixes", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;putFloat(Ljava/lang/String;F)V"))
    private static void levelTweak(CompoundTag instance, String pKey, float pValue, Operation<Void> original, @Local(argsOnly = true, name = "arg0") ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains(AUGMENT_DATA)) {
            original.call(instance, pKey, pValue);
            return;
        }

        if (pKey.equals("apotheosis:durable")) {
            original.call(instance, pKey, pValue);
            return;
        }

        float currentPower = pValue;
        float affixMultiplier = 1f;

        for (AugmentInstance inst : AugmentHelper.getAugments(stack).instances()) {
            IAugmentInnerData innerData = inst.getData();
            if (innerData instanceof SupremacyAugment.SupremacyData data) {
                float power = data.getAffixPower();
                currentPower = Mth.clamp(Math.max(pValue, power), 0f, SupremacyAugment.MAX_AFFIX_LEVEL);
            } else if (innerData instanceof IAffixPowerProvider provider) {
                affixMultiplier += provider.getAffixPower();
            }
        }

        original.call(instance, pKey, Mth.clamp(currentPower * affixMultiplier, 0f, SupremacyAugment.MAX_AFFIX_LEVEL));
    }
}