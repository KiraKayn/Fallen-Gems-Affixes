package net.kayn.fallen_gems_affixes.mixin;

import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.kayn.fallen_gems_affixes.adventure.set.SetAffixHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(value = AffixHelper.class, remap = false)
public class AffixHelperMixin {

    @Inject(method = "getAffixesImpl", at = @At("HEAD"), cancellable = true)
    private static void fga$injectFabledAffixPreservation(
            ItemStack stack,
            CallbackInfoReturnable<Map<DynamicHolder<? extends Affix>, AffixInstance>> cir) {

        if (stack.isEmpty()) return;

        DynamicHolder<LootRarity> rarityHolder = AffixHelper.getRarity(stack);
        if (!rarityHolder.isBound()) return;

        ResourceLocation rarityKey = RarityRegistry.INSTANCE.getKey(rarityHolder.get());
        if (rarityKey == null) return;
        if (!"fallen_gems_affixes:fabled".equals(rarityKey.toString())) return;

        if (!SetAffixHelper.hasSetAffix(stack)) return;

        CompoundTag afxData = stack.getTagElement(AffixHelper.AFFIX_DATA);
        if (afxData == null || !afxData.contains(AffixHelper.AFFIXES)) return;

        CompoundTag affixesTag = afxData.getCompound(AffixHelper.AFFIXES);
        if (affixesTag.isEmpty()) {
            cir.setReturnValue(Collections.emptyMap());
            return;
        }

        LootCategory cat = LootCategory.forItem(stack);
        Map<DynamicHolder<? extends Affix>, AffixInstance> result = new HashMap<>();

        for (String key : affixesTag.getAllKeys()) {
            ResourceLocation affixId;
            try {
                affixId = new ResourceLocation(key);
            } catch (Exception e) {
                continue;
            }

            DynamicHolder<Affix> affixHolder = AffixRegistry.INSTANCE.holder(affixId);
            if (!affixHolder.isBound()) continue;

            float lvl = affixesTag.getFloat(key);
            DynamicHolder<LootRarity> effectiveRarity = fga$findBestCompatibleRarity(affixHolder.get(), stack, cat);
            result.put(affixHolder, new AffixInstance(affixHolder, stack, effectiveRarity, lvl));
        }

        cir.setReturnValue(Collections.unmodifiableMap(result));
    }

    @Unique
    private static DynamicHolder<LootRarity> fga$findBestCompatibleRarity(Affix affix, ItemStack stack, LootCategory cat) {
        List<DynamicHolder<LootRarity>> ordered = RarityRegistry.INSTANCE.getOrderedRarities();
        for (int i = ordered.size() - 1; i >= 0; i--) {
            DynamicHolder<LootRarity> holder = ordered.get(i);
            if (!holder.isBound()) continue;
            ResourceLocation key = RarityRegistry.INSTANCE.getKey(holder.get());
            if (key == null) continue;
            if ("fallen_gems_affixes:fabled".equals(key.toString())) continue;
            try {
                if (affix.canApplyTo(stack, cat, holder.get())) return holder;
            } catch (Exception ignored) {}
        }
        return RarityRegistry.getMinRarity();
    }
}