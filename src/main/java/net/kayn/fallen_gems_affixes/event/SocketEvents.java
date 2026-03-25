package net.kayn.fallen_gems_affixes.event;

import dev.shadowsoffire.apotheosis.adventure.event.GetItemSocketsEvent;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import net.kayn.fallen_gems_affixes.config.ModConfig;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "fallen_gems_affixes")
public class SocketEvents {

    @SubscribeEvent
    public static void onGetSockets(GetItemSocketsEvent event) {
        var stack = event.getStack();

        LootCategory cat = LootCategory.forItem(stack);
        if (cat.isNone()) return;

        int base = event.getSockets();
        int extra = ModConfig.EXTRA_SOCKETS.get();

        if (extra <= 0) return;

        event.setSockets(base + extra);
    }
}
