package net.kayn.fallen_gems_affixes.mixin;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.affix.AffixType;
import dev.shadowsoffire.apotheosis.affix.ItemAffixes;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootController;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.kayn.fallen_gems_affixes.loot.LootCategories;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.stream.Stream;

@Mixin(value = LootController.class, remap = false)
public class LootControllerMixin {
    @Inject(method = "getAvailableAffixes", at = @At(
            value = "INVOKE_ASSIGN",
            target = "Lnet/minecraft/world/item/ItemStack;getOrDefault(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;",
            args = {
                    "fuzz=2"
            }
    ),
            locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private static void availableTweak(ItemStack stack, LootRarity rarity, AffixType type, CallbackInfoReturnable<Stream<DynamicHolder<Affix>>> cir, LootCategory cat, ItemAffixes current) {
        if (cat == null) return;
        try {
            LootCategory cat2;
            if (cat == Apoth.LootCategories.MELEE_WEAPON && LootCategories.Check.heavyWeaponCheck(stack)) {
                    cat2 = LootCategories.HEAVY_WEAPON;
            } else {
                return;
            }
            cir.setReturnValue(
                    AffixHelper.byType(type).stream()
                            .filter(a -> a.get().canApplyTo(stack, cat, rarity) || a.get().canApplyTo(stack, cat2, rarity))
                            .filter(a -> a.get().isCompatibleWith(current))
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
