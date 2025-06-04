package net.kayn.fallen_gems_affixes.attributes;

import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.entity.EntityType;

@Mod.EventBusSubscriber(modid = FallenGemsAffixes.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AAEntityAttributes {
    @SubscribeEvent
    public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, AAAttributes.KICK_REDUCTION.get());
        event.add(EntityType.PLAYER, AAAttributes.PROJECTILE_SPEED.get());
        event.add(EntityType.PLAYER, AAAttributes.FIRE_RATE.get());
        event.add(EntityType.PLAYER, AAAttributes.ADDITIONAL_AMMO.get());
        event.add(EntityType.PLAYER, AAAttributes.SPREAD_REDUCTION.get());
        event.add(EntityType.PLAYER, AAAttributes.RELOAD_SPEED.get());
        event.add(EntityType.PLAYER, AAAttributes.BULLET_DAMAGE.get());
        event.add(EntityType.PLAYER, AAAttributes.MAX_HEALTH_DAMAGE.get());
    }
}