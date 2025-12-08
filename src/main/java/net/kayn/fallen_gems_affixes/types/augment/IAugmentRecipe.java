package net.kayn.fallen_gems_affixes.types.augment;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import net.kayn.fallen_gems_affixes.Fallen;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.SmithingRecipe;

import static net.kayn.fallen_gems_affixes.Fallen.AugmentMisc.AUGMENTS;

public interface IAugmentRecipe extends SmithingRecipe {
    /**
     * Get the augment data in the ingredient.
     */
    default ListTag getAugmentData(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(Fallen.AugmentMisc.AUGMENT_DATA)) {
            return stack.getTagElement(Fallen.AugmentMisc.AUGMENT_DATA).getList(AUGMENTS, Tag.TAG_COMPOUND);
        }
        else {
            return new ListTag();
        }
    }
    default ItemStack addAugmentData() {
        return new ItemStack(Items.DIAMOND);
    };
}
