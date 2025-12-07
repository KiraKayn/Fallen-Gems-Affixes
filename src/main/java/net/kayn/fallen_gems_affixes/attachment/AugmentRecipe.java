package net.kayn.fallen_gems_affixes.attachment;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.item.augments.AugmentItem;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentRecipe;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.Set;

import static net.kayn.fallen_gems_affixes.Fallen.AugmentMisc.*;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AugmentRecipe implements IAugmentRecipe {

    private final ResourceLocation augmentId;
    private final ItemStack addition;
    private final Set<LootCategory> validCategories;

    public AugmentRecipe(ResourceLocation augmentId, ItemStack addition, Set<LootCategory> validCategories) {
        this.augmentId = augmentId;
        this.addition = addition;
        this.validCategories = validCategories;
    }

    public ItemStack getAddition() { return addition; }
    public Set<LootCategory> getValidCategories() { return validCategories; }
    public ResourceLocation getAugmentId() { return augmentId; }

    @Override
    public boolean matches(Container inv, Level level) {
        ItemStack base = inv.getItem(1);
        ItemStack add = inv.getItem(2);
        return isBaseIngredient(base) && isAdditionIngredient(add);
    }

    @Override
    public ItemStack assemble(Container inv, RegistryAccess access) {
        return addAugmentData(inv.getItem(1).copy(), inv.getItem(2));
    }

    private ItemStack addAugmentData(ItemStack result, ItemStack addition) {
        if (addition.isEmpty()) return result;

        CompoundTag resultTag = result.getOrCreateTag();
        CompoundTag resultAugmentData = resultTag.getCompound(AUGMENT_DATA);

        CompoundTag additionTag = addition.getTag();
        ListTag additionAugments = (additionTag != null && additionTag.contains(AUGMENT_DATA))
                ? additionTag.getCompound(AUGMENT_DATA).getList(AUGMENTS, Tag.TAG_COMPOUND)
                : new ListTag();

        ListTag resultAugments = resultAugmentData.getList(AUGMENTS, Tag.TAG_COMPOUND);
        Set<String> existing = new HashSet<>();

        for (Tag t : resultAugments) {
            if (t instanceof CompoundTag c && c.contains(TYPE)) existing.add(c.getString(TYPE));
        }
        for (Tag t : additionAugments) {
            if (t instanceof CompoundTag c && c.contains(TYPE)) {
                if (!existing.contains(c.getString(TYPE))) resultAugments.add(c.copy());
            }
        }

        resultAugmentData.put(AUGMENTS, resultAugments);
        resultTag.put(AUGMENT_DATA, resultAugmentData);
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() { return Fallen.RecipeSerializers.ADD_AUGMENT.get(); }

    @Override
    public boolean isTemplateIngredient(ItemStack stack) { return stack.isEmpty(); }

    @Override
    public boolean isBaseIngredient(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (!validCategories.isEmpty()) {
            LootCategory category = LootCategory.forItem(stack);
            return validCategories.contains(category);
        }
        return true;
    }

    @Override
    public boolean isAdditionIngredient(ItemStack stack) {
        return stack.getItem() instanceof AugmentItem &&
                AugmentItem.getAugmentId(stack).equals(augmentId.toString());
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) { return ItemStack.EMPTY; }

    @Override
    public ResourceLocation getId() { return augmentId; }
}