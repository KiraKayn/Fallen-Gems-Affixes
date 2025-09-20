package net.kayn.fallen_gems_affixes.client.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class AugmentClientTooltipComponent implements ClientTooltipComponent {

    private static final ResourceLocation AUGMENT_ICON =
            new ResourceLocation("fallen_gems_affixes", "textures/gui/augment_socket.png");

    private static final Component TEXT =
            Component.literal("Empty Augment");

    private final int spacing = Minecraft.getInstance().font.lineHeight + 2;

    public AugmentClientTooltipComponent(AugmentTooltipComponent ignored) {
    }

    @Override
    public int getHeight() {
        return spacing;
    }

    @Override
    public int getWidth(Font font) {
        return font.width(TEXT) + 12; // text + icon spacing
    }

    @Override
    public void renderImage(@NotNull Font font, int x, int y, GuiGraphics gui) {
        RenderSystem.enableBlend();
        gui.blit(AUGMENT_ICON, x, y, 0, 0, 0, 9, 9, 9, 9);
    }

    @Override
    public void renderText(Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource bufferSource) {
        font.drawInBatch(TEXT, x + 12, y + 1, 0xAABBCC, true, matrix, bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
    }
}