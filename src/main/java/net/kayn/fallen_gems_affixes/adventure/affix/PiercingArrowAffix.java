package net.kayn.fallen_gems_affixes.adventure.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixDefinition;
import dev.shadowsoffire.apotheosis.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

import java.util.Map;
import java.util.Set;

public class PiercingArrowAffix extends Affix {

    public static final Codec<PiercingArrowAffix> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                    affixDef(),
                    LootRarity.mapCodec(StepFunction.CODEC).fieldOf("values").forGetter(a -> a.values),
                    LootCategory.SET_CODEC.fieldOf("types").forGetter(a -> a.types)
            ).apply(inst, PiercingArrowAffix::new));

    protected final Map<LootRarity, StepFunction> values;
    protected final Set<LootCategory> types;

    public PiercingArrowAffix(AffixDefinition def, Map<LootRarity, StepFunction> values, Set<LootCategory> types) {
        super(def);
        this.values = values;
        this.types  = types;
    }

    @Override
    public Codec<? extends Affix> getCodec() { return CODEC; }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        if (!this.values.containsKey(rarity)) return false;
        return this.types.isEmpty() || this.types.contains(cat);
    }

    @Override
    public MutableComponent getDescription(AffixInstance inst, AttributeTooltipContext ctx) {
        return Component.translatable(
                "affix.fallen_gems_affixes.piercing_arrow.desc",
                getPierceLevel(inst.getRarity(), inst.level()));
    }

    @Override
    public Component getAugmentingText(AffixInstance inst, AttributeTooltipContext ctx) {
        MutableComponent comp = this.getDescription(inst, ctx);
        int min = getPierceLevel(inst.getRarity(), 0);
        int max = getPierceLevel(inst.getRarity(), 1);
        return comp.append(valueBounds(
                Component.literal(String.valueOf(min)),
                Component.literal(String.valueOf(max))));
    }

    public int getPierceLevel(LootRarity rarity, float level) {
        StepFunction f = this.values.get(rarity);
        return f != null ? (int) f.get(level) : 0;
    }

    @Override
    public void onProjectileFired(AffixInstance inst, LivingEntity user, Projectile projectile) {
        if (!(projectile instanceof AbstractArrow arrow)) return;
        int pierceLevel = getPierceLevel(inst.getRarity(), inst.level());
        if (pierceLevel > 0) {
            arrow.setPierceLevel((byte) Math.max(arrow.getPierceLevel(), pierceLevel));
        }
    }
}