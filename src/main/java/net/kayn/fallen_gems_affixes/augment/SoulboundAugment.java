package net.kayn.fallen_gems_affixes.augment;

import com.mojang.blaze3d.vertex.PoseStack;
import net.kayn.fallen_gems_affixes.registry.ModItems;
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

public class SoulboundAugment implements IAugment {
    private static final ResourceLocation SOULBOUND_ID = new ResourceLocation("fallen_gems_affixes", "soulbound");

    @Override
    public ResourceLocation getId() {
        return SOULBOUND_ID;
    }

    public static ResourceLocation augmentId() {
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
        PoseStack pose = gui.pose();
        pose.pushPose();
        pose.scale(0.5F, 0.5F, 1);
        gui.renderFakeItem(new ItemStack(ModItems.SOULBOUND_AUGMENT_ITEM.get()), 2 * x + 1, 2 * y + 1);
        pose.popPose();
    }

    @Override
    public MutableComponent organizeTooltipText(IAugmentInnerData innerData) {
        return IAugment.super.organizeTooltipText(innerData);
    }

    @Override
    public IAugmentInnerData deserializeInnerData(CompoundTag tag) {
        return IAugmentInnerData.EMPTY;
    }

    @Override
    public String toString() {
        return "SoulboundAugment{" + "id=" + augmentId() + "}";
    }
}