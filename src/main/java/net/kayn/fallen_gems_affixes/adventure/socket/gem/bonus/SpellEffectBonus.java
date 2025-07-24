package net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.socket.gem.GemView;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import net.kayn.fallen_gems_affixes.adventure.affix.SpellEffectAffix;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

import java.util.Map;

public class SpellEffectBonus extends GemBonus {

    public static final Codec<SpellEffectBonus> CODEC = RecordCodecBuilder.create((inst) -> inst
            .group(
                    gemClass(),
                    BuiltInRegistries.MOB_EFFECT.holderByNameCodec().fieldOf("mob_effect").forGetter((a) -> a.effect),
                    SpellEffectAffix.Target.CODEC.fieldOf("target").forGetter((a) -> a.target),
                    Purity.mapCodec(EffectData.CODEC).fieldOf("values").forGetter((a) -> a.values),
                    Codec.BOOL.optionalFieldOf("stack_on_reapply", false).forGetter((a) -> a.stackOnReapply),
                    Codec.intRange(1, 255).optionalFieldOf("stacking_limit", 255).forGetter(a -> a.stackingLimit))
            .apply(inst, SpellEffectBonus::new));

    protected final Holder<MobEffect> effect;
    public final SpellEffectAffix.Target target;
    protected final Map<Purity, EffectData> values;
    protected final int stackingLimit;
    protected final boolean stackOnReapply;

    public SpellEffectBonus(GemClass gemClass, Holder<MobEffect> effect, SpellEffectAffix.Target target, Map<Purity, EffectData> values, boolean stackOnReapply, int stackingLimit) {
        super(gemClass);
        this.effect = effect;
        this.target = target;
        this.values = values;
        this.stackOnReapply = stackOnReapply;
        this.stackingLimit = stackingLimit;
    }

    @Override
    public Codec<? extends GemBonus> getCodec() {
        return CODEC;
    }

    @Override
    public boolean supports(Purity purity) {
        return this.values.containsKey(purity);
    }

    @Override
    public Component getSocketBonusTooltip(GemView gem, AttributeTooltipContext ctx) {
        MobEffectInstance inst = ((SpellEffectBonus.EffectData) this.values.get(gem.purity())).build(this.effect);
        MutableComponent comp = this.target.toComponent(new Object[]{toComponent(inst, ctx.tickRate())}).withStyle(ChatFormatting.YELLOW);
        int cooldown = this.getCooldown(gem.purity());
        if (cooldown != 0) {
            Component cd = Component.translatable("affix.apotheosis.cooldown", new Object[]{StringUtil.formatTickDuration(cooldown, ctx.tickRate())});
            comp = comp.append(" ").append(cd);
        }

        if (this.stackOnReapply) {
            comp = comp.append(" ").append(Component.translatable("affix.apotheosis.stacking"));
        }

        return comp;
    }

    public void applyEffect(GemInstance gem, LivingEntity target) {
        int cooldown = this.getCooldown(gem.purity());
        if (cooldown == 0 || !Affix.isOnCooldown(makeUniqueId(gem), cooldown, target)) {
            SpellEffectBonus.EffectData data = (SpellEffectBonus.EffectData) this.values.get(gem.purity());
            MobEffectInstance inst = target.getEffect(this.effect);
            if (this.stackOnReapply && inst != null) {
                if (inst != null) {
                    int amplifier = Math.min(this.stackingLimit, (int) (inst.getAmplifier() + 1 + data.amplifier));
                    MobEffectInstance newInst = new MobEffectInstance(this.effect, Math.max(inst.getDuration(), data.duration), amplifier);
                    target.addEffect(newInst);
                }
            } else {
                target.addEffect(data.build(this.effect));
            }

            Affix.startCooldown(makeUniqueId(gem), target);
        }
    }

    protected int getCooldown(Purity purity) {
        SpellEffectBonus.EffectData data = (SpellEffectBonus.EffectData) this.values.get(purity);
        return data.cooldown;
    }

    public static MutableComponent toComponent(MobEffectInstance inst, float tickRate) {
        MutableComponent comp = Component.translatable(inst.getDescriptionId());
        Holder<MobEffect> effect = inst.getEffect();

        if (inst.getAmplifier() > 0) {
            comp = Component.translatable("potion.withAmplifier", comp, Component.translatable("potion.potency." + inst.getAmplifier()));
        }

        if (inst.getDuration() > 20) {
            comp = Component.translatable("potion.withDuration", comp, MobEffectUtil.formatDuration(inst, 1, tickRate));
        }

        return comp.withStyle(effect.value().getCategory().getTooltipFormatting());
    }


    public static record EffectData(int duration, int amplifier, int cooldown) {
        private static Codec<SpellEffectBonus.EffectData> CODEC = RecordCodecBuilder.create((inst) -> inst
                .group(
                        Codec.INT.fieldOf("duration").forGetter(SpellEffectBonus.EffectData::duration),
                        Codec.INT.fieldOf("amplifier").forGetter(SpellEffectBonus.EffectData::amplifier),
                        Codec.INT.optionalFieldOf("cooldown", 0).forGetter(SpellEffectBonus.EffectData::cooldown))
                .apply(inst, SpellEffectBonus.EffectData::new));

        public MobEffectInstance build(Holder<MobEffect> effect) {
            return new MobEffectInstance(effect, this.duration, this.amplifier);
        }
    }
}
