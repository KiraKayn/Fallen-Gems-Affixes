package net.kayn.fallen_gems_affixes.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryExtension;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.level.block.Blocks;

import java.util.IdentityHashMap;
import java.util.Map;

public class FallenSmithingCategory implements IRecipeCategory<SmithingRecipe> {

    public static final RecipeType<SmithingRecipe> RECIPE_TYPE = RecipeType.create(FallenGemsAffixes.MOD_ID, "smithing", SmithingRecipe.class);

    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/gui/container/smithing.png");

    private static final Map<Class<? extends SmithingRecipe>, Extension<SmithingRecipe>> EXTENSIONS = new IdentityHashMap<>();

    private final IDrawable background;
    private final IDrawable icon;

    public FallenSmithingCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(TEXTURE, 0, 40, 120, 30);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(Blocks.SMITHING_TABLE));
    }

    @Override
    public RecipeType<SmithingRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.fallen_gems_affixes.transmutation");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SmithingRecipe recipe, IFocusGroup focuses) {
        if (EXTENSIONS.containsKey(recipe.getClass())) {
            EXTENSIONS.get(recipe.getClass()).setRecipe(builder, recipe, focuses);
        }
    }

    @Override
    public void draw(SmithingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics gfx, double mouseX, double mouseY) {
        if (EXTENSIONS.containsKey(recipe.getClass())) {
            EXTENSIONS.get(recipe.getClass()).draw(recipe, recipeSlotsView, gfx, mouseX, mouseY);
        }
    }

    @Override
    public boolean isHandled(SmithingRecipe recipe) {
        return EXTENSIONS.containsKey(recipe.getClass());
    }


    public interface Extension<R extends SmithingRecipe> extends IRecipeCategoryExtension {
        void setRecipe(IRecipeLayoutBuilder builder, R recipe, IFocusGroup focuses);

        default void draw(R recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics gfx, double mouseX, double mouseY) {}
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <R extends SmithingRecipe> void registerExtension(Class<R> clazz, Extension<R> ext) {
        EXTENSIONS.put(clazz, (Extension) ext);
    }
}