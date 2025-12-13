package net.kayn.fallen_gems_affixes.recipe;

import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.attachment.AugmentSlotHelper;
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

public class SocketConversionRecipe extends SmithingTransformRecipe {

    private static final ResourceLocation ID = new ResourceLocation("fallen_gems_affixes:socket_conversion");

    public SocketConversionRecipe() {
        super(ID, Ingredient.EMPTY, Ingredient.EMPTY, Ingredient.of(ModItems.SIGIL_OF_ASCENSION.get()), ItemStack.EMPTY);
    }

    @Override
    public boolean matches(Container inv, Level level) {
        ItemStack base = inv.getItem(1);
        ItemStack sigil = inv.getItem(2);

        // Must be the sigil item
        if (!sigil.is(ModItems.SIGIL_OF_ASCENSION.get())) return false;

        // Must have at least 1 gem socket to convert
        int gemSockets = SocketHelper.getSockets(base);
        if (gemSockets <= 0) return false;

        // Check if all sockets are filled with gems
        int filledSockets = 0;
        for (var gem : SocketHelper.getGems(base).gems()) {
            if (gem.isValid()) {
                filledSockets++;
            }
        }

        // Must have at least 1 empty socket (no gems added)
        int emptySockets = gemSockets - filledSockets;
        if (emptySockets <= 0) return false;

        // Must not be at max augment slots already
        return AugmentSlotHelper.canAddMoreSlots(base);
    }

    @Override
    public ItemStack assemble(Container inv, RegistryAccess access) {
        ItemStack base = inv.getItem(1);
        ItemStack result = base.copy();
        result.setCount(1);

        // Remove 1 gem socket
        int currentGemSockets = SocketHelper.getSockets(result);
        SocketHelper.setSockets(result, currentGemSockets - 1);

        // Add 1 augment slot using helper
        AugmentSlotHelper.addAugmentSlots(result, 1);

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
        return Fallen.RecipeSerializers.SOCKET_CONVERSION.get();
    }
}