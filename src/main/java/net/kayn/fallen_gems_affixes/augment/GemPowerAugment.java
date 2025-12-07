package net.kayn.fallen_gems_affixes.augment;

import net.kayn.fallen_gems_affixes.types.augment.IAugment;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentInnerData;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

public class GemPowerAugment implements IAugment {

    public static final ResourceLocation GEM_POWER_ID =
            new ResourceLocation("fallen_gems_affixes", "gem_power");

    /** Return ID for static references */
    public static ResourceLocation getAugmentId() {
        return GEM_POWER_ID;
    }

    @Override
    public ResourceLocation getId() {
        return GEM_POWER_ID;
    }

    @Override
    public boolean isUnique() {
        return true;
    }

    @Override
    public boolean needsInstance() {
        return false; // Like Soulbound, no per-item instance needed
    }

    @Override
    public void renderImage(@NotNull Font font, int x, int y, GuiGraphics gui, IAugmentInnerData innerData) {
        // Draw the base augment icon
        gui.blit(IAugment.AUGMENT_ICON, x, y, 0, 0, 9, 9, 9, 9);

        // Render a diamond icon on top (or your custom item)
        gui.renderFakeItem(new ItemStack(Items.DIAMOND), x, y);
    }

    @Override
    public MutableComponent organizeTooltipText(IAugmentInnerData innerData) {
        return IAugment.super.organizeTooltipText(innerData);
    }

    @Override
    public IAugmentInnerData deserializeInnerData(CompoundTag tag) {
        return IAugmentInnerData.EMPTY;
    }
}