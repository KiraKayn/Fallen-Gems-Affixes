package net.kayn.fallen_gems_affixes.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;

public class PlayerCriticalHitEvent extends Event {
    private final Player player;
    private final LivingEntity target;

    public PlayerCriticalHitEvent(Player player, LivingEntity target) {
        this.player = player;
        this.target = target;
    }

    public Player getPlayer() { return player; }
    public LivingEntity getTarget() { return target; }
}