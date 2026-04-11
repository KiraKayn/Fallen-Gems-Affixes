package net.kayn.fallen_gems_affixes.adventure.socket.gem.storage;

import com.google.common.base.Strings;
import dev.shadowsoffire.apotheosis.adventure.affix.salvaging.SalvagingScreen;
import dev.shadowsoffire.apotheosis.adventure.client.GrayBufferSource;
import dev.shadowsoffire.apotheosis.adventure.client.SimpleTexButton;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.Nullable;
import java.util.*;

public class GemCaseScreen extends AbstractContainerScreen<GemCaseMenu> {

    public static final ResourceLocation TEXTURES = new ResourceLocation(FallenGemsAffixes.MOD_ID, "textures/gui/gem_case.png");

    public static final int MAX_ROWS = 3;
    public static final int SLOTS_PER_ROW = 6;

    private static final int MAX_VISIBLE_RARITIES = 6;

    protected float scrollOffs;
    protected boolean scrolling;
    protected int startIndex;

    private int rarityScrollOffset = 0;

    public List<SafeSlot> data = new ArrayList<>();

    private final List<SimpleTexButton> upgradeButtons = new ArrayList<>();

    private final List<Component> upgradeButtonLabels = new ArrayList<>();

    @Nullable
    protected EditBox filter;

    public GemCaseScreen(GemCaseMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageHeight = 230;
        containerChanged();
        menu.setNotifier(this::containerChanged);
    }

    @Override
    protected void init() {
        super.init();

        filter = addRenderableWidget(new EditBox(font, getGuiLeft() + 16, getGuiTop() + 16, 110, 11, filter, Component.literal("")));
        filter.setBordered(false);
        filter.setTextColor(0x97714F);
        filter.setResponder(t -> containerChanged());
        setFocused(filter);

        for (int i = 0; i < MAX_ROWS * SLOTS_PER_ROW; i++) {
            addRenderableWidget(new GemCaseSelectButton(this, i, getGuiLeft() + 21 + (i % SLOTS_PER_ROW) * 18, getGuiTop() + 31 + (i / SLOTS_PER_ROW) * 19));
        }

        upgradeButtons.clear();
        upgradeButtonLabels.clear();
        List<DynamicHolder<LootRarity>> rarities = menu.getOrderedRarities();
        for (int i = 0; i < rarities.size() - 1; i++) {
            final DynamicHolder<LootRarity> fromRarity = rarities.get(i);
            final DynamicHolder<LootRarity> toRarity = rarities.get(i + 1);
            final int fromOrdinal = fromRarity.get().ordinal();

            upgradeButtonLabels.add(Component.translatable("button.fallen_gems_affixes.gem_case.upgrade", fromRarity.get().toComponent(), toRarity.get().toComponent()));

            SimpleTexButton btn = new SimpleTexButton(getGuiLeft() + 30, getGuiTop() + 109, 16, 16, 291, 29, TEXTURES, 307, 256, b -> {
                boolean shift = Screen.hasShiftDown();
                int id = fromOrdinal | (shift ? 0x1000 : 0);
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
            }, CommonComponents.EMPTY);
            btn.visible = false;
            upgradeButtons.add(btn);
            addRenderableWidget(btn);
        }

        containerChanged();
    }

    private void updateRarityView() {
        int slotIdx = 0;
        for (Slot slot : menu.slots) {
            if (!(slot instanceof GemCaseSlot)) continue;
            int visPos = slotIdx - rarityScrollOffset;

            if (visPos >= 0 && visPos < MAX_VISIBLE_RARITIES) {
                moveSlot(slot, 21 + visPos * 18);
            } else {
                moveSlot(slot, -1000);
            }
            slotIdx++;
        }
        List<DynamicHolder<LootRarity>> rarities = menu.getOrderedRarities();
        for (int i = 0; i < upgradeButtons.size(); i++) {
            int visPos = i - rarityScrollOffset;
            boolean vis = visPos >= 0 && visPos < MAX_VISIBLE_RARITIES - 1;
            SimpleTexButton btn = upgradeButtons.get(i);
            btn.visible = vis;
            if (vis) {
                btn.setX(getGuiLeft() + 30 + visPos * 18);
            }
            if (i < rarities.size() - 1) {
                btn.active = menu.getUpgradeMatch(rarities.get(i)) != null;
            }
        }
    }

