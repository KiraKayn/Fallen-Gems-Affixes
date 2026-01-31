package net.kayn.fallen_gems_affixes.recipe;

import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.registry.ModItems;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TransmutationRecipe extends SmithingTransformRecipe {

    private static final ResourceLocation ID = new ResourceLocation("fallen_gems_affixes:transmutation");

    private static Ingredient buildGearIngredient() {
        List<Item> gear = new ArrayList<>();
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            ItemStack s = new ItemStack(item);
            if (!LootCategory.forItem(s).isNone()) {
                gear.add(item);
            }
        }
        return gear.isEmpty() ? Ingredient.EMPTY : Ingredient.of(gear.toArray(new Item[0]));
    }

    public TransmutationRecipe() {
        super(ID, Ingredient.of(ModItems.SIGIL_OF_TRANSMUTATION.get()), buildGearIngredient(), buildGearIngredient(), ItemStack.EMPTY);
    }

    @Override
    public boolean matches(Container inv, Level level) {
        ItemStack sigil = inv.getItem(0);
        ItemStack base = inv.getItem(1);
        ItemStack source = inv.getItem(2);

        if (!sigil.is(ModItems.SIGIL_OF_TRANSMUTATION.get())) return false;

        if (base.isEmpty() || source.isEmpty()) return false;

        LootCategory baseCat = LootCategory.forItem(base);
        LootCategory srcCat = LootCategory.forItem(source);
        if (baseCat.isNone() || srcCat.isNone() || baseCat != srcCat) return false;

        if (AffixHelper.hasAffixes(base)) return false;
        if (SocketHelper.getSockets(base) > 0) return false;
        if (base.getTagElement(AffixHelper.AFFIX_DATA) != null) return false;
        if (base.getTag() != null && base.getTag().contains(Fallen.AugmentMisc.AUGMENT_DATA)) return false;

        boolean hasAffixes = AffixHelper.hasAffixes(source);
        boolean hasSockets = SocketHelper.getSockets(source) > 0;
        boolean hasAugments = source.getTagElement(Fallen.AugmentMisc.AUGMENT_DATA) != null;

        return hasAffixes || hasSockets || hasAugments;
    }

    @Override
    public ItemStack assemble(Container inv, RegistryAccess access) {
        ItemStack base = inv.getItem(1);
        ItemStack source = inv.getItem(2);

        ItemStack result = base.copy();
        result.setCount(1);

        CompoundTag srcAffix = source.getTagElement(AffixHelper.AFFIX_DATA);
        if (srcAffix != null) {
            result.getOrCreateTag().put(AffixHelper.AFFIX_DATA, srcAffix.copy());
        }

        CompoundTag srcAugmentRoot = source.getTagElement(Fallen.AugmentMisc.AUGMENT_DATA);
        if (srcAugmentRoot != null) {
            CompoundTag newAugmentRoot = srcAugmentRoot.copy();
            ListTag augments = newAugmentRoot.getList(Fallen.AugmentMisc.AUGMENTS, Tag.TAG_COMPOUND);
            for (int i = 0; i < augments.size(); i++) {
                CompoundTag augTag = augments.getCompound(i);
                augTag.putUUID(Fallen.AugmentMisc.UNIQUE_ID, UUID.randomUUID());
            }
            result.getOrCreateTag().put(Fallen.AugmentMisc.AUGMENT_DATA, newAugmentRoot);
        }

        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 3;
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
        return Fallen.RecipeSerializers.TRANSMUTATION.get();
    }
}