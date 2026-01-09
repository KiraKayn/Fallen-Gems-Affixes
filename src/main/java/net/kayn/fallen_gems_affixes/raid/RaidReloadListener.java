package net.kayn.fallen_gems_affixes.raid;

import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FallenGemsAffixes.MOD_ID)
public class RaidReloadListener {

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new RaidDataLoader());
    }
}