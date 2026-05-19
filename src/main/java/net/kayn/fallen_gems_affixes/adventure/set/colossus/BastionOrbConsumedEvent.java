package net.kayn.fallen_gems_affixes.adventure.set.colossus;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;

public class BastionOrbConsumedEvent extends Event {
    private final Player player;
    private final int consumed;
    private final int orbsBefore;
    private final int orbsAfter;

    public BastionOrbConsumedEvent(Player player, int consumed, int orbsBefore, int orbsAfter) {
        this.player = player;
        this.consumed = consumed;
        this.orbsBefore = orbsBefore;
        this.orbsAfter = orbsAfter;
    }

    public Player getPlayer() { return player; }
    public int getConsumed() { return consumed; }
    public int getOrbsBefore() { return orbsBefore; }
    public int getOrbsAfter() { return orbsAfter; }
}
