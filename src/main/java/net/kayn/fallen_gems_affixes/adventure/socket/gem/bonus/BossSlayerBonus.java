package net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

public class BossSlayerBonus extends GemBonus {

    public static final Codec<BossSlayerBonus> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            gemClass(),
            ResourceLocation.CODEC.fieldOf("entity_tag").forGetter(b -> b.entityTag.location()),
            VALUES_CODEC.fieldOf("values").forGetter(b -> b.values)
    ).apply(inst, (gemClass, tagId, values) ->
            new BossSlayerBonus(gemClass, TagKey.create(ForgeRegistries.ENTITY_TYPES.getRegistryKey(), tagId), values)
    ));

    public final TagKey<EntityType<?>> entityTag;
    public final Map<LootRarity, StepFunction> values;

    public BossSlayerBonus(GemClass gemClass, TagKey<EntityType<?>> tag, Map<LootRarity, StepFunction> values) {
        super(new ResourceLocation(FallenGemsAffixes.MOD_ID, "boss_slayer"), gemClass);
        this.entityTag = tag;
        this.values = values;
    }

    @Override
    public Component getSocketBonusTooltip(ItemStack gem, LootRarity rarity) {
        double percent = values.get(rarity).get(0) * 100;
        return Component.translatable("bonus.fallen_gems_affixes.boss_slayer.desc", String.format("%.0f", percent))
                .withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public boolean supports(LootRarity rarity) {
        return values.containsKey(rarity);
    }

    @Override
    public BossSlayerBonus validate() {
        Preconditions.checkNotNull(this.entityTag, "BossSlayerBonus missing entity tag");
        Preconditions.checkNotNull(this.values, "BossSlayerBonus missing values");
        return this;
    }

    @Override
    public int getNumberOfUUIDs() {
        return 0;
    }

    @Override
    public Codec<? extends GemBonus> getCodec() {
        return CODEC;
    }
}