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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Set;

public class ManaShieldAffix extends Affix {

    public static final Codec<ManaShieldAffix> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                    GemBonus.VALUES_CODEC.fieldOf("values").forGetter(a -> a.values),
                    Codec.FLOAT.fieldOf("mana_threshold").forGetter(a -> a.manaThreshold),
                    LootCategory.SET_CODEC.fieldOf("types").forGetter(a -> a.types))
            .apply(inst, ManaShieldAffix::new));

    protected final Map<LootRarity, StepFunction> values;
    protected final float manaThreshold;
    protected final Set<LootCategory> types;

    public ManaShieldAffix(Map<LootRarity, StepFunction> values,
                           float manaThreshold,
                           Set<LootCategory> types) {
        super(AffixType.ABILITY);
        this.values         = values;
        this.manaThreshold  = manaThreshold;
        this.types          = types;
    }

    @Override
    public float onHurt(ItemStack stack, LootRarity rarity, float level,
                        DamageSource src, LivingEntity ent, float amount) {
        if (!(ent instanceof Player player)) return amount;

        MagicData magicData = MagicData.getPlayerMagicData(player);
        float maxMana = (float) player.getAttributeValue(io.redspace.ironsspellbooks.api.registry.AttributeRegistry.MAX_MANA.get());
        if (maxMana <= 0f) return amount;

        float manaPercent = magicData.getMana() / maxMana;
        if (manaPercent < manaThreshold) return amount;

        float reduction = values.get(rarity).get(level);
        return amount * (1f - reduction);
    }


    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        if (cat.isNone()) return false;
        return (types.isEmpty() || types.contains(cat)) && values.containsKey(rarity);
    }

    @Override
    public MutableComponent getDescription(ItemStack stack, LootRarity rarity, float level) {
        float reduction = values.get(rarity).get(level);
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