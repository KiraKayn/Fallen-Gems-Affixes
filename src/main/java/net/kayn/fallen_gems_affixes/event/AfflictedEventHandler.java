package net.kayn.fallen_gems_affixes.event;

import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import net.kayn.fallen_gems_affixes.adventure.affix.AfflictedAffix;
// import net.kayn.fallen_gems_affixes.adventure.entity.EntityAffixHelper;   // TODO
// import net.kayn.fallen_gems_affixes.adventure.entity.EntityAffixInstance; // TODO
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class AfflictedEventHandler {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingHurt(LivingIncomingDamageEvent event) {
        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof LivingEntity living)) return;
        if (living.level().isClientSide()) return;

        float totalBonus = getTotalDamageBonus(living);
        if (totalBonus <= 0f) return;

        event.setAmount(event.getAmount() * (1f + totalBonus));
    }

    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        if (event.getEffectInstance().getEffect().value().getCategory() != MobEffectCategory.HARMFUL) return;
        updateSpeedModifier(entity);
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        if (event.getEffect().value().getCategory() != MobEffectCategory.HARMFUL) return;
        updateSpeedModifier(entity);
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        updateSpeedModifier(event.getEntity());
    }

    private static void updateSpeedModifier(LivingEntity entity) {
        var speedAttr = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr == null) return;
        speedAttr.removeModifier(AfflictedAffix.SPEED_MODIFIER_ID);
        float totalSpeedBonus = getTotalSpeedBonus(entity);
        if (totalSpeedBonus > 0f) {
            speedAttr.addTransientModifier(new AttributeModifier(
                    AfflictedAffix.SPEED_MODIFIER_ID,
                    totalSpeedBonus,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
    }

    private static float getTotalDamageBonus(LivingEntity entity) {
        float total = 0f;
        for (ItemStack stack : entity.getAllSlots()) {
            for (var inst : AffixHelper.getAffixes(stack).values()) {
                if (!inst.isValid()) continue;
                if (!(inst.affix().get() instanceof AfflictedAffix affix)) continue;
                total += affix.getDamageBonus(entity, inst.rarity().get(), inst.level());
            }
        }
        // TODO: re-enable entity affix loop
        return total;
    }

    private static float getTotalSpeedBonus(LivingEntity entity) {
        float total = 0f;
        for (ItemStack stack : entity.getAllSlots()) {
            for (var inst : AffixHelper.getAffixes(stack).values()) {
                if (!inst.isValid()) continue;
                if (!(inst.affix().get() instanceof AfflictedAffix affix)) continue;
                total += affix.getSpeedBonus(entity, inst.rarity().get(), inst.level());
            }
        }
        // TODO: re-enable entity affix loop
        return total;
    }
}