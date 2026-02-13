package net.kayn.fallen_gems_affixes.recipe;

import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.attachment.AugmentSlotHelper;
import net.kayn.fallen_gems_affixes.registry.ModItems;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class SeveranceRecipe extends SmithingTransformRecipe {

    private static final ResourceLocation ID = new ResourceLocation("fallen_gems_affixes:severance");

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
        AugmentSlotHelper.setAugmentSlots(out, slots);

        return out;
    }

    public void onCraft(Container inv, Player player, ItemStack output) {
        ItemStack base = inv.getItem(1);
        if (base.hasTag() && base.getTag().contains(Fallen.AugmentMisc.AUGMENT_DATA)) {
            CompoundTag augmentRoot = base.getTagElement(Fallen.AugmentMisc.AUGMENT_DATA);
            if (augmentRoot != null) {
                ListTag augments = augmentRoot.getList(Fallen.AugmentMisc.AUGMENTS, Tag.TAG_COMPOUND);
                for (int i = 0; i < augments.size(); i++) {
                    try {
                        ItemStack stack = ItemStack.of(augments.getCompound(i));
                        if (!stack.isEmpty()) {
                            if (!player.addItem(stack)) {
                                Block.popResource(player.level(), player.blockPosition(), stack);
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }
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
