package net.kayn.fallen_gems_affixes.attachment;

import net.kayn.fallen_gems_affixes.types.augment.IAugmentAccessor;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentContainer;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentHandler;

public class AugmentAccessor implements IAugmentAccessor {

    private final IAugmentContainer container;
    private final IAugmentHandler handler;

    public AugmentAccessor() {
        this.container = new AugmentContainer();
        this.handler = new AugmentHandler(this.container);
    }

    @Override
    public IAugmentHandler getHandler() {
        return this.handler;
    }

    @Override
    public IAugmentContainer getContainer() {
        return this.container;
    }
}