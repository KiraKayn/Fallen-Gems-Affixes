package net.kayn.fallen_gems_affixes.event;

import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.event.GetItemSocketsEvent;
import net.kayn.fallen_gems_affixes.adventure.affix.DoubleStrikeAffix;
import net.kayn.fallen_gems_affixes.adventure.affix.SocketBonusAffix;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;

public class FallenEventHandler {

    private static final Set<UUID> DOUBLE_STRIKE_GUARD = new HashSet<>();

    private static final Map<UUID, List<PendingStrike>> PENDING_STRIKES = new HashMap<>();

    private static final Set<String> SCHEDULED_KEYS = new HashSet<>();

    private static final int DELAY_TICKS = 4;

    @SubscribeEvent
    public static void hookAddSocketsAffix(GetItemSocketsEvent event) {
        ItemStack stack = event.getStack();

        if (!AffixHelper.hasAffixes(stack)) return;

        int affixBonus = java.util.stream.StreamSupport.stream(AffixHelper.streamAffixes(stack).spliterator(), false)
                .filter(inst -> inst.affix().isBound() && inst.affix().get() instanceof SocketBonusAffix)
                .mapToInt(inst -> {
                    SocketBonusAffix affix = (SocketBonusAffix) inst.affix().get();
                    return affix.getBonusSockets(inst.rarity().get(), inst.level());
                }).sum();

        if (affixBonus > 0) {
            event.setSockets(event.getSockets() + affixBonus);
        }
    }

    // --- double strike (DELAYED) ---
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onDoubleStrike(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;
        if (event.getSource().getDirectEntity() != attacker) return;

        LivingEntity target = event.getEntity();

        // avoid recursion when a queued/delayed hit is being applied
        if (DOUBLE_STRIKE_GUARD.contains(target.getUUID())) return;

        float originalDamage = event.getAmount();
        float totalBonusDamage = 0f;

        // IMPORTANT: only check the attacker's main hand (weapon) for the affix
        ItemStack weapon = attacker.getMainHandItem();
        if (weapon != null && !weapon.isEmpty() && AffixHelper.hasAffixes(weapon)) {
            var list = AffixHelper.streamAffixes(weapon).toList();
            for (var inst : list) {
                if (!inst.affix().isBound()) continue;

                if (inst.affix().get() instanceof DoubleStrikeAffix affix) {
                    float bonus = affix.calculateBonusDamage(attacker, target, event.getSource(), originalDamage, inst.rarity().get(), inst.level());
                    totalBonusDamage += bonus;
                }
            }
        }

        if (totalBonusDamage <= 0f) return;

        // Prevent scheduling the same attacker->target more than once per game tick
        long tick = attacker.level() instanceof ServerLevel srv ? srv.getGameTime() : 0L;
        String key = attacker.getUUID().toString() + ":" + target.getUUID().toString() + ":" + tick;

        synchronized (SCHEDULED_KEYS) {
            if (SCHEDULED_KEYS.contains(key)) {
                // already scheduled this tick for the same attacker+target
                return;
            } else {
                SCHEDULED_KEYS.add(key);
            }
        }

        // schedule delayed damage instead of applying immediately
        if (target.level() instanceof ServerLevel serverLevel) {
            PendingStrike ps = new PendingStrike(serverLevel, target.getUUID(), event.getSource(), totalBonusDamage, DELAY_TICKS, key);
            synchronized (PENDING_STRIKES) {
                PENDING_STRIKES.computeIfAbsent(target.getUUID(), k -> new ArrayList<>()).add(ps);
            }
        } else {
            synchronized (SCHEDULED_KEYS) {
                SCHEDULED_KEYS.remove(key);
            }
        }
    }

    // Server tick handler that counts down and applies pending strikes
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return; // apply at end of tick

        synchronized (PENDING_STRIKES) {
            Iterator<Map.Entry<UUID, List<PendingStrike>>> it = PENDING_STRIKES.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<UUID, List<PendingStrike>> entry = it.next();
                List<PendingStrike> list = entry.getValue();
                Iterator<PendingStrike> li = list.iterator();

                while (li.hasNext()) {
                    PendingStrike ps = li.next();
                    ps.ticksLeft--;
                    if (ps.ticksLeft <= 0) {
                        // time to apply
                        li.remove();

                        // remove the scheduled key so future ticks can schedule again
                        synchronized (SCHEDULED_KEYS) {
                            SCHEDULED_KEYS.remove(ps.scheduledKey);
                        }

                        var entity = ps.level.getEntity(ps.targetUuid);
                        if (entity instanceof LivingEntity target) {
                            // guard to avoid recursion when applying the extra damage
                            DOUBLE_STRIKE_GUARD.add(target.getUUID());
                            try {
                                target.hurt(ps.source, ps.damageAmount);
                            } finally {
                                DOUBLE_STRIKE_GUARD.remove(target.getUUID());
                            }
                        }
                    }
                }

                if (list.isEmpty()) {
                    it.remove();
                }
            }
        }
    }

    // simple holder for scheduled strikes
    private static final class PendingStrike {
        final ServerLevel level;
        final UUID targetUuid;
        final DamageSource source;
        final float damageAmount;
        final String scheduledKey;
        int ticksLeft;

        PendingStrike(ServerLevel level, UUID targetUuid, DamageSource source, float damageAmount, int ticksLeft, String scheduledKey) {
            this.level = level;
            this.targetUuid = targetUuid;
            this.source = source;
            this.damageAmount = damageAmount;
            this.ticksLeft = ticksLeft;
            this.scheduledKey = scheduledKey;
        }
    }
}