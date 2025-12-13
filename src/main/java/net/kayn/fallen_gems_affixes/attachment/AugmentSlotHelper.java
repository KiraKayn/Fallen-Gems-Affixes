package net.kayn.fallen_gems_affixes.attachment;

import net.kayn.fallen_gems_affixes.Fallen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;


public class AugmentSlotHelper {

    public static final String AUGMENT_SLOTS = "augment_slots";
    public static final int MAX_AUGMENT_SLOTS = 2;

    public static int getAugmentSlots(ItemStack stack) {
        if (!stack.hasTag()) {
            return 0;
        }
        CompoundTag augmentData = stack.getTagElement(Fallen.AugmentMisc.AUGMENT_DATA);
        return augmentData != null ? augmentData.getInt(AUGMENT_SLOTS) : 0;
    }

    public static void setAugmentSlots(ItemStack stack, int slots) {
        stack.getOrCreateTagElement(Fallen.AugmentMisc.AUGMENT_DATA).putInt(AUGMENT_SLOTS, Math.min(slots, MAX_AUGMENT_SLOTS));
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

    public static int getAugmentCount(ItemStack stack) {
        if (!stack.hasTag() || !Objects.requireNonNull(stack.getTag()).contains(Fallen.AugmentMisc.AUGMENT_DATA)) {
            return 0;
        }
        CompoundTag augmentData = stack.getTag().getCompound(Fallen.AugmentMisc.AUGMENT_DATA);
        ListTag augments = augmentData.getList(Fallen.AugmentMisc.AUGMENTS, 10);
        return augments.size();
    }

    public static boolean canAddMoreSlots(ItemStack stack) {
        return getAugmentSlots(stack) < MAX_AUGMENT_SLOTS;
    }

    public static int getEmptySlots(ItemStack stack) {
        return getAugmentSlots(stack) - getAugmentCount(stack);
    }
}