package net.kayn.fallen_gems_affixes.mixin;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.GemBonus;
import net.kayn.fallen_gems_affixes.loot.LootCategories;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.HashMap;

@Mixin(Gem.class)
public class GemMixin {
    @Final
    @Shadow
    protected HashMap<LootCategory, GemBonus> bonusMap;

    @Inject(method = "isValidIn", at = @At(
            value = "INVOKE_ASSIGN",
            target = "Ldev/shadowsoffire/apotheosis/loot/LootCategory;forItem(Lnet/minecraft/world/item/ItemStack;)Ldev/shadowsoffire/apotheosis/loot/LootCategory;"),
    locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void validTweak(ItemStack socketed, ItemStack gem, Purity purity, CallbackInfoReturnable<Boolean> cir, LootCategory cat) {
        if (cat == null) return;
        try {
            if (cat == Apoth.LootCategories.MELEE_WEAPON && LootCategories.Check.heavyWeaponCheck(socketed)) {
                if (this.bonusMap.containsKey(LootCategories.HEAVY_WEAPON) && this.bonusMap.get(LootCategories.HEAVY_WEAPON).supports(purity)) cir.setReturnValue(true);
            } else return;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
