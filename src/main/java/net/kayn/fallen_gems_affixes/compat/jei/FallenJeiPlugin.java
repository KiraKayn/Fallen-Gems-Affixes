package net.kayn.fallen_gems_affixes.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.item.augments.AugmentItem;
import net.kayn.fallen_gems_affixes.recipe.TransmutationRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

@JeiPlugin
public class FallenJeiPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(FallenGemsAffixes.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration reg) {
        reg.addRecipeCategories(new FallenSmithingCategory(reg.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration reg) {
        FallenSmithingCategory.registerExtension(TransmutationRecipe.class, new TransmutationExtension());

        List<SmithingRecipe> recipes = Minecraft.getInstance().level.getRecipeManager()
                .getAllRecipesFor(RecipeType.SMITHING)
                .stream()
                .filter(TransmutationRecipe.class::isInstance)
                .map(SmithingRecipe.class::cast)
                .toList();

        reg.addRecipes(FallenSmithingCategory.RECIPE_TYPE, recipes);
    }

    @Override
    public void onRuntimeAvailable(mezz.jei.api.runtime.IJeiRuntime jeiRuntime) {
        List<SmithingRecipe> recipesToHide = Minecraft.getInstance().level.getRecipeManager()
                .getAllRecipesFor(RecipeType.SMITHING)
                .stream()
                .filter(TransmutationRecipe.class::isInstance)
                .map(SmithingRecipe.class::cast)
                .toList();

        jeiRuntime.getRecipeManager().hideRecipes(mezz.jei.api.constants.RecipeTypes.SMITHING, recipesToHide);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration reg) {
        reg.addRecipeCatalyst(new ItemStack(Blocks.SMITHING_TABLE), FallenSmithingCategory.RECIPE_TYPE);
    }

    static class AugmentSubtypes implements IIngredientSubtypeInterpreter<ItemStack> {
        @Override
        public String apply(ItemStack stack, UidContext context) {
            ResourceLocation augmentId = AugmentItem.getAugmentId(stack);
            if (augmentId == null) {
                return ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
            }
            return augmentId.toString();
        }
    }
}