package net.kayn.fallen_gems_affixes.recipe;

import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.adventure.socket.SocketTierManager;
import net.kayn.fallen_gems_affixes.adventure.socket.TieredSocketHelper;
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

public class ElevationRecipe extends SmithingTransformRecipe {

    private static final ResourceLocation ID = ResourceLocation.parse("fallen_gems_affixes:elevation");

    public ElevationRecipe() {
        super(ID, Ingredient.EMPTY, Ingredient.EMPTY,
                Ingredient.of(ModItems.SIGIL_OF_ELEVATION.get()),
                ItemStack.EMPTY);
    }

    @Override
    public boolean matches(Container inv, Level level) {
        ItemStack base  = inv.getItem(1);
        ItemStack sigil = inv.getItem(2);

        if (!sigil.is(ModItems.SIGIL_OF_ELEVATION.get())) return false;
        if (base.isEmpty()) return false;

        int maxOrdinal = SocketTierManager.INSTANCE.getMaxOrdinal();
        if (maxOrdinal < 0) return false;

        int[] tiers = TieredSocketHelper.getSocketTiers(base);
        for (int tier : tiers) {
            if (tier >= 0 && tier < maxOrdinal) return true;
        }
        return false;
    }

    @Override
    public ItemStack assemble(Container inv, RegistryAccess access) {
        ItemStack base   = inv.getItem(1);
        ItemStack result = base.copy();
        result.setCount(1);

        int maxOrdinal = SocketTierManager.INSTANCE.getMaxOrdinal();
        int[] tiers = TieredSocketHelper.getSocketTiers(result);

        for (int i = 0; i < tiers.length; i++) {
            int tier = tiers[i];
            if (tier >= 0 && tier < maxOrdinal) {
                tiers[i] = tier + 1;
            }
        }

        TieredSocketHelper.setSocketTiers(result, tiers);
        return result;
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
        return Fallen.RecipeSerializers.SOCKET_ELEVATION.get();
    }
}