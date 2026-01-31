package net.kayn.fallen_gems_affixes.recipe;

import com.google.gson.JsonObject;
import dev.shadowsoffire.apotheosis.adventure.AdventureModule.ApothSmithingRecipe;
import dev.shadowsoffire.apotheosis.adventure.socket.ReactiveSmithingRecipe;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.registry.ModItems;
import net.kayn.fallen_gems_affixes.attachment.AugmentSlotHelper;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class SeveranceRecipe extends ApothSmithingRecipe implements ReactiveSmithingRecipe {

    private static final ResourceLocation ID = new ResourceLocation("fallen_gems_affixes:severance");

    public SeveranceRecipe() {
        super(ID, Ingredient.EMPTY, Ingredient.of(ModItems.SIGIL_OF_SEVERANCE.get()), ItemStack.EMPTY);
    }

    @Override
    public boolean matches(Container inv, Level level) {
        ItemStack base = inv.getItem(BASE);
        ItemStack sigil = inv.getItem(ADDITION);

        if (base.isEmpty()) return false;
        if (sigil.getItem() != ModItems.SIGIL_OF_SEVERANCE.get()) return false;

        return AugmentSlotHelper.getAugmentCount(base) > 0;
    }

    @Override
    public ItemStack assemble(Container inv, RegistryAccess regs) {
        ItemStack out = inv.getItem(BASE).copy();
        if (out.isEmpty()) return ItemStack.EMPTY;

        int slots = AugmentSlotHelper.getAugmentSlots(out);
        out.getOrCreateTag().remove(Fallen.AugmentMisc.AUGMENT_DATA);
        AugmentSlotHelper.setAugmentSlots(out, slots);

        return out;
    }
    @Override
    public void onCraft(Container inv, Player player, ItemStack output) {
        ItemStack base = inv.getItem(BASE);
        if (base.hasTag() && base.getTag().contains(Fallen.AugmentMisc.AUGMENT_DATA)) {
            CompoundTag augmentRoot = base.getTagElement(Fallen.AugmentMisc.AUGMENT_DATA);
            ListTag augments = augmentRoot.getList(Fallen.AugmentMisc.AUGMENTS, Tag.TAG_COMPOUND);
            for (int i = 0; i < augments.size(); i++) {
                try {
                    ItemStack stack = ItemStack.of(augments.getCompound(i));
                    if (!stack.isEmpty()) {
                        if (!player.addItem(stack)) {
                            Block.popResource(player.level(), player.blockPosition(), stack);
                        }
                    }
                } catch (Exception ignored) {}
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

    public static class Serializer implements RecipeSerializer<SeveranceRecipe> {

        public static final Serializer INSTANCE = new Serializer();

        @Override
        public SeveranceRecipe fromJson(ResourceLocation id, JsonObject json) {
            return new SeveranceRecipe();
        }

        @Override
        public SeveranceRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            return new SeveranceRecipe();
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, SeveranceRecipe recipe) {
        }
    }
}