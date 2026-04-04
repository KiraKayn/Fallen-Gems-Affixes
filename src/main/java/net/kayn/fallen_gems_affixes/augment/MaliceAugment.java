package net.kayn.fallen_gems_affixes.augment;

import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.DurableAffix;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.item.augments.AugmentItem;
import net.kayn.fallen_gems_affixes.types.augment.IAugment;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentInnerData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class MaliceAugment implements IAugment {

    public static final ResourceLocation MALICE_ID =
            new ResourceLocation(FallenGemsAffixes.MOD_ID, "malice");

    private static final String AFFIX_DATA  = "affix_data";
    private static final String AFFIXES_KEY = "affixes";
    public  static final float  MAX_AFFIX_LEVEL = SupremacyAugment.MAX_AFFIX_LEVEL;

    private static final Random RANDOM = new Random();

    public static ResourceLocation augmentId() { return MALICE_ID; }

    @Override public ResourceLocation getId()   { return MALICE_ID; }
    @Override public boolean isUnique()          { return true; }
    @Override public boolean needsInstance()     { return false; }


    @Override
    public IAugmentInnerData deserializeInnerData(CompoundTag tag) {
        MaliceData data = new MaliceData();
        data.deserializeNBT(tag);
        return data;
    }

    @Override
    public void renderImage(@NotNull Font font, int x, int y, GuiGraphics gui,
                            IAugmentInnerData innerData) {
        gui.blit(IAugment.AUGMENT_ICON, x, y, 0, 0, 0, 9, 9, 9, 9);
        ItemStack displayStack = AugmentItem.createAugment(MALICE_ID);
        var pose = gui.pose();
        pose.pushPose();
        pose.translate(x, y, 0);
        pose.scale(0.5F, 0.5F, 1);
        gui.renderFakeItem(displayStack, 0, 0);
        pose.popPose();
    }


    @Override
    public MutableComponent organizeTooltipText(IAugmentInnerData innerData) {
        if (innerData instanceof MaliceData data && data.revealed) {
            if (data.affixDominant) {
                return Component.translatable(
                        "fallen_gems_affixes.augment.malice.socket_desc.affix",
                        String.format("%.1f", data.affixPower),
                        String.format("%.1f", data.gemPower)
                ).withStyle(ChatFormatting.YELLOW);
            } else {
                return Component.translatable(
                        "fallen_gems_affixes.augment.malice.socket_desc.gem",
                        String.format("%.1f", data.gemPower),
                        String.format("%.1f", data.affixPower)
                ).withStyle(ChatFormatting.YELLOW);
            }
        }
        return Component.translatable("fallen_gems_affixes.augment.malice.socket_desc.hidden")
                .withStyle(ChatFormatting.GOLD);
    }

    @Override
    public void appendItemTooltip(ItemStack stack, @Nullable Level level,
                                  List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("fallen_gems_affixes.augment.malice.type")
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("• ")
                .withStyle(ChatFormatting.YELLOW)
                .append(Component.translatable("fallen_gems_affixes.augment.malice.desc.chance")
                        .withStyle(ChatFormatting.YELLOW)));
        tooltip.add(Component.literal("  → ")
                .withStyle(ChatFormatting.YELLOW)
                .append(Component.translatable("fallen_gems_affixes.augment.malice.desc.option_a")
                        .withStyle(ChatFormatting.YELLOW)));
        tooltip.add(Component.literal("  → ")
                .withStyle(ChatFormatting.YELLOW)
                .append(Component.translatable("fallen_gems_affixes.augment.malice.desc.option_b")
                        .withStyle(ChatFormatting.YELLOW)));
    }

    public static void ensurePendingRoll(ItemStack augmentItem) {
        if (augmentItem.isEmpty()) return;
        CompoundTag tag = augmentItem.getTag();
        if (tag == null || !tag.contains(Fallen.AugmentMisc.AUGMENT_DATA)) return;
        ListTag list = tag.getCompound(Fallen.AugmentMisc.AUGMENT_DATA)
                .getList(Fallen.AugmentMisc.AUGMENTS, Tag.TAG_COMPOUND);
        boolean isMalice = false;
        for (int i = 0; i < list.size(); i++) {
            if (MALICE_ID.toString().equals(list.getCompound(i).getString(Fallen.AugmentMisc.TYPE))) {
                isMalice = true;
                break;
            }
        }
        if (!isMalice) return;
        if (!tag.contains("malice_pending_dominant")) {
            tag.putBoolean("malice_pending_dominant", RANDOM.nextBoolean());
        }
    }

    public static void applyFromPendingRoll(ItemStack result, ItemStack augmentItem) {
        CompoundTag augTag = augmentItem.getTag();

        boolean affixDominant = (augTag != null && augTag.contains("malice_pending_dominant"))
                ? augTag.getBoolean("malice_pending_dominant")
                : RANDOM.nextBoolean();

        float dominantPower = 2.0f;
        float penaltyPower  = 0.5f;
        if (augTag != null && augTag.contains(Fallen.AugmentMisc.AUGMENT_DATA)) {
            ListTag list = augTag.getCompound(Fallen.AugmentMisc.AUGMENT_DATA)
                    .getList(Fallen.AugmentMisc.AUGMENTS, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);
                if (!MALICE_ID.toString().equals(entry.getString(Fallen.AugmentMisc.TYPE))) continue;
                CompoundTag inner = entry.getCompound(Fallen.AugmentMisc.INNER_DATA);
                if (inner.contains("powerBoost")) dominantPower = inner.getFloat("powerBoost");
                if (inner.contains("powerNerf"))  penaltyPower  = inner.getFloat("powerNerf");
                break;
            }
        }

        float affixPower = affixDominant ? dominantPower : penaltyPower;
        float gemPower   = affixDominant ? penaltyPower  : dominantPower;

        CompoundTag tag = result.getTag();
        if (tag == null || !tag.contains(Fallen.AugmentMisc.AUGMENT_DATA)) return;
        CompoundTag augmentData = tag.getCompound(Fallen.AugmentMisc.AUGMENT_DATA);
        ListTag augments = augmentData.getList(Fallen.AugmentMisc.AUGMENTS, Tag.TAG_COMPOUND);
        for (int i = 0; i < augments.size(); i++) {
            CompoundTag entry = augments.getCompound(i);
            if (!MALICE_ID.toString().equals(entry.getString(Fallen.AugmentMisc.TYPE))) continue;
            CompoundTag inner = entry.getCompound(Fallen.AugmentMisc.INNER_DATA);
            inner.putBoolean("affixDominant", affixDominant);
            inner.putFloat("affixPower",      affixPower);
            inner.putFloat("gemPower",        gemPower);
            inner.putBoolean("revealed",      false);
            entry.put(Fallen.AugmentMisc.INNER_DATA, inner);
            break;
        }
        augmentData.put(Fallen.AugmentMisc.AUGMENTS, augments);
        tag.put(Fallen.AugmentMisc.AUGMENT_DATA, augmentData);

        applyAffixPower(result, affixPower);
    }

    public static void revealIfPending(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(Fallen.AugmentMisc.AUGMENT_DATA)) return;
        CompoundTag augmentData = tag.getCompound(Fallen.AugmentMisc.AUGMENT_DATA);
        ListTag augments = augmentData.getList(Fallen.AugmentMisc.AUGMENTS, Tag.TAG_COMPOUND);
        for (int i = 0; i < augments.size(); i++) {
            CompoundTag entry = augments.getCompound(i);
            if (!MALICE_ID.toString().equals(entry.getString(Fallen.AugmentMisc.TYPE))) continue;
            CompoundTag inner = entry.getCompound(Fallen.AugmentMisc.INNER_DATA);
            if (!inner.contains("affixPower") || inner.getBoolean("revealed")) break;
            inner.putBoolean("revealed", true);
            entry.put(Fallen.AugmentMisc.INNER_DATA, inner);
            augmentData.put(Fallen.AugmentMisc.AUGMENTS, augments);
            tag.put(Fallen.AugmentMisc.AUGMENT_DATA, augmentData);
            break;
        }
    }

    public static void applyAffixPower(ItemStack stack, float multiplier) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(AFFIX_DATA)) return;

        CompoundTag affixData = tag.getCompound(AFFIX_DATA);
        if (!affixData.contains(AFFIXES_KEY)) return;

        Set<String>  durableKeys = getDurableAffixKeys(stack);
        CompoundTag  affixes     = affixData.getCompound(AFFIXES_KEY);
        CompoundTag  newAffixes  = new CompoundTag();

        for (String key : affixes.getAllKeys()) {
            float base = affixes.getFloat(key);
            if (durableKeys.contains(key)) {
                newAffixes.putFloat(key, base);
            } else {
                newAffixes.putFloat(key, Mth.clamp(base * multiplier, 0f, MAX_AFFIX_LEVEL));
            }
        }

        affixData.put(AFFIXES_KEY, newAffixes);
        tag.put(AFFIX_DATA, affixData);
    }

    public static boolean hasRevealedMalice(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(Fallen.AugmentMisc.AUGMENT_DATA)) return false;
        ListTag list = tag.getCompound(Fallen.AugmentMisc.AUGMENT_DATA)
                .getList(Fallen.AugmentMisc.AUGMENTS, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            if (!MALICE_ID.toString().equals(entry.getString(Fallen.AugmentMisc.TYPE))) continue;
            return entry.getCompound(Fallen.AugmentMisc.INNER_DATA).getBoolean("revealed");
        }
        return false;
    }

    private static Set<String> getDurableAffixKeys(ItemStack stack) {
        Set<String> keys = new HashSet<>();
        Map<DynamicHolder<? extends Affix>, AffixInstance> affixes = AffixHelper.getAffixes(stack);
        if (affixes == null) return keys;
        for (var entry : affixes.entrySet()) {
            if (entry.getValue().affix().get() instanceof DurableAffix)
                keys.add(entry.getKey().getId().toString());
        }
        return keys;
    }


    @Nullable
    public static Float getMaliceGemPower(ItemStack stack) {
        return readInnerFloat(stack, "gemPower");
    }

    @Nullable
    public static Float getMaliceAffixPower(ItemStack stack) {
        return readInnerFloat(stack, "affixPower");
    }

    @Nullable
    private static Float readInnerFloat(ItemStack stack, String key) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(Fallen.AugmentMisc.AUGMENT_DATA)) return null;

        ListTag list = tag.getCompound(Fallen.AugmentMisc.AUGMENT_DATA)
                .getList(Fallen.AugmentMisc.AUGMENTS, Tag.TAG_COMPOUND);

        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            if (!MALICE_ID.toString().equals(entry.getString(Fallen.AugmentMisc.TYPE))) continue;
            CompoundTag inner = entry.getCompound(Fallen.AugmentMisc.INNER_DATA);
            if (inner.contains(key)) return inner.getFloat(key);
        }
        return null;
    }


    public static class MaliceData implements IAugmentInnerData {
        boolean affixDominant = true;
        float   affixPower    = 2.0f;
        float   gemPower      = 0.5f;
        boolean revealed      = false;

        @Override public void enable()              {}
        @Override public void disable()             {}
        @Override public boolean isFunctional()     { return true; }

        @Override
        public MutableComponent combineText() {
            if (!revealed)
                return Component.translatable("fallen_gems_affixes.augment.malice.socket_desc.hidden");
            if (affixDominant)
                return Component.translatable("fallen_gems_affixes.augment.malice.socket_desc.affix",
                        String.format("%.1f", affixPower), String.format("%.1f", gemPower));
            return Component.translatable("fallen_gems_affixes.augment.malice.socket_desc.gem",
                    String.format("%.1f", gemPower), String.format("%.1f", affixPower));
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("affixDominant", affixDominant);
            tag.putFloat("affixPower",      affixPower);
            tag.putFloat("gemPower",        gemPower);
            tag.putBoolean("revealed",      revealed);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            affixDominant = tag.getBoolean("affixDominant");
            affixPower    = tag.getFloat("affixPower");
            gemPower      = tag.getFloat("gemPower");
            revealed      = tag.getBoolean("revealed");
        }
    }

    @Override
    public String toString() {
        return "MaliceAugment{id=" + augmentId() + "}";
    }
}