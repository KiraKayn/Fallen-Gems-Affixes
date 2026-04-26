package net.kayn.fallen_gems_affixes.recipe;

import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.attachment.augment.AugmentSlotHelper;
import net.kayn.fallen_gems_affixes.registry.ModItems;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class SeveranceRecipe extends SmithingTransformRecipe {

    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "severance");

    public SeveranceRecipe() {
        super(ID, Ingredient.EMPTY, Ingredient.EMPTY, Ingredient.of(ModItems.SIGIL_OF_SEVERANCE.get()), ItemStack.EMPTY);
    }

    @Override
    public boolean matches(Container inv, Level level) {
        ItemStack base = inv.getItem(1);
        ItemStack sigil = inv.getItem(2);

        if (base.isEmpty()) return false;
        if (!sigil.is(ModItems.SIGIL_OF_SEVERANCE.get())) return false;

        return AugmentSlotHelper.getAugmentCount(base) > 0;
    }

    @Override
    public ItemStack assemble(Container inv, RegistryAccess regs) {
        ItemStack out = inv.getItem(1).copy();
        if (out.isEmpty()) return ItemStack.EMPTY;

        int slots = AugmentSlotHelper.getAugmentSlots(out);
        out.getOrCreateTag().remove(Fallen.AugmentMisc.AUGMENT_DATA);
        out.getOrCreateTag().remove(Fallen.AugmentMisc.FABLED_TAG);
        AugmentSlotHelper.setAugmentSlots(out, slots);

        return out;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Fallen.RecipeSerializers.SEVERANCE.get();
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
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
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
}