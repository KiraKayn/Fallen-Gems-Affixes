package net.kayn.fallen_gems_affixes.types.augment;

import net.kayn.fallen_gems_affixes.Fallen;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.SmithingRecipe;

import java.util.Objects;

import static net.kayn.fallen_gems_affixes.Fallen.AugmentMisc.AUGMENTS;

public interface IAugmentRecipe extends SmithingRecipe {
    /**
     * Get the augment data in the ingredient.
     */
    default ListTag getAugmentData(ItemStack stack) {
        if (stack.hasTag() && Objects.requireNonNull(stack.getTag()).contains(Fallen.AugmentMisc.AUGMENT_DATA)) {
            return Objects.requireNonNull(stack.getTagElement(Fallen.AugmentMisc.AUGMENT_DATA)).getList(AUGMENTS, Tag.TAG_COMPOUND);
        }
        else {
            return new ListTag();
        }
    }
    default ItemStack addAugmentData() {
        return new ItemStack(Items.DIAMOND);
    };
}
