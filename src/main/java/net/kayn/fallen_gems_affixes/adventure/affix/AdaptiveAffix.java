package net.kayn.fallen_gems_affixes.adventure.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixType;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AdaptiveAffix extends Affix {

    public static final Codec<AdaptiveAffix> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                    LootRarity.mapCodec(Codec.FLOAT).fieldOf("reduction_per_hit").forGetter(a -> a.reductionPerHit),
                    LootRarity.mapCodec(Codec.FLOAT).fieldOf("max_reduction").forGetter(a -> a.maxReduction),
                    LootRarity.mapCodec(Codec.INT).fieldOf("duration_ticks").forGetter(a -> a.durationTicks),
                    LootCategory.SET_CODEC.fieldOf("types").forGetter(a -> a.types))
            .apply(inst, AdaptiveAffix::new));

    protected final Map<LootRarity, Float> reductionPerHit;
    protected final Map<LootRarity, Float> maxReduction;
    protected final Map<LootRarity, Integer> durationTicks;
    protected final Set<LootCategory> types;

    private static final Map<UUID, Map<ResourceLocation, float[]>> STATES = new HashMap<>();

    public AdaptiveAffix(Map<LootRarity, Float> reductionPerHit,
                         Map<LootRarity, Float> maxReduction,
                         Map<LootRarity, Integer> durationTicks,
                         Set<LootCategory> types) {
        super(AffixType.ABILITY);
        this.reductionPerHit = reductionPerHit;
        this.maxReduction    = maxReduction;
        this.durationTicks   = durationTicks;
        this.types           = types;
    }

    @Override
    public float onHurt(ItemStack stack, LootRarity rarity, float level,
                        DamageSource src, LivingEntity ent, float amount) {
        ResourceLocation damageTypeId = ent.level().registryAccess()
                .registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE)
                .getKey(src.type());
        if (damageTypeId == null) return amount;

        UUID playerId = ent.getUUID();
        long gameTime = ent.level().getGameTime();
        float perHit  = reductionPerHit.getOrDefault(rarity, 0.03f);
        float max     = maxReduction.getOrDefault(rarity, 0.25f);
        int duration  = durationTicks.getOrDefault(rarity, 100);

        Map<ResourceLocation, float[]> playerState = STATES.computeIfAbsent(playerId, k -> new HashMap<>());
        float[] entry = playerState.get(damageTypeId);

        float currentReduction;
        if (entry == null || gameTime > entry[1]) {
            currentReduction = perHit;
        } else {
            currentReduction = Math.min(entry[0] + perHit, max);
        }

        playerState.put(damageTypeId, new float[]{ currentReduction, gameTime + duration });
        return amount * (1f - currentReduction);
    }

    public static void clearState(UUID uuid) {
        STATES.remove(uuid);
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        if (cat.isNone()) return false;
        return (types.isEmpty() || types.contains(cat)) && reductionPerHit.containsKey(rarity);
    }

    @Override
    public MutableComponent getDescription(ItemStack stack, LootRarity rarity, float level) {
        float perHit   = reductionPerHit.getOrDefault(rarity, 0.03f);
        float max      = maxReduction.getOrDefault(rarity, 0.25f);
        float duration = durationTicks.getOrDefault(rarity, 100) / 20f;
        return Component.translatable(
                "affix.fallen_gems_affixes.adaptive.desc",
                Affix.fmt(perHit * 100f),
                Affix.fmt(max * 100f),
                Affix.fmt(duration)
        ).withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }
}