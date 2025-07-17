package net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.PotionAffix.Target;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemItem;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.attributeslib.AttributesLib;
import dev.shadowsoffire.attributeslib.api.IFormattableAttribute;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

public class AttributeEffectBonus extends GemBonus {

    public static final Codec<AttributeEffectBonus> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                    gemClass(),
                    ForgeRegistries.ATTRIBUTES.getCodec().fieldOf("attribute").forGetter(a -> a.attribute),
                    PlaceboCodecs.enumCodec(Operation.class).fieldOf("operation").forGetter(a -> a.operation),
                    VALUES_CODEC.fieldOf("attribute_values").forGetter(a -> a.attributeValues),
                    ForgeRegistries.MOB_EFFECTS.getCodec().fieldOf("mob_effect").forGetter(a -> a.effect),
                    Target.CODEC.fieldOf("target").forGetter(a -> a.target),
                    LootRarity.mapCodec(EffectData.CODEC).fieldOf("effect_values").forGetter(a -> a.effectValues),
                    PlaceboCodecs.nullableField(Codec.BOOL, "stack_on_reapply", false).forGetter(a -> a.stackOnReapply))
            .apply(inst, AttributeEffectBonus::new));

    protected final Attribute attribute;
    protected final Operation operation;
    protected final Map<LootRarity, StepFunction> attributeValues;
    protected final MobEffect effect;
    protected final Target target;
    protected final Map<LootRarity, EffectData> effectValues;
    protected final boolean stackOnReapply;

    public AttributeEffectBonus(GemClass gemClass, Attribute attribute, Operation operation, Map<LootRarity, StepFunction> attributeValues,
                                MobEffect effect, Target target, Map<LootRarity, EffectData> effectValues, boolean stackOnReapply) {
        super(new ResourceLocation("fallen_gems_affixes", "attribute_effect"), gemClass);
        this.attribute = attribute;
        this.operation = operation;
        this.attributeValues = attributeValues;
        this.effect = effect;
        this.target = target;
        this.effectValues = effectValues;
        this.stackOnReapply = stackOnReapply;
    }

    @Override
    public void addModifiers(ItemStack gem, LootRarity rarity, BiConsumer<Attribute, AttributeModifier> map) {
        map.accept(this.attribute, this.readAttributeModifier(gem, rarity, GemItem.getUUIDs(gem).get(0)));
    }

    @Override
    public Component getSocketBonusTooltip(ItemStack gem, LootRarity rarity) {
        Component attributeTooltip = IFormattableAttribute.toComponent(this.attribute, this.readAttributeModifier(gem, rarity, UUID.randomUUID()), AttributesLib.getTooltipFlag());

        MobEffectInstance inst = this.effectValues.get(rarity).build(this.effect);
        MutableComponent effectTooltip = this.target.toComponent(toComponent(inst)).withStyle(ChatFormatting.YELLOW);

        int cooldown = this.getCooldown(rarity);
        if (cooldown != 0) {
            Component cd = Component.translatable("affix.apotheosis.cooldown", StringUtil.formatTickDuration(cooldown));
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
    public int getNumberOfUUIDs() {
        return 1;
    }

    @Override
    public Codec<? extends GemBonus> getCodec() {
        return CODEC;
    }

    @Override
    public boolean supports(LootRarity rarity) {
        return this.attributeValues.containsKey(rarity) && this.effectValues.containsKey(rarity);
    }

    @Override
    public void doPostHurt(ItemStack gem, LootRarity rarity, LivingEntity user, Entity attacker) {
        if (this.target == Target.HURT_SELF) this.applyEffect(gem, user, rarity);
        else if (this.target == Target.HURT_ATTACKER) {
            if (attacker instanceof LivingEntity tLiving) {
                this.applyEffect(gem, tLiving, rarity);
            }
        }
    }

    @Override
    public void doPostAttack(ItemStack gem, LootRarity rarity, LivingEntity user, Entity target) {
        if (this.target == Target.ATTACK_SELF) this.applyEffect(gem, user, rarity);
        else if (this.target == Target.ATTACK_TARGET) {
            if (target instanceof LivingEntity tLiving) {
                this.applyEffect(gem, tLiving, rarity);
            }
        }
    }

    @Override
    public void onBlockBreak(ItemStack gem, LootRarity rarity, Player player, LevelAccessor world, BlockPos pos, BlockState state) {
        if (this.target == Target.BREAK_SELF) {
            this.applyEffect(gem, player, rarity);
        }
    }

    @Override
    public void onArrowImpact(ItemStack gemStack, LootRarity rarity, AbstractArrow arrow, HitResult res) {
        if (this.target == Target.ARROW_SELF) {
            if (arrow.getOwner() instanceof LivingEntity owner) {
                this.applyEffect(gemStack, owner, rarity);
            }
        } else if (this.target == Target.ARROW_TARGET) {
            if (res.getType() == Type.ENTITY && ((EntityHitResult) res).getEntity() instanceof LivingEntity target) {
                this.applyEffect(gemStack, target, rarity);
            }
        }
    }

    @Override
    public float onShieldBlock(ItemStack gem, LootRarity rarity, LivingEntity entity, DamageSource source, float amount) {
        if (this.target == Target.BLOCK_SELF) {
            this.applyEffect(gem, entity, rarity);
        } else if (this.target == Target.BLOCK_ATTACKER && source.getDirectEntity() instanceof LivingEntity target) {
            this.applyEffect(gem, target, rarity);
        }
        return amount;
    }

    protected int getCooldown(LootRarity rarity) {
        EffectData data = this.effectValues.get(rarity);
        return data.cooldown;
    }

    private void applyEffect(ItemStack gemStack, LivingEntity target, LootRarity rarity) {
        int cooldown = this.getCooldown(rarity);
        if (cooldown != 0 && Affix.isOnCooldown(this.getCooldownId(gemStack), cooldown, target)) return;

        EffectData data = this.effectValues.get(rarity);
        var inst = target.getEffect(this.effect);
        if (this.stackOnReapply && inst != null) {
            var newInst = new MobEffectInstance(this.effect, Math.max(inst.getDuration(), data.duration), inst.getAmplifier() + 1 + data.amplifier);
            target.addEffect(newInst);
        } else {
            target.addEffect(data.build(this.effect));
        }
        Affix.startCooldown(this.getCooldownId(gemStack), target);
    }

    public AttributeModifier readAttributeModifier(ItemStack gem, LootRarity rarity, UUID id) {
        return new AttributeModifier(id, "apoth.gem_modifier", this.attributeValues.get(rarity).get(0), this.operation);
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

    public record EffectData(int duration, int amplifier, int cooldown) {

        private static final Codec<EffectData> CODEC = RecordCodecBuilder.create(inst -> inst
                .group(
                        Codec.INT.fieldOf("duration").forGetter(EffectData::duration),
                        Codec.INT.fieldOf("amplifier").forGetter(EffectData::amplifier),
                        PlaceboCodecs.nullableField(Codec.INT, "cooldown", 0).forGetter(EffectData::cooldown))
                .apply(inst, EffectData::new));

        public MobEffectInstance build(MobEffect effect) {
            return new MobEffectInstance(effect, this.duration, this.amplifier);
        }
    }

    @Override
    public AttributeEffectBonus validate() {
        Preconditions.checkNotNull(this.attribute, "Invalid AttributeEffectBonus with null attribute");
        Preconditions.checkNotNull(this.operation, "Invalid AttributeEffectBonus with null operation");
        Preconditions.checkNotNull(this.attributeValues, "Invalid AttributeEffectBonus with null attribute values");
        Preconditions.checkNotNull(this.effect, "Null mob effect");
        Preconditions.checkNotNull(this.target, "Null target");
        Preconditions.checkNotNull(this.effectValues, "Null effect values map");
        return this;
    }
}