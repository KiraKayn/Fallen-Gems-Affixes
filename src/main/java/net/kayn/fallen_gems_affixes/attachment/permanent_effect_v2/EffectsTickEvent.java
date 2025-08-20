package net.kayn.fallen_gems_affixes.attachment.permanent_effect_v2;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;

public abstract class EffectsTickEvent extends Event {
    private final LivingEntity entity;

    protected EffectsTickEvent(LivingEntity entity) {
        this.entity = entity;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public static class Pre extends EffectsTickEvent {
        public Pre(LivingEntity entity) {
            super(entity);
        }
    }
}
