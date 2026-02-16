package net.kayn.fallen_gems_affixes.compat.jei;

import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.kayn.fallen_gems_affixes.recipe.TransmutationRecipe;
import net.kayn.fallen_gems_affixes.registry.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TransmutationExtension implements FallenSmithingCategory.Extension<TransmutationRecipe> {

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, TransmutationRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 8, 8)
                .addItemStack(new ItemStack(ModItems.SIGIL_OF_TRANSMUTATION.get()));

        builder.addSlot(RecipeIngredientRole.INPUT, 26, 8)
                .addItemStack(new ItemStack(Items.DIAMOND_SWORD));

        ItemStack source = createAffixedExample();
        builder.addSlot(RecipeIngredientRole.INPUT, 44, 8)
                .addItemStack(source);

        ItemStack result = new ItemStack(Items.DIAMOND_SWORD);
        copyAffixData(source, result);
        builder.addSlot(RecipeIngredientRole.OUTPUT, 98, 8)
                .addItemStack(result);
    }

    private ItemStack createAffixedExample() {
        ItemStack stack = new ItemStack(Items.IRON_SWORD);

        LootRarity rarity = RarityRegistry.INSTANCE.getValues().stream()
                .filter(r -> "epic".equals(Objects.requireNonNull(RarityRegistry.INSTANCE.getKey(r)).getPath()))
                .findFirst()
                .orElse(RarityRegistry.getMinRarity().get());

        AffixHelper.setRarity(stack, rarity);
        SocketHelper.setSockets(stack, 3);

        Map<DynamicHolder<? extends Affix>, AffixInstance> affixes = new HashMap<>();
        var registry = dev.shadowsoffire.apotheosis.adventure.affix.AffixRegistry.INSTANCE;

        registry.getValues().stream()
                .filter(affix -> affix.canApplyTo(stack, dev.shadowsoffire.apotheosis.adventure.loot.LootCategory.SWORD, rarity))
                .limit(3)
                .forEach(affix -> {
                    DynamicHolder<? extends Affix> holder = registry.holder(affix);
                    affixes.put(holder, new AffixInstance(holder, stack, RarityRegistry.INSTANCE.holder(rarity), 1.0f));
                });

        if (!affixes.isEmpty()) {
            AffixHelper.setAffixes(stack, affixes);
        }

        return stack;
    }

    private void copyAffixData(ItemStack source, ItemStack target) {
        var affixes = AffixHelper.getAffixes(source);
        if (!affixes.isEmpty()) {
            Map<DynamicHolder<? extends Affix>, AffixInstance> newAffixes = new HashMap<>();
            affixes.forEach((holder, inst) -> {
                newAffixes.put(holder, new AffixInstance(holder, target, inst.rarity(), inst.level()));
            });
            AffixHelper.setAffixes(target, newAffixes);
        }

        DynamicHolder<LootRarity> rarityHolder = AffixHelper.getRarity(source);
        if (rarityHolder.isBound()) {
            AffixHelper.setRarity(target, rarityHolder.get());
        }

        int sockets = SocketHelper.getSockets(source);
        if (sockets > 0) {
            SocketHelper.setSockets(target, sockets);
        }
    }
}