    private void moveSlot(Slot slot, int x) {
        try {
            java.lang.reflect.Field xField = Slot.class.getDeclaredField("x");
            java.lang.reflect.Field yField = Slot.class.getDeclaredField("y");
            xField.setAccessible(true);
            yField.setAccessible(true);
            xField.setInt(slot, x);
            yField.setInt(slot, 91);
        } catch (Exception e) {
            FallenGemsAffixes.LOGGER.error("Failed to move slot via reflection", e);
        }
    }

    private boolean canScrollLeft() {
        return rarityScrollOffset > 0;
    }

    private boolean canScrollRight() {
        return rarityScrollOffset + MAX_VISIBLE_RARITIES < menu.getOrderedRarities().size();
    }

    private boolean isOverRarityScrollArea(double mx, double my) {
        int x0 = leftPos + 14;
        int x1 = leftPos + 14 + MAX_VISIBLE_RARITIES * 18 + 4;
        int y0 = topPos + 83;
        int y1 = topPos + 127;
        return mx >= x0 && mx < x1 && my >= y0 && my < y1;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (minecraft.options.keyInventory.matches(keyCode, scanCode) && getFocused() == filter) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        renderBackground(gfx);
        super.render(gfx, mouseX, mouseY, partialTicks);

        for (Slot slot : menu.slots) {
            if (!(slot instanceof GemCaseSlot gss)) continue;
            if (slot.x < 0) continue;

            Gem gem = menu.selectedGem;
            if (gem == null) continue;
            int count = menu.getGemCount(gem, gss.rarity);
            if (count <= 1) continue;

            String countStr = GemCaseBlock.format(count);
            float scale = countStr.length() > 2 ? 2.0f / countStr.length() : 1.0f;
            gfx.pose().pushPose();
            gfx.pose().scale(scale, scale, 1);
            gfx.pose().translate(0, 0, 300);
            float tx = (leftPos + slot.x + 16 - (font.width(countStr) - 1) * scale) / scale;
            float ty = (topPos + slot.y + 16 - (font.lineHeight - 2) * scale) / scale;
            gfx.drawString(font, countStr, (int) tx, (int) ty, 0xFFFFFF, true);
            gfx.pose().popPose();
        }

        if (canScrollLeft()) {
            gfx.drawString(font, "◄", leftPos + 13, topPos + 93, 0xFFAAAAAA, false);
        }
        if (canScrollRight()) {
            gfx.drawString(font, "►", leftPos + 21 + MAX_VISIBLE_RARITIES * 18 + 1, topPos + 93, 0xFFAAAAAA, false);
        }

        renderTooltip(gfx, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partial, int mouseX, int mouseY) {
        int left = leftPos, top = topPos;
        gfx.blit(TEXTURES, left, top, 0, 0, imageWidth, imageHeight, 307, 256);
        int scrollbarPos = (int) (90F * scrollOffs);
        gfx.blit(TEXTURES, left + 13, top + 29 + scrollbarPos, 303, isScrollBarActive() ? 0 : 12, 4, 12, 307, 256);
        gfx.blit(TEXTURES, left - 65, top + 16, 198, 0, 65, 193, 307, 256);

        Gem selected = getSelectedGem();
        if (selected != null) {
            List<DynamicHolder<LootRarity>> rarities = menu.getOrderedRarities();
            for (int i = 0; i < rarities.size(); i++) {
                int visPos = i - rarityScrollOffset;
                if (visPos < 0 || visPos >= MAX_VISIBLE_RARITIES) continue;
                DynamicHolder<LootRarity> r = rarities.get(i);
                if (!r.get().isAtLeast(selected.getMinRarity())) continue;
                if (!r.get().isAtMost(selected.getMaxRarity())) continue;
                if (menu.getGemCount(selected, r) > 0) continue;
                ItemStack ghost = GemRegistry.createGemStack(selected, r.get());
                SalvagingScreen.renderGuiItem(gfx, ghost, left + 21 + visPos * 18, top + 91, GrayBufferSource::new);
            }
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics gfx, int mouseX, int mouseY) {
        super.renderTooltip(gfx, mouseX, mouseY);

        for (int i = 0; i < upgradeButtons.size(); i++) {
            SimpleTexButton btn = upgradeButtons.get(i);
            if (!btn.visible || !btn.isHovered()) continue;

            List<DynamicHolder<LootRarity>> rarities = menu.getOrderedRarities();
            DynamicHolder<LootRarity> from = rarities.get(i);
            GemUpgradeMatch match = menu.getUpgradeMatch(from);
            List<Component> tooltip = new ArrayList<>();

            if (btn.active && match != null) {
                tooltip.add(upgradeButtonLabels.get(i));
                ItemStack dustStack = menu.upgradeMatInv.getItem(match.dustSlot());
                ItemStack matStack = menu.upgradeMatInv.getItem(match.matSlot());
                tooltip.add(Component.translatable("button.fallen_gems_affixes.gem_case.upgrade_cost", match.dustCost(), dustStack.getHoverName(), match.matCost(), matStack.getHoverName()));
                if (Screen.hasShiftDown()) {
                    tooltip.add(Component.translatable("button.fallen_gems_affixes.gem_case.upgrade_all").withStyle(ChatFormatting.YELLOW));
                }
            } else {
                tooltip.add(upgradeButtonLabels.get(i).copy().withStyle(ChatFormatting.GRAY));
                Gem selected = getSelectedGem();
                if (selected != null && menu.getGemCount(selected, from) < 2) {
                    tooltip.add(Component.translatable("button.fallen_gems_affixes.gem_case.upgrade_no_gems").withStyle(ChatFormatting.RED));
                } else {
                    tooltip.add(Component.translatable("button.fallen_gems_affixes.gem_case.upgrade_no_materials").withStyle(ChatFormatting.RED));
                }
            }

            gfx.renderComponentTooltip(font, tooltip, mouseX, mouseY);
            return;
        }
        Gem selected = getSelectedGem();
        if (selected == null) return;
        List<DynamicHolder<LootRarity>> rarities = menu.getOrderedRarities();
        for (int i = 0; i < rarities.size(); i++) {
            int visPos = i - rarityScrollOffset;
            if (visPos < 0 || visPos >= MAX_VISIBLE_RARITIES) continue;
            DynamicHolder<LootRarity> r = rarities.get(i);
            if (!r.get().isAtLeast(selected.getMinRarity())) continue;
            if (!r.get().isAtMost(selected.getMaxRarity())) continue;
            if (menu.getGemCount(selected, r) != 0) continue;

            int slotScreenX = getGuiLeft() + 21 + visPos * 18;
            int slotScreenY = getGuiTop() + 91;
            if (isHovering(21 + visPos * 18, 91, 16, 16, mouseX, mouseY) && menu.getCarried().isEmpty()) {
                ItemStack ghost = GemRegistry.createGemStack(selected, r.get());
                List<Component> tooltip = new ArrayList<>();
                tooltip.add(ghost.getHoverName());
                tooltip.add(Component.translatable("tooltip.fallen_gems_affixes.gem_case.none_owned").withStyle(ChatFormatting.RED));
                tooltip.add(CommonComponents.EMPTY);
                ghost.getItem().appendHoverText(ghost, null, tooltip, TooltipFlag.Default.NORMAL);
                gfx.renderComponentTooltip(font, tooltip, mouseX, mouseY);
                return;
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
    }

    @Override
    public int getSlotColor(int index) {
        return 0x40FFFFFF;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        scrolling = false;
        if (isHovering(14, 29, 4, 103, mx, my)) {
            scrolling = true;
            mouseDragged(mx, my, button, 0, 0);
            return true;
        }
        if (filter != null && filter.isHovered() && button == org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            filter.setValue("");
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (scrolling && isScrollBarActive()) {
            int barTop = topPos + 14, barBot = barTop + 103;
            scrollOffs = Mth.clamp(((float) my - barTop - 6F) / (barBot - barTop - 12F) - 0.12F, 0F, 1F);
            startIndex = (int) (scrollOffs * getOffscreenRows() + 0.5);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        if (isOverRarityScrollArea(mx, my)) {
            int maxOffset = Math.max(0, menu.getOrderedRarities().size() - MAX_VISIBLE_RARITIES);
            int newOffset = Mth.clamp(rarityScrollOffset + (delta > 0 ? 1 : -1), 0, maxOffset);
            if (newOffset != rarityScrollOffset) {
                rarityScrollOffset = newOffset;
                updateRarityView();
            }
            return true;
        }
        if (isScrollBarActive()) {
            scrollOffs = Mth.clamp((float) (scrollOffs - delta / getOffscreenRows()), 0F, 1F);
            startIndex = (int) (scrollOffs * getOffscreenRows() + 0.5);
        }
        return true;
    }

    private void containerChanged() {
        data.clear();
        for (Gem gem : GemRegistry.INSTANCE.getValues()) {
            ItemStack display = GemRegistry.createGemStack(gem, gem.getMinRarity());
            data.add(new SafeSlot(gem, menu.getGemCount(gem), display));
        }
        data = filter(data);

        if (!isScrollBarActive()) {
            scrollOffs = 0;
            startIndex = 0;
        }

        data.sort(Comparator.<SafeSlot, Boolean>comparing(s -> s.count() <= 0).thenComparing(s -> GemRegistry.INSTANCE.getKey(s.gem()).toString()));

        if (!upgradeButtons.isEmpty()) {
            updateRarityView();
        }
    }

    private List<SafeSlot> filter(List<SafeSlot> list) {
        list.removeIf(s -> !isAllowedByItem(s) || !isAllowedBySearch(s));
        return list;
    }

    private boolean isAllowedByItem(SafeSlot slot) {
        ItemStack stack = menu.ioInv.getItem(1);
        if (stack.isEmpty()) return true;
        LootCategory cat = LootCategory.forItem(stack);
        if (cat.isNone()) return false;
        return slot.gem().getBonus(cat, slot.gem().getMinRarity()).isPresent();
    }

    private boolean isAllowedBySearch(SafeSlot slot) {
        String name = slot.displayStack().getDisplayName().getString().toLowerCase(Locale.ROOT);
        String search = filter == null ? "" : filter.getValue().trim().toLowerCase(Locale.ROOT);
        return Strings.isNullOrEmpty(search) || ChatFormatting.stripFormatting(name).contains(search);
    }

    @Nullable
    public Gem getSelectedGem() {
        return menu.selectedGem;
    }

    private boolean isScrollBarActive() {
        return data.size() > MAX_ROWS * SLOTS_PER_ROW;
    }

    private int getOffscreenRows() {
        return (int) Math.ceil((data.size() - MAX_ROWS * SLOTS_PER_ROW) / (float) SLOTS_PER_ROW);
    }

    public static void handleSelectedGem(DynamicHolder<Gem> gem) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof GemCaseScreen screen) {
            screen.menu.setSelectedGem(gem);
        }
    }

    public record SafeSlot(Gem gem, int count, ItemStack displayStack) {
    }
}