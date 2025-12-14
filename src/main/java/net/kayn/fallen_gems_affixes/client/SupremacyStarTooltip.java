package net.kayn.fallen_gems_affixes.client;

import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AttributeAffix;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.augment.SupremacyAugment;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber(modid = FallenGemsAffixes.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class SupremacyStarTooltip {

    private static final String FABLED_KEY = "fallen_gems_affixes:fabled";

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        CompoundTag root = stack.getTag();
        if (root == null || !root.getBoolean(FABLED_KEY)) return;

        List<Component> tooltip = event.getToolTip();
        if (tooltip == null || tooltip.isEmpty()) return;
        Set<String> boostedDescriptions = new HashSet<>();
        Set<String> boostedAttributes = new HashSet<>();

        AffixHelper.streamAffixes(stack).forEach(inst -> {
            if (inst.level() > SupremacyAugment.STANDARD_MAX_LEVEL) {
                Affix affix = inst.affix().get();

                if (affix instanceof AttributeAffix) {
                    Component augText = affix.getAugmentingText(
                            inst.stack(),
                            inst.rarity().get(),
                            inst.level()
                    );
                    if (augText != null && !augText.getString().isEmpty()) {
                        String attrText = augText.getString().trim();
                        boostedAttributes.add(attrText);
                    }
                } else {
                    Component desc = affix.getDescription(
                            inst.stack(),
                            inst.rarity().get(),
                            inst.level()
                    );
                    if (desc != null && !desc.getString().isEmpty()) {
                        boostedDescriptions.add(desc.getString().trim());
                    }
                }
            }
        });

        if (boostedDescriptions.isEmpty() && boostedAttributes.isEmpty()) return;
        for (int i = 0; i < tooltip.size(); i++) {
        }
        for (int i = 0; i < tooltip.size(); i++) {
            Component line = tooltip.get(i);
            String lineStr = line.getString().trim();

            if (lineStr.isEmpty()) continue;

            boolean shouldStar = false;
            String cleanLine = stripFormattingChars(lineStr);
            if (lineStr.startsWith("•")) {
                for (String boostedDesc : boostedDescriptions) {
                    String cleanDesc = stripFormattingChars(boostedDesc);
                    if (cleanLine.equals(cleanDesc)) {
                        shouldStar = true;
                        break;
                    }
                }
            }
            else {
                for (String attrText : boostedAttributes) {
                    String cleanAttr = stripFormattingChars(attrText);
                    if (cleanLine.contains(cleanAttr) || cleanAttr.contains(cleanLine)) {
                        shouldStar = true;
                        break;
                    }
                }
            }

            if (shouldStar) {
                tooltip.set(i, starPrefix(line));
            }
        }
    }

    private static String stripFormattingChars(String s) {
        return s.replaceAll("§.", "")
                .replace("• ", "")
                .replace("★ ", "")
                .replace("+", "")
                .replace("-", "")
                .trim();
    }

    private static MutableComponent starPrefix(Component component) {
        return Component.literal("★ ").withStyle(ChatFormatting.DARK_RED).append(component.copy());
    }
}