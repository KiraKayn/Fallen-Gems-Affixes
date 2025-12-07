package net.kayn.fallen_gems_affixes.item.augments;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class AugmentItem extends Item {

    public AugmentItem(Properties properties) {
        super(properties);
    }

    /** Create stack with a specific augment ID in NBT */
    public static ItemStack createStack(Item item, String augmentId) {
        ItemStack stack = new ItemStack(item);
        stack.getOrCreateTag().putString("fallen_gems_affixes:augment_id", augmentId);
        return stack;
    }

    /** Read augment ID from a stack */
    public static String getAugmentId(ItemStack stack) {
        if (stack.hasTag()) {
            assert stack.getTag() != null;
            if (stack.getTag().contains("fallen_gems_affixes:augment_id")) {
                return stack.getTag().getString("fallen_gems_affixes:augment_id");
            }
        }
        return "";
    }
}
