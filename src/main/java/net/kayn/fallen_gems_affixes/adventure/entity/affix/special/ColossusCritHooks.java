package net.kayn.fallen_gems_affixes.adventure.entity.affix.special;

import dev.shadowsoffire.attributeslib.api.ALObjects;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = FallenGemsAffixes.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ColossusCritHooks {

    private static final UUID NO_CRIT_UUID = UUID.fromString("7e3f1b44-4d1d-4b0e-8e7a-8d7a9f8f2c11");

    private static final AttributeModifier NO_CRIT_MOD =
            new AttributeModifier(NO_CRIT_UUID, "fallen_gems_affixes:colossus_no_crit", -1000.0D, Operation.ADDITION);

    private static final Map<UUID, Integer> SUPPRESSED = new HashMap<>();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void blockVanillaCrit(CriticalHitEvent e) {
        if (!(e.getTarget() instanceof LivingEntity target)) return;
        if (!hasColossus(target)) return;

        e.setResult(net.minecraftforge.eventbus.api.Event.Result.DENY);
        e.setDamageModifier(1.0F);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void suppressApothCrit(LivingHurtEvent e) {
        if (!(e.getEntity() instanceof LivingEntity)) return;
        LivingEntity target = e.getEntity();
        if (!hasColossus(target)) return;

        if (!(e.getSource().getEntity() instanceof LivingEntity attacker)) return;

        AttributeInstance critChance = attacker.getAttribute(ALObjects.Attributes.CRIT_CHANCE.get());
        if (critChance == null) return;

        UUID id = attacker.getUUID();
        int depth = SUPPRESSED.getOrDefault(id, 0);
        SUPPRESSED.put(id, depth + 1);

        if (depth == 0 && critChance.getModifier(NO_CRIT_UUID) == null) {
            critChance.addTransientModifier(NO_CRIT_MOD);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void restoreApothCrit(LivingHurtEvent e) {
        if (!(e.getSource().getEntity() instanceof LivingEntity attacker)) return;

        UUID id = attacker.getUUID();
        Integer depth = SUPPRESSED.get(id);
        if (depth == null) return;

        depth--;
        if (depth <= 0) {
            SUPPRESSED.remove(id);

            AttributeInstance critChance = attacker.getAttribute(ALObjects.Attributes.CRIT_CHANCE.get());
            if (critChance != null) {
                critChance.removeModifier(NO_CRIT_UUID);
            }
        } else {
            SUPPRESSED.put(id, depth);
        }
    }

    private static boolean hasColossus(LivingEntity entity) {
        return false;
    }

    private ColossusCritHooks() {}
}