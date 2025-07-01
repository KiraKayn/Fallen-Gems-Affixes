package net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus;

import java.util.*;
import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.PotionAffix.Target;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
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
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.registries.ForgeRegistries;

public class MultiEffectBonus extends GemBonus {

    public static Codec<MultiEffectBonus> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                    gemClass(),
                    EffectInst.CODEC.listOf().fieldOf("effects").forGetter(a -> a.effects),
                    Target.CODEC.fieldOf("target").forGetter(a -> a.target),
                    PlaceboCodecs.nullableField(Codec.BOOL, "stack_on_reapply", false).forGetter(a -> a.stackOnReapply),
                    Codec.STRING.fieldOf("desc").forGetter(a -> a.desc))
            .apply(inst, MultiEffectBonus::new));

    protected final List<EffectInst> effects;
    protected final Target target;
    protected final boolean stackOnReapply;
    protected final String desc;

    public MultiEffectBonus(GemClass gemClass, List<EffectInst> effects, Target target, boolean stackOnReapply, String desc) {
        super(new ResourceLocation("fallen_gems_affixes", "multi_effect"), gemClass);
        this.effects = effects;
        this.target = target;
        this.stackOnReapply = stackOnReapply;
        this.desc = desc;
    }

    @Override
    public Component getSocketBonusTooltip(ItemStack gem, LootRarity rarity) {
        Object[] values = new Object[this.effects.size()];
        int i = 0;
        for (EffectInst effectInst : this.effects) {
            MobEffectInstance inst = effectInst.values.get(rarity).build(effectInst.effect);
            MutableComponent effectComp = toComponent(inst);
            int cooldown = effectInst.values.get(rarity).cooldown;
            if (cooldown != 0) {
                Component cd = Component.translatable("affix.apotheosis.cooldown", StringUtil.formatTickDuration(cooldown)).withStyle(ChatFormatting.YELLOW);
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

    @Override
    public MultiEffectBonus validate() {
        Preconditions.checkNotNull(this.effects, "Invalid MultiEffectBonus with null effects");
        Preconditions.checkNotNull(this.target, "Invalid MultiEffectBonus with null target");

        List<Set<LootRarity>> rarityChecks = new ArrayList<>();
        for (EffectInst inst : this.effects) {
            var set = new HashSet<LootRarity>();
            RarityRegistry.INSTANCE.getValues().stream().filter(inst.values::containsKey).forEach(set::add);
            rarityChecks.add(set);
        }
        Preconditions.checkArgument(rarityChecks.stream().mapToInt(Set::size).allMatch(size -> size == rarityChecks.get(0).size()));
        return this;
    }

    @Override
    public boolean supports(LootRarity rarity) {
        return this.effects.get(0).values.containsKey(rarity);
    }

    @Override
    public int getNumberOfUUIDs() {
        return 0;
    }

    @Override
    public Codec<? extends GemBonus> getCodec() {
        return CODEC;
    }

    @Override
    public void doPostHurt(ItemStack gem, LootRarity rarity, LivingEntity user, Entity attacker) {
        if (this.target == Target.HURT_SELF) applyEffects(gem, user, rarity);
        else if (this.target == Target.HURT_ATTACKER && attacker instanceof LivingEntity tLiving) {
            applyEffects(gem, tLiving, rarity);
        }
    }

    @Override
    public void doPostAttack(ItemStack gem, LootRarity rarity, LivingEntity user, Entity target) {
        if (this.target == Target.ATTACK_SELF) applyEffects(gem, user, rarity);
        else if (this.target == Target.ATTACK_TARGET && target instanceof LivingEntity tLiving) {
            applyEffects(gem, tLiving, rarity);
        }
    }

    @Override
    public void onBlockBreak(ItemStack gem, LootRarity rarity, Player player, LevelAccessor world, BlockPos pos, BlockState state) {
        if (this.target == Target.BREAK_SELF) {
            applyEffects(gem, player, rarity);
        }
    }

    @Override
    public void onArrowImpact(ItemStack gem, LootRarity rarity, AbstractArrow arrow, HitResult res) {
        if (this.target == Target.ARROW_SELF && arrow.getOwner() instanceof LivingEntity owner) {
            applyEffects(gem, owner, rarity);
        } else if (this.target == Target.ARROW_TARGET && res.getType() == Type.ENTITY && ((EntityHitResult) res).getEntity() instanceof LivingEntity target) {
            applyEffects(gem, target, rarity);
        }
    }

    @Override
    public float onShieldBlock(ItemStack gem, LootRarity rarity, LivingEntity entity, DamageSource source, float amount) {
        if (this.target == Target.BLOCK_SELF) {
            applyEffects(gem, entity, rarity);
        } else if (this.target == Target.BLOCK_ATTACKER && source.getDirectEntity() instanceof LivingEntity target) {
            applyEffects(gem, target, rarity);
        }
        return amount;
    }

    private void applyEffects(ItemStack gemStack, LivingEntity target, LootRarity rarity) {
        for (EffectInst effectInst : this.effects) {
            applySingleEffect(gemStack, target, rarity, effectInst);
        }
    }

    private void applySingleEffect(ItemStack gemStack, LivingEntity target, LootRarity rarity, EffectInst effectInst) {
        EffectData data = effectInst.values.get(rarity);
        if (data == null) return;

        int cooldown = data.cooldown;

        ResourceLocation cooldownId = new ResourceLocation(
                this.getId().getNamespace(),
                this.getId().getPath() + "_" + ForgeRegistries.MOB_EFFECTS.getKey(effectInst.effect).getPath()
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

    public static MutableComponent toComponent(MobEffectInstance inst) {
        MutableComponent comp = Component.translatable(inst.getDescriptionId());
        MobEffect effect = inst.getEffect();

        if (inst.getAmplifier() > 0) {
            comp = Component.translatable("potion.withAmplifier", comp, Component.translatable("potion.potency." + inst.getAmplifier()));
        }

        if (inst.getDuration() > 20) {
            comp = Component.translatable("potion.withDuration", comp, MobEffectUtil.formatDuration(inst, 1));
        }

        return comp.withStyle(effect.getCategory().getTooltipFormatting());
    }

    protected static record EffectInst(MobEffect effect, Map<LootRarity, EffectData> values) {
        public static Codec<EffectInst> CODEC = RecordCodecBuilder.create(inst -> inst
                .group(
                        ForgeRegistries.MOB_EFFECTS.getCodec().fieldOf("mob_effect").forGetter(EffectInst::effect),
                        LootRarity.mapCodec(EffectData.CODEC).fieldOf("values").forGetter(EffectInst::values))
                .apply(inst, EffectInst::new));
    }

    public static record EffectData(int duration, int amplifier, int cooldown) {
        public static Codec<EffectData> CODEC = RecordCodecBuilder.create(inst -> inst
                .group(
                        Codec.INT.fieldOf("duration").forGetter(EffectData::duration),
                        Codec.INT.fieldOf("amplifier").forGetter(EffectData::amplifier),
                        PlaceboCodecs.nullableField(Codec.INT, "cooldown", 0).forGetter(EffectData::cooldown))
                .apply(inst, EffectData::new));

        public MobEffectInstance build(MobEffect effect) {
            return new MobEffectInstance(effect, this.duration, this.amplifier);
        }
    }
}