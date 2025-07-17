package net.kayn.fallen_gems_affixes.adventure.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixType;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import dev.shadowsoffire.placebo.util.StepFunction;
import io.redspace.ironsspellbooks.api.events.SpellDamageEvent;
import io.redspace.ironsspellbooks.api.events.SpellHealEvent;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringUtil;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.Set;

public class SpellEffectAffix extends Affix {

    public static final Codec<SpellEffectAffix> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                    ForgeRegistries.MOB_EFFECTS.getCodec().fieldOf("mob_effect").forGetter(a -> a.effect),
                    SpellEffectAffix.Target.CODEC.fieldOf("target").forGetter(a -> a.target),
                    LootRarity.mapCodec(SpellEffectAffix.EffectData.CODEC).fieldOf("values").forGetter(a -> a.values),
                    PlaceboCodecs.nullableField(Codec.INT, "cooldown", 0).forGetter(a -> a.cooldown),
                    LootCategory.SET_CODEC.fieldOf("types").forGetter(a -> a.types),
                    PlaceboCodecs.nullableField(Codec.BOOL, "stack_on_reapply", false).forGetter(a -> a.stackOnReapply),
                    Codec.intRange(1, 255).optionalFieldOf("stacking_limit", 255).forGetter(a -> a.stackingLimit))
            .apply(inst, SpellEffectAffix::new));


    protected final int cooldown;
    protected final MobEffect effect;
    public final Target target;
    protected final Map<LootRarity, EffectData> values;
    protected final Set<LootCategory> types;
    protected final int stackingLimit;
    protected final boolean stackOnReapply;

    public SpellEffectAffix(MobEffect effect, Target target, Map<LootRarity, EffectData> values, int cooldown, Set<LootCategory> types, boolean stackOnReapply, int stackingLimit)
    {
        super(AffixType.ABILITY);
        this.effect = effect;
        this.target = target;
        this.values = values;
        this.cooldown = cooldown;
        this.types = types;
        this.stackOnReapply = stackOnReapply;
        this.stackingLimit = stackingLimit;
    }

    @Override
    public MutableComponent getDescription(ItemStack stack, LootRarity rarity, float level) {
        MobEffectInstance inst = this.values.get(rarity).build(this.effect, level);
        MutableComponent comp = this.target.toComponent(toComponent(inst), toComponent(inst));
        int cooldown = this.getCooldown(rarity);
        if (cooldown != 0) {
            Component cd = Component.translatable("affix.apotheosis.cooldown", StringUtil.formatTickDuration(cooldown));
            comp = comp.append(" ").append(cd);
        }
        if (this.stackOnReapply) {
            comp = comp.append(" ").append(Component.translatable("affix.apotheosis.stacking"));
        }
        return comp;
    }

    @Override
    public Component getAugmentingText(ItemStack stack, LootRarity rarity, float level) {
        MobEffectInstance inst = this.values.get(rarity).build(this.effect, level);
        MutableComponent comp = this.target.toComponent(toComponent(inst));

        MobEffectInstance min = this.values.get(rarity).build(this.effect, 0);
        MobEffectInstance max = this.values.get(rarity).build(this.effect, 1);

        if (min.getAmplifier() != max.getAmplifier()) {
            // Vanilla ships potion.potency.0 as an empty string, so we have to fix that here
            Component minComp = min.getAmplifier() == 0 ? Component.literal("I") : Component.translatable("potion.potency." + min.getAmplifier());
            Component maxComp = Component.translatable("potion.potency." + max.getAmplifier());
            comp.append(valueBounds(minComp, maxComp));
        }

        if (!this.effect.isInstantenous() && min.getDuration() != max.getDuration()) {
            Component minComp = MobEffectUtil.formatDuration(min, 1);
            Component maxComp = MobEffectUtil.formatDuration(max, 1);
            comp.append(valueBounds(minComp, maxComp));
        }

        int cooldown = this.getCooldown(rarity);
        if (cooldown != 0) {
            Component cd = Component.translatable("affix.apotheosis.cooldown", StringUtil.formatTickDuration(cooldown));
            comp = comp.append(" ").append(cd);
        }
        if (this.stackOnReapply) {
            comp = comp.append(" ").append(Component.translatable("affix.apotheosis.stacking"));
        }

        return comp;
    }

    @Override
    public boolean canApplyTo(ItemStack itemStack, LootCategory lootCategory, LootRarity lootRarity) {
        return (this.types.isEmpty() || this.types.contains(lootCategory)) && this.values.containsKey(lootRarity);
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }

    public enum Target {
        SPELL_DAMAGE_SELF("spell_damage_self"),
        SPELL_DAMAGE_TARGET("spell_damage_target"),
        SPELL_HEAL_SELF("spell_heal_self"),
        SPELL_HEAL_TARGET("spell_heal_target");

        public static final Codec<SpellEffectAffix.Target> CODEC = PlaceboCodecs.enumCodec(SpellEffectAffix.Target.class);

        private final String id;

        Target(String id) {
            this.id = id;
        }

        public MutableComponent toComponent(Object... args) {
            return Component.translatable("affix.fallen_gems_affixes.target." + this.id, args);
        }
    }

    public void applyEffect(LivingEntity target, LootRarity rarity, float level) {
        if (target.level().isClientSide()) return;

        int cooldown = this.getCooldown(rarity);
        if (cooldown != 0 && isOnCooldown(this.getId(), cooldown, target)) return;
        SpellEffectAffix.EffectData data = this.values.get(rarity);
        var inst = target.getEffect(this.effect);
        if (this.stackOnReapply && inst != null) {
            if (inst != null) {
                int amplifier = Math.min(this.stackingLimit, (int) (inst.getAmplifier() + 1 + data.amplifier.get(level)));
                var newInst = new MobEffectInstance(this.effect, (int) Math.max(inst.getDuration(), data.duration.get(level)), amplifier);
                target.addEffect(newInst);
            }
        }
        else {
            target.addEffect(data.build(this.effect, level));
        }
        startCooldown(this.getId(), target);
    }

    protected int getCooldown(LootRarity rarity) {
        SpellEffectAffix.EffectData data = this.values.get(rarity);
        if (data.cooldown != -1) return data.cooldown;
        return this.cooldown;
    }

    public static Component toComponent(MobEffectInstance inst) {
        MutableComponent mutablecomponent = Component.translatable(inst.getDescriptionId());
        MobEffect mobeffect = inst.getEffect();

        if (inst.getAmplifier() > 0) {
            mutablecomponent = Component.translatable("potion.withAmplifier", mutablecomponent, Component.translatable("potion.potency." + inst.getAmplifier()));
        }

        if (inst.getDuration() > 20) {
            mutablecomponent = Component.translatable("potion.withDuration", mutablecomponent, MobEffectUtil.formatDuration(inst, 1));
        }

        return mutablecomponent.withStyle(mobeffect.getCategory().getTooltipFormatting());
    }


    public static record EffectData(StepFunction duration, StepFunction amplifier, int cooldown) {

        private static final Codec<SpellEffectAffix.EffectData> CODEC = RecordCodecBuilder.create(inst -> inst
                .group(
                        StepFunction.CODEC.fieldOf("duration").forGetter(SpellEffectAffix.EffectData::duration),
                        StepFunction.CODEC.fieldOf("amplifier").forGetter(SpellEffectAffix.EffectData::amplifier),
                        PlaceboCodecs.nullableField(Codec.INT, "cooldown", -1).forGetter(SpellEffectAffix.EffectData::cooldown))
                .apply(inst, SpellEffectAffix.EffectData::new));

        public MobEffectInstance build(MobEffect effect, float level) {
            return new MobEffectInstance(effect, this.duration.getInt(level), this.amplifier.getInt(level));
        }
    }
}
