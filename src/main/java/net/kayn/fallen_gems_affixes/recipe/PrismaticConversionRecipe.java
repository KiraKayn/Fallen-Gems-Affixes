package net.kayn.fallen_gems_affixes.recipe;

import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.adventure.socket.CatalystSocketConfig;
import net.kayn.fallen_gems_affixes.adventure.socket.CatalystSocketHelper;
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

public class PrismaticConversionRecipe extends SmithingTransformRecipe {

    private static final ResourceLocation ID = ResourceLocation.parse("fallen_gems_affixes:prismatic_conversion");

    public PrismaticConversionRecipe() {
        super(ID, Ingredient.EMPTY, Ingredient.EMPTY, Ingredient.of(ModItems.SIGIL_OF_PRISMATIC_CONVERSION.get()), ItemStack.EMPTY);
    }

    @Override
    public boolean matches(Container inv, Level level) {
        ItemStack base  = inv.getItem(1);
        ItemStack sigil = inv.getItem(2);

        if (!sigil.is(ModItems.SIGIL_OF_PRISMATIC_CONVERSION.get())) return false;
        if (base.isEmpty()) return false;
        if (CatalystSocketHelper.hasCatalystSocket(base)) return false;
        if (SocketHelper.getSockets(base) <= 0) return false;

        for (var gem : SocketHelper.getGems(base).gems()) {
            if (gem.isValid()) return false;
        }

        return true;
    }

    @Override
    public ItemStack assemble(Container inv, RegistryAccess access) {
        ItemStack base   = inv.getItem(1);
        ItemStack result = base.copy();
        result.setCount(1);

        int totalSockets = SocketHelper.getSockets(result);
        float power = CatalystSocketConfig.INSTANCE.getPowerPerSocket();

        CatalystSocketHelper.apply(result, totalSockets, power);
        return result;
    }

    @Override public boolean canCraftInDimensions(int w, int h) { return w * h >= 2; }
    @Override public ItemStack getResultItem(RegistryAccess a) { return ItemStack.EMPTY; }
    @Override public ItemStack getToastSymbol() { return new ItemStack(Blocks.SMITHING_TABLE); }
    @Override public ResourceLocation getId() { return ID; }
    @Override public RecipeType<?> getType() { return RecipeType.SMITHING; }
    @Override public boolean isSpecial() { return true; }
    @Override public RecipeSerializer<?> getSerializer() { return Fallen.RecipeSerializers.PRISMATIC_CONVERSION.get(); }
}