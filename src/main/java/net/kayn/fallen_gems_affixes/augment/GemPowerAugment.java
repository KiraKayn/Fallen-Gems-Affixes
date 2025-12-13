package net.kayn.fallen_gems_affixes.augment;

import com.mojang.blaze3d.vertex.PoseStack;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.attachment.AugmentInstance;
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

public class GemPowerAugment implements IAugment {
    private static final ResourceLocation GEM_POWER_ID = new ResourceLocation("fallen_gems_affixes", "gem_power");

    public static ResourceLocation augmentId() {
        return GEM_POWER_ID;
    }

    @Override
    public ResourceLocation getId() {
        return GEM_POWER_ID;
    }

    @Override
    public IAugmentInnerData parse(CompoundTag augmentData) {
        return IAugment.super.parse(augmentData);
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
    public AugmentInstance createInstanceFromStack(ItemStack stack) {
        return IAugment.super.createInstanceFromStack(stack);
    }

    @Override
    public void renderImage(@NotNull Font font, int x, int y, GuiGraphics gui, IAugmentInnerData innerData) {
        gui.blit(IAugment.AUGMENT_ICON, x, y, 0, 0, 0, 9, 9, 9, 9);

        var data = AugmentItem.getAugmentData(GEM_POWER_ID);
        if (data == null) return;

        ItemStack stack = AugmentItem.createAugment(GEM_POWER_ID);

        PoseStack pose = gui.pose();
        pose.pushPose();
        pose.translate(x, y, 0);
        pose.scale(0.5F, 0.5F, 1);
        gui.renderFakeItem(stack, 0, 0);
        pose.popPose();
    }

    public MutableComponent organizeTooltipText(IAugmentInnerData innerData) {
        MutableComponent comp = Component.translatable("fallen_gems_affixes.augment.gem_power.desc")
                .withStyle(net.minecraft.ChatFormatting.YELLOW);
        return comp;
    }

    @Override
    public IAugmentInnerData deserializeInnerData(CompoundTag tag) {
        IAugmentInnerData augmentInnerData = new GemPowerData();
        augmentInnerData.deserializeNBT(tag);
        return augmentInnerData;
    }

    @Override
    public void appendItemTooltip(ItemStack stack, @Nullable Level level, java.util.List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("fallen_gems_affixes.augment.gem_power.type")
                .withStyle(ChatFormatting.GOLD));

        float power = 1.0f;
        AugmentItem.AugmentData data = null;

        ResourceLocation id = AugmentItem.getAugmentId(stack);
        if (id != null) {
            data = AugmentItem.getAugmentData(id);
        }
        if (data == null) {
            data = AugmentItem.getAugmentData(GEM_POWER_ID);
        }

        if (data != null) {
            power = data.getPower();
        }

        tooltip.add(Component.literal("• ")
                .withStyle(ChatFormatting.YELLOW)
                .append(Component.translatable("fallen_gems_affixes.augment.gem_power.desc", power)
                        .withStyle(ChatFormatting.YELLOW)));
    }

    public static class GemPowerData implements IAugmentInnerData {
        private float power;

        public float getPower() {
            return power;
        }

        @Override
        public void enable() {

        }

        @Override
        public void disable() {

        }

        @Override
        public boolean isFunctional() {
            return true;
        }

        @Override
        public MutableComponent combineText() {
            return Component.translatable(Fallen.Augments.GEM_POWER.getDescString(), power);
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putFloat("power", power);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            power = tag.getFloat("power");
        }
    }

    @Override
    public String toString() {
        return "GemPowerAugment{" + "id=" + augmentId() + "}";
    }
}