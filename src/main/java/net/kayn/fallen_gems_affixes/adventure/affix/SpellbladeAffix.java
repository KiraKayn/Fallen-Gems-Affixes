package net.kayn.fallen_gems_affixes.adventure.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixType;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.placebo.util.StepFunction;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class SpellbladeAffix extends Affix {

    public static final Codec<SpellbladeAffix> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                    GemBonus.VALUES_CODEC.fieldOf("values").forGetter(a -> a.values),
                    LootCategory.SET_CODEC.fieldOf("types").forGetter(a -> a.types))
            .apply(inst, SpellbladeAffix::new));

    protected final Map<LootRarity, StepFunction> values;
    protected final Set<LootCategory> types;

    public SpellbladeAffix(Map<LootRarity, StepFunction> values, Set<LootCategory> types) {
        super(AffixType.ABILITY);
        this.values = values;
        this.types  = types;
    }

    @Override
    public void doPostAttack(ItemStack stack, LootRarity rarity, float level,
                             LivingEntity user, @Nullable Entity target) {
        if (!(target instanceof LivingEntity)) return;
        if (!(user instanceof ServerPlayer)) return;

        MagicData magicData = MagicData.getPlayerMagicData(user);
        if (magicData == null) return;

        var cooldowns = magicData.getPlayerCooldowns();
        if (!cooldowns.hasCooldownsActive()) return;

        int reduction = (int) values.get(rarity).get(level);
        if (reduction <= 0) return;

        for (String spellId : new ArrayList<>(cooldowns.getSpellCooldowns().keySet())) {
            var instance = cooldowns.getSpellCooldowns().get(spellId);
            if (instance == null) continue;
            instance.decrementBy(reduction);
            if (instance.getCooldownRemaining() <= 0) {
                cooldowns.removeCooldown(spellId);
            }
        }

        if (user instanceof ServerPlayer sp) {
            cooldowns.syncToPlayer(sp);
        }
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        if (cat.isNone()) return false;
        return (types.isEmpty() || types.contains(cat)) && values.containsKey(rarity);
    }

    @Override
    public MutableComponent getDescription(ItemStack stack, LootRarity rarity, float level) {
        float seconds = values.get(rarity).get(level) / 20f;
        return Component.translatable(
                "affix.fallen_gems_affixes.spellblade.desc",
                Affix.fmt(seconds)
        ).withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }
}