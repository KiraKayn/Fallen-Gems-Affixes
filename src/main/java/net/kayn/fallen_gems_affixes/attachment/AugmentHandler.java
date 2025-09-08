package net.kayn.fallen_gems_affixes.attachment;

import net.kayn.fallen_gems_affixes.types.augment.IAugment;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentContainer;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentHandler;
import net.minecraft.resources.ResourceLocation;

public class AugmentHandler implements IAugmentHandler {

    private final IAugmentContainer container;

    // Constructor needs a container to manage augments
    public AugmentHandler(IAugmentContainer container) {
        this.container = container;
    }

    @Override
    public boolean addAugment(ResourceLocation id) {
        if (container.hasAugment(id)) return false;

        IAugment augment = AugmentRegistry.get(id);
        if (augment == null) return false;

        container.addAugment(augment, new AugmentInstance(augment));
        return true;
    }

    @Override
    public boolean removeAugment(ResourceLocation id) {
        if (!container.hasAugment(id)) return false;

        container.removeAugment(id);
        return true;
    }

    @Override
    public boolean hasAugment(ResourceLocation id) {
        return container.hasAugment(id);
    }

    @Override
    public IAugmentContainer getContainer() {
        return container;
    }
}