package net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.socket.gem.GemView;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

import java.util.Map;

public class BossSlayerBonus extends GemBonus {

    public static final Codec<BossSlayerBonus> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            gemClass(),
            ResourceLocation.CODEC.fieldOf("entity_tag").forGetter(b -> b.entityTag.location()),
            Purity.mapCodec(Codec.DOUBLE).fieldOf("values").forGetter(a -> a.values)
    ).apply(inst, (gemClass, tagId, values) ->
            new BossSlayerBonus(gemClass, TagKey.create(BuiltInRegistries.ENTITY_TYPE.key(), tagId), values)
    ));

    public final TagKey<EntityType<?>> entityTag;
    public final Map<Purity, Double> values;

    public BossSlayerBonus(GemClass gemClass, TagKey<EntityType<?>> tag, Map<Purity, Double> values) {
        super(gemClass);
        this.entityTag = tag;
        this.values = values;
    }

    @Override
    public Codec<? extends GemBonus> getCodec() {
        return CODEC;
    }

    @Override
    public boolean supports(Purity purity) {
        return values.containsKey(purity);
    }

    @Override
    public Component getSocketBonusTooltip(GemView gem, AttributeTooltipContext ctx) {
        double percent = values.get(gem.purity()) * 100;
        return Component.translatable("bonus.fallen_gems_affixes.boss_slayer.desc", String.format("%.0f", percent))
                .withStyle(ChatFormatting.YELLOW);
    }
}