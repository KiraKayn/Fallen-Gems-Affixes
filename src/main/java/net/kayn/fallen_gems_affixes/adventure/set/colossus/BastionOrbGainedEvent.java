package net.kayn.fallen_gems_affixes.adventure.set.colossus;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;

public class BastionOrbGainedEvent extends Event {
    private final Player player;
    private final int amount;
    private final int totalOrbs;

    public BastionOrbGainedEvent(Player player, int amount, int totalOrbs) {
        this.player = player;
        this.amount = amount;
        this.totalOrbs = totalOrbs;
    }

    public Player getPlayer() { return player; }
    public int getAmount() { return amount; }
    public int getTotalOrbs() { return totalOrbs; }
}
