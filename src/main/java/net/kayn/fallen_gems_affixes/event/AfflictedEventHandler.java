package net.kayn.fallen_gems_affixes.event;

import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import net.kayn.fallen_gems_affixes.adventure.affix.AfflictedAffix;
import net.kayn.fallen_gems_affixes.adventure.entity.EntityAffixHelper;
import net.kayn.fallen_gems_affixes.adventure.entity.EntityAffixInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class AfflictedEventHandler {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingHurt(LivingHurtEvent event) {
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
        if (event.getEffectInstance().getEffect().getCategory() != net.minecraft.world.effect.MobEffectCategory.HARMFUL) return;
        updateSpeedModifier(entity);
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        if (event.getEffect().getCategory() != net.minecraft.world.effect.MobEffectCategory.HARMFUL) return;
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
        speedAttr.removeModifier(AfflictedAffix.SPEED_MODIFIER_UUID);
        float totalSpeedBonus = getTotalSpeedBonus(entity);
        if (totalSpeedBonus > 0f) {
            speedAttr.addTransientModifier(new AttributeModifier(
                    AfflictedAffix.SPEED_MODIFIER_UUID,
                    AfflictedAffix.SPEED_MODIFIER_NAME,
                    totalSpeedBonus,
                    AttributeModifier.Operation.MULTIPLY_BASE));
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

        for (EntityAffixInstance inst : EntityAffixHelper.getAffixes(entity)) {
            if (!inst.isValid()) continue;
            if (!(inst.affix().get() instanceof AfflictedAffix affix)) continue;
            total += affix.getDamageBonus(entity, inst.rarity().get(), inst.level());
        }

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

        for (EntityAffixInstance inst : EntityAffixHelper.getAffixes(entity)) {
            if (!inst.isValid()) continue;
            if (!(inst.affix().get() instanceof AfflictedAffix affix)) continue;
            total += affix.getSpeedBonus(entity, inst.rarity().get(), inst.level());
        }

        return total;
    }
}