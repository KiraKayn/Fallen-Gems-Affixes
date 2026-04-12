package net.kayn.fallen_gems_affixes.adventure.entity;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

/**
 * Central event handler for entity-level affixes.
 *
 * <p><b>Standard Affix methods fired here (no per-class changes needed):</b>
 * <ul>
 *   <li>{@code onHurt}        — damage modification for the defender</li>
 *   <li>{@code getDamageBonus}— extra damage added by the attacker</li>
 *   <li>{@code doPostAttack}  — post-attack effects for the attacker</li>
 *   <li>{@code doPostHurt}    — post-hurt effects for the defender</li>
 * </ul>
 *
 * <p><b>Tick-based logic:</b> Affixes implementing {@link EntityAffixBehavior}
 * have {@link EntityAffixBehavior#tickEntityAffix} called every
 * {@link EntityAffixBehavior#tickInterval()} ticks.
 */
public final class EntityAffixEventHandler {

    private EntityAffixEventHandler() {}

    //combat

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        LivingEntity defender       = event.getEntity();
        DamageSource source         = event.getSource();
        Entity       attackerEntity = source.getEntity();

        //defender
        List<EntityAffixInstance> defAffixes = EntityAffixHelper.getAffixes(defender);
        if (!defAffixes.isEmpty()) {
            float amount = event.getAmount();
            for (EntityAffixInstance inst : defAffixes) {
                if (!inst.isValid()) continue;
                amount = inst.onHurt(source, defender, amount);
                if (amount <= 0f) {
                    event.setCanceled(true);
                    return;
                }
            }
            event.setAmount(amount);
        }

        //attacker
        if (attackerEntity instanceof LivingEntity attacker) {
            List<EntityAffixInstance> atkAffixes = EntityAffixHelper.getAffixes(attacker);
            if (!atkAffixes.isEmpty()) {
                MobType defMobType  = defender.getMobType();
                float   bonusDamage = 0f;
                for (EntityAffixInstance inst : atkAffixes) {
                    if (!inst.isValid()) continue;
                    bonusDamage += inst.getDamageBonus(defMobType);
                    inst.doPostAttack(attacker, defender);
                }
                if (bonusDamage > 0f) {
                    event.setAmount(event.getAmount() + bonusDamage);
                }
            }
        }

        //defender
        for (EntityAffixInstance inst : defAffixes) {
            if (!inst.isValid()) continue;
            inst.doPostHurt(defender, attackerEntity);
        }
    }

    //tick

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        if (!EntityAffixHelper.hasAffixes(entity)) return;

        List<EntityAffixInstance> affixes = EntityAffixHelper.getAffixes(entity);
        for (EntityAffixInstance inst : affixes) {
            if (!inst.isValid()) continue;
            if (!(inst.affix().get() instanceof EntityAffixBehavior behavior)) continue;

            int interval = behavior.tickInterval();
            if (interval <= 0 || entity.tickCount % interval != 0) continue;

            behavior.tickEntityAffix(entity, inst.rarity().get(), inst.level());
        }
    }
}