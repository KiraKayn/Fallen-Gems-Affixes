package net.kayn.fallen_gems_affixes.adventure.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixType;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.util.SpellCastUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.util.RandomSource;

import javax.annotation.Nullable;
import java.util.*;

public class SpellCastAffix extends Affix {

    private static final ThreadLocal<Set<UUID>> TRIGGERING_CASTERS = ThreadLocal.withInitial(HashSet::new);
    private static final Map<String, Map<UUID, Long>> COOLDOWNS = new HashMap<>();

    public static boolean isCurrentlyTriggering(LivingEntity caster) {
        return TRIGGERING_CASTERS.get().contains(caster.getUUID());
    }

    private static void setTriggering(LivingEntity caster, boolean value) {
        if (value) TRIGGERING_CASTERS.get().add(caster.getUUID());
        else TRIGGERING_CASTERS.get().remove(caster.getUUID());
    }

    public static boolean isOnCooldown(String id, int ignoredCooldown, LivingEntity caster) {
        Map<UUID, Long> cdMap = COOLDOWNS.getOrDefault(id, new HashMap<>());
        long now = caster.level().getGameTime();
        return cdMap.getOrDefault(caster.getUUID(), 0L) > now;
    }

    public static void startCooldown(String id, LivingEntity caster, int cooldown) {
        COOLDOWNS.computeIfAbsent(id, k -> new HashMap<>())
                .put(caster.getUUID(), caster.level().getGameTime() + cooldown);
    }

