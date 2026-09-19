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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

import java.util.Map;
import java.util.Set;

public class AfflictedAffix extends Affix /* implements EntityAffixBehavior */ {

    public static final ResourceLocation SPEED_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "afflicted_speed");

    public static final Codec<AfflictedAffix> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                    affixDef(),
                    LootRarity.mapCodec(StepFunction.CODEC).fieldOf("damage_per_effect").forGetter(a -> a.damagePerEffect),
                    LootRarity.mapCodec(StepFunction.CODEC).fieldOf("speed_per_effect").forGetter(a -> a.speedPerEffect),
                    LootCategory.SET_CODEC.fieldOf("types").forGetter(a -> a.types))
            .apply(inst, AfflictedAffix::new));

    protected final Map<LootRarity, StepFunction> damagePerEffect;
    protected final Map<LootRarity, StepFunction> speedPerEffect;
    protected final Set<LootCategory>             types;

    public AfflictedAffix(AffixDefinition def,
                          Map<LootRarity, StepFunction> damagePerEffect,
                          Map<LootRarity, StepFunction> speedPerEffect,
                          Set<LootCategory> types) {
        super(def);
        this.damagePerEffect = damagePerEffect;
        this.speedPerEffect  = speedPerEffect;
        this.types = types;
    }

    public float getDamageBonus(LivingEntity entity, LootRarity rarity, float level) {
        int count = countNegativeEffects(entity);
        return count == 0 ? 0f : damagePerEffect.get(rarity).get(level) * count;
    }

    public float getSpeedBonus(LivingEntity entity, LootRarity rarity, float level) {
        int count = countNegativeEffects(entity);
        return count == 0 ? 0f : speedPerEffect.get(rarity).get(level) * count;
    }

    public static int countNegativeEffects(LivingEntity entity) {
        return (int) entity.getActiveEffects().stream()
                .filter(e -> e.getEffect().value().getCategory() == MobEffectCategory.HARMFUL)
                .count();
    }

    // TODO: re-enable when EntityAffixBehavior is ported
    // @Override
    // public void tickEntityAffix(LivingEntity entity, LootRarity rarity, float level) {
    //     AttributeInstance speedAttr = entity.getAttribute(Attributes.MOVEMENT_SPEED);
    //     if (speedAttr == null) return;
    //     speedAttr.removeModifier(SPEED_MODIFIER_ID);
    //     float bonus = getSpeedBonus(entity, rarity, level);
    //     if (bonus > 0f) {
    //         speedAttr.addTransientModifier(new AttributeModifier(
    //                 SPEED_MODIFIER_ID, bonus,
    //                 AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    //     }
    // }

    // TODO: re-enable when EntityAffixBehavior is ported
    // @Override
    // public int tickInterval() { return 5; }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        if (cat.isNone()) return false;
        return (types.isEmpty() || types.contains(cat)) && damagePerEffect.containsKey(rarity);
    }

    @Override
    public MutableComponent getDescription(AffixInstance inst, AttributeTooltipContext ctx) {
        LootRarity rarity = inst.getRarity();
        float level = inst.level();
        float dmg   = damagePerEffect.get(rarity).get(level);
        float speed = speedPerEffect.get(rarity).get(level);
        return Component.translatable(
                "affix.fallen_gems_affixes.afflicted.desc",
                Affix.fmt(dmg * 100f),
                Affix.fmt(speed * 100f)
        ).withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }
}