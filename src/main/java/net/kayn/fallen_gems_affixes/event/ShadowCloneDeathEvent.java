package net.kayn.fallen_gems_affixes.event;

import net.kayn.fallen_gems_affixes.entity.ShadowCloneEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;

public class ShadowCloneDeathEvent extends Event {
    private final ShadowCloneEntity clone;

    public ShadowCloneDeathEvent(ShadowCloneEntity clone) {
        this.clone = clone;
    }

    public ShadowCloneEntity getClone() { return clone; }

    @Nullable
    public Player getOwner() { return clone.getOwner(); }
}