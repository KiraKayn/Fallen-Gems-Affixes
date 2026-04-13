package net.kayn.fallen_gems_affixes.attachment.augment;

import net.kayn.fallen_gems_affixes.types.augment.IAugment;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentContainer;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentHandler;

public record AugmentHandler(IAugmentContainer container) implements IAugmentHandler {

    // Constructor needs a container to manage augments

    @Override
    public void addAugment(AugmentInstance instance) {
        container.addAugment(instance);
    }

    @Override
    public boolean removeAugment(AugmentInstance instance) {
        return container.removeAugment(instance);
    }

    @Override
    public boolean removeAugment(IAugment augment) {
        return container.removeAugment(augment);
    }

    @Override
    public boolean hasAugment(IAugment augment) {
        return container.hasAugment(augment);
    }
}