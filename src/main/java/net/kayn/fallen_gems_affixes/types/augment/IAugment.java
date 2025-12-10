package net.kayn.fallen_gems_affixes.types.augment;

import net.kayn.fallen_gems_affixes.attachment.AugmentInstance;
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

public interface IAugment {
    ResourceLocation AUGMENT_ICON = new ResourceLocation("fallen_gems_affixes", "textures/gui/augment_socket.png");

    Component TEXT = Component.literal("Empty Augment");

//    static Component getTooltip(CompoundTag tag) {
//        return Component.translatable(tag.getString(Fallen.AugmentMisc.TYPE));
//    }


    //Every augment must have a unique ID.
     // Used for serialization

    ResourceLocation getId();

    default IAugmentInnerData parse(CompoundTag augmentData) {
        if (!needsInstance()) {
            return null;
        }
        return IAugmentInnerData.EMPTY;
    }

    boolean isUnique();

    boolean needsInstance();

    default AugmentInstance createInstanceFromStack(ItemStack stack) {
        if (!needsInstance()) {
            return null;
        }
        return new AugmentInstance();
    }

//    TooltipComponent createTooltipComponent(CompoundTag tag);

    void renderImage(@NotNull Font font, int x, int y, GuiGraphics gui, IAugmentInnerData innerData);

//    default void renderText(Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource bufferSource, IAugmentInnerData innerData) {
//        font.drawInBatch(organizeTooltipText(innerData), x + 12, y + 1, 0xAABBCC, true, matrix, bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
//    };

    default MutableComponent organizeTooltipText(IAugmentInnerData innerData) {
        return Component.translatable(this.getId().toString());
    };

    default void appendItemTooltip(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
    }

    default String getDescString() {
        return "desc." + this.getId().toString();
    }

    IAugmentInnerData deserializeInnerData(CompoundTag tag);
}