package net.kayn.fallen_gems_affixes.attachment;

import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.config.ModConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class AugmentSlotHelper {

    public static final String AUGMENT_SLOTS = "augment_slots";

    public static int getAugmentSlots(ItemStack stack) {
        if (!stack.hasTag()) {
            return 0;
        }
        CompoundTag augmentData = stack.getTagElement(Fallen.AugmentMisc.AUGMENT_DATA);
        return augmentData != null ? augmentData.getInt(AUGMENT_SLOTS) : 0;
    }

    public static void setAugmentSlots(ItemStack stack, int slots) {
        int max = ModConfig.MAX_AUGMENT_SLOTS.get();
        stack.getOrCreateTagElement(Fallen.AugmentMisc.AUGMENT_DATA)
                .putInt(AUGMENT_SLOTS, Math.min(slots, max));
    }

    public static void addAugmentSlots(ItemStack stack, int amount) {
        int current = getAugmentSlots(stack);
        setAugmentSlots(stack, current + amount);
    }

    public static boolean hasAvailableSlots(ItemStack stack) {
        int current = getAugmentCount(stack);
        int max = getAugmentSlots(stack);
        return current < max;
    }

    @SuppressWarnings("ConstantConditions")
    public static int getAugmentCount(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains(Fallen.AugmentMisc.AUGMENT_DATA)) {
            return 0;
        }
        CompoundTag augmentData = stack.getTag().getCompound(Fallen.AugmentMisc.AUGMENT_DATA);
        ListTag augments = augmentData.getList(Fallen.AugmentMisc.AUGMENTS, 10);
        return augments.size();
    }

    public static boolean canAddMoreSlots(ItemStack stack) {
        return getAugmentSlots(stack) < ModConfig.MAX_AUGMENT_SLOTS.get();
    }

    public static int getEmptySlots(ItemStack stack) {
        int max = ModConfig.MAX_AUGMENT_SLOTS.get();
        return Mth.clamp(getAugmentSlots(stack) - getAugmentCount(stack), 0, max);
    }
}