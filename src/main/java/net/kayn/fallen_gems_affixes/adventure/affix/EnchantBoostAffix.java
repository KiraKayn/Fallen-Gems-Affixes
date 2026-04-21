package net.kayn.fallen_gems_affixes.adventure.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixType;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EnchantBoostAffix extends Affix {

    private static final String TAG_CHOSEN_ENCHANT = "enchant_boost_target";

    public static final Codec<EnchantBoostAffix> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            GemBonus.VALUES_CODEC.fieldOf("values").forGetter(a -> a.values),
            LootCategory.SET_CODEC.fieldOf("types").forGetter(a -> a.types)
    ).apply(inst, EnchantBoostAffix::new));

    protected final Map<LootRarity, StepFunction> values;
    protected final Set<LootCategory> types;

    public EnchantBoostAffix(Map<LootRarity, StepFunction> values, Set<LootCategory> types) {
        super(AffixType.ABILITY);
        this.values = values;
        this.types = types;
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        if (cat.isNone()) return false;
        if (!types.isEmpty() && !types.contains(cat)) return false;
        if (!values.containsKey(rarity)) return false;
        return !EnchantmentHelper.getEnchantments(stack).isEmpty();
    }

    private Enchantment getOrPickEnchantment(ItemStack stack) {
        CompoundTag affixData = stack.getTagElement("affix_data");
        if (affixData != null && affixData.contains(TAG_CHOSEN_ENCHANT)) {
            String id = affixData.getString(TAG_CHOSEN_ENCHANT);
            Enchantment stored = ForgeRegistries.ENCHANTMENTS.getValue(ResourceLocation.parse(id));
            if (stored != null) return stored;
        }

        Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(stack);
        if (enchants.isEmpty()) return null;

        List<Enchantment> list = new ArrayList<>(enchants.keySet());
        int hash = Math.abs(stack.getOrCreateTag().hashCode());
        Enchantment chosen = list.get(hash % list.size());

        CompoundTag tag = stack.getOrCreateTagElement("affix_data");
        ResourceLocation key = ForgeRegistries.ENCHANTMENTS.getKey(chosen);
        if (key != null) tag.putString(TAG_CHOSEN_ENCHANT, key.toString());

        return chosen;
    }

    @Override
    public void getEnchantmentLevels(ItemStack stack, LootRarity rarity, float level, Map<Enchantment, Integer> enchantments) {
        Enchantment target = getOrPickEnchantment(stack);
        if (target == null) return;
        int bonus = (int) values.get(rarity).get(level);
        enchantments.merge(target, bonus, Integer::sum);
    }

    @Override
    public MutableComponent getDescription(ItemStack stack, LootRarity rarity, float level) {
        Enchantment target = getOrPickEnchantment(stack);
        int bonus = (int) values.get(rarity).get(level);

        if (target == null) {
            return Component.translatable("affix.fallen_gems_affixes.enchant_boost.desc.unknown")
                    .withStyle(ChatFormatting.YELLOW);
        }

        Component enchantName = Component.translatable(target.getDescriptionId())
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