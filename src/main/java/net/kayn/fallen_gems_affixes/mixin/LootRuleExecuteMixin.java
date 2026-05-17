package net.kayn.fallen_gems_affixes.mixin;

import dev.shadowsoffire.apotheosis.adventure.affix.AffixType;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.kayn.fallen_gems_affixes.util.AffixTypeExtender;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableInt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(value = LootRarity.LootRule.class, remap = false)
public abstract class LootRuleExecuteMixin {
    @Shadow
    public abstract AffixType type();

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private void skipSetAffix(ItemStack stack, LootRarity rarity,
                              Set<DynamicHolder<?>> currentAffixes, MutableInt sockets,
                              RandomSource rand, CallbackInfo ci) {
        if (AffixTypeExtender.SET_AFFIX != null && this.type() == AffixTypeExtender.SET_AFFIX) {
            ci.cancel();
        }
    }
}