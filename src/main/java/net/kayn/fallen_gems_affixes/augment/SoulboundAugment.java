package net.kayn.fallen_gems_affixes.augment;

import net.kayn.fallen_gems_affixes.registry.ModItems;
import net.kayn.fallen_gems_affixes.types.augment.IAugment;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentInnerData;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SoulboundAugment implements IAugment {

    public static final ResourceLocation SOULBOUND_ID =
            new ResourceLocation("fallen_gems_affixes", "soulbound");

    /** Return ID for static references */
    public static ResourceLocation getAugmentId() {
        return SOULBOUND_ID;
    }

    @Override
    public ResourceLocation getId() {
        return SOULBOUND_ID;
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
        gui.blit(IAugment.AUGMENT_ICON, x, y, 0, 0, 0, 9, 9, 9, 9);
        gui.renderFakeItem(new ItemStack(ModItems.AUGMENT_ITEM.get()), x, y);
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