package net.kayn.fallen_gems_affixes.augment;

import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.types.augment.IAugment;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentInnerData;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class SupremacyAugment implements IAugment {
    private static final ResourceLocation SUPREMACY_ID = new ResourceLocation(FallenGemsAffixes.MOD_ID, "supremacy");
    public static final float STANDARD_MAX_LEVEL = 1.0f;
    public static final float MAX_AFFIX_LEVEL = 2.0f;

    public static ResourceLocation augmentId() {
        return SUPREMACY_ID;
    }

    @Override
    public ResourceLocation getId() {
        return SUPREMACY_ID;
    }

    @Override
    public boolean isUnique() {
        return true;
    }

    @Override
    public boolean needsInstance() {
        return false;
    }

    @Override
    public void renderImage(@NotNull Font font, int x, int y, GuiGraphics gui, IAugmentInnerData innerData) {

    }

    @Override
    public IAugmentInnerData deserializeInnerData(CompoundTag tag) {
        return IAugmentInnerData.EMPTY;
    }

    public static void apply(ItemStack stack) {
        Map<DynamicHolder<? extends Affix>, AffixInstance> affixes = AffixHelper.getAffixes(stack);
        Map<DynamicHolder<? extends Affix>, AffixInstance> newAffixes = new HashMap<>();
        affixes.forEach((affix, affixIns) -> {
            newAffixes.put(affix, new AffixInstance(affixIns.affix(), affixIns.stack(), affixIns.rarity(), Mth.clamp(affixIns.level() * 1.5f, 0, MAX_AFFIX_LEVEL)));
        });

        AffixHelper.setAffixes(stack, newAffixes);
    }
}
