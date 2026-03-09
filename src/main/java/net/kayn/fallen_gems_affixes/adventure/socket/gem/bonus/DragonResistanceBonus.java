package net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus;

import com.google.common.base.Preconditions;
import com.ibm.icu.text.DecimalFormat;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.util.BossUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class DragonResistanceBonus extends GemBonus implements IDamageOrResistanceBonus {

    public static final Codec<DragonResistanceBonus> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            gemClass(),
            ResourceLocation.CODEC.fieldOf("entity_tag").forGetter(b -> b.entityTag.location()),
            VALUES_CODEC.fieldOf("values").forGetter(b -> b.values)
    ).apply(inst, (gemClass, tagId, values) ->
            new DragonResistanceBonus(gemClass, TagKey.create(ForgeRegistries.ENTITY_TYPES.getRegistryKey(), tagId), values)
    ));

    public final TagKey<EntityType<?>> entityTag;
    public final Map<LootRarity, StepFunction> values;

    public DragonResistanceBonus(GemClass gemClass, TagKey<EntityType<?>> tag, Map<LootRarity, StepFunction> values) {
        super(new ResourceLocation(FallenGemsAffixes.MOD_ID, "dragon_resistance"), gemClass);
        this.entityTag = tag;
        this.values = values;
    }

    @Override
    public Component getSocketBonusTooltip(ItemStack gem, LootRarity rarity) {
        double percent = values.get(rarity).get(0) * 100;
        DecimalFormat df = new DecimalFormat("#.##");
        return Component.translatable("bonus.fallen_gems_affixes.dragon_resistance.desc", df.format(percent)).withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public boolean supports(LootRarity rarity) {
        return values.containsKey(rarity);
    }

    @Override
    public DragonResistanceBonus validate() {
        Preconditions.checkNotNull(this.entityTag, "DragonResistanceBonus missing entity tag");
        Preconditions.checkNotNull(this.values, "DragonResistanceBonus missing values");
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

    @Override
    public @NotNull BonusType getBonusType() {
        return BonusType.RESISTANCE;
    }

    @Override
    public @NotNull BonusName getBonusName() {
        return BonusName.DRAGON;
    }

    @Override
    public float getValue(BonusType type, LootRarity rarity, float level) {
        return this.values.get(rarity).get(level);
    }

    @Override
    public boolean checkCondition(LivingEntity attacker, LivingEntity target, DamageSource damageSource, BonusType damage) {
        return BossUtil.isExtraBossTag(attacker, DragonSlayerBonus.DRAGON_TAG);
    }
}