package net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.socket.gem.GemView;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.GemBonus;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

import java.util.Map;

public class PermanentEffectBonus extends GemBonus {
    private final Holder<MobEffect> effect;
    private final Map<Purity, Integer> values;

    public static final Codec<PermanentEffectBonus> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            gemClass(),
            BuiltInRegistries.MOB_EFFECT.holderByNameCodec()
                    .fieldOf("effect")
                    .forGetter(b -> b.effect),
            Codec.unboundedMap(Purity.CODEC, Codec.INT)
                    .fieldOf("values")
                    .forGetter(b -> b.values)
    ).apply(inst, PermanentEffectBonus::new));

    public PermanentEffectBonus(GemClass gemClass, Holder<MobEffect> effect, Map<Purity, Integer> values) {
        super(gemClass);
        this.effect = effect;
        this.values = values;
    }

    @Override
    public Codec<? extends GemBonus> getCodec() {
        return CODEC;
    }

    public int getAmplifier(Purity purity) {
        return this.values.get(purity);
    }

    public Holder<MobEffect> getEffect() {
        return this.effect;
    }

    @Override
    public boolean supports(Purity purity) {
        return this.values.containsKey(purity);
    }

    @Override
    public Component getSocketBonusTooltip(GemView gem, AttributeTooltipContext ctx) {
        int amplifier = this.values.get(gem.purity());
        MutableComponent effectName = Component.translatable(this.effect.value().getDescriptionId());

        if (amplifier > 0) {
            effectName = Component.translatable("potion.withAmplifier",
                    effectName,
                    Component.translatable("potion.potency." + amplifier ));
        }

        effectName = effectName.withStyle(this.effect.value().getCategory().getTooltipFormatting());

        MutableComponent comp = Component.translatable("bonus.fallen_gems_affixes:permanent_effect", effectName)
                .withStyle(ChatFormatting.YELLOW);
        Component infinity = Component.literal("[").append(Component.translatable("affix.fallen_gems_affixes.infinity")).append("]");

        return comp.append(" ").append(infinity);
    }
}