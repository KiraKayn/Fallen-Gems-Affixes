package net.kayn.fallen_gems_affixes.adventure.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixDefinition;
import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BerserkerAffix extends Affix {

    public static final Codec<BerserkerAffix> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                    affixDef(),
                    LootRarity.mapCodec(StepFunction.CODEC).fieldOf("values").forGetter(a -> a.values),
                    LootCategory.SET_CODEC.fieldOf("types").forGetter(a -> a.types))
            .apply(inst, BerserkerAffix::new));

    protected final Map<LootRarity, StepFunction> values;
    protected final Set<LootCategory> types;

    private record BerserkerState(UUID targetId, int stacks) {}
    private static final Map<UUID, BerserkerState> STATES = new HashMap<>();

    public BerserkerAffix(AffixDefinition def,
                          Map<LootRarity, StepFunction> values,
                          Set<LootCategory> types) {
        super(def);
        this.values = values;
        this.types  = types;
    }

    @Override
    public void doPostAttack(AffixInstance inst, LivingEntity user, @Nullable Entity target) {
        if (!(target instanceof LivingEntity living)) return;
        UUID attackerId = user.getUUID();
        UUID targetId   = living.getUUID();

        BerserkerState state = STATES.get(attackerId);
        int newStacks;
        if (state != null && state.targetId().equals(targetId)) {
            newStacks = state.stacks() + 1;
        } else {
            newStacks = 1;
        }
        STATES.put(attackerId, new BerserkerState(targetId, newStacks));
    }

    public static float getDamageBonus(LivingEntity attacker) {
        BerserkerState state = STATES.get(attacker.getUUID());
        if (state == null || state.stacks() < 2) return 0f;

        float bonusPerStack = 0f;
        for (ItemStack stack : attacker.getAllSlots()) {
            for (AffixInstance inst : AffixHelper.getAffixes(stack).values()) {
                if (!inst.isValid()) continue;
                if (!(inst.affix().get() instanceof BerserkerAffix affix)) continue;
                bonusPerStack += affix.values.get(inst.getRarity()).get(inst.level());
            }
        }
        return bonusPerStack * (state.stacks() - 1);
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingIncomingDamageEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        Entity attacker = event.getSource().getEntity();

        if (event.getEntity() instanceof LivingEntity defender) {
            boolean hasAffix = false;
            for (ItemStack stack : defender.getAllSlots()) {
                if (AffixHelper.getAffixes(stack).values().stream()
                        .anyMatch(inst -> inst.isValid() && inst.affix().get() instanceof BerserkerAffix)) {
                    hasAffix = true;
                    break;
                }
            }
            if (hasAffix) {
                STATES.remove(defender.getUUID());
            }
        }

        if (attacker instanceof LivingEntity living) {
            BerserkerState state = STATES.get(living.getUUID());
            if (state == null) return;

            float totalBonus = getDamageBonus(living);
            if (totalBonus > 0f) {
                event.setAmount(event.getAmount() * (1f + totalBonus));
            }
        }
    }

    public static void clearState(UUID uuid) {
        STATES.remove(uuid);
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        if (cat.isNone()) return false;
        return (types.isEmpty() || types.contains(cat)) && values.containsKey(rarity);
    }

    @Override
    public MutableComponent getDescription(AffixInstance inst, AttributeTooltipContext ctx) {
        LootRarity rarity = inst.getRarity();
        float level = inst.level();
        return Component.translatable(
                "affix.fallen_gems_affixes.berserker.desc",
                Affix.fmt(values.get(rarity).get(level) * 100f)
        ).withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }
}