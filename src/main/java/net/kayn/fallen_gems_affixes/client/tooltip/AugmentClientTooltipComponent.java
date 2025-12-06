package net.kayn.fallen_gems_affixes.client.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import net.kayn.fallen_gems_affixes.types.augment.IAugment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.Objects;

public class AugmentClientTooltipComponent implements ClientTooltipComponent {
    private final AugmentTooltipComponent tooltip;
    private final Component actualComponent;

//    private static final ResourceLocation AUGMENT_ICON = IAugment.AUGMENT_ICON;
//
//    private static final Component TEXT = IAugment.TEXT;

    private final int spacing = Minecraft.getInstance().font.lineHeight + 2;

    public AugmentClientTooltipComponent(AugmentTooltipComponent tooltip) {
        this.tooltip = tooltip;
        if (tooltip.augment() != null) {
            this.actualComponent = tooltip.augment().organizeTooltipText(tooltip.augmentInnerData());
        } else {
            this.actualComponent = null;
        }
    }

    @Override
    public int getHeight() {
        return spacing;
    }

    @Override
    public int getWidth(Font font) {
        return font.width(Objects.requireNonNullElse(actualComponent, IAugment.TEXT)) + 12; // text + icon spacing
    }

    @Override
    public void renderImage(@NotNull Font font, int x, int y, GuiGraphics gui) {
        RenderSystem.enableBlend();
        if (tooltip.augment() != null) {
            tooltip.augment().renderImage(font, x, y, gui, tooltip.augmentInnerData());
        }
        else {
            gui.blit(IAugment.AUGMENT_ICON, x, y, 0, 0, 0, 9, 9, 9, 9);
        }
    }

    @Override
    public void renderText(Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource bufferSource) {
        if (tooltip.augment() != null) {
            font.drawInBatch(actualComponent, x + 12, y + 1, 0xAABBCC, true, matrix, bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
        }
        else {
            font.drawInBatch(IAugment.TEXT, x + 12, y + 1, 0xAABBCC, true, matrix, bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
        }
    }
}