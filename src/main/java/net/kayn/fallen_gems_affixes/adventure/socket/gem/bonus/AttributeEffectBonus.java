package net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.effect.MobEffectAffix.Target;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.mixin.LivingEntityInvoker;
import dev.shadowsoffire.apotheosis.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.socket.gem.GemView;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.MobEffectBonus;
import dev.shadowsoffire.apothic_attributes.modifiers.StackAttributeModifiersEvent;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import dev.shadowsoffire.placebo.util.StepFunction;
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
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
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

import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class AttributeEffectBonus extends GemBonus {

    public static final Codec<AttributeEffectBonus> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                    gemClass(),
                    BuiltInRegistries.ATTRIBUTE.holderByNameCodec().fieldOf("attribute").forGetter(a -> a.attribute),
                    PlaceboCodecs.enumCodec(Operation.class).fieldOf("operation").forGetter(a -> a.operation),
                    Purity.mapCodec(Codec.DOUBLE).fieldOf("attribute_values").forGetter(a -> a.attributeValues),
                    BuiltInRegistries.MOB_EFFECT.holderByNameCodec().fieldOf("mob_effect").forGetter(a -> a.effect),
                    Target.CODEC.fieldOf("target").forGetter(a -> a.target),
                    Purity.mapCodec(EffectData.CODEC).fieldOf("effect_values").forGetter(a -> a.effectValues),
                    Codec.BOOL.optionalFieldOf("stack_on_reapply", false).forGetter(a -> a.stackOnReapply),
                    Codec.intRange(1, 255).optionalFieldOf("stacking_limit", 255).forGetter(a -> a.stackingLimit))
            .apply(inst, AttributeEffectBonus::new));

    protected final Holder<Attribute> attribute;
    protected final Operation operation;
    protected final Map<Purity, Double> attributeValues;
    protected final Holder<MobEffect> effect;
    protected final Target target;
    protected final Map<Purity, EffectData> effectValues;
    protected final boolean stackOnReapply;
    protected final int stackingLimit;

    public AttributeEffectBonus(GemClass gemClass, Holder<Attribute> attribute, Operation operation, Map<Purity, Double> attributeValues,
                                Holder<MobEffect> effect, Target target, Map<Purity, EffectData> effectValues, boolean stackOnReapply, int stackingLimit) {
        super(gemClass);
        this.attribute = attribute;
        this.operation = operation;
        this.attributeValues = attributeValues;
        this.effect = effect;
        this.target = target;
        this.effectValues = effectValues;
        this.stackOnReapply = stackOnReapply;
        this.stackingLimit = stackingLimit;
    }

    @Override
    public void addModifiers(GemInstance gem, StackAttributeModifiersEvent event) {
        event.addModifier(this.attribute, this.createModifier(gem), gem.category().getSlots());
    }

    @Override
    public void skipModifierIds(GemInstance gem, Consumer<ResourceLocation> skip) {
        skip.accept(makeUniqueId(gem));
    }

    @Override
    public boolean supports(Purity purity) {
        return this.attributeValues.containsKey(purity) && this.effectValues.containsKey(purity);
    }

    @Override
    public Component getSocketBonusTooltip(GemView gem, AttributeTooltipContext ctx) {
        Component attributeTooltip = this.attribute.value().toComponent(this.createModifier(gem), ctx.flag());

        MobEffectInstance inst = this.effectValues.get(gem.purity()).build(this.effect);
        MutableComponent effectTooltip = this.target.toComponent(toComponent(inst, ctx.tickRate())).withStyle(ChatFormatting.YELLOW);

        int cooldown = this.getCooldown(gem.purity());
        if (cooldown != 0) {
            Component cd = Component.translatable("affix.apotheosis.cooldown", StringUtil.formatTickDuration(cooldown, ctx.tickRate()));
            effectTooltip = effectTooltip.append(" ").append(cd);
        }
        if (this.stackOnReapply) {
            effectTooltip = effectTooltip.append(" ").append(Component.translatable("affix.apotheosis.stacking"));
        }

        return Component.empty()
                .append(attributeTooltip)
                .append(Component.literal(" • ").withStyle(ChatFormatting.YELLOW))
                .append(effectTooltip);
    }

    @Override
    public Codec<? extends GemBonus> getCodec() {
        return CODEC;
    }

    @Override
    public void doPostHurt(GemInstance inst, LivingEntity user, DamageSource source) {
        if (this.target == Target.HURT_SELF) {
            this.applyEffect(inst, user);
        }
        else if (this.target == Target.HURT_ATTACKER) {
            if (source.getEntity() instanceof LivingEntity tLiving) {
                this.applyEffect(inst, tLiving);
            }
        }
    }

    @Override
    public void doPostAttack(GemInstance inst, LivingEntity user, Entity target) {
        if (this.target == Target.ATTACK_SELF) {
            this.applyEffect(inst, user);
        }
        else if (this.target == Target.ATTACK_TARGET) {
            if (target instanceof LivingEntity tLiving) {
                this.applyEffect(inst, tLiving);
            }
        }
    }

    @Override
    public void onBlockBreak(GemInstance inst, Player player, LevelAccessor world, BlockPos pos, BlockState state) {
        if (this.target == Target.BREAK_SELF) {
            this.applyEffect(inst, player);
        }
    }

    @Override
    public void onProjectileImpact(GemInstance inst, Projectile proj, HitResult res) {
        if (res.getType() == Type.ENTITY && ((EntityHitResult) res).getEntity() instanceof LivingEntity target) {
            switch (this.target) {
                case ARROW_SELF -> {
                    if (proj instanceof AbstractArrow && proj.getOwner() instanceof LivingEntity owner) {
                        this.applyEffect(inst, owner);
                    }
                }
                case ARROW_TARGET -> {
                    if (proj instanceof AbstractArrow) {
                        this.applyEffect(inst, target);
                    }
                }
                case PROJECTILE_SELF -> {
                    if (proj.getOwner() instanceof LivingEntity owner) {
                        this.applyEffect(inst, owner);
                    }
                }
                case PROJECTILE_TARGET -> {
                    this.applyEffect(inst, target);
                }
                default -> {}
            }
        }
    }

    @Override
    public float onShieldBlock(GemInstance inst, LivingEntity entity, DamageSource source, float amount) {
        if (this.target == Target.BLOCK_SELF) {
            this.applyEffect(inst, entity);
        }
        else if (this.target == Target.BLOCK_ATTACKER && source.getDirectEntity() instanceof LivingEntity target) {
            this.applyEffect(inst, target);
        }
        return amount;
    }

    protected int getCooldown(Purity purity) {
        EffectData data = this.effectValues.get(purity);
        return data.cooldown;
    }

    private void applyEffect(GemInstance inst, LivingEntity target) {
        int cooldown = this.getCooldown(inst.purity());
        if (cooldown != 0 && Affix.isOnCooldown(makeUniqueId(inst), cooldown, target)) {
            return;
        }
        EffectData data = this.effectValues.get(inst.purity());
        MobEffectInstance effectInst = target.getEffect(this.effect);
        if (this.stackOnReapply && effectInst != null) {
            if (inst != null) {
                int duration = Math.max(effectInst.getDuration(), data.duration);
                int amp = Math.min(this.stackingLimit, effectInst.getAmplifier() + 1 + data.amplifier);
                var newInst = new MobEffectInstance(this.effect, duration, amp, effectInst.isAmbient(), effectInst.isVisible());
                effectInst.update(newInst);
                ((LivingEntityInvoker) target).callOnEffectUpdated(effectInst, true, null);
                effectInst.onEffectStarted(target);
            }
        }
        else {
            target.addEffect(data.build(this.effect));
        }
        Affix.startCooldown(makeUniqueId(inst), target);
    }

    public AttributeModifier createModifier(GemView gem) {
        double value = this.attributeValues.get(gem.purity());
        return new AttributeModifier(makeUniqueId(gem), value, this.operation);
    }

    public static Component toComponent(MobEffectInstance inst, float tickRate) {
        MutableComponent mutablecomponent = Component.translatable(inst.getDescriptionId());
        Holder<MobEffect> mobEffect = inst.getEffect();

        if (inst.getAmplifier() > 0) {
            mutablecomponent = Component.translatable("potion.withAmplifier", mutablecomponent, Component.translatable("potion.potency." + inst.getAmplifier()));
        }

        if (inst.getDuration() > 20) {
            mutablecomponent = Component.translatable("potion.withDuration", mutablecomponent, MobEffectUtil.formatDuration(inst, 1, tickRate));
        }

        return mutablecomponent.withStyle(mobEffect.value().getCategory().getTooltipFormatting());
    }

    public record EffectData(int duration, int amplifier, int cooldown) {

        private static final Codec<EffectData> CODEC = RecordCodecBuilder.create(inst -> inst
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