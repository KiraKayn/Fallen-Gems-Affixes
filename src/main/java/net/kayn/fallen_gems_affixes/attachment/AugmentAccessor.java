package net.kayn.fallen_gems_affixes.attachment;

import net.kayn.fallen_gems_affixes.types.augment.IAugmentAccessor;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentContainer;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentHandler;
import net.minecraft.world.entity.LivingEntity;

public class AugmentAccessor implements IAugmentAccessor {

    private final LivingEntity entity;
    private final IAugmentContainer container;
    private final IAugmentHandler handler;

    public AugmentAccessor(LivingEntity entity) {
        this.entity = entity;
        this.container = new AugmentContainer();
        this.handler = new AugmentHandler(this.container);
    }

    @Override
    public IAugmentHandler getHandler() {
        return this.handler;
    }

    public LivingEntity getEntity() {
        return this.entity;
    }

    @Override
    public IAugmentContainer getContainer() {
        return this.container;
    }
}