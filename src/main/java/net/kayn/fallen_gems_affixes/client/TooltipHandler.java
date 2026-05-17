package net.kayn.fallen_gems_affixes.client;

import net.kayn.fallen_gems_affixes.adventure.set.SetAffixHelper;
import net.kayn.fallen_gems_affixes.adventure.set.SetAffixInstance;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = "fallen_gems_affixes", value = Dist.CLIENT)
public class TooltipHandler {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!SetAffixHelper.hasSetAffix(stack)) return;

        SetAffixInstance inst = SetAffixHelper.getSetAffixInstance(stack).orElse(null);
        if (inst == null || !inst.isValid()) return;

        List<Component> tooltip = event.getToolTip();

        MutableComponent bullet = Component.literal(" \u2022 ").withStyle(ChatFormatting.DARK_GRAY);
        MutableComponent rawDesc = Component.empty().append(inst.getDescription()).withStyle(ChatFormatting.DARK_RED);
        MutableComponent setAffixLine = bullet.copy().append(rawDesc);

        tooltip.removeIf(line -> line.getString().equals(setAffixLine.getString()));

        int insertPos = Math.min(1, tooltip.size());
        tooltip.add(insertPos, setAffixLine);

        ResourceLocation setId = inst.afx().getSetId();
        String setKey = setId.toString().replace(":", ".");
        int[] thresholds = inst.afx().getBonusThresholds();
        int maxPieces = thresholds.length > 0 ? thresholds[thresholds.length - 1] : 5;

        int pieceCount = 0;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ResourceLocation equippedSetId = SetAffixHelper.getSetId(player.getItemBySlot(slot));
                if (setId.equals(equippedSetId)) pieceCount++;
            }
        }
        final int finalPieceCount = pieceCount;

        tooltip.add(Component.empty());

        MutableComponent setHeader = Component.translatable("set." + setKey)
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD)
                .append(Component.literal(" (" + finalPieceCount + "/" + maxPieces + ")")
                        .withStyle(ChatFormatting.GRAY));
        tooltip.add(setHeader);

        for (int threshold : thresholds) {
            boolean unlocked = finalPieceCount >= threshold;
            MutableComponent icon = Component.literal(unlocked ? " \u2714 " : " \u2718 ")
                    .withStyle(unlocked ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY);
            MutableComponent tierDesc = Component.translatable("set_bonus." + setKey + "." + threshold)
                    .withStyle(unlocked ? ChatFormatting.GRAY : ChatFormatting.DARK_GRAY);
            MutableComponent tierLine = icon.append(
                    Component.literal("(" + threshold + ") ").withStyle(ChatFormatting.DARK_GRAY)
            ).append(tierDesc);
            tooltip.add(tierLine);
        }
    }
}