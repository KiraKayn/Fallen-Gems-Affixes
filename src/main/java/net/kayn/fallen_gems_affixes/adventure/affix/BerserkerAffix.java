package net.kayn.fallen_gems_affixes.adventure.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixType;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BerserkerAffix extends Affix {

    public static final Codec<BerserkerAffix> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                    GemBonus.VALUES_CODEC.fieldOf("values").forGetter(a -> a.values),
                    LootCategory.SET_CODEC.fieldOf("types").forGetter(a -> a.types))
            .apply(inst, BerserkerAffix::new));

    protected final Map<LootRarity, StepFunction> values;
    protected final Set<LootCategory> types;

    private record BerserkerState(UUID targetId, int stacks) {}
    private static final Map<UUID, BerserkerState> STATES = new HashMap<>();

    public BerserkerAffix(Map<LootRarity, StepFunction> values, Set<LootCategory> types) {
        super(AffixType.ABILITY);
        this.values = values;
        this.types  = types;
    }

    @Override
    public void doPostAttack(ItemStack stack, LootRarity rarity, float level,
                             LivingEntity user, @Nullable Entity target) {
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

    public static float getDamageBonus(LivingEntity attacker, LootRarity rarity, float level) {
        BerserkerState state = STATES.get(attacker.getUUID());
        if (state == null || state.stacks() < 2) return 0f;

        float bonusPerStack = 0f;
        for (ItemStack stack : attacker.getAllSlots()) {
            for (var inst : AffixHelper.getAffixes(stack).values()) {
                if (!inst.isValid()) continue;
                if (!(inst.affix().get() instanceof BerserkerAffix affix)) continue;
                bonusPerStack += affix.values.get(inst.rarity().get()).get(inst.level());
            }
        }
        return bonusPerStack * (state.stacks() - 1);
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        Entity attacker = event.getSource().getEntity();

        if (event.getEntity() instanceof LivingEntity) {
            LivingEntity defender = event.getEntity();
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

            float totalBonus = getDamageBonus(living, null, 0);
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
    public MutableComponent getDescription(ItemStack stack, LootRarity rarity, float level) {
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