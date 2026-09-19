package net.kayn.fallen_gems_affixes.adventure.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixDefinition;
import dev.shadowsoffire.apotheosis.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

import java.util.Map;
import java.util.Set;

public class ChainShotAffix extends Affix {

    public static final String KEY_CHAIN_ARROW = "fga:chain_arrow";
    public static final String KEY_CACHED_RANGE = "fga:chain_range";
    public static final String KEY_LAST_ARROW_DAMAGE = "fga:chain_last_damage";

    public static final Codec<ChainShotAffix> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                    affixDef(),
                    LootRarity.mapCodec(Codec.BOOL).fieldOf("values").forGetter(a -> a.values),
                    LootCategory.SET_CODEC.fieldOf("types").forGetter(a -> a.types),
                    Codec.FLOAT.optionalFieldOf("max_range", 32f).forGetter(a -> a.maxRange)
            ).apply(inst, ChainShotAffix::new));

    protected final Map<LootRarity, Boolean> values;
    protected final Set<LootCategory> types;
    protected final float maxRange;

    public ChainShotAffix(AffixDefinition def, Map<LootRarity, Boolean> values,
                          Set<LootCategory> types, float maxRange) {
        super(def);
        this.values   = values;
        this.types    = types;
        this.maxRange = maxRange;
    }

    @Override
    public Codec<? extends Affix> getCodec() { return CODEC; }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        if (!Boolean.TRUE.equals(this.values.get(rarity))) return false;
        return this.types.isEmpty() || this.types.contains(cat);
    }

    @Override
    public MutableComponent getDescription(AffixInstance inst, AttributeTooltipContext ctx) {
        return Component.translatable(
                "affix.fallen_gems_affixes.chain_shot.desc",
                (int) this.maxRange);
    }

    @Override
    public Component getAugmentingText(AffixInstance inst, AttributeTooltipContext ctx) {
        return getDescription(inst, ctx);
    }

    public float getMaxRange() { return maxRange; }

    @Override
    public void onProjectileFired(AffixInstance inst, LivingEntity user, Projectile projectile) {
        projectile.getPersistentData().putFloat(KEY_CACHED_RANGE, maxRange);
    }
}