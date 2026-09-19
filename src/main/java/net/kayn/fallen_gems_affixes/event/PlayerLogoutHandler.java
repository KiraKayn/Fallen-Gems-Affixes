package net.kayn.fallen_gems_affixes.event;

import net.kayn.fallen_gems_affixes.adventure.affix.*;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.UUID;

public class PlayerLogoutHandler {

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        UUID uuid = player.getUUID();
        SpellFocusAffix.clearState(uuid);
        BerserkerAffix.clearState(uuid);
        AdaptiveAffix.clearState(uuid);
    }
}