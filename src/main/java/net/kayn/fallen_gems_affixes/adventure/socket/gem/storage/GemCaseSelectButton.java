package net.kayn.fallen_gems_affixes.adventure.socket.gem.storage;

import dev.shadowsoffire.apotheosis.adventure.affix.salvaging.SalvagingScreen;
import dev.shadowsoffire.apotheosis.adventure.client.GrayBufferSource;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.kayn.fallen_gems_affixes.adventure.socket.gem.storage.GemCaseScreen.SafeSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.function.Function;

public class GemCaseSelectButton extends AbstractButton {

    protected final GemCaseScreen screen;
    protected final int index;

    public GemCaseSelectButton(GemCaseScreen screen, int index, int x, int y) {
        super(x, y, 16, 16, CommonComponents.EMPTY);
        this.screen = screen;
        this.index = index;
    }

    @Override
    protected void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        SafeSlot slot = getSafeSlot();
        if (slot == null) return;

        Minecraft mc = Minecraft.getInstance();
        int count = screen.getMenu().getGemCount(slot.gem());
        Function<MultiBufferSource, MultiBufferSource> wrapper = count == 0 ? GrayBufferSource::new : Function.identity();

        SalvagingScreen.renderGuiItem(gfx, slot.displayStack(), getX(), getY(), wrapper);
        if (count > 1) {
            String countStr = GemCaseBlock.format(count);
            float scale = countStr.length() > 2 ? 2.0f / countStr.length() : 1.0f;
            gfx.pose().pushPose();
            gfx.pose().scale(scale, scale, 1);
            gfx.pose().translate(0, 0, 200);
            float textX = (getX() + 16 - (mc.font.width(countStr) - 1) * scale) / scale;
            float textY = (getY() + 16 - (mc.font.lineHeight - 2) * scale) / scale;
            gfx.drawString(mc.font, countStr, (int) textX, (int) textY, 0xAAFFFFFF, true);
            gfx.pose().popPose();
        }
        if (isHovered()) {
            gfx.pose().pushPose();
            gfx.pose().translate(0, 0, 200);
            gfx.fill(getX(), getY(), getX() + 16, getY() + 16, 0x40FFFFFF);
            gfx.pose().popPose();
            gfx.renderTooltip(mc.font, Component.translatable(slot.displayStack().getDescriptionId()), mouseX, mouseY);
        }
    }

    @Override
    public void onPress() {
        SafeSlot slot = getSafeSlot();
        if (slot == null) return;
        DynamicHolder<Gem> holder = GemRegistry.INSTANCE.holder(GemRegistry.INSTANCE.getKey(slot.gem()));
        screen.getMenu().setSelectedGem(holder);
        GemCaseNetwork.CHANNEL.sendToServer(new GemCaseNetwork.GemCaseSelectMessage(holder.getId()));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput out) {
    }

    @Nullable
    private SafeSlot getSafeSlot() {
        int idx = screen.startIndex * GemCaseScreen.SLOTS_PER_ROW + index;
        return (idx >= 0 && idx < screen.data.size()) ? screen.data.get(idx) : null;
    }
}