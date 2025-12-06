package net.kayn.fallen_gems_affixes.attachment;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.augment.AugmentRegistry;
import net.kayn.fallen_gems_affixes.types.augment.IAugment;
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
    final ResourceLocation id;
    final ResourceLocation augmentId;
    final ItemStack augmentItem;
    final Set<LootCategory> validCategories;

    public AugmentRecipe(ResourceLocation id, ResourceLocation augmentId, ItemStack augmentItem, Set<LootCategory> validCategories) {
        this.id = id;
        this.augmentId = augmentId;
        this.augmentItem = augmentItem;
        this.validCategories = validCategories;
    }

    @Override
    public boolean matches(Container inv, Level level) {
        ItemStack base = inv.getItem(1);
        ItemStack addition = inv.getItem(2);

        if (base.isEmpty() || addition.isEmpty()) return false;

        if (!ItemStack.isSameItem(addition, augmentItem)) return false;

        if (!validCategories.isEmpty()) {
            LootCategory category = LootCategory.forItem(base);
            return validCategories.contains(category);
        }

        return true;
    }

    @Override
    public ItemStack assemble(Container inv, RegistryAccess access) {
        ItemStack result = inv.getItem(1).copy();
        return addAugmentData(result);
    }

    private ItemStack addAugmentData(ItemStack result) {
        CompoundTag tag = result.getOrCreateTag();
        CompoundTag augmentData = tag.getCompound(Fallen.AugmentMisc.AUGMENT_DATA);
        ListTag augments = augmentData.getList(AUGMENTS, Tag.TAG_COMPOUND);

        Set<String> existingTypes = new HashSet<>();
        for (Tag t : augments) {
            if (t instanceof CompoundTag c && c.contains(TYPE)) {
                existingTypes.add(c.getString(TYPE));
            }
        }

        IAugment augment = AugmentRegistry.get(augmentId);
        String typeString = augmentId.toString();

        if (!existingTypes.contains(typeString) || (augment != null && !augment.isUnique())) {
            CompoundTag augmentTag = new CompoundTag();
            augmentTag.putString(TYPE, typeString);
            augments.add(augmentTag);
        }

        augmentData.put(AUGMENTS, augments);
        tag.put(Fallen.AugmentMisc.AUGMENT_DATA, augmentData);
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) {
        return true;
    }

    @Override
    public boolean isTemplateIngredient(ItemStack pStack) {
        return pStack.isEmpty();
    }

    @Override
    public boolean isBaseIngredient(ItemStack pStack) {
        if (pStack.isEmpty()) return false;

        if (!validCategories.isEmpty()) {
            LootCategory category = LootCategory.forItem(pStack);
            return validCategories.contains(category);
        }

        return true;
    }

    @Override
    public boolean isAdditionIngredient(ItemStack pStack) {
        return ItemStack.isSameItem(pStack, augmentItem);
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Fallen.RecipeSerializers.ADD_AUGMENT.get();
    }

    public ResourceLocation getAugmentId() {
        return augmentId;
    }

    public Set<LootCategory> getValidCategories() {
        return validCategories;
    }
}