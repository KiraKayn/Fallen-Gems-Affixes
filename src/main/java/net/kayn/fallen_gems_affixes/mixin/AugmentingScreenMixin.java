package net.kayn.fallen_gems_affixes.mixin;

import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.adventure.affix.augmenting.AugmentingMenu;
import dev.shadowsoffire.apotheosis.adventure.affix.augmenting.AugmentingScreen;
import dev.shadowsoffire.apotheosis.adventure.client.SimpleTexButton;
import net.kayn.fallen_gems_affixes.recipe.ErasureRecipe;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(AugmentingScreen.class)
public class AugmentingScreenMixin {

    @Shadow
    protected SimpleTexButton rerollBtn;

    @Shadow
    protected List<AffixInstance> currentItemAffixes;

    @Inject(
            method = "updateCachedState",
            at = @At("TAIL"),
            remap = false
    )
    private void disableRerollForScrollAffixes(CallbackInfo ci) {
        if (rerollBtn == null || !rerollBtn.active) return;
        if (currentItemAffixes == null || currentItemAffixes.isEmpty()) return;

        AugmentingMenu menu = ((AugmentingScreen)(Object)this).getMenu();
        ItemStack mainItem = menu.getMainItem();
        if (!mainItem.hasTag() || !mainItem.getTag().contains(ErasureRecipe.TAG_SCROLL_AFFIXES)) return;

        ListTag scrollList = mainItem.getTag().getList(ErasureRecipe.TAG_SCROLL_AFFIXES, Tag.TAG_STRING);

        for (AffixInstance inst : currentItemAffixes) {
            for (int i = 0; i < scrollList.size(); i++) {
                if (scrollList.getString(i).equals(inst.affix().getId().toString())) {
                    rerollBtn.active = false;
                    rerollBtn.setInactiveMessage(
                            Component.translatable("button.fallen_gems_affixes.scroll_affix_no_reroll")
                                    .withStyle(ChatFormatting.RED)
                    );
                    return;
                }
            }
        }
    }
}