package net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.placebo.util.StepFunction;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public class SpellEchoBonus extends GemBonus {

    public static final Codec<SpellEchoBonus> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            gemClass(),
            ResourceLocation.CODEC.fieldOf("school").forGetter(b -> b.schoolId),
            VALUES_CODEC.fieldOf("delay_ticks").forGetter(b -> b.delayTicks)
    ).apply(inst, SpellEchoBonus::new));

    private final ResourceLocation schoolId;
    private final Map<LootRarity, StepFunction> delayTicks;

    public SpellEchoBonus(GemClass gemClass, ResourceLocation schoolId, Map<LootRarity, StepFunction> delayTicks) {
        super(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "spell_echo"), gemClass);
        this.schoolId = schoolId;
        this.delayTicks = delayTicks;
    }

    public ResourceLocation getSchoolId() {
        return schoolId;
    }

    public SchoolType getSchool() {
        return SchoolRegistry.REGISTRY.get().getValue(schoolId);
    }

    public int getDelayTicks(LootRarity rarity) {
        StepFunction fn = delayTicks.get(rarity);
        return fn != null ? (int) fn.get(0) : 10;
    }

    @Override
    public Component getSocketBonusTooltip(ItemStack gem, LootRarity rarity) {
        SchoolType school = getSchool();
        int ticks = getDelayTicks(rarity);
        float seconds = ticks / 20f;
        String timeStr = seconds == (int) seconds
                ? String.valueOf((int) seconds) + "s"
                : String.format("%.1fs", seconds);

        Component schoolName = school != null
                ? school.getDisplayName().copy().withStyle(school.getDisplayName().getStyle())
                : Component.literal(schoolId.getPath()).withStyle(ChatFormatting.YELLOW);

        Component timeComp = Component.literal(" [" + timeStr + "]").withStyle(ChatFormatting.YELLOW);

        return Component.translatable("bonus.fallen_gems_affixes.spell_echo.desc",
                schoolName,
                timeComp
        ).withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public boolean supports(LootRarity rarity) {
        return delayTicks.containsKey(rarity);
    }

    @Override
    public SpellEchoBonus validate() {
        Preconditions.checkNotNull(this.schoolId, "SpellEchoBonus missing school");
        Preconditions.checkNotNull(this.delayTicks, "SpellEchoBonus missing delay_ticks");
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