package net.kayn.fallen_gems_affixes.attachment;

import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.augment.AugmentRegistry;
import net.kayn.fallen_gems_affixes.registry.ModItems;
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
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.Set;

import static net.kayn.fallen_gems_affixes.Fallen.AugmentMisc.AUGMENTS;
import static net.kayn.fallen_gems_affixes.Fallen.AugmentMisc.TYPE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AugmentRecipe implements IAugmentRecipe {
    final ResourceLocation id;
    final Ingredient base;
    final Ingredient addition;

    public AugmentRecipe(ResourceLocation id, Ingredient base, Ingredient addition) {
        this.id = id;
        this.base = base;
        this.addition = addition;
    }

    @Override
    public boolean matches(Container inv, Level level) {
        return isBaseIngredient(inv.getItem(1)) && isAdditionIngredient(inv.getItem(2));
    }

    @Override
    public ItemStack assemble(Container inv, RegistryAccess access) {
        ItemStack result = inv.getItem(1).copy();
        ItemStack addition = inv.getItem(2).copy();
        return addAugmentData(result, addition);
    }

    /**
     * Add the augment data to the result item.
     */
    private ItemStack addAugmentData(ItemStack result, ItemStack addition) {
        ListTag ingredientAugments = getAugmentData(addition);
        if (ingredientAugments.isEmpty()) {
            return result;
        }

        // Operating item's nbt data.
        CompoundTag tag = result.getOrCreateTag();
        CompoundTag augmentData = tag.getCompound(Fallen.AugmentMisc.AUGMENT_DATA);
        ListTag augments = augmentData.getList(AUGMENTS, Tag.TAG_COMPOUND);

        // Create a set for overlapping types.
        Set<String> existingTypes = new HashSet<>();
        for (Tag t : augments) {
            if (t instanceof CompoundTag c && c.contains(TYPE)) {
                existingTypes.add(c.getString(TYPE));
            }
        }

        for (Tag t : ingredientAugments) {
            if (t instanceof CompoundTag c && c.contains(TYPE)) {
                String type = c.getString(TYPE);
                ResourceLocation loc = ResourceLocation.tryParse(type);
                IAugment augment = AugmentRegistry.get(loc);
                if (!existingTypes.contains(type) || !augment.isUnique()) {
                    augments.add(c.copy());
                    existingTypes.add(type);
                }
            }
        }

        // Finalize tag.
        augmentData.put(AUGMENTS, augments);
        tag.put(Fallen.AugmentMisc.AUGMENT_DATA, augmentData);
        return result;
    }

    /**
     * Get the augment data in the ingredient.
     */
    private ListTag getAugmentData(ItemStack stack) {
        // Testing with nether star, adding a soulbound augment.
        if (stack.is(ModItems.SOULBOUND_AUGMENT_ITEM.get())) {
            CompoundTag tag = stack.getOrCreateTag();
            CompoundTag augmentData = new CompoundTag();
            ListTag augments = new ListTag();
            CompoundTag augment1 = new CompoundTag();
            augment1.putString(TYPE, Fallen.Augments.SOUL_BOUND.getId().toString());
            augments.add(augment1);
            augmentData.put(AUGMENTS, augments);
            tag.put(Fallen.AugmentMisc.AUGMENT_DATA, augmentData);
            return augments;
        }

        if (stack.hasTag() && stack.getTag().contains(Fallen.AugmentMisc.AUGMENT_DATA)) {
            return stack.getTagElement(Fallen.AugmentMisc.AUGMENT_DATA).getList(AUGMENTS, Tag.TAG_COMPOUND);
        }
        else {
            return new ListTag();
        }
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) {
        return true;
    }

    @Override
    public boolean isTemplateIngredient(ItemStack pStack) {
        return false;
    }

    @Override
    public boolean isBaseIngredient(ItemStack pStack) {
        if (pStack == null) {
            return false;
        } else if (base.isEmpty()) {
            return pStack.isEmpty();
        } else {
            return true;
//            for(ItemStack itemstack : base.getItems()) {
//                if (itemstack.is(pStack.getItem())) {
//                    return true;
//                }
//            }
//
//            return false;
        }
//        return base.test(pStack);
    }

    @Override
    public boolean isAdditionIngredient(ItemStack pStack) {
        return addition.test(pStack);
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        return base.getItems().length > 0 ? base.getItems()[0].copy() : ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Fallen.RecipeSerializers.ADD_AUGMENT.get();
    }
}
