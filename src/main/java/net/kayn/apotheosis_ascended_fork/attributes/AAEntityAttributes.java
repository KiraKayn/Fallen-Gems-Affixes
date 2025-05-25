package net.kayn.apotheosis_ascended_fork.attributes;

import net.kayn.apotheosis_ascended_fork.ApotheosisAscendedFork;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.entity.EntityType;

@Mod.EventBusSubscriber(modid = ApotheosisAscendedFork.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
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
    }
}