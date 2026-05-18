package net.kayn.fallen_gems_affixes.adventure.entity.affix;

import net.kayn.fallen_gems_affixes.adventure.entity.MobAffixHelper;
import net.kayn.fallen_gems_affixes.adventure.entity.MobAffixHelper.ResolvedMobAffix;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

public final class MobAffixEventHandler {

    private static final ThreadLocal<Boolean> PROCESSING = ThreadLocal.withInitial(() -> false);

    private MobAffixEventHandler() {}

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (PROCESSING.get()) return;
        if (event.getEntity().level().isClientSide()) return;

        PROCESSING.set(true);
        try {
            LivingEntity defender = event.getEntity();
            DamageSource source   = event.getSource();
            Entity attackerEntity = source.getEntity();

            List<ResolvedMobAffix> defAffixes = MobAffixHelper.getAffixes(defender);
            if (!defAffixes.isEmpty()) {
                float amount = event.getAmount();
                for (ResolvedMobAffix r : defAffixes) {
                    amount = r.affix().onHurt(defender, source, amount, r.level());
                    if (amount <= 0f) {
                        event.setCanceled(true);
                        return;
                    }
                }
                event.setAmount(amount);
            }

            if (attackerEntity instanceof LivingEntity attacker) {
                List<ResolvedMobAffix> atkAffixes = MobAffixHelper.getAffixes(attacker);
                if (!atkAffixes.isEmpty()) {
                    MobType defType = defender.getMobType();
                    float bonus = 0f;
                    for (ResolvedMobAffix r : atkAffixes) {
                        bonus += r.affix().getDamageBonus(attacker, defType, r.level());
                        r.affix().doPostAttack(attacker, defender, r.level());
                    }
                    if (bonus > 0f) event.setAmount(event.getAmount() + bonus);
                }
            }

            for (ResolvedMobAffix r : defAffixes) {
                r.affix().doPostHurt(defender, attackerEntity, r.level());
            }
        } finally {
            PROCESSING.set(false);
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        if (!MobAffixHelper.hasAffixes(entity)) return;

        for (ResolvedMobAffix r : MobAffixHelper.getAffixes(entity)) {
            int interval = r.affix().getTickInterval();
            if (interval <= 0 || entity.tickCount % interval != 0) continue;
            r.affix().onTick(entity, r.level());
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        LivingEntity entity = event.getEntity();
        for (ResolvedMobAffix r : MobAffixHelper.getAffixes(entity)) {
            r.affix().onDeath(entity, event.getSource(), r.level());
        }
    }
}