package net.kayn.fallen_gems_affixes.recipe;

import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.DurableAffix;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.attachment.AugmentSlotHelper;
import net.kayn.fallen_gems_affixes.augment.MaliceAugment;
import net.kayn.fallen_gems_affixes.augment.SupremacyAugment;
import net.kayn.fallen_gems_affixes.registry.ModItems;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
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

import java.util.HashMap;
import java.util.Map;

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

        boolean hadSupremacy = hasSupremacy(out);
        boolean hadMalice    = MaliceAugment.hasRevealedMalice(out);
        Float maliceAffixPower = hadMalice ? MaliceAugment.getMaliceAffixPower(out) : null;

        int slots = AugmentSlotHelper.getAugmentSlots(out);
        out.getOrCreateTag().remove(Fallen.AugmentMisc.AUGMENT_DATA);
        out.getOrCreateTag().remove("fallen_gems_affixes:fabled");
        AugmentSlotHelper.setAugmentSlots(out, slots);

        if (hadSupremacy) {
            restoreAffixLevels(out);
        }

        if (hadMalice && maliceAffixPower != null && maliceAffixPower != 0f && maliceAffixPower != 1f) {
            MaliceAugment.applyAffixPower(out, 1f / maliceAffixPower);
        }

        return out;
    }

    private boolean hasSupremacy(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(Fallen.AugmentMisc.AUGMENT_DATA)) {
            CompoundTag augmentData = stack.getTagElement(Fallen.AugmentMisc.AUGMENT_DATA);
            ListTag augments = augmentData.getList(Fallen.AugmentMisc.AUGMENTS, Tag.TAG_COMPOUND);
            for (int i = 0; i < augments.size(); i++) {
                CompoundTag augment = augments.getCompound(i);
                if (augment.getString(Fallen.AugmentMisc.TYPE).equals(Fallen.Augments.SUPREMACY_STRING)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void restoreAffixLevels(ItemStack stack) {
        var affixes = AffixHelper.getAffixes(stack);
        if (affixes == null || affixes.isEmpty()) return;

        Map<DynamicHolder<? extends dev.shadowsoffire.apotheosis.adventure.affix.Affix>, AffixInstance> newAffixes = new HashMap<>();

        for (var entry : affixes.entrySet()) {
            var holder = entry.getKey();
            var affixIns = entry.getValue();
            var affix = holder.get();

            if (!(affix instanceof DurableAffix)) {
                float currentLevel = affixIns.level();
                if (currentLevel > SupremacyAugment.STANDARD_MAX_LEVEL) {
                    float restoredLevel = Mth.clamp(currentLevel, 0, SupremacyAugment.STANDARD_MAX_LEVEL);

                    newAffixes.put(holder, new AffixInstance(
                            affixIns.affix(),
                            affixIns.stack(),
                            affixIns.rarity(),
                            restoredLevel
                    ));
                } else {
                    newAffixes.put(holder, affixIns);
                }
            } else {
                newAffixes.put(holder, affixIns);
            }
        }

        AffixHelper.setAffixes(stack, newAffixes);
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