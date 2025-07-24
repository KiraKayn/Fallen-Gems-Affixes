package net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.effect.MobEffectAffix.Target;
import dev.shadowsoffire.apotheosis.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.socket.gem.GemView;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

import java.util.*;

public class MultiEffectBonus extends GemBonus {

    public static Codec<MultiEffectBonus> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                    gemClass(),
                    EffectInst.CODEC.listOf().fieldOf("effects").forGetter(a -> a.effects),
                    Target.CODEC.fieldOf("target").forGetter(a -> a.target),
                    Codec.BOOL.optionalFieldOf("stack_on_reapply", false).forGetter(a -> a.stackOnReapply),
                    Codec.STRING.fieldOf("desc").forGetter(a -> a.desc))
            .apply(inst, MultiEffectBonus::new));

    protected final List<EffectInst> effects;
    protected final Target target;
    protected final boolean stackOnReapply;
    protected final String desc;

    public MultiEffectBonus(GemClass gemClass, List<EffectInst> effects, Target target, boolean stackOnReapply, String desc) {
        super(gemClass);
        this.effects = effects;
        this.target = target;
        this.stackOnReapply = stackOnReapply;
        this.desc = desc;
    }

    @Override
    public Codec<? extends GemBonus> getCodec() {
        return CODEC;
    }

    @Override
    public void doPostHurt(GemInstance inst, LivingEntity user, DamageSource source) {
        if (this.target == Target.HURT_SELF) applyEffects(inst.gemStack(), user, inst.purity());
        else if (this.target == Target.HURT_ATTACKER && user instanceof LivingEntity tLiving) {
            applyEffects(inst.gemStack(), tLiving, inst.purity());
        }
    }

    @Override
    public void doPostAttack(GemInstance inst, LivingEntity user, Entity target) {
        if (this.target == Target.ATTACK_SELF) applyEffects(inst.gemStack(), user, inst.purity());
        else if (this.target == Target.ATTACK_TARGET && target instanceof LivingEntity tLiving) {
            applyEffects(inst.gemStack(), tLiving, inst.purity());
        }
    }

    @Override
    public void onBlockBreak(GemInstance inst, Player player, LevelAccessor world, BlockPos pos, BlockState state) {
        if (this.target == Target.BREAK_SELF) {
            applyEffects(inst.gemStack(), player, inst.purity());
        }
    }

    @Override
    public void onProjectileImpact(GemInstance inst, Projectile proj, HitResult res) {
        if (this.target == Target.ARROW_SELF && proj.getOwner() instanceof LivingEntity owner) {
            applyEffects(inst.gemStack(), owner, inst.purity());
        } else if (this.target == Target.ARROW_TARGET && res.getType() == Type.ENTITY && ((EntityHitResult) res).getEntity() instanceof LivingEntity target) {
            applyEffects(inst.gemStack(), target, inst.purity());
        }
    }

    @Override
    public float onShieldBlock(GemInstance inst, LivingEntity entity, DamageSource source, float amount) {
        if (this.target == Target.BLOCK_SELF) {
            applyEffects(inst.gemStack(), entity, inst.purity());
        } else if (this.target == Target.BLOCK_ATTACKER && source.getDirectEntity() instanceof LivingEntity target) {
            applyEffects(inst.gemStack(), target, inst.purity());
        }
        return amount;
    }

    private void applyEffects(ItemStack gemStack, LivingEntity target, Purity purity) {
        for (EffectInst effectInst : this.effects) {
            applySingleEffect(gemStack, target, purity, effectInst);
        }
    }

    private void applySingleEffect(ItemStack gemStack, LivingEntity target, Purity purity, EffectInst effectInst) {
        EffectData data = effectInst.values.get(purity);
        if (data == null) return;

        int cooldown = data.cooldown;

        ResourceLocation cooldownId = ResourceLocation.fromNamespaceAndPath(
                this.getTypeKey().getNamespace(),
                this.getTypeKey().getPath() + "_" + BuiltInRegistries.MOB_EFFECT.getKey(effectInst.effect.value()).getPath()
        );

        if (cooldown > 0 && Affix.isOnCooldown(cooldownId, cooldown, target)) return;

        MobEffectInstance newInst;
        MobEffectInstance existing = target.getEffect(effectInst.effect);

        if (this.stackOnReapply && existing != null) {
            newInst = new MobEffectInstance(effectInst.effect,
                    Math.max(existing.getDuration(), data.duration),
                    existing.getAmplifier() + 1 + data.amplifier);
        } else {
            newInst = data.build(effectInst.effect);
        }

        target.addEffect(newInst);

        if (cooldown > 0) {
            Affix.startCooldown(cooldownId, target);
        }
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

    @Override
    public boolean supports(Purity purity) {
        return this.effects.getFirst().values.containsKey(purity);
    }

    @Override
    public Component getSocketBonusTooltip(GemView gem, AttributeTooltipContext ctx) {
        Object[] values = new Object[this.effects.size()];
        int i = 0;
        for (EffectInst effectInst : this.effects) {
            MobEffectInstance inst = effectInst.values.get(gem.purity()).build(effectInst.effect);
            MutableComponent effectComp = toComponent(inst, ctx.tickRate());
            int cooldown = effectInst.values.get(gem.purity()).cooldown;
            if (cooldown != 0) {
                Component cd = Component.translatable("affix.apotheosis.cooldown", StringUtil.formatTickDuration(cooldown, ctx.tickRate())).withStyle(ChatFormatting.YELLOW);
                effectComp = effectComp.append(" ").append(cd);
            }
            values[i++] = effectComp;
        }

        MutableComponent comp = this.target.toComponent(Component.translatable(this.desc, values)).withStyle(ChatFormatting.YELLOW);

        if (this.stackOnReapply) {
            comp = comp.append(" ").append(Component.translatable("affix.apotheosis.stacking"));
        }

        return comp;
    }

    protected static record EffectInst(Holder<MobEffect> effect, Map<Purity, EffectData> values) {
        public static Codec<EffectInst> CODEC = RecordCodecBuilder.create(inst -> inst
                .group(
                        BuiltInRegistries.MOB_EFFECT.holderByNameCodec().fieldOf("mob_effect").forGetter(EffectInst::effect),
                        Purity.mapCodec(EffectData.CODEC).fieldOf("values").forGetter(EffectInst::values))
                .apply(inst, EffectInst::new));
    }

    public static record EffectData(int duration, int amplifier, int cooldown) {
        public static Codec<EffectData> CODEC = RecordCodecBuilder.create(inst -> inst
                .group(
                        Codec.INT.fieldOf("duration").forGetter(EffectData::duration),
                        Codec.INT.fieldOf("amplifier").forGetter(EffectData::amplifier),
                        Codec.INT.optionalFieldOf("cooldown", 0).forGetter(EffectData::cooldown))
                .apply(inst, EffectData::new));

        public MobEffectInstance build(Holder<MobEffect> effect) {
            return new MobEffectInstance(effect, this.duration, this.amplifier);
        }
    }
}