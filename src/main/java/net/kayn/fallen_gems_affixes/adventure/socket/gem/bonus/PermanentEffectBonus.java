package net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public class PermanentEffectBonus extends GemBonus {

    private final MobEffect effect;
    private final Map<LootRarity, Integer> values;

    public static final Codec<PermanentEffectBonus> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            gemClass(),
            ResourceLocation.CODEC.fieldOf("effect")
                    .xmap(BuiltInRegistries.MOB_EFFECT::get, BuiltInRegistries.MOB_EFFECT::getKey)
                    .forGetter(b -> b.effect),

            Codec.unboundedMap(LootRarity.CODEC, Codec.INT)
                    .fieldOf("values")
                    .forGetter(b -> b.values)
    ).apply(inst, PermanentEffectBonus::new));

    public PermanentEffectBonus(GemClass gemClass, MobEffect effect, Map<LootRarity, Integer> values) {
        super(new ResourceLocation("fallen_gems_affixes", "permanent_effect"), gemClass);
        this.effect = effect;
        this.values = values;
    }

    @Override
    public GemBonus validate() {
        Preconditions.checkNotNull(this.effect, "Null mob effect");
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
    public Component getSocketBonusTooltip(ItemStack itemStack, LootRarity lootRarity) {
        int amplifier = this.values.get(lootRarity);
        MutableComponent effectName = Component.translatable(this.effect.getDescriptionId());

        if (amplifier > 0) {
            effectName = Component.translatable("potion.withAmplifier",
                    effectName,
                    Component.translatable("potion.potency." + amplifier ));
        }

        effectName = effectName.withStyle(this.effect.getCategory().getTooltipFormatting());

        MutableComponent comp = Component.translatable("bonus.fallen_gems_affixes:permanent_effect", effectName)
                .withStyle(ChatFormatting.YELLOW);
        Component infinity = Component.literal("[").append(Component.translatable("affix.fallen_gems_affixes.infinity")).append("]");

        return comp.append(" ").append(infinity);
    }

    @Override
    public Codec<? extends GemBonus> getCodec() {
        return CODEC;
    }

    public MobEffect getEffect() {
        return this.effect;
    }

    public int getAmplifier(LootRarity rarity) {
        return this.values.get(rarity);
    }
}