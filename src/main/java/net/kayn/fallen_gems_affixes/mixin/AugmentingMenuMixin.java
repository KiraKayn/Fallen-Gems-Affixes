package net.kayn.fallen_gems_affixes.mixin;

import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.adventure.affix.augmenting.AugmentingMenu;
import net.kayn.fallen_gems_affixes.augment.GenesisAugment;
import net.kayn.fallen_gems_affixes.recipe.ErasureRecipe;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
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

    @Inject(
            method = "clickMenuButton",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void blockScrollAffixReroll(Player player, int id, CallbackInfoReturnable<Boolean> cir) {
        if ((id & 0b1) != AugmentingMenu.REROLL) return;

        int selected = id >> 1;
        ItemStack mainItem = ((AugmentingMenu) (Object) this).getMainItem();
        if (mainItem.isEmpty() || !mainItem.hasTag()) return;
        if (!mainItem.getTag().contains(ErasureRecipe.TAG_SCROLL_AFFIXES)) return;

        List<AffixInstance> affixes = AugmentingMenu.computeItemAffixes(mainItem);
        if (selected >= affixes.size()) return;

        AffixInstance inst = affixes.get(selected);
        ListTag scrollList = mainItem.getTag().getList(ErasureRecipe.TAG_SCROLL_AFFIXES, Tag.TAG_STRING);
        for (int i = 0; i < scrollList.size(); i++) {
            if (scrollList.getString(i).equals(inst.affix().getId().toString())) {
                cir.setReturnValue(false);
                cir.cancel();
                return;
            }
        }
    }
}