    public static final Codec<SpellCastAffix> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            SpellRegistry.REGISTRY.get().getCodec().fieldOf("spell").forGetter(affix -> affix.spell),
            TriggerType.CODEC.fieldOf("trigger").forGetter(affix -> affix.trigger),
            LootRarity.mapCodec(TriggerData.CODEC).fieldOf("values").forGetter(affix -> affix.values),
            PlaceboCodecs.nullableField(Codec.INT, "cooldown", 0).forGetter(affix -> affix.cooldown),
            LootCategory.SET_CODEC.fieldOf("types").forGetter(affix -> affix.types),
            TargetType.CODEC.optionalFieldOf("target").forGetter(affix -> affix.target)
    ).apply(inst, SpellCastAffix::new));

    protected final AbstractSpell spell;
    public final TriggerType trigger;
    protected final Map<LootRarity, TriggerData> values;
    protected final int cooldown;
    protected final Set<LootCategory> types;
    public final Optional<TargetType> target;

    public SpellCastAffix(AbstractSpell spell, TriggerType trigger, Map<LootRarity, TriggerData> values,
                          int cooldown, Set<LootCategory> types, Optional<TargetType> target) {
        super(AffixType.ABILITY);
        this.spell = spell;
        this.trigger = trigger;
        this.values = values;
        this.cooldown = cooldown;
        this.types = types;
        this.target = target;
    }

    private int getCooldown(LootRarity rarity) {
        TriggerData data = this.values.get(rarity);
        if (data != null && data.cooldown() >= 0) {
            return data.cooldown();
        }
        return this.cooldown;
    }

    public void triggerSpell(LivingEntity caster, LivingEntity target, LootRarity rarity, int spellLevelOverride) {
        if (isCurrentlyTriggering(caster)) return;

        TriggerData data = this.values.get(rarity);
        if (data == null || caster.level().isClientSide()) return;

        int spellLevel = spellLevelOverride > 0 ? spellLevelOverride : data.level().getRandomLevel(caster.getRandom());
        String spellId = this.spell.getSpellId();

        MagicData magicData = MagicData.getPlayerMagicData(caster);
        boolean hasActiveRecast = magicData.getPlayerRecasts().hasRecastForSpell(spellId);

        int cooldownTicks = Math.max(2, this.getCooldown(rarity));
        if (!hasActiveRecast && isOnCooldown(this.getId().toString(), cooldownTicks, caster)) return;

        setTriggering(caster, true);
        try {
            SpellCastUtil.castSpell(caster, this.spell, spellLevel, target);
            if (!hasActiveRecast) startCooldown(this.getId().toString(), caster, cooldownTicks);
        } catch (Exception e) {
            StackTraceElement top = e.getStackTrace()[0];
            if (top.getMethodName().startsWith("irons_Restrictions")) {
                FallenGemsAffixes.LOGGER.warn("Spell cast failed due to: irons_Restrictions");
            } else {
                FallenGemsAffixes.LOGGER.warn("Spell {} cast failed due to: {}", this.spell, e);
            }
        } finally {
            Objects.requireNonNull(caster.level().getServer()).execute(() -> setTriggering(caster, false));
        }
    }

    private LivingEntity determineTarget(LivingEntity caster, LivingEntity defaultTarget) {
        return this.target
                .map(targetType -> targetType == TargetType.SELF ? caster : defaultTarget)
                .orElse(defaultTarget);
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        return (this.types.isEmpty() || this.types.contains(cat)) && this.values.containsKey(rarity);
    }

    @Override
    public MutableComponent getDescription(ItemStack stack, LootRarity rarity, float affixLevel) {
        TriggerData data = this.values.get(rarity);
        if (data == null) return Component.empty();

        int spellLevel = data.level().min() + Math.round(affixLevel * (data.level().max() - data.level().min()));

        Component coloredSpellName = this.spell.getDisplayName(null).copy()
                .append(" ").append(Component.translatable("enchantment.level." + spellLevel))
                .withStyle(this.spell.getSchoolType().getDisplayName().getStyle());

        boolean isSelfCast = this.target.map(t -> t == TargetType.SELF).orElse(false);
        MutableComponent comp = this.trigger.toComponent(coloredSpellName, isSelfCast);

        int cooldownTicks = this.getCooldown(rarity);
        if (cooldownTicks != 0) {
            Component cd = Component.translatable("affix.apotheosis.cooldown",
                    StringUtil.formatTickDuration(cooldownTicks));
            comp = comp.append(" ").append(cd);
        }

        return comp;
    }

    @Override
    public Component getAugmentingText(ItemStack stack, LootRarity rarity, float affixLevel) {
        TriggerData data = this.values.get(rarity);
        if (data == null) return Component.empty();

        int spellLevel = data.level().min() + Math.round(affixLevel * (data.level().max() - data.level().min()));

        Component coloredSpellName = this.spell.getDisplayName(null).copy()
                .append(" ").append(Component.translatable("enchantment.level." + spellLevel))
                .withStyle(this.spell.getSchoolType().getDisplayName().getStyle());

        boolean isSelfCast = this.target.map(t -> t == TargetType.SELF).orElse(false);
        MutableComponent comp = this.trigger.toComponent(coloredSpellName, isSelfCast);

        int cooldownTicks = this.getCooldown(rarity);
        if (cooldownTicks != 0) {
            Component cd = Component.translatable("affix.apotheosis.cooldown",
                    StringUtil.formatTickDuration(cooldownTicks));
            comp = comp.append(" ").append(cd);
        }

        return comp;
    }

    @Override
    public void doPostAttack(ItemStack stack, LootRarity rarity, float affixLevel,
                             LivingEntity user, @Nullable Entity target) {
        if (this.trigger == TriggerType.MELEE_HIT && target instanceof LivingEntity livingTarget) {
            LivingEntity actualTarget = determineTarget(user, livingTarget);
            triggerSpell(user, actualTarget, rarity, 0);
        }
    }

    @Override
    public void doPostHurt(ItemStack stack, LootRarity rarity, float affixLevel,
                           LivingEntity user, @Nullable Entity attacker) {
        if (this.trigger == TriggerType.HURT && attacker instanceof LivingEntity livingAttacker) {
            LivingEntity defaultTarget = this.target.map(t -> t == TargetType.TARGET ? livingAttacker : user)
                    .orElse(user);
            LivingEntity actualTarget = determineTarget(user, defaultTarget);
            triggerSpell(user, actualTarget, rarity, 0);
        }
    }

    @Override
    public void onArrowImpact(AbstractArrow arrow, LootRarity rarity, float affixLevel,
                              HitResult res, HitResult.Type type) {
        if (this.trigger == TriggerType.PROJECTILE_HIT && type == HitResult.Type.ENTITY &&
                res instanceof EntityHitResult entityHit &&
                entityHit.getEntity() instanceof LivingEntity hitEntity &&
                arrow.getOwner() instanceof LivingEntity owner) {
            LivingEntity actualTarget = determineTarget(owner, hitEntity);
            triggerSpell(owner, actualTarget, rarity, 0);
        }
    }

    public enum TriggerType {
        SPELL_DAMAGE("spell_damage"),
        SPELL_HEAL("spell_heal"),
        MELEE_HIT("melee_hit"),
        PROJECTILE_HIT("projectile_hit"),
        HURT("hurt");

        public static final Codec<TriggerType> CODEC = PlaceboCodecs.enumCodec(TriggerType.class);
        private final String id;

        TriggerType(String id) {
            this.id = id;
        }

        public MutableComponent toComponent(Component spellName, boolean isSelfCast) {
            String key = "affix.fallen_gems_affixes.trigger." + this.id + (isSelfCast ? ".self" : "");
            return Component.translatable(key, spellName);
        }
    }

    public enum TargetType {
        SELF, TARGET;

        public static final Codec<TargetType> CODEC = PlaceboCodecs.enumCodec(TargetType.class);
    }

    public record LevelRange(int min, int max) {
        public static final Codec<LevelRange> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        Codec.INT.fieldOf("min").forGetter(LevelRange::min),
                        Codec.INT.fieldOf("max").forGetter(LevelRange::max)
                ).apply(inst, LevelRange::new)
        );

        public int getRandomLevel(RandomSource random) {
            return min + random.nextInt(max - min + 1);
        }
    }

    public record TriggerData(LevelRange level, int cooldown) {
        public static final Codec<TriggerData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                LevelRange.CODEC.fieldOf("level").forGetter(TriggerData::level),
                PlaceboCodecs.nullableField(Codec.INT, "cooldown", -1).forGetter(TriggerData::cooldown)
        ).apply(inst, TriggerData::new));
    }
}