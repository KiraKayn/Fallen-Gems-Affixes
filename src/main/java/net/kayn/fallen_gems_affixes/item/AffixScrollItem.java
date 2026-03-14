package net.kayn.fallen_gems_affixes.item;

import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AffixScrollItem extends Item {

    public static final String TAG_AFFIX_ID    = "fga:scroll_affix_id";
    public static final String TAG_AFFIX_RARITY = "fga:scroll_affix_rarity";
    public static final String TAG_AFFIX_LEVEL  = "fga:scroll_affix_level";

    public AffixScrollItem(Properties properties) {
        super(properties);
    }

    public static ItemStack createScroll(ResourceLocation affixId, LootRarity rarity, float level) {
        ItemStack stack = new ItemStack(net.kayn.fallen_gems_affixes.registry.ModItems.AFFIX_SCROLL.get());
        stack.getOrCreateTag().putString(TAG_AFFIX_ID, affixId.toString());
        stack.getOrCreateTag().putString(TAG_AFFIX_RARITY, RarityRegistry.INSTANCE.getKey(rarity).toString());
        stack.getOrCreateTag().putFloat(TAG_AFFIX_LEVEL, level);
        return stack;
    }

    @Nullable
    public static ResourceLocation getAffixId(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains(TAG_AFFIX_ID)) return null;
        return ResourceLocation.tryParse(stack.getTag().getString(TAG_AFFIX_ID));
    }

    @Nullable
    public static LootRarity getAffixRarity(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains(TAG_AFFIX_RARITY)) return null;
        try {
            return RarityRegistry.byLegacyId(stack.getTag().getString(TAG_AFFIX_RARITY)).get();
        } catch (Exception e) {
            return null;
        }
    }

    public static float getAffixLevel(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains(TAG_AFFIX_LEVEL)) return 0.5f;
        return stack.getTag().getFloat(TAG_AFFIX_LEVEL);
    }

    public static boolean hasAffix(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(TAG_AFFIX_ID);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return hasAffix(stack);
    }

    @Override
    public Component getName(ItemStack stack) {
        LootRarity rarity = getAffixRarity(stack);
        if (rarity != null) {
            return Component.translatable("item.fallen_gems_affixes.affix_scroll")
                    .withStyle(Style.EMPTY.withColor(rarity.getColor()));
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        ResourceLocation affixId = getAffixId(stack);
        LootRarity rarity = getAffixRarity(stack);
        float affixLevel = getAffixLevel(stack);

        if (affixId != null && rarity != null) {
            Affix affix = AffixRegistry.INSTANCE.getValue(affixId);
            if (affix != null) {
                var desc = affix.getDescription(stack, rarity, affixLevel);
                if (desc.getString().isEmpty()) {
                    desc = (net.minecraft.network.chat.MutableComponent) affix.getAugmentingText(stack, rarity, affixLevel);
                    desc.getSiblings().clear();
                }
                tooltip.add(desc.withStyle(ChatFormatting.GRAY));
            } else {
                tooltip.add(Component.literal(affixId.toString()).withStyle(ChatFormatting.DARK_GRAY));
            }
            tooltip.add(Component.translatable("rarity." + RarityRegistry.INSTANCE.getKey(rarity))
                    .withStyle(Style.EMPTY.withColor(rarity.getColor())));
        } else {
            tooltip.add(Component.translatable("item.fallen_gems_affixes.affix_scroll.empty")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }

        tooltip.add(Component.translatable("item.fallen_gems_affixes.affix_scroll.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }
}