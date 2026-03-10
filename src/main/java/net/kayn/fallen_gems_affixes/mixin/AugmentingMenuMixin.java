package net.kayn.fallen_gems_affixes.mixin;

import dev.shadowsoffire.apotheosis.adventure.affix.augmenting.AugmentingMenu;
import net.kayn.fallen_gems_affixes.augment.GenesisAugment;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Predicate;

@Mixin(AugmentingMenu.class)
public class AugmentingMenuMixin {
    @ModifyArg(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/shadowsoffire/apotheosis/adventure/affix/augmenting/AugmentingMenu$1;<init>(Ldev/shadowsoffire/apotheosis/adventure/affix/augmenting/AugmentingMenu;Ldev/shadowsoffire/placebo/cap/InternalItemHandler;IIILjava/util/function/Predicate;)V",
                    ordinal = 0
            ),
            index = 5)
    public Predicate<ItemStack> addAugmentingTableGenesisCheck(Predicate<ItemStack> predicate) {
        return (stack) -> predicate.test(stack) && GenesisAugment.getGenesisGemPower(stack) == null;
    }
}
