package net.kayn.fallen_gems_affixes.recipe;

import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.registry.ModItems;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
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

public class ConfluenceRecipe extends SmithingTransformRecipe {

    private static final ResourceLocation ID = ResourceLocation.parse("fallen_gems_affixes:confluence");

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

    public ConfluenceRecipe() {
        super(ID,
                Ingredient.of(ModItems.SIGIL_OF_CONFLUENCE.get()),
                buildGearIngredient(),
                buildGearIngredient(),
                ItemStack.EMPTY);
    }

    @Override
    public boolean matches(Container inv, Level level) {
        ItemStack sigil  = inv.getItem(0);
        ItemStack base   = inv.getItem(1);
        ItemStack source = inv.getItem(2);

        if (!sigil.is(ModItems.SIGIL_OF_CONFLUENCE.get())) return false;
        if (base.isEmpty() || source.isEmpty()) return false;

        LootCategory baseCat = LootCategory.forItem(base);
        LootCategory srcCat  = LootCategory.forItem(source);
        if (baseCat.isNone() || srcCat.isNone() || baseCat != srcCat) return false;

        DynamicHolder<LootRarity> baseRarity = AffixHelper.getRarity(base);
        DynamicHolder<LootRarity> srcRarity  = AffixHelper.getRarity(source);
        if (!baseRarity.isBound() || !srcRarity.isBound()) return false;
        if (!baseRarity.getId().equals(srcRarity.getId())) return false;

        if (base.getTag()   != null && base.getTag().contains(Fallen.AugmentMisc.AUGMENT_DATA))   return false;
        if (source.getTag() != null && source.getTag().contains(Fallen.AugmentMisc.AUGMENT_DATA)) return false;

        if (base.getTag()   != null && base.getTag().contains(Fallen.AugmentMisc.AFFIX_COMBINED))   return false;
        if (source.getTag() != null && source.getTag().contains(Fallen.AugmentMisc.AFFIX_COMBINED)) return false;

        boolean baseHasAffixes = AffixHelper.hasAffixes(base);
        boolean srcHasAffixes  = AffixHelper.hasAffixes(source);
        boolean srcHasSockets  = SocketHelper.getSockets(source) > 0;

        return baseHasAffixes || srcHasAffixes || srcHasSockets;
    }

    @Override
    public ItemStack assemble(Container inv, RegistryAccess access) {
        ItemStack base   = inv.getItem(1);
        ItemStack source = inv.getItem(2);

        ItemStack result = base.copy();
        result.setCount(1);

        CompoundTag baseAffixData = base.getTagElement(AffixHelper.AFFIX_DATA);
        CompoundTag srcAffixData  = source.getTagElement(AffixHelper.AFFIX_DATA);

        if (srcAffixData != null || baseAffixData != null) {

            CompoundTag mergedAffixData = srcAffixData != null
                    ? srcAffixData.copy()
                    : new CompoundTag();
            if (baseAffixData != null && baseAffixData.contains(AffixHelper.AFFIXES, Tag.TAG_COMPOUND)) {
                CompoundTag baseAffixes = baseAffixData.getCompound(AffixHelper.AFFIXES);

                CompoundTag mergedAffixes = mergedAffixData.contains(AffixHelper.AFFIXES, Tag.TAG_COMPOUND)
                        ? mergedAffixData.getCompound(AffixHelper.AFFIXES).copy()
                        : new CompoundTag();


                for (String key : baseAffixes.getAllKeys()) {
                    if (!mergedAffixes.contains(key)) {
                        mergedAffixes.putFloat(key, baseAffixes.getFloat(key));
                    }
                }
                mergedAffixData.put(AffixHelper.AFFIXES, mergedAffixes);
            }

            if (baseAffixData != null && baseAffixData.contains(AffixHelper.RARITY)) {
                mergedAffixData.putString(AffixHelper.RARITY, baseAffixData.getString(AffixHelper.RARITY));
            }

            result.getOrCreateTag().put(AffixHelper.AFFIX_DATA, mergedAffixData);
        }

        result.getOrCreateTag().putBoolean(Fallen.AugmentMisc.AFFIX_COMBINED, true);

        result.getOrCreateTag().remove(Fallen.AugmentMisc.AUGMENT_DATA);

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
        return Fallen.RecipeSerializers.CONFLUENCE.get();
    }
}