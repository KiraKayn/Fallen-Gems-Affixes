package net.kayn.fallen_gems_affixes.adventure.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixDefinition;
import dev.shadowsoffire.apotheosis.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.placebo.util.StepFunction;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

import java.util.Map;
import java.util.Set;

public class ManaBlockAffix extends Affix {

    public static final Codec<ManaBlockAffix> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                    affixDef(),
                    LootRarity.mapCodec(StepFunction.CODEC).fieldOf("values").forGetter(a -> a.values),
                    LootRarity.mapCodec(StepFunction.CODEC).fieldOf("mana_cost").forGetter(a -> a.manaCost),
                    Codec.FLOAT.fieldOf("least_mana").forGetter(a -> a.leastMana),
                    LootCategory.SET_CODEC.fieldOf("types").forGetter(a -> a.types))
            .apply(inst, ManaBlockAffix::new));

    protected final Map<LootRarity, StepFunction> values;
    protected final Map<LootRarity, StepFunction> manaCost;
    protected final float leastMana;
    protected final Set<LootCategory> types;

    public ManaBlockAffix(AffixDefinition def,
                          Map<LootRarity, StepFunction> values,
                          Map<LootRarity, StepFunction> manaCost,
                          float leastMana,
                          Set<LootCategory> types) {
        super(def);
        this.values    = values;
        this.manaCost  = manaCost;
        this.leastMana = leastMana;
        this.types     = types;
    }

    @Override
    public float onHurt(AffixInstance inst, DamageSource src, LivingEntity ent, float amount) {
        if (!(ent instanceof Player player)) return amount;

        MagicData magicData = MagicData.getPlayerMagicData(player);
        float mana = magicData.getMana();

        if (mana <= leastMana) return amount;

        float maxMana = (float) player.getAttributeValue(AttributeRegistry.MAX_MANA);
        if (maxMana <= 0f) return amount;

        LootRarity rarity = inst.getRarity();
        float level = inst.level();
        float reductionFactor = values.get(rarity).get(level);
        float reduction = amount * reductionFactor;
        float drainFactor = manaCost.get(rarity).get(level);
        float baseDrain = maxMana * drainFactor;

        if (baseDrain + reduction > mana) {
            reduction = mana - leastMana;
        }

        float actualDrain = baseDrain + reduction;
        float newMana = Math.max(mana - actualDrain, leastMana);
        magicData.setMana(newMana);

        return amount - reduction;
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        if (cat.isNone()) return false;
        return (types.isEmpty() || types.contains(cat))
                && values.containsKey(rarity)
                && manaCost.containsKey(rarity);
    }

    @Override
    public MutableComponent getDescription(AffixInstance inst, AttributeTooltipContext ctx) {
        LootRarity rarity = inst.getRarity();
        float level = inst.level();
        float reductionFactor = values.get(rarity).get(level);
        float drainFactor = manaCost.get(rarity).get(level);
        return Component.translatable(
                "affix.fallen_gems_affixes.mana_block.desc",
                Affix.fmt(leastMana),
                Affix.fmt(reductionFactor * 100f),
                Affix.fmt(drainFactor * 100f)
        ).withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }
}