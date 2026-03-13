package net.kayn.fallen_gems_affixes.adventure.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixType;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SpellFocusAffix extends Affix {

    public static final Codec<SpellFocusAffix> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                    GemBonus.VALUES_CODEC.fieldOf("values").forGetter(a -> a.values),
                    GemBonus.VALUES_CODEC.fieldOf("max_stacks").forGetter(a -> a.maxStacks),
                    LootCategory.SET_CODEC.fieldOf("types").forGetter(a -> a.types))
            .apply(inst, SpellFocusAffix::new));

    protected final Map<LootRarity, StepFunction> values;
    protected final Map<LootRarity, StepFunction> maxStacks;
    protected final Set<LootCategory> types;


    private record FocusState(String spellId, UUID targetId, int stacks) {
        FocusState withStacks(int newStacks) {
            return new FocusState(spellId, targetId, newStacks);
        }
    }

    private static final Map<UUID, FocusState> FOCUS_STATES = new HashMap<>();

    public SpellFocusAffix(Map<LootRarity, StepFunction> values,
                           Map<LootRarity, StepFunction> maxStacks,
                           Set<LootCategory> types) {
        super(AffixType.ABILITY);
        this.values    = values;
        this.maxStacks = maxStacks;
        this.types     = types;
    }

    public float onSpellDamage(LivingEntity caster, LivingEntity target,
                               String spellId, LootRarity rarity, float level) {
        UUID casterId = caster.getUUID();
        UUID targetId = target.getUUID();

        FocusState state = FOCUS_STATES.get(casterId);

        int newStacks;
        if (state != null && state.spellId().equals(spellId) && state.targetId().equals(targetId)) {
            int max = (int) getMaxStacks(rarity, level);
            newStacks = Math.min(state.stacks() + 1, max);
        } else {
            newStacks = 1;
        }

        FOCUS_STATES.put(casterId, new FocusState(spellId, targetId, newStacks));

        if (newStacks < 2) return 1.0f;
        float bonusPerStack = values.get(rarity).get(level);
        return 1.0f + bonusPerStack * (newStacks - 1);
    }

    public static void clearState(UUID casterUUID) {
        FOCUS_STATES.remove(casterUUID);
    }


    private float getMaxStacks(LootRarity rarity, float level) {
        StepFunction fn = maxStacks.get(rarity);
        return fn != null ? fn.get(level) : 5f;
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        if (cat.isNone()) return false;
        return (types.isEmpty() || types.contains(cat)) && values.containsKey(rarity);
    }

    @Override
    public MutableComponent getDescription(ItemStack stack, LootRarity rarity, float level) {
        float bonusPerStack = values.get(rarity).get(level);
        int max             = (int) getMaxStacks(rarity, level);
        return Component.translatable(
                "affix.fallen_gems_affixes.spell_focus.desc",
                Affix.fmt(bonusPerStack * 100f),
                max
        ).withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }
}