package net.kayn.fallen_gems_affixes.event;

import net.kayn.fallen_gems_affixes.adventure.affix.AdaptiveAffix;
import net.kayn.fallen_gems_affixes.adventure.affix.BerserkerAffix;
import net.kayn.fallen_gems_affixes.adventure.affix.FortifyAffix;
import net.kayn.fallen_gems_affixes.adventure.affix.SpellFocusAffix;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
        FortifyAffix.clearState(uuid);
    }
}