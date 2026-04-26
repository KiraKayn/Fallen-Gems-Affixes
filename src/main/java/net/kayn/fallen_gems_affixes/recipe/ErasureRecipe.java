package net.kayn.fallen_gems_affixes.recipe;

import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.registry.ModItems;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.HashMap;
import java.util.Map;

public class ErasureRecipe extends SmithingTransformRecipe {

    public static final String TAG_SCROLL_AFFIXES = "fga:scroll_affixes";
    public static final String TAG_SCROLL_SLOTS_USED = "fga:scroll_slots_used";

    private static final ResourceLocation ID = ResourceLocation.parse("fallen_gems_affixes:erasure");

    public ErasureRecipe() {
        super(ID, Ingredient.EMPTY, Ingredient.EMPTY, Ingredient.of(ModItems.SIGIL_OF_ERASURE.get()), ItemStack.EMPTY);
    }

    @Override
    public boolean matches(Container inv, Level level) {
        ItemStack base = inv.getItem(1);
        ItemStack sigil = inv.getItem(2);
        if (base.isEmpty()) return false;
        if (!sigil.is(ModItems.SIGIL_OF_ERASURE.get())) return false;
        return getScrollAffixCount(base) > 0;
    }

    @Override
    public ItemStack assemble(Container inv, RegistryAccess regs) {
        ItemStack out = inv.getItem(1).copy();
        if (out.isEmpty()) return ItemStack.EMPTY;
        removeScrollAffixes(out);
        return out;
    }

    private static int getScrollAffixCount(ItemStack stack) {
        if (!stack.hasTag()) return 0;
        CompoundTag tag = stack.getTag();
        if (!tag.contains(TAG_SCROLL_AFFIXES)) return 0;
        return tag.getList(TAG_SCROLL_AFFIXES, 8).size();
    }

    public static void removeScrollAffixes(ItemStack stack) {
        if (!stack.hasTag()) return;

        CompoundTag tag = stack.getTag();
        if (tag == null) return;

        if (tag.contains(TAG_SCROLL_AFFIXES)) {
            ListTag scrollAffixList = tag.getList(TAG_SCROLL_AFFIXES, 8);
            var currentAffixes = AffixHelper.getAffixes(stack);

            if (currentAffixes != null && !currentAffixes.isEmpty()) {
                Map<DynamicHolder<? extends dev.shadowsoffire.apotheosis.adventure.affix.Affix>, AffixInstance> kept = new HashMap<>();

                for (var entry : currentAffixes.entrySet()) {
                    ResourceLocation id = entry.getKey().getId();
                    boolean isScrollAffix = false;

                    for (int i = 0; i < scrollAffixList.size(); i++) {
                        if (scrollAffixList.getString(i).equals(id.toString())) {
                            isScrollAffix = true;
                            break;
                        }
                    }

                    if (!isScrollAffix) {
                        kept.put(entry.getKey(), entry.getValue());
                    }
                }
                AffixHelper.setAffixes(stack, kept);
            }
            tag.remove(TAG_SCROLL_AFFIXES);
        }
        tag.remove(TAG_SCROLL_SLOTS_USED);
    }

    public static int getScrollSlotsUsed(ItemStack stack) {
        if (!stack.hasTag()) return 0;
        return stack.getTag().getInt(TAG_SCROLL_SLOTS_USED);
    }

    public static void markScrollAffix(ItemStack stack, ResourceLocation affixId) {
        CompoundTag tag = stack.getOrCreateTag();
        ListTag list = tag.contains(TAG_SCROLL_AFFIXES) ? tag.getList(TAG_SCROLL_AFFIXES, 8) : new ListTag();
        net.minecraft.nbt.StringTag entry = net.minecraft.nbt.StringTag.valueOf(affixId.toString());
        list.add(entry);
        tag.put(TAG_SCROLL_AFFIXES, list);
        tag.putInt(TAG_SCROLL_SLOTS_USED, list.size());
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Fallen.RecipeSerializers.ERASURE.get();
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