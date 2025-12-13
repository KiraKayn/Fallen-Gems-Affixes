package net.kayn.fallen_gems_affixes.attachment;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
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
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import javax.annotation.ParametersAreNonnullByDefault;

import java.util.HashSet;
import java.util.Set;

import static net.kayn.fallen_gems_affixes.Fallen.AugmentMisc.*;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AugmentRecipe extends SmithingTransformRecipe implements IAugmentRecipe {
    private static final ResourceLocation ID = new ResourceLocation("fallen_gems_affixes:add_augment");

    public AugmentRecipe() {
        super(ID, Ingredient.EMPTY, Ingredient.EMPTY, Ingredient.of(ModItems.AUGMENT_ITEM.get()), ItemStack.EMPTY);
    }

    @Override
    public boolean matches(Container inv, Level level) {
        ItemStack base = inv.getItem(1).copy();
        ItemStack augmentItem = inv.getItem(2).copy();

        LootCategory cat = LootCategory.forItem(base);

        if (!categoryMatches(augmentItem, cat)) {
            return false;
        }

        return AugmentSlotHelper.hasAvailableSlots(base);
    }

    @Override
    public ItemStack assemble(Container inv, RegistryAccess access) {
        ItemStack result = inv.getItem(1).copy();
        result.setCount(1);
        ItemStack augmentItem = inv.getItem(2).copy();
        augmentItem.setCount(1);
        return addAugmentData(result, augmentItem);
    }

    private boolean categoryMatches(ItemStack augmentItem, LootCategory cat) {
        if (augmentItem.hasTag()) {
            assert augmentItem.getTag() != null;
            if (augmentItem.getTag().contains(Fallen.AugmentMisc.AUGMENT_DATA)) {
                CompoundTag augmentData = augmentItem.getTagElement(Fallen.AugmentMisc.AUGMENT_DATA);
                assert augmentData != null;
                ListTag categories = augmentData.getList(CATEGORIES, Tag.TAG_STRING);
                if (categories.isEmpty()) return true;
                for (Tag catName : categories) {
                    String category = catName.getAsString();
                    LootCategory cat1 = LootCategory.byId(category);
                    if (cat.equals(cat1)) return true;
                }
            }
        }
        return false;
    }

    private ItemStack addAugmentData(ItemStack result, ItemStack augmentItem) {
        if (!AugmentSlotHelper.hasAvailableSlots(result)) {
            return result;
        }

        // Get the AugmentData from AugmentItem
        ListTag ingredientAugments = getAugmentData(augmentItem);
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

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return pWidth * pHeight >= 2;
    }

    @Override
    public boolean isBaseIngredient(ItemStack pStack) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess regs) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(Blocks.SMITHING_TABLE);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeType.SMITHING;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Fallen.RecipeSerializers.ADD_AUGMENT.get();
    }
}