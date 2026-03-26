package net.kayn.fallen_gems_affixes.adventure.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixType;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.placebo.util.StepFunction;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Set;

public class ManaBlockAffix extends Affix {

    public static final Codec<ManaBlockAffix> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                    GemBonus.VALUES_CODEC.fieldOf("values").forGetter(a -> a.values),
                    Codec.FLOAT.fieldOf("least_mana").forGetter(a -> a.leastMana),
                    LootCategory.SET_CODEC.fieldOf("types").forGetter(a -> a.types))
            .apply(inst, ManaBlockAffix::new));

    protected final Map<LootRarity, StepFunction> values;
    protected final float leastMana;
    protected final Set<LootCategory> types;

    public ManaBlockAffix(Map<LootRarity, StepFunction> values, float leastMana,
                          Set<LootCategory> types) {
        super(AffixType.ABILITY);
        this.values         = values;
        this.leastMana = leastMana;
        this.types          = types;
    }

    @Override
    public float onHurt(ItemStack stack, LootRarity rarity, float level,
                        DamageSource src, LivingEntity ent, float amount) {
        if (!(ent instanceof Player player)) return amount;

        MagicData magicData = MagicData.getPlayerMagicData(player);
        float maxMana = (float) player.getAttributeValue(io.redspace.ironsspellbooks.api.registry.AttributeRegistry.MAX_MANA.get());
        if (maxMana <= 0f) return amount;
        float reductionFactor = values.get(rarity).get(level);
        float mana = magicData.getMana();
        float reduction = amount * reductionFactor;
        if (mana > leastMana && reduction > mana) {
            magicData.setMana(leastMana);
            return amount;
        }
        magicData.setMana(mana - reduction);

        return amount - reduction;
    }


    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        if (cat.isNone()) return false;
        return (types.isEmpty() || types.contains(cat)) && values.containsKey(rarity);
    }

    @Override
    public MutableComponent getDescription(ItemStack stack, LootRarity rarity, float level) {
        float reductionFactor = values.get(rarity).get(level);
        return Component.translatable(
                "affix.fallen_gems_affixes.mana_block.desc",
                Affix.fmt(reductionFactor * 100f),
                Affix.fmt(leastMana)
        ).withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }
}