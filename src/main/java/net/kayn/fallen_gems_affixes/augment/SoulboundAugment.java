package net.kayn.fallen_gems_affixes.augment;

import com.mojang.blaze3d.vertex.PoseStack;
import net.kayn.fallen_gems_affixes.item.augments.AugmentItem;
import net.kayn.fallen_gems_affixes.types.augment.IAugment;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentInnerData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

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

        var data = AugmentItem.getAugmentData(SOULBOUND_ID);
        if (data == null) return;

        ItemStack stack = AugmentItem.createAugment(SOULBOUND_ID);

        PoseStack pose = gui.pose();
        pose.pushPose();
        pose.translate(x, y, 0);
        pose.scale(0.5F, 0.5F, 1);
        gui.renderFakeItem(stack, 0, 0);
        pose.popPose();
    }

    @Override
    public MutableComponent organizeTooltipText(IAugmentInnerData innerData) {
        MutableComponent comp = Component.translatable("fallen_gems_affixes.augment.soulbound.desc")
                .withStyle(net.minecraft.ChatFormatting.YELLOW);
        return comp;
    }

    @Override
    public void appendItemTooltip(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("fallen_gems_affixes.augment.soulbound.type")
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("• ")
                .withStyle(ChatFormatting.YELLOW)
                .append(Component.translatable("fallen_gems_affixes.augment.soulbound.desc")
                .withStyle(ChatFormatting.YELLOW)));
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