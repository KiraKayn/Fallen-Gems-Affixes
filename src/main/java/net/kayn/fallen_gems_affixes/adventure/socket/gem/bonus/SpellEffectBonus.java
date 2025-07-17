package net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import net.kayn.fallen_gems_affixes.adventure.affix.SpellEffectAffix;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

public class SpellEffectBonus extends GemBonus {

    public static final Codec<SpellEffectBonus> CODEC = RecordCodecBuilder.create((inst) -> inst
            .group(
                    gemClass(),
                    ForgeRegistries.MOB_EFFECTS.getCodec().fieldOf("mob_effect").forGetter((a) -> a.effect),
                    SpellEffectAffix.Target.CODEC.fieldOf("target").forGetter((a) -> a.target),
                    LootRarity.mapCodec(SpellEffectBonus.EffectData.CODEC).fieldOf("values").forGetter((a) -> a.values),
                    PlaceboCodecs.nullableField(Codec.BOOL, "stack_on_reapply", false).forGetter((a) -> a.stackOnReapply),
                    Codec.intRange(1, 255).optionalFieldOf("stacking_limit", 255).forGetter(a -> a.stackingLimit))
            .apply(inst, SpellEffectBonus::new));

    protected final MobEffect effect;
    public final SpellEffectAffix.Target target;
    protected final Map<LootRarity, EffectData> values;
    protected final int stackingLimit;
    protected final boolean stackOnReapply;

    public SpellEffectBonus(GemClass gemClass, MobEffect effect, SpellEffectAffix.Target target, Map<LootRarity, SpellEffectBonus.EffectData> values, boolean stackOnReapply, int stackingLimit) {
        super(new ResourceLocation("fallen_gems_affixes","mob_effect"), gemClass);
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
    public SpellEffectBonus validate() {
        Preconditions.checkNotNull(this.effect, "Null mob effect");
        Preconditions.checkNotNull(this.target, "Null target");
        Preconditions.checkNotNull(this.values, "Null values map");
        return this;
    }

    @Override
    public boolean supports(LootRarity rarity) {
        return this.values.containsKey(rarity);
    }

    @Override
    public int getNumberOfUUIDs() {
        return 0;
    }

    @Override
    public Component getSocketBonusTooltip(ItemStack gem, LootRarity rarity) {
        MobEffectInstance inst = ((SpellEffectBonus.EffectData) this.values.get(rarity)).build(this.effect);
        MutableComponent comp = this.target.toComponent(new Object[]{toComponent(inst)}).withStyle(ChatFormatting.YELLOW);
        int cooldown = this.getCooldown(rarity);
        if (cooldown != 0) {
            Component cd = Component.translatable("affix.apotheosis.cooldown", new Object[]{StringUtil.formatTickDuration(cooldown)});
            comp = comp.append(" ").append(cd);
        }

        if (this.stackOnReapply) {
            comp = comp.append(" ").append(Component.translatable("affix.apotheosis.stacking"));
        }

        return comp;
    }

    public void applyEffect(ItemStack gemStack, LivingEntity target, LootRarity rarity) {
        int cooldown = this.getCooldown(rarity);
        if (cooldown == 0 || !Affix.isOnCooldown(this.getCooldownId(gemStack), cooldown, target)) {
            SpellEffectBonus.EffectData data = (SpellEffectBonus.EffectData) this.values.get(rarity);
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

            Affix.startCooldown(this.getCooldownId(gemStack), target);
        }
    }

    protected int getCooldown(LootRarity rarity) {
        SpellEffectBonus.EffectData data = (SpellEffectBonus.EffectData) this.values.get(rarity);
        return data.cooldown;
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


    public static record EffectData(int duration, int amplifier, int cooldown) {
        private static Codec<SpellEffectBonus.EffectData> CODEC = RecordCodecBuilder.create((inst) -> inst
                .group(
                        Codec.INT.fieldOf("duration").forGetter(SpellEffectBonus.EffectData::duration),
                        Codec.INT.fieldOf("amplifier").forGetter(SpellEffectBonus.EffectData::amplifier),
                        PlaceboCodecs.nullableField(Codec.INT, "cooldown", 0).forGetter(SpellEffectBonus.EffectData::cooldown))
                .apply(inst, SpellEffectBonus.EffectData::new));

        public MobEffectInstance build(MobEffect effect) {
            return new MobEffectInstance(effect, this.duration, this.amplifier);
        }
    }
}
