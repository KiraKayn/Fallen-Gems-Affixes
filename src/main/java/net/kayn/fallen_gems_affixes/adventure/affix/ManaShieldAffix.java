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

public class ManaShieldAffix extends Affix {

    public static final Codec<ManaShieldAffix> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                    affixDef(),
                    LootRarity.mapCodec(StepFunction.CODEC).fieldOf("values").forGetter(a -> a.values),
                    Codec.FLOAT.fieldOf("mana_threshold").forGetter(a -> a.manaThreshold),
                    LootCategory.SET_CODEC.fieldOf("types").forGetter(a -> a.types))
            .apply(inst, ManaShieldAffix::new));

    protected final Map<LootRarity, StepFunction> values;
    protected final float manaThreshold;
    protected final Set<LootCategory> types;

    public ManaShieldAffix(AffixDefinition def,
                           Map<LootRarity, StepFunction> values,
                           float manaThreshold,
                           Set<LootCategory> types) {
        super(def);
        this.values        = values;
        this.manaThreshold = manaThreshold;
        this.types         = types;
    }

    @Override
    public float onHurt(AffixInstance inst, DamageSource src, LivingEntity ent, float amount) {
        if (!(ent instanceof Player player)) return amount;

        MagicData magicData = MagicData.getPlayerMagicData(player);
        float maxMana = (float) player.getAttributeValue(AttributeRegistry.MAX_MANA);
        if (maxMana <= 0f) return amount;

        float manaPercent = magicData.getMana() / maxMana;
        if (manaPercent < manaThreshold) return amount;

        float reduction = values.get(inst.getRarity()).get(inst.level());
        return amount * (1f - reduction);
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        if (cat.isNone()) return false;
        return (types.isEmpty() || types.contains(cat)) && values.containsKey(rarity);
    }

    @Override
    public MutableComponent getDescription(AffixInstance inst, AttributeTooltipContext ctx) {
        float reduction = values.get(inst.getRarity()).get(inst.level());
        return Component.translatable(
                "affix.fallen_gems_affixes.mana_shield.desc",
                Affix.fmt(reduction * 100f),
                Affix.fmt(manaThreshold * 100f)
        ).withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }
}