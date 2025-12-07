package net.kayn.fallen_gems_affixes.augment;

import com.mojang.blaze3d.vertex.PoseStack;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.attachment.AugmentInstance;
import net.kayn.fallen_gems_affixes.types.augment.IAugment;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentInnerData;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

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
        return true;
    }

    @Override
    public AugmentInstance createInstanceFromStack(ItemStack stack) {
        return IAugment.super.createInstanceFromStack(stack);
    }

    @Override
    public void renderImage(@NotNull Font font, int x, int y, GuiGraphics gui, IAugmentInnerData innerData) {
        gui.blit(IAugment.AUGMENT_ICON, x, y, 0, 0, 0, 9, 9, 9, 9);
        PoseStack pose = gui.pose();
        pose.pushPose();
        pose.scale(0.5F, 0.5F, 1);
        gui.renderFakeItem(new ItemStack(Items.DIAMOND),2 * x + 1, 2 * y + 1);
        pose.popPose();
    }

    @Override
    public MutableComponent organizeTooltipText(IAugmentInnerData innerData) {
        return innerData.combineText();
    }

    @Override
    public IAugmentInnerData deserializeInnerData(CompoundTag tag) {
        IAugmentInnerData augmentInnerData = new GemPowerData();
        augmentInnerData.deserializeNBT(tag);
        return augmentInnerData;
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
}
