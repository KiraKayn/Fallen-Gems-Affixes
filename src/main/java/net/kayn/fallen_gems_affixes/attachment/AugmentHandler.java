package net.kayn.fallen_gems_affixes.attachment;

import net.kayn.fallen_gems_affixes.types.augment.IAugment;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentContainer;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentHandler;

public class AugmentHandler implements IAugmentHandler {

    private final IAugmentContainer container;

    // Constructor needs a container to manage augments
    public AugmentHandler(IAugmentContainer container) {
        this.container = container;
    }

    @Override
    public void addAugment(AugmentInstance instance) {
        container.addAugment(instance);
    }

    @Override
    public boolean removeAugment(IAugment augment) {
        return container.removeAugment(augment);
    }

    @Override
    public boolean hasAugment(IAugment augment) {
        return container.hasAugment(augment);
    }

    @Override
    public IAugmentContainer getContainer() {
        return container;
    }
}