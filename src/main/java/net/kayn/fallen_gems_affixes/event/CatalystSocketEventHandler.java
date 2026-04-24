package net.kayn.fallen_gems_affixes.event;

import dev.shadowsoffire.apotheosis.adventure.event.GetItemSocketsEvent;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.adventure.socket.CatalystSocketHelper;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FallenGemsAffixes.MOD_ID)
public class CatalystSocketEventHandler {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onGetSockets(GetItemSocketsEvent event) {
        if (!CatalystSocketHelper.hasCatalystSocket(event.getStack())) return;
        event.setSockets(1);
    }
}
