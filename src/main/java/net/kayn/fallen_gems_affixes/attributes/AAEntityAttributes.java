package net.kayn.fallen_gems_affixes.attributes;

import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;

@EventBusSubscriber(modid = FallenGemsAffixes.MOD_ID)
public class AAEntityAttributes {
    @SubscribeEvent
    public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, AAAttributes.KICK_REDUCTION);
        event.add(EntityType.PLAYER, AAAttributes.PROJECTILE_SPEED);
        event.add(EntityType.PLAYER, AAAttributes.FIRE_RATE);
        event.add(EntityType.PLAYER, AAAttributes.ADDITIONAL_AMMO);
        event.add(EntityType.PLAYER, AAAttributes.SPREAD_REDUCTION);
        event.add(EntityType.PLAYER, AAAttributes.RELOAD_SPEED);
        event.add(EntityType.PLAYER, AAAttributes.BULLET_DAMAGE);
        event.add(EntityType.PLAYER, AAAttributes.MAX_HEALTH_DAMAGE);
    }
}