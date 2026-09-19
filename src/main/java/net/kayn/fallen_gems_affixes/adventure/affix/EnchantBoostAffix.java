package net.kayn.fallen_gems_affixes.adventure.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixDefinition;
import dev.shadowsoffire.apotheosis.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.event.enchanting.GetEnchantmentLevelEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EnchantBoostAffix extends Affix {

    private static final String TAG_CHOSEN_ENCHANT = "enchant_boost_target";

    public static final Codec<EnchantBoostAffix> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            affixDef(),
            LootRarity.mapCodec(StepFunction.CODEC).fieldOf("values").forGetter(a -> a.values),
            LootCategory.SET_CODEC.fieldOf("types").forGetter(a -> a.types)
    ).apply(inst, EnchantBoostAffix::new));

    protected final Map<LootRarity, StepFunction> values;
    protected final Set<LootCategory> types;

    public EnchantBoostAffix(AffixDefinition def, Map<LootRarity, StepFunction> values, Set<LootCategory> types) {
        super(def);
        this.values = values;
        this.types = types;
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        if (cat.isNone()) return false;
        if (!types.isEmpty() && !types.contains(cat)) return false;
        if (!values.containsKey(rarity)) return false;
        return !stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty();
    }

    private Holder<Enchantment> getOrPickEnchantment(ItemStack stack) {
        ItemEnchantments enchants = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (enchants.isEmpty()) return null;

        CompoundTag affixData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (affixData.contains(TAG_CHOSEN_ENCHANT)) {
            String id = affixData.getString(TAG_CHOSEN_ENCHANT);
            for (Holder<Enchantment> h : enchants.keySet()) {
                ResourceLocation key = h.unwrapKey().map(ResourceKey::location).orElse(null);
                if (key != null && key.toString().equals(id)) {
                    return h;
                }
            }
        }

        List<Holder<Enchantment>> list = new ArrayList<>(enchants.keySet());
        int hash = Math.abs(stack.getComponents().hashCode());
        Holder<Enchantment> chosen = list.get(hash % list.size());

        ResourceLocation key = chosen.unwrapKey().map(ResourceKey::location).orElse(null);
        if (key != null) {
            CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            tag.putString(TAG_CHOSEN_ENCHANT, key.toString());
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
        return chosen;
    }

    @Override
    public void getEnchantmentLevels(AffixInstance inst, GetEnchantmentLevelEvent event) {
        Holder<Enchantment> target = getOrPickEnchantment(inst.stack());
        if (target == null) return;
        int bonus = (int) values.get(inst.getRarity()).get(inst.level());
        event.getEnchantments().upgrade(target, bonus);
    }

    @Override
    public MutableComponent getDescription(AffixInstance inst, AttributeTooltipContext ctx) {
        Holder<Enchantment> target = getOrPickEnchantment(inst.stack());
        int bonus = (int) values.get(inst.getRarity()).get(inst.level());

        if (target == null) {
            return Component.translatable("affix.fallen_gems_affixes.enchant_boost.desc.unknown")
                    .withStyle(ChatFormatting.YELLOW);
        }

        Component enchantName = target.value().description().copy()
                .withStyle(ChatFormatting.DARK_PURPLE);
        Component bonusComp = Component.literal("+" + bonus)
                .withStyle(ChatFormatting.YELLOW);

        return Component.translatable(
                "affix.fallen_gems_affixes.enchant_boost.desc",
                bonusComp,
                enchantName
        ).withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }
}