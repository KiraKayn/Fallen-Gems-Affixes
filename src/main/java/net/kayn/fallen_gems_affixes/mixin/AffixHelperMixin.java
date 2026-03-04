package net.kayn.fallen_gems_affixes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.augment.GenesisAugment;
import net.kayn.fallen_gems_affixes.augment.SupremacyAugment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static net.kayn.fallen_gems_affixes.Fallen.AugmentMisc.*;

@Mixin(AffixHelper.class)
public class AffixHelperMixin {
    @SuppressWarnings("ConstantConditions")
    @WrapOperation(method = "setAffixes", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;putFloat(Ljava/lang/String;F)V"))
    private static void levelTweak(CompoundTag instance, String pKey, float pValue, Operation<Void> original, @Local(argsOnly = true, name = "arg0") ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(AUGMENT_DATA)) {
            CompoundTag augmentData = stack.getTagElement(AUGMENT_DATA);
            ListTag augments = augmentData.getList(AUGMENTS, CompoundTag.TAG_COMPOUND);
            float currentPower = pValue;
            float genesisMultiplier = 1f;
            for (int i = 0; i < augments.size(); i++) {
                CompoundTag augment = augments.getCompound(i);
                if (!pKey.equals("apotheosis:durable")) {
                    if (augment.getString(TYPE).equals(Fallen.Augments.SUPREMACY_STRING)) {
                        float power = augment.getCompound(INNER_DATA).getFloat("power");
                        currentPower = Mth.clamp(Math.max(pValue , power), 0f, SupremacyAugment.MAX_AFFIX_LEVEL);
                    } else if (augment.getString(TYPE).equals(Fallen.Augments.GENESIS_STRING)) {
                        CompoundTag inner = augment.getCompound(Fallen.AugmentMisc.INNER_DATA);
                        genesisMultiplier = inner.getFloat("affixPower");
                    }
                }
            }
            original.call(instance, pKey, Mth.clamp(currentPower * genesisMultiplier, 0f, SupremacyAugment.MAX_AFFIX_LEVEL));
            return;
        }
        original.call(instance, pKey, pValue);
    }

}
