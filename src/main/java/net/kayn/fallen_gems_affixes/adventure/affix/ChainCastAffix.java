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
import net.kayn.fallen_gems_affixes.event.SpellEventHandler;
import net.kayn.fallen_gems_affixes.util.DelayedTaskScheduler;
import net.kayn.fallen_gems_affixes.util.SpellCastUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class ChainCastAffix extends Affix {

    private static final int MIN_DELAY_TICKS = 2; // minimum safe value

    private static final Map<String, Map<UUID, Long>> COOLDOWNS = new HashMap<>();

    public static boolean isOnCooldown(String id, LivingEntity entity) {
        Map<UUID, Long> cdMap = COOLDOWNS.getOrDefault(id, Collections.emptyMap());
        return cdMap.getOrDefault(entity.getUUID(), 0L) > entity.level().getGameTime();
    }

    public static void startCooldown(String id, LivingEntity entity, int durationTicks) {
        COOLDOWNS.computeIfAbsent(id, k -> new HashMap<>())
                .put(entity.getUUID(), entity.level().getGameTime() + durationTicks);
    }

    public static final Codec<ChainCastAffix> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            SpellRegistry.REGISTRY.get().getCodec()
                    .optionalFieldOf("trigger_spell")
                    .forGetter(a -> a.triggerSpell),
            SpellRegistry.REGISTRY.get().getCodec()
                    .fieldOf("spell")
                    .forGetter(a -> a.spell),
            Codec.INT
                    .optionalFieldOf("cooldown_between_casts", MIN_DELAY_TICKS)
                    .forGetter(a -> a.cooldownBetweenCasts),
            LootRarity.mapCodec(SpellCastAffix.TriggerData.CODEC)
                    .fieldOf("values")
                    .forGetter(a -> a.values),
            PlaceboCodecs.nullableField(Codec.INT, "cooldown", 0)
                    .forGetter(a -> a.cooldown),
            LootCategory.SET_CODEC
                    .fieldOf("types")
                    .forGetter(a -> a.types),
            SpellCastAffix.TargetType.CODEC
                    .optionalFieldOf("target")
                    .forGetter(a -> a.target)
    ).apply(inst, ChainCastAffix::new));

    public final Optional<AbstractSpell> triggerSpell;
    protected final AbstractSpell spell;
    protected final int cooldownBetweenCasts;
    protected final Map<LootRarity, SpellCastAffix.TriggerData> values;
    protected final int cooldown;
    protected final Set<LootCategory> types;
    public final Optional<SpellCastAffix.TargetType> target;

    public ChainCastAffix(
            Optional<AbstractSpell> triggerSpell,
            AbstractSpell spell,
            int cooldownBetweenCasts,
            Map<LootRarity, SpellCastAffix.TriggerData> values,
            int cooldown,
            Set<LootCategory> types,
            Optional<SpellCastAffix.TargetType> target) {
        super(AffixType.ABILITY);
        this.triggerSpell = triggerSpell;
        this.spell = spell;
        this.cooldownBetweenCasts = cooldownBetweenCasts;
        this.values = values;
        this.cooldown = cooldown;
        this.types = types;
        this.target = target;
    }


    public void onSpellCast(
            LivingEntity caster,
            String castSpellId,
            LivingEntity spellTarget,
            LootRarity rarity) {

        if (caster.level().isClientSide()) return;
        if (SpellCastAffix.isCurrentlyTriggering(caster)) return;
        if (isOnCooldown(this.getId().toString(), caster)) return;

        if (castSpellId == null) {
            if (this.triggerSpell.isPresent()) return;
        } else {
            if (this.triggerSpell.isPresent() &&
                    !this.triggerSpell.get().getSpellId().equals(castSpellId)) return;
        }

        SpellCastAffix.TriggerData data = this.values.get(rarity);
        if (data == null) return;

        if (this.cooldownBetweenCasts < MIN_DELAY_TICKS) {
            FallenGemsAffixes.LOGGER.error(
                    "ChainCastAffix [{}]: cooldown_between_casts is {} tick(s), minimum is {} (0.1s). " +
                            "The spell will not cast correctly. Please set it to at least {}.",
                    this.getId(), this.cooldownBetweenCasts, MIN_DELAY_TICKS, MIN_DELAY_TICKS);
            return;
        }

        int spellLevel = data.level().getRandomLevel(caster.getRandom());
        LivingEntity actualTarget = resolveTarget(caster, spellTarget);

        int cooldownTicks = getEffectiveCooldown(rarity);
        if (cooldownTicks > 0) {
            startCooldown(this.getId().toString(), caster, cooldownTicks);
        }

        MinecraftServer server = caster.level().getServer();
        if (server == null) return;

        DelayedTaskScheduler.schedule(caster.level(), this.cooldownBetweenCasts,
                () -> waitForCastThenFire(caster, actualTarget, spellLevel, server));
    }

    private void waitForCastThenFire(LivingEntity caster, LivingEntity actualTarget, int spellLevel, MinecraftServer server) {
        if (caster.isRemoved()) return;

        MagicData magicData = MagicData.getPlayerMagicData(caster);
        if (magicData.isCasting()) {
            DelayedTaskScheduler.schedule(caster.level(), 1,
                    () -> waitForCastThenFire(caster, actualTarget, spellLevel, server));
            return;
        }

        SpellCastAffix.setTriggering(caster, true);
        try {
            // Reset i-frames on the entity the triggering spell actually hit
            // actualTarget may be the caster for non-damage triggered chaincasts,
            // so look up the real last-hit entity the same way the echo handler does
            if (caster.level() instanceof ServerLevel sl) {
                UUID lastTargetId = SpellEventHandler.LAST_SPELL_DAMAGE_TARGET.get(caster.getUUID());
                if (lastTargetId != null) {
                    Entity lastTarget = sl.getEntity(lastTargetId);
                    if (lastTarget instanceof LivingEntity le && !le.isRemoved()) {
                        le.invulnerableTime = 0;
                    }
                }
            }
            if (!actualTarget.isRemoved()) {
                actualTarget.invulnerableTime = 0;
            }
            SpellCastUtil.castSpell(caster, this.spell, spellLevel, actualTarget);
        } catch (Exception e) {
            FallenGemsAffixes.LOGGER.warn(
                    "ChainCastAffix: spell {} cast failed: {}",
                    this.spell.getSpellId(), e.getMessage());
        } finally {
            server.execute(() -> SpellCastAffix.setTriggering(caster, false));
        }
    }

    private LivingEntity resolveTarget(LivingEntity caster, LivingEntity defaultTarget) {
        if (this.target.isPresent() && this.target.get() == SpellCastAffix.TargetType.SELF) {
            return caster;
        }
        return defaultTarget != null ? defaultTarget : caster;
    }

    private int getEffectiveCooldown(LootRarity rarity) {
        SpellCastAffix.TriggerData data = this.values.get(rarity);
        if (data != null && data.cooldown() >= 0) return data.cooldown();
        return this.cooldown;
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
        return buildDescription(rarity, affixLevel);
    }

    @Override
    public Component getAugmentingText(ItemStack stack, LootRarity rarity, float affixLevel) {
        return buildDescription(rarity, affixLevel);
    }

    private MutableComponent buildDescription(LootRarity rarity, float affixLevel) {
        SpellCastAffix.TriggerData data = this.values.get(rarity);
        if (data == null) return Component.empty();

        int spellLevel = data.level().min() +
                Math.round(affixLevel * (data.level().max() - data.level().min()));

        Component coloredSpellName = this.spell.getDisplayName(null).copy()
                .append(" ")
                .append(Component.translatable("enchantment.level." + spellLevel))
                .withStyle(this.spell.getSchoolType().getDisplayName().getStyle());

        MutableComponent comp = Component.translatable(
                "affix.fallen_gems_affixes.chain_cast",
                coloredSpellName
        ).withStyle(ChatFormatting.YELLOW);

        Component triggerName = this.triggerSpell
                .map(ts -> (Component) ts.getDisplayName(null).copy()
                        .withStyle(ts.getSchoolType().getDisplayName().getStyle()))
                .orElseGet(() -> Component.translatable("affix.fallen_gems_affixes.chain_cast.any_spell")
                        .withStyle(ChatFormatting.YELLOW));
        comp.append(
                Component.translatable("affix.fallen_gems_affixes.chain_cast.trigger", triggerName)
                        .withStyle(ChatFormatting.YELLOW)
        );

        int cooldownTicks = getEffectiveCooldown(rarity);
        if (cooldownTicks != 0) {
            comp.append(
                    Component.literal(" ")
                            .append(Component.translatable("affix.apotheosis.cooldown",
                                    StringUtil.formatTickDuration(cooldownTicks)))
                            .withStyle(ChatFormatting.YELLOW)
            );
        }

        return comp;
    }
}