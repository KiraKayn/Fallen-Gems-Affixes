package net.kayn.fallen_gems_affixes.raid;

import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FallenGemsAffixes.MOD_ID)
public class RaidEventHandler {

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        for (ServerLevel level : event.getServer().getAllLevels()) {
            try { RaidManager.get().onTick(level); }
            catch (Exception ex) { FallenGemsAffixes.LOGGER.error("Error ticking raids in level {}", level.dimension().location(), ex); }
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        RaidManager.get().onEntityDeath(entity.getUUID());

        if (entity instanceof ServerPlayer player) {
            RaidManager.get().onPlayerDeath(player.getUUID());
        }
    }